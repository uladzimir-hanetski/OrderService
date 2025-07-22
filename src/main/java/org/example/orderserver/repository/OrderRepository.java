package org.example.orderserver.repository;

import org.example.orderserver.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    @Query("select o from Order o where o.status in :statuses")
    List<Order> findByStatuses(List<String> statuses);

    @Query("select o from Order o where o.id in :ids")
    List<Order> findByIds(List<UUID> ids);
}