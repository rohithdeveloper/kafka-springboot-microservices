# Docker Setup Guide

This guide explains how to run all microservices in Docker containers.

## Prerequisites

- Docker and Docker Compose installed
- Git installed
- At least 4GB of available RAM

## Quick Start

### 1. Clone the Repository

```bash
git clone https://github.com/rohithdeveloper/kafka-springboot-microservices.git
cd kafka-springboot-microservices
```

### 2. Build and Run All Services

```bash
docker-compose up -d
```

This command will:
- Build Docker images for all three microservices
- Start Zookeeper container
- Start Kafka broker
- Start Order Service (port 8080)
- Start Stock Service (port 8081)
- Start Email Service (port 8082)

### 3. Verify Services are Running

```bash
docker-compose ps
```

Expected output:
```
CONTAINER ID   IMAGE                                    COMMAND                 STATUS
...            kafka-springboot-microservices-order-service   "java -jar..."      Up 2 minutes
...            kafka-springboot-microservices-stock-service   "java -jar..."      Up 2 minutes
...            kafka-springboot-microservices-email-service   "java -jar..."      Up 2 minutes
...            confluentinc/cp-kafka:7.5.0              "/etc/confluent..."    Up 2 minutes
...            confluentinc/cp-zookeeper:7.5.0          "/etc/confluent..."    Up 3 minutes
```

## Individual Service Management

### Build Individual Service Images

```bash
# Build Order Service
docker build -t order-service:1.0.0 -f order-service/Dockerfile .

# Build Stock Service
docker build -t stock-service:1.0.0 -f stock-service/Dockerfile .

# Build Email Service
docker build -t email-service:1.0.0 -f email-service/Dockerfile .
```

### Run Individual Services

```bash
# Start only Kafka and Zookeeper
docker-compose up -d zookeeper kafka

# Wait for Kafka to be healthy, then start services
sleep 30

# Start individual service
docker-compose up -d order-service
docker-compose up -d stock-service
docker-compose up -d email-service
```

### View Service Logs

```bash
# View all logs
docker-compose logs -f

# View specific service logs
docker-compose logs -f order-service
docker-compose logs -f stock-service
docker-compose logs -f email-service

# View Kafka logs
docker-compose logs -f kafka
```

## Service Endpoints

Once all services are running, you can access them at:

- **Order Service**: http://localhost:8080
- **Stock Service**: http://localhost:8081
- **Email Service**: http://localhost:8082
- **H2 Console (Order)**: http://localhost:8080/h2-console
- **H2 Console (Stock)**: http://localhost:8081/h2-console
- **H2 Console (Email)**: http://localhost:8082/h2-console

## Testing Services in Docker

### 1. Add Stock

```bash
curl -X POST http://localhost:8081/api/stock \
  -H "Content-Type: application/json" \
  -d '{
    "productId": "PROD001",
    "quantity": 100,
    "reservedQuantity": 0
  }'
```

### 2. Create Order

```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "CUST001",
    "customerEmail": "customer@example.com",
    "productId": "PROD001",
    "quantity": 5,
    "price": 29.99
  }'
```

### 3. Retrieve Orders

```bash
curl http://localhost:8080/api/orders
```

### 4. Get Stock Details

```bash
curl http://localhost:8081/api/stock/PROD001
```

### 5. Get Emails

```bash
curl http://localhost:8082/api/emails/order/{orderId}
```

## Managing Containers

### Stop All Services

```bash
docker-compose down
```

### Stop Specific Service

```bash
docker-compose stop order-service
```

### Start Specific Service

```bash
docker-compose start order-service
```

### Restart All Services

```bash
docker-compose restart
```

### Remove All Containers and Volumes

```bash
docker-compose down -v
```

## Viewing Kafka Topics and Messages

### List All Topics

```bash
docker exec kafka kafka-topics --list --bootstrap-server localhost:9092
```

### Create a Topic Manually

```bash
docker exec kafka kafka-topics --create \
  --topic test-topic \
  --bootstrap-server localhost:9092 \
  --partitions 3 \
  --replication-factor 1
```

### Consume Messages from a Topic

```bash
# View all messages from beginning
docker exec kafka kafka-console-consumer \
  --topic order-events \
  --bootstrap-server localhost:9092 \
  --from-beginning

# View real-time messages
docker exec kafka kafka-console-consumer \
  --topic order-events \
  --bootstrap-server localhost:9092
```

### Produce Test Messages

```bash
docker exec -it kafka kafka-console-producer \
  --topic order-events \
  --bootstrap-server localhost:9092
```

## Dockerfile Explanations

### Multi-Stage Build

Each service uses a **multi-stage Docker build** for optimization:

1. **Builder Stage**: Compiles the Maven project and produces a JAR file
2. **Runtime Stage**: Runs the compiled JAR with a lightweight JRE image

Benefits:
- Smaller final image size (only JRE, not build tools)
- Faster runtime startup
- Security improvements (no build tools in production image)

### Example Dockerfile Structure

```dockerfile
# Stage 1: Build
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
COPY service-name ./service-name
RUN mvn clean package -DskipTests -pl service-name -am

# Stage 2: Runtime
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/service-name/target/service-name-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

## Docker Compose Network

All services communicate through a bridge network named `kafka-network`. This allows:

- Inter-service communication using service names (e.g., `kafka:29092`)
- Kafka accessibility on port `9092` from host machine
- Services use internal Kafka address `kafka:29092` for communication

## Environment Variables

Services read environment variables from `docker-compose.yml`:

```yaml
environment:
  SPRING_KAFKA_BOOTSTRAP_SERVERS: kafka:29092
  SPRING_DATASOURCE_URL: jdbc:h2:mem:orderdb
  SPRING_DATASOURCE_USERNAME: sa
```

## Health Checks

Each service includes health checks configured in `docker-compose.yml`:

```yaml
healthcheck:
  test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
  interval: 30s
  timeout: 10s
  retries: 3
  start_period: 40s
```

This ensures:
- Services are fully started before dependent services begin
- Automatic restart if a service becomes unhealthy

## Troubleshooting

### Services fail to start

1. Check Docker daemon is running:
   ```bash
   docker ps
   ```

2. Check logs:
   ```bash
   docker-compose logs -f
   ```

3. Ensure ports are not in use:
   ```bash
   netstat -tuln | grep -E '2181|9092|8080|8081|8082'
   ```

### Kafka connection refused

1. Verify Kafka container is running:
   ```bash
   docker ps | grep kafka
   ```

2. Check Kafka logs:
   ```bash
   docker-compose logs kafka
   ```

3. Restart Kafka:
   ```bash
   docker-compose restart kafka
   ```

### OutOfMemory errors

1. Increase Docker memory:
   ```bash
   docker-compose down
   # Restart Docker daemon with more memory
   docker-compose up -d
   ```

2. Or add memory limits in docker-compose.yml:
   ```yaml
   services:
     order-service:
       mem_limit: 512m
   ```

## Performance Optimization

### Add Resource Limits

```yaml
services:
  order-service:
    mem_limit: 512m
    cpus: '0.5'
  stock-service:
    mem_limit: 512m
    cpus: '0.5'
  email-service:
    mem_limit: 512m
    cpus: '0.5'
```

### View Resource Usage

```bash
docker stats
```

## Cleanup

### Remove Unused Resources

```bash
# Remove stopped containers
docker container prune

# Remove unused images
docker image prune

# Remove unused volumes
docker volume prune

# Remove all unused resources
docker system prune -a
```

## Production Deployment

For production:

1. Use specific image versions (not `latest`)
2. Configure resource limits
3. Set up proper logging
4. Use environment-specific configuration
5. Implement security best practices (network policies, secrets management)
6. Use Kubernetes or Docker Swarm for orchestration
