# Build stage
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

# Copy parent pom.xml
COPY pom.xml .

# Copy order service
COPY order-service ./order-service

# Build the order service
RUN mvn clean package -DskipTests -pl order-service -am

# Runtime stage
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy jar from builder stage
COPY --from=builder /app/order-service/target/order-service-1.0.0.jar app.jar

# Expose port
EXPOSE 8080

# Set environment variables
ENV KAFKA_BOOTSTRAP_SERVERS=kafka:29092

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
