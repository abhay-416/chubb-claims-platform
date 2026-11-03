# Prompt 05: Security & Role-Based Access Control

## User Instruction
Implement stateless Spring Security 6 with JWT token authentication.
Configure roles:
- `ROLE_CLAIMANT`: Can submit FNOL claims, view their own claims, and respond to info requests.
- `ROLE_CLAIMS_OFFICER`: Can view incoming queue, assign claims, start reviews, request info, and assess (approve/reject).
- `ROLE_CLAIMS_MANAGER`: Full visibility, can view team workload and financial liability exposure across all 6 APAC markets.

# Prompt 06: Event-Driven Messaging (Kafka vs REST)

## User Instruction
Integrate Kafka event publishing for key domain events:
- `ClaimCreatedEvent`
- `ClaimStatusChangedEvent`
- `ClaimAssignedEvent`
- `ClaimSettledEvent`

## Dual-Execution Design
To guarantee that the application runs locally in under 5 minutes without mandatory Docker/Kafka running, implement a dual event publisher:
- In `prod` profile: Uses `KafkaTemplate` to publish to Apache Kafka topics.
- In `default`/`dev` profile: Uses Spring `ApplicationEventPublisher` with an asynchronous listener, logging event payload and maintaining audit integrity with zero external dependencies.
