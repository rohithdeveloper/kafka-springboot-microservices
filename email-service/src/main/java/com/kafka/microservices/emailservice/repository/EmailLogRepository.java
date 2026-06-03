package com.kafka.microservices.emailservice.repository;

import com.kafka.microservices.emailservice.model.EmailLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmailLogRepository extends JpaRepository<EmailLog, String> {
    List<EmailLog> findByOrderId(String orderId);
    List<EmailLog> findByStatus(String status);
}