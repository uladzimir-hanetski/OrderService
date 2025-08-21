package org.example.orderserver.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.example.orderserver.entity.OrderMessage;
import org.example.orderserver.entity.PaymentMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.FixedBackOff;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {
    private final String kafkaUri;
    private final String deadPaymentsTopic;

    public KafkaConfig(@Value("${KAFKA_URI}") String kafkaUri,
                       @Value("${DEAD_PAYMENTS_TOPIC}") String deadPaymentsTopic) {
        this.kafkaUri = kafkaUri;
        this.deadPaymentsTopic = deadPaymentsTopic;
    }

    @Bean
    public ConsumerFactory<String, PaymentMessage> consumerFactory() {
        JsonDeserializer<PaymentMessage> deserializer = new JsonDeserializer<>(PaymentMessage.class, false);

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaUri);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PaymentMessage> kafkaListenerContainerFactory(
            KafkaTemplate<String, PaymentMessage> dlqKafkaTemplate) {

        ConcurrentKafkaListenerContainerFactory<String, PaymentMessage> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());

        DeadLetterPublishingRecoverer recoverer =
                new DeadLetterPublishingRecoverer(dlqKafkaTemplate, (message, exception)
                        -> new TopicPartition(deadPaymentsTopic, message.partition()));

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
                recoverer, new FixedBackOff(1000L, 3)
        );
        factory.setCommonErrorHandler(errorHandler);

        return factory;
    }

    @Bean
    public ProducerFactory<String, PaymentMessage> producerFactory() {
        return new DefaultKafkaProducerFactory<>(defaultProducerConfig());
    }

    @Bean
    public KafkaTemplate<String, PaymentMessage> dlqKafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public ProducerFactory<String, OrderMessage> orderProducerFactory() {
        return new DefaultKafkaProducerFactory<>(defaultProducerConfig());
    }

    @Bean
    public KafkaTemplate<String, OrderMessage> kafkaTemplate() {
        return new KafkaTemplate<>(orderProducerFactory());
    }

    private Map<String, Object> defaultProducerConfig() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaUri);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        return props;
    }
}
