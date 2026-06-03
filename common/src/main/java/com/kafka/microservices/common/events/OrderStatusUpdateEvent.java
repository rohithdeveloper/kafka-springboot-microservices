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
public class OrderStatusUpdateEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private String orderId;
    private String status;
    private String message;
    private LocalDateTime updatedAt;
}