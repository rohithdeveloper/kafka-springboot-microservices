package com.kafka.microservices.emailservice.service;

import com.kafka.microservices.common.events.OrderCreatedEvent;
import com.kafka.microservices.common.events.OrderStatusUpdateEvent;
import com.kafka.microservices.emailservice.model.EmailLog;
import com.kafka.microservices.emailservice.repository.EmailLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class EmailService {

    @Autowired
    private EmailLogRepository emailLogRepository;

    @KafkaListener(topics = "order-created-events", groupId = "email-service-group")
    public void handleOrderCreatedEvent(OrderCreatedEvent event) {
        log.info("Received OrderCreatedEvent for orderId: {}, email: {}",
                event.getOrderId(), event.getCustomerEmail());

        String subject = "Order Confirmation - #" + event.getOrderId();
        String message = buildOrderCreatedEmailBody(event);

        sendEmail(event.getOrderId(), event.getCustomerEmail(), subject, message, "ORDER_CREATED");
    }

    @KafkaListener(topics = "stock-updated-events", groupId = "email-service-group")
    public void handleStockUpdateEvent(com.kafka.microservices.common.events.StockUpdatedEvent event) {
        log.info("Received StockUpdatedEvent for orderId: {}, success: {}",
                event.getOrderId(), event.getSuccess());

        // Fetch order details to get email (in production, we'd call order service or have it in the event)
        // For now, we log the event
        if (event.getSuccess()) {
            log.info("Order {} confirmed - stock reserved successfully", event.getOrderId());
        } else {
            log.warn("Order {} failed - insufficient stock", event.getOrderId());
        }
    }

    @KafkaListener(topics = "order-status-update-events", groupId = "email-service-group")
    public void handleOrderStatusUpdateEvent(OrderStatusUpdateEvent event) {
        log.info("Received OrderStatusUpdateEvent for orderId: {}, status: {}",
                event.getOrderId(), event.getStatus());

        // Log the status update
        EmailLog emailLog = EmailLog.builder()
                .orderId(event.getOrderId())
                .recipientEmail("admin@example.com") // Placeholder
                .subject("Order Status Update - #" + event.getOrderId())
                .message(event.getMessage())
                .eventType("ORDER_STATUS_UPDATE")
                .status("LOGGED")
                .createdAt(LocalDateTime.now())
                .build();

        emailLogRepository.save(emailLog);
        log.info("Status update logged for order: {}", event.getOrderId());
    }

    public void sendEmail(String orderId, String recipientEmail, String subject, String message, String eventType) {
        log.info("Sending email to: {}, subject: {}", recipientEmail, subject);

        try {
            // In a real scenario, you would use JavaMailSender to send actual emails
            // For now, we're just logging the email

            // TODO: Implement actual email sending using JavaMailSender
            // mailSender.send(new SimpleMailMessage(...));

            EmailLog emailLog = EmailLog.builder()
                    .orderId(orderId)
                    .recipientEmail(recipientEmail)
                    .subject(subject)
                    .message(message)
                    .eventType(eventType)
                    .status("SENT")
                    .createdAt(LocalDateTime.now())
                    .build();

            emailLogRepository.save(emailLog);
            log.info("Email sent and logged for order: {}", orderId);
        } catch (Exception e) {
            log.error("Error sending email for order: {}", orderId, e);

            EmailLog emailLog = EmailLog.builder()
                    .orderId(orderId)
                    .recipientEmail(recipientEmail)
                    .subject(subject)
                    .message(message)
                    .eventType(eventType)
                    .status("FAILED")
                    .failureReason(e.getMessage())
                    .createdAt(LocalDateTime.now())
                    .build();

            emailLogRepository.save(emailLog);
        }
    }

    private String buildOrderCreatedEmailBody(OrderCreatedEvent event) {
        return String.format(
                "Dear Customer,\n\n" +
                "Your order has been created successfully.\n\n" +
                "Order Details:\n" +
                "Order ID: %s\n" +
                "Product ID: %s\n" +
                "Quantity: %d\n" +
                "Price: $%.2f\n" +
                "Total: $%.2f\n\n" +
                "Status: %s\n\n" +
                "Thank you for your order!\n\n" +
                "Best regards,\nOrder Management System",
                event.getOrderId(),
                event.getProductId(),
                event.getQuantity(),
                event.getPrice(),
                event.getPrice() * event.getQuantity(),
                event.getStatus()
        );
    }

    public EmailLog getEmailLog(String emailLogId) {
        log.info("Fetching email log: {}", emailLogId);
        return emailLogRepository.findById(emailLogId)
                .orElseThrow(() -> new RuntimeException("Email log not found: " + emailLogId));
    }

    public List<EmailLog> getEmailLogsByOrder(String orderId) {
        log.info("Fetching email logs for order: {}", orderId);
        return emailLogRepository.findByOrderId(orderId);
    }

    public List<EmailLog> getEmailLogsByStatus(String status) {
        log.info("Fetching email logs with status: {}", status);
        return emailLogRepository.findByStatus(status);
    }
}
