# Build stage
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

# Copy parent pom.xml
COPY pom.xml .

# Copy email service
COPY email-service ./email-service

# Build the email service
RUN mvn clean package -DskipTests -pl email-service -am

# Runtime stage
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy jar from builder stage
COPY --from=builder /app/email-service/target/email-service-1.0.0.jar app.jar

# Expose port
EXPOSE 8082

# Set environment variables
ENV KAFKA_BOOTSTRAP_SERVERS=kafka:29092

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
