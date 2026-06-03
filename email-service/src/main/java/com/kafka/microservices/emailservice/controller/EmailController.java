package com.kafka.microservices.emailservice.controller;

import com.kafka.microservices.emailservice.model.EmailLog;
import com.kafka.microservices.emailservice.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/email")
@CrossOrigin(origins = "*")
public class EmailController {

    @Autowired
    private EmailService emailService;

    @GetMapping("/log/{emailLogId}")
    public ResponseEntity<?> getEmailLog(@PathVariable String emailLogId) {
        try {
            log.info("Fetching email log: {}", emailLogId);
            EmailLog emailLog = emailService.getEmailLog(emailLogId);
            return ResponseEntity.ok(emailLog);
        } catch (Exception e) {
            log.error("Error fetching email log", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<?> getEmailLogsByOrder(@PathVariable String orderId) {
        try {
            log.info("Fetching email logs for order: {}", orderId);
            List<EmailLog> emailLogs = emailService.getEmailLogsByOrder(orderId);
            return ResponseEntity.ok(emailLogs);
        } catch (Exception e) {
            log.error("Error fetching email logs", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<?> getEmailLogsByStatus(@PathVariable String status) {
        try {
            log.info("Fetching email logs with status: {}", status);
            List<EmailLog> emailLogs = emailService.getEmailLogsByStatus(status);
            return ResponseEntity.ok(emailLogs);
        } catch (Exception e) {
            log.error("Error fetching email logs", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
