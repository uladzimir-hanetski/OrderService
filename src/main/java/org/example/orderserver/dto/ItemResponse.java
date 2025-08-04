package org.example.orderserver.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class ItemResponse {
    private UUID id;
    private String name;
    private Float price;
}
