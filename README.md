# Kafka Spring Boot Microservices

A multi-module Spring Boot microservices project using Apache Kafka as the message broker for asynchronous communication between services.

## Project Architecture

This project demonstrates a distributed order processing system with three main microservices:

### 1. **Order Service** (Port: 8081)
- Creates and manages customer orders
- Publishes `OrderCreatedEvent` to Kafka when a new order is created
- Listens for `StockUpdatedEvent` to update order status
- Database: H2 (in-memory)

### 2. **Stock Service** (Port: 8082)
- Manages product inventory
- Listens for `OrderCreatedEvent` and reserves stock
- Publishes `StockUpdatedEvent` with success/failure status
- Handles stock reservation and release
- Database: H2 (in-memory)

### 3. **Email Service** (Port: 8083)
- Sends email notifications for order events
- Listens for `OrderCreatedEvent` and publishes confirmation emails
- Logs all email communications
- Database: H2 (in-memory)

### 4. **Common Module**
- Shared event classes for inter-service communication
- Reusable utilities and models

## Event Flow

```
Order Service          Stock Service          Email Service
    |                      |                        |
    |-- OrderCreatedEvent ->|                        |
    |                       |-- StockUpdatedEvent -->|
    |<-- (listens)          |                    (logs event)
    |                       |
    | (updates status)      |
```

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- Docker and Docker Compose (for Kafka and Zookeeper)

## Setup and Running

### 1. Start Kafka and Zookeeper

```bash
docker-compose up -d
```

Verify that Kafka and Zookeeper are running:
- Zookeeper: http://localhost:2181
- Kafka: localhost:9092

### 2. Build the Project

```bash
mvn clean install
```

### 3. Run Each Service

Open three terminal windows and run each service:

**Terminal 1 - Order Service:**
```bash
cd order-service
mvn spring-boot:run
```

**Terminal 2 - Stock Service:**
```bash
cd stock-service
mvn spring-boot:run
```

**Terminal 3 - Email Service:**
```bash
cd email-service
mvn spring-boot:run
```

## API Endpoints

### Order Service (http://localhost:8081)

- **Create Order**
  ```
  POST /api/orders/create?productId=PROD001&quantity=5&price=100.0&customerEmail=customer@example.com
  ```

- **Get Order**
  ```
  GET /api/orders/{orderId}
  ```

- **Get Orders by Customer**
  ```
  GET /api/orders/customer/{email}
  ```

- **Get Orders by Status**
  ```
  GET /api/orders/status/{status}
  ```

### Stock Service (http://localhost:8082)

- **Initialize Stock**
  ```
  POST /api/stock/initialize?productId=PROD001&quantity=100
  ```

- **Get Stock**
  ```
  GET /api/stock/{productId}
  ```

- **Release Stock**
  ```
  POST /api/stock/{productId}/release?quantity=5
  ```

### Email Service (http://localhost:8083)

- **Get Email Log**
  ```
  GET /api/email/log/{emailLogId}
  ```

- **Get Email Logs by Order**
  ```
  GET /api/email/order/{orderId}
  ```

- **Get Email Logs by Status**
  ```
  GET /api/email/status/{status}
  ```

## Testing the Workflow

### 1. Initialize Stock

```bash
curl -X POST "http://localhost:8082/api/stock/initialize?productId=PROD001&quantity=100"
```

### 2. Create an Order

```bash
curl -X POST "http://localhost:8081/api/orders/create?productId=PROD001&quantity=5&price=100.0&customerEmail=customer@example.com"
```

Response:
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "productId": "PROD001",
  "quantity": 5,
  "price": 100.0,
  "customerEmail": "customer@example.com",
  "status": "PENDING",
  "createdAt": "2024-01-15T10:30:00"
}
```

### 3. Check Order Status (after a few seconds)

```bash
curl "http://localhost:8081/api/orders/{orderId}"
```

Order status should change to "CONFIRMED" if stock was available.

### 4. Check Stock

```bash
curl "http://localhost:8082/api/stock/PROD001"
```

Response:
```json
{
  "id": "prod-123",
  "productId": "PROD001",
  "availableQuantity": 95,
  "reservedQuantity": 5,
  "createdAt": "2024-01-15T10:25:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

## Kafka Topics

The following Kafka topics are automatically created:

- `order-created-events` - Events when orders are created
- `stock-updated-events` - Events when stock is updated
- `order-status-update-events` - Events for order status changes

## Architecture Highlights

1. **Event-Driven**: Services communicate through Kafka events rather than direct API calls
2. **Loose Coupling**: Services are independent and can be deployed separately
3. **Scalability**: Easy to scale individual services based on load
4. **Asynchronous Processing**: Non-blocking operations improve throughput
5. **Database Isolation**: Each service has its own database (polyglot persistence)

## Technologies Used

- **Spring Boot 3.2.0** - Framework for building microservices
- **Spring Kafka** - Kafka integration
- **Spring Data JPA** - ORM for database operations
- **H2 Database** - In-memory database for development
- **Apache Kafka 7.5.0** - Message broker
- **Zookeeper** - Kafka coordination
- **Maven** - Build tool
- **Lombok** - Reduce boilerplate code
- **Docker & Docker Compose** - Container orchestration

## Future Enhancements

- [ ] Add authentication and authorization
- [ ] Implement circuit breaker pattern
- [ ] Add distributed tracing (Sleuth + Zipkin)
- [ ] Implement service discovery (Consul/Eureka)
- [ ] Add API Gateway
- [ ] Implement dead letter queues for failed messages
- [ ] Add monitoring and metrics (Prometheus + Grafana)
- [ ] Implement health checks and readiness probes
- [ ] Add integration tests
- [ ] Setup CI/CD pipeline

## Troubleshooting

### Kafka Connection Issues
- Ensure Kafka container is running: `docker-compose ps`
- Check logs: `docker-compose logs kafka`
- Restart services: `docker-compose restart`

### Database Issues
- H2 console available at: http://localhost:8081/h2-console
- Connection string: `jdbc:h2:mem:orderdb`
- Username: `sa`, Password: (leave blank)

### Port Already in Use
- Change port in `application.yml` for the specific service
- Or kill the process: `lsof -ti:8081 | xargs kill -9`

## Contributing

Feel free to submit issues and enhancement requests!

## License

This project is open source and available under the MIT License.
