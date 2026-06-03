package com.kafka.microservices.stockservice.service;

import com.kafka.microservices.common.events.OrderCreatedEvent;
import com.kafka.microservices.common.events.StockUpdatedEvent;
import com.kafka.microservices.stockservice.model.Stock;
import com.kafka.microservices.stockservice.repository.StockRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
public class StockService {

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = "order-created-events", groupId = "stock-service-group")
    @Transactional
    public void handleOrderCreatedEvent(OrderCreatedEvent event) {
        log.info("Received OrderCreatedEvent for product: {}, quantity: {}, orderId: {}",
                event.getProductId(), event.getQuantity(), event.getOrderId());

        try {
            Stock stock = stockRepository.findByProductId(event.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + event.getProductId()));

            // Check if sufficient stock available
            if (stock.getAvailableQuantity() >= event.getQuantity()) {
                // Reserve stock
                stock.setAvailableQuantity(stock.getAvailableQuantity() - event.getQuantity());
                stock.setReservedQuantity(stock.getReservedQuantity() + event.getQuantity());
                stock.setUpdatedAt(LocalDateTime.now());
                stockRepository.save(stock);

                log.info("Stock reserved for order: {}, remaining available: {}",
                        event.getOrderId(), stock.getAvailableQuantity());

                // Publish success event
                StockUpdatedEvent successEvent = StockUpdatedEvent.builder()
                        .orderId(event.getOrderId())
                        .productId(event.getProductId())
                        .quantity(event.getQuantity())
                        .action("RESERVED")
                        .success(true)
                        .updatedAt(LocalDateTime.now())
                        .build();

                kafkaTemplate.send("stock-updated-events", event.getOrderId(), successEvent);
                log.info("StockUpdatedEvent (success) published for order: {}", event.getOrderId());
            } else {
                log.warn("Insufficient stock for order: {}. Required: {}, Available: {}",
                        event.getOrderId(), event.getQuantity(), stock.getAvailableQuantity());

                // Publish failure event
                StockUpdatedEvent failureEvent = StockUpdatedEvent.builder()
                        .orderId(event.getOrderId())
                        .productId(event.getProductId())
                        .quantity(event.getQuantity())
                        .action("RESERVED")
                        .success(false)
                        .updatedAt(LocalDateTime.now())
                        .build();

                kafkaTemplate.send("stock-updated-events", event.getOrderId(), failureEvent);
            }
        } catch (Exception e) {
            log.error("Error processing OrderCreatedEvent for orderId: {}", event.getOrderId(), e);

            // Publish failure event
            StockUpdatedEvent failureEvent = StockUpdatedEvent.builder()
                    .orderId(event.getOrderId())
                    .productId(event.getProductId())
                    .quantity(event.getQuantity())
                    .action("RESERVED")
                    .success(false)
                    .updatedAt(LocalDateTime.now())
                    .build();

            kafkaTemplate.send("stock-updated-events", event.getOrderId(), failureEvent);
        }
    }

    public Stock initializeStock(String productId, Integer quantity) {
        log.info("Initializing stock for productId: {}, quantity: {}", productId, quantity);

        Stock stock = Stock.builder()
                .productId(productId)
                .availableQuantity(quantity)
                .reservedQuantity(0)
                .createdAt(LocalDateTime.now())
                .build();

        Stock savedStock = stockRepository.save(stock);
        log.info("Stock initialized with ID: {}", savedStock.getId());
        return savedStock;
    }

    public Stock getStock(String productId) {
        log.info("Fetching stock for productId: {}", productId);
        return stockRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));
    }

    @Transactional
    public Stock releaseStock(String productId, Integer quantity) {
        log.info("Releasing stock for productId: {}, quantity: {}", productId, quantity);

        Stock stock = stockRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));

        if (stock.getReservedQuantity() >= quantity) {
            stock.setReservedQuantity(stock.getReservedQuantity() - quantity);
            stock.setAvailableQuantity(stock.getAvailableQuantity() + quantity);
            stock.setUpdatedAt(LocalDateTime.now());
            return stockRepository.save(stock);
        } else {
            throw new RuntimeException("Invalid quantity to release");
        }
    }
}
