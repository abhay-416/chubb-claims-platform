# Multi-stage Dockerfile for Chubb Claims Backend
# Stage 1: Build JAR with Maven & OpenJDK 21
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Minimal JRE Runtime (OpenJDK 21)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S chubb && adduser -S chubb -G chubb
USER chubb
COPY --from=builder /app/target/chubb-claims-backend-*.jar app.jar

EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=docker
ENTRYPOINT ["java", "-jar", "app.jar"]
