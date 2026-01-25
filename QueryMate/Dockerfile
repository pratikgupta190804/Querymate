# Use OpenJDK base image
FROM openjdk:17-jdk-slim

# Set environment variable for app jar name
ARG JAR_FILE=target/*.jar

# Set working directory
WORKDIR /app

# Copy jar file to container
COPY ${JAR_FILE} app.jar

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]
