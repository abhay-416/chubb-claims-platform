# Prompt 09: Containerization, Docker Compose & Local Reproducibility

## User Instruction
Provide a single-command reproducible local environment that boots all platform services in under 5 minutes without manual infrastructure setup:
1. Multi-stage Dockerfile for Spring Boot 3.x backend on Eclipse Temurin Java 21 JDK.
2. Multi-stage Dockerfile for Angular 18 frontend with Nginx alpine reverse proxy routing `/api/` traffic to the backend.
3. `docker-compose.yml` orchestrating:
   - PostgreSQL 16 database with health check.
   - Apache Kafka 3.7 running in modern KRaft mode (no Zookeeper dependency).
   - Backend service waiting for Postgres and Kafka readiness.
   - Frontend service exposing port 80/4200.
4. Dual-profile architecture in `application.yml`:
   - `dev` profile: In-memory H2 database + Spring ApplicationEvents (0 external dependencies required for immediate IDE running).
   - `docker` profile: PostgreSQL 16 + Apache Kafka broker.

## Verification
Ensure the platform starts seamlessly with:
```bash
docker compose up --build
```
And passes all container health probes.
