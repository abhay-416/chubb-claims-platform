# Prompt 06: Event-Driven Messaging (Kafka vs REST)

## User Instruction
Integrate Kafka event publishing for key domain events across Chubb APAC operations:
- `ClaimCreatedEvent`
- `ClaimStatusChangedEvent`
- `ClaimAssignedEvent`
- `ClaimSettledEvent`

## Dual-Execution Design
To guarantee that the application runs locally in under 5 minutes without mandatory external infrastructure:
- **`prod`/`docker` profile:** Uses `KafkaTemplate` to publish to Apache Kafka cluster on `chubb.apac.claims.events`.
- **`dev`/`default` profile:** Uses Spring `ApplicationEventPublisher` with an asynchronous listener, logging event payload and maintaining audit integrity with zero external dependencies.

## Transactional Consistency
Document the **Transactional Outbox Pattern** (`outbox_events` table) to resolve dual-write distributed transaction vulnerabilities between PostgreSQL transactions and Kafka message dispatching.
