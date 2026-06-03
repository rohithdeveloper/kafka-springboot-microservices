package com.kafka.microservices.common.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailNotificationEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private String orderId;
    private String recipientEmail;
    private String subject;
    private String message;
    private String eventType; // ORDER_CREATED, ORDER_CONFIRMED, ORDER_FAILED
    private LocalDateTime createdAt;
}