package org.example.orderserver.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.groups.Default;
import lombok.Data;
import org.example.orderserver.entity.OrderStatus;
import java.util.List;

@Data
public class OrderRequest {

    public interface CreateValidation extends Default {}
    public interface UpdateValidation extends Default {}

    @NotNull(message = "Order status cannot be empty",
    groups = CreateValidation.class)
    private OrderStatus status;

    @Valid
    private List<OrderItemRequest> orderItems;

    @Email(message = "Incorrect email format",
    groups = {UpdateValidation.class, CreateValidation.class})
    @NotBlank(message = "Email cannot be empty",
    groups = CreateValidation.class)
    private String userEmail;
}
