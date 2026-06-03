package com.kafka.microservices.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailEvent {
    @JsonProperty("email_id")
    private String emailId;

    @JsonProperty("order_id")
    private String orderId;

    @JsonProperty("customer_email")
    private String customerEmail;

    @JsonProperty("subject")
    private String subject;

    @JsonProperty("body")
    private String body;

    @JsonProperty("status")
    private String status; // PENDING, SENT, FAILED

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("sent_at")
    private LocalDateTime sentAt;
}
