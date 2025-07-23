package org.example.orderserver.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.groups.Default;
import lombok.Data;

@Data
public class ItemRequest {

    public interface CreateValidation extends Default {}
    public interface UpdateValidation extends Default {}

    @NotBlank(message = "Item name cannot be empty",
    groups = CreateValidation.class)
    private String name;

    @Positive(message = "Item price cannot be negative",
    groups = {CreateValidation.class, UpdateValidation.class})
    private Float price;
}
