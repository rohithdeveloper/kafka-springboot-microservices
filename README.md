# Kafka Spring Boot Microservices

A multi-module Spring Boot microservices project using Apache Kafka as the message broker for inter-service communication.

## Project Structure

```
kafka-springboot-microservices/
├── order-service/       # Order Service microservice
├── stock-service/       # Stock Service microservice
├── email-service/       # Email Service microservice
├── common/             # Shared models and utilities
├── pom.xml             # Parent POM
└── docker-compose.yml  # Zookeeper and Kafka configuration
```

## Services Overview

### 1. Order Service
- Handles order creation and management
- Publishes order events to Kafka
- Consumes stock confirmation events
- REST API on port 8001

### 2. Stock Service
- Manages inventory/stock
- Listens to order events
- Updates stock and publishes confirmation events
- REST API on port 8002

### 3. Email Service
- Sends email notifications
- Listens to order and stock events
- Consumes events and sends appropriate emails
- REST API on port 8003

## Prerequisites

- Java 17+
- Maven 3.6+
- Docker and Docker Compose (for Kafka and Zookeeper)

## Setup Instructions

### Step 1: Start Kafka and Zookeeper

The docker-compose.yml file is configured for your local Docker setup with Zookeeper and Kafka.

```bash
docker-compose up -d
```

This will start:
- Zookeeper on port 2181
- Kafka on port 9092

Verify Kafka is running:
```bash
docker ps
docker logs kafka
```

### Step 2: Build the Project

```bash
mvn clean install
```

### Step 3: Run the Microservices

Each service can be run independently:

```bash
# Order Service
cd order-service
mvn spring-boot:run

# Stock Service (new terminal)
cd stock-service
mvn spring-boot:run

# Email Service (new terminal)
cd email-service
mvn spring-boot:run
```

## Kafka Topics

The following topics are automatically created and used by the microservices:

- `orders` - Order events published by Order Service
- `stock-updates` - Stock events published by Stock Service
- `email-notifications` - Email notification events published by all services

## API Endpoints

### Order Service (Port 8001)
- `POST /api/orders` - Create a new order
- `GET /api/orders/{orderId}` - Get order details
- `GET /api/orders` - Get all orders

### Stock Service (Port 8002)
- `GET /api/stock/{productId}` - Get stock details
- `PUT /api/stock/{productId}` - Update stock
- `GET /api/stock` - Get all stock items

### Email Service (Port 8003)
- `GET /api/emails/status` - Service health check
- `GET /api/emails/notifications` - Get all sent notifications

## Configuration

### Kafka Configuration
All services are configured to connect to Kafka at `localhost:9092`.

### Database
- Order Service: H2 (in-memory)
- Stock Service: H2 (in-memory)
- Email Service: No database (uses in-memory cache)

## Event Flow Example

1. User creates an order via Order Service API
2. Order Service publishes "OrderCreated" event to `orders` topic
3. Stock Service listens and processes the order
4. Stock Service publishes "StockReserved/StockFailed" event to `stock-updates` topic
5. Email Service listens to both topics and sends notifications

## Technology Stack

- Spring Boot 3.2.0
- Spring Kafka
- Spring Data JPA
- Spring Web
- Kafka 3.6.0
- Zookeeper 3.6
- H2 Database
- Maven

## Troubleshooting

### Kafka Connection Issues
- Ensure Kafka is running: `docker ps`
- Check Kafka logs: `docker logs kafka`
- Verify Kafka is accessible on port 9092

### Topic Creation
Topics are created automatically when services start. If needed, manually create topics:

```bash
docker exec -it kafka kafka-topics --create --topic orders --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1
docker exec -it kafka kafka-topics --create --topic stock-updates --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1
docker exec -it kafka kafka-topics --create --topic email-notifications --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1
```

### View Kafka Topics
```bash
docker exec -it kafka kafka-topics --list --bootstrap-server localhost:9092
```

### View Messages in Topic
```bash
docker exec -it kafka kafka-console-consumer --topic orders --from-beginning --bootstrap-server localhost:9092
```

## Stop Services

### Stop All Microservices
Press `Ctrl+C` in each terminal where services are running.

### Stop Kafka and Zookeeper
```bash
docker-compose down
```

To remove volumes as well:
```bash
docker-compose down -v
```

## Future Enhancements

- Add API Gateway (Spring Cloud Gateway)
- Implement circuit breaker pattern (Resilience4j)
- Add distributed tracing with Sleuth and Zipkin
- Implement service discovery with Eureka
- Add comprehensive error handling and logging
- Implement retry policies with exponential backoff
- Add comprehensive unit and integration tests
- Implement monitoring with Prometheus and Grafana

## License

This project is licensed under the MIT License.
