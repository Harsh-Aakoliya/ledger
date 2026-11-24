
# ==========================================
# STAGE 1: BUILD SPRING BOOT APPLICATION
# ==========================================
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copy Maven configuration
COPY pom.xml .

# Download dependencies first (improves build caching)
RUN mvn dependency:go-offline -B

# Copy application source
COPY src ./src

# Build application without compiling or running tests
RUN mvn clean package -B -Dmaven.test.skip=true

# ==========================================
# STAGE 2: RUN SPRING BOOT APPLICATION
# ==========================================
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy generated Spring Boot JAR
COPY --from=build /app/target/*.jar app.jar

# Render port
EXPOSE 8080

# Start application
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-jar", "app.jar"]
