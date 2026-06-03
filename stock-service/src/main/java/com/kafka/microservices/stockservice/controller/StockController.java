package com.kafka.microservices.stockservice.controller;

import com.kafka.microservices.stockservice.model.Stock;
import com.kafka.microservices.stockservice.service.StockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/stock")
@CrossOrigin(origins = "*")
public class StockController {

    @Autowired
    private StockService stockService;

    @PostMapping("/initialize")
    public ResponseEntity<?> initializeStock(
            @RequestParam String productId,
            @RequestParam Integer quantity) {

        try {
            log.info("Initializing stock - productId: {}, quantity: {}", productId, quantity);
            Stock stock = stockService.initializeStock(productId, quantity);
            return ResponseEntity.status(HttpStatus.CREATED).body(stock);
        } catch (Exception e) {
            log.error("Error initializing stock", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/{productId}")
    public ResponseEntity<?> getStock(@PathVariable String productId) {
        try {
            log.info("Fetching stock for product: {}", productId);
            Stock stock = stockService.getStock(productId);
            return ResponseEntity.ok(stock);
        } catch (Exception e) {
            log.error("Error fetching stock", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    @PostMapping("/{productId}/release")
    public ResponseEntity<?> releaseStock(
            @PathVariable String productId,
            @RequestParam Integer quantity) {

        try {
            log.info("Releasing stock - productId: {}, quantity: {}", productId, quantity);
            Stock stock = stockService.releaseStock(productId, quantity);
            return ResponseEntity.ok(stock);
        } catch (Exception e) {
            log.error("Error releasing stock", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
