package org.example.orderserver.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.groups.Default;
import lombok.Data;
import java.util.UUID;

@Data
public class OrderItemRequest {

    public interface CreateValidation extends Default {}
    public interface UpdateValidation extends Default {}

    @Positive(message = "Item quantity must be positive",
    groups = {CreateValidation.class, UpdateValidation.class})
    private Long quantity;

    @NotNull(message = "Item ID cannot be empty",
    groups = CreateValidation.class)
    private UUID itemId;
}
