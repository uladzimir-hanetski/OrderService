package org.example.orderserver.entity;

public enum OrderStatus {
    CREATED, TO_PAY, IN_PROGRESS, COMPLETED,
    DELIVERED, TERMINATED, TAKEN_AWAY;
}
