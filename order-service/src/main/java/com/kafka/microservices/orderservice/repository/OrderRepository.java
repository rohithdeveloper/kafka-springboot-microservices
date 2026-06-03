package com.kafka.microservices.orderservice.repository;

import com.kafka.microservices.orderservice.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    List<Order> findByCustomerEmail(String customerEmail);
    List<Order> findByStatus(String status);
}