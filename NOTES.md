# Technical Decisions & Assumptions

## 1. Architectural Strategy: Modular Monolith vs Microservices
- **Decision:** Implemented a **Modular Monolith** rather than a distributed set of microservices.
- **Justification:**
  1. The take-home assessment was designed for a 3-5 hour sprint window.
  2. Microservices introduce premature complexity: distributed transactions across claim and assessment contexts, network latency, distributed tracing (Zipkin/Jaeger), and complex container orchestration.
  3. Logical boundaries (`api`, `domain`, `service`, `infrastructure`, `security`) are strictly isolated, enabling extraction into independent microservices (e.g. `fnol-intake-service`, `assessment-workflow-service`, `liability-analytics-service`) in the future with zero code rewrites.

## 2. Sync REST vs Async Event-Driven (Kafka) Boundaries
- **Synchronous REST / HTTP:**
  - `POST /claims` (FNOL): The policyholder requires an immediate acknowledgement with an official Claim Number (`CLM-SG-2026-XXXX`).
  - `POST /assign`, `POST /review`, `POST /assess`: Adjusters need immediate validation of state transitions and instant feedback.
  - `GET /dashboard/liability`: Managers need synchronous real-time reads of exposure totals.
- **Asynchronous Apache Kafka:**
  - `ClaimCreatedEvent`: Dispatched to Kafka to trigger customer confirmation SMS/Email and asynchronous fraud scoring.
  - `ClaimStatusChangedEvent`: Dispatched to Kafka for immutable audit streaming into enterprise data lakes.
  - `ClaimSettledEvent`: Dispatched to Kafka to trigger downstream accounting and core banking payout integrations.
- **Reliability Pattern in Production:**
  - While this sprint publishes events directly via `EventPublisher`, in a full production system the **Transactional Outbox Pattern** (writing events to a dedicated DB outbox table within the same ACID transaction and publishing with Debezium CDC) would guarantee at-least-once delivery without distributed two-phase commits.

## 3. Dual-Profile Execution Architecture ("Under 5 Minutes" Rule)
- **Problem:** Take-home examiners have less than 10 minutes to evaluate code. If the project strictly requires PostgreSQL and a multi-broker Kafka cluster to run tests, startup latency or port collisions could block evaluation.
- **Solution:**
  - **`dev` / default profile:** Uses H2 in-memory DB + Spring In-Memory ApplicationEvents with asynchronous listeners. Running `mvn test` or `mvn spring-boot:run` requires **zero external infrastructure**.
  - **`docker` profile:** Spins up PostgreSQL 16 + Confluent Kafka in KRaft mode + Spring Boot + Angular in a single `docker compose up` command.

## 4. Multi-Market Awareness (6 APAC Jurisdictions)
- Chubb APAC processes claims across 6 markets: Singapore, Hong Kong, Australia, Thailand, Malaysia, Indonesia.
- All entities, claims, and dashboard calculations partition by `Market`. This enables jurisdiction-specific regulatory rules, currencies, and localized SLA timers.

## 5. Liability Exposure Formula
```
Outstanding Liability Exposure = SUM(
  CASE
    WHEN status IN ('SUBMITTED', 'ASSIGNED', 'UNDER_REVIEW', 'ADDITIONAL_INFO_REQUIRED') THEN estimated_amount
    WHEN status = 'APPROVED' THEN approved_amount
    ELSE 0
  END
)
```
- Claims in `SETTLED` or `REJECTED` status have zero outstanding liability exposure.
