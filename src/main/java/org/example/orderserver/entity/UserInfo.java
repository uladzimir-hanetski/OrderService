package org.example.orderserver.entity;

import lombok.Data;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class UserInfo {
    private UUID id;
    private String name;
    private String surname;
    private LocalDate birthDate;
    private String email;
}
