package com.kafka.microservices.orderservice.service;

import com.kafka.microservices.common.events.OrderCreatedEvent;
import com.kafka.microservices.common.events.StockUpdatedEvent;
import com.kafka.microservices.orderservice.model.Order;
import com.kafka.microservices.orderservice.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public Order createOrder(String productId, Integer quantity, Double price, String customerEmail) {
        log.info("Creating order for customer: {}, product: {}, quantity: {}", customerEmail, productId, quantity);

        Order order = Order.builder()
                .productId(productId)
                .quantity(quantity)
                .price(price)
                .customerEmail(customerEmail)
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .build();

        Order savedOrder = orderRepository.save(order);
        log.info("Order created with ID: {}", savedOrder.getId());

        // Publish OrderCreatedEvent to Kafka
        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderId(savedOrder.getId())
                .productId(productId)
                .quantity(quantity)
                .price(price)
                .customerEmail(customerEmail)
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .build();

        kafkaTemplate.send("order-created-events", savedOrder.getId(), event);
        log.info("OrderCreatedEvent published for order: {}", savedOrder.getId());

        return savedOrder;
    }

    @KafkaListener(topics = "stock-updated-events", groupId = "order-service-group")
    public void handleStockUpdateEvent(StockUpdatedEvent event) {
        log.info("Received StockUpdatedEvent for order: {}, success: {}", event.getOrderId(), event.getSuccess());

        Order order = orderRepository.findById(event.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found: " + event.getOrderId()));

        if (event.getSuccess()) {
            order.setStatus("CONFIRMED");
            log.info("Order {} confirmed", event.getOrderId());
        } else {
            order.setStatus("FAILED");
            log.warn("Order {} failed - insufficient stock", event.getOrderId());
        }

        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
    }

    public Order getOrder(String orderId) {
        log.info("Fetching order: {}", orderId);
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
    }

    public List<Order> getOrdersByCustomer(String customerEmail) {
        log.info("Fetching orders for customer: {}", customerEmail);
        return orderRepository.findByCustomerEmail(customerEmail);
    }

    public List<Order> getOrdersByStatus(String status) {
        log.info("Fetching orders with status: {}", status);
        return orderRepository.findByStatus(status);
    }
}
