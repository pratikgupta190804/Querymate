# ===== BUILD STAGE =====
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

# Copy Maven wrapper and pom.xml first (for better caching)
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Make mvnw executable
RUN chmod +x mvnw

# Download dependencies (cached layer)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application (skip tests for faster build)
RUN ./mvnw clean package -DskipTests -B

# ===== RUNTIME STAGE =====
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Create non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy the built jar from builder stage
COPY --from=builder /app/target/querymate-backend.jar app.jar

# Change ownership to non-root user
RUN chown -R appuser:appgroup /app

USER appuser

# Expose the port (Render uses PORT env var)
EXPOSE 10000

# Run the application with optimized JVM settings for Render free tier (512MB RAM)
ENTRYPOINT ["java","-XX:+UseContainerSupport","-XX:MaxRAMPercentage=70.0","-XX:+UseG1GC","-XX:+UseStringDeduplication", "-Djava.security.egd=file:/dev/./urandom", "-jar","app.jar"]
