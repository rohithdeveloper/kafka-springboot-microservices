package com.kafka.microservices.emailservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "email_logs")
public class EmailLog {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String orderId;

    @Column(nullable = false)
    private String recipientEmail;

    @Column(nullable = false)
    private String subject;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(nullable = false)
    private String eventType; // ORDER_CREATED, ORDER_CONFIRMED, ORDER_FAILED

    @Column(nullable = false)
    private String status; // SENT, FAILED, PENDING

    @Column
    private String failureReason;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
