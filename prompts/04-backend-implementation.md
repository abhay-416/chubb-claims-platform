# Prompt 04: Backend Implementation & Clean Architecture

## User Instruction
Implement the Spring Boot 3 + Java 21 backend following strict layered architecture:
- `api`: Controllers, request/response DTOs, Bean Validation, GlobalExceptionHandler
- `domain`: Entities, Enums, State Transition Validators
- `service`: Core business workflows, audit generation, liability exposure calculation
- `infrastructure`: Spring Data JPA repositories, Kafka/Spring event publishers

## Liability Exposure Formula
```
Outstanding Liability Exposure = SUM(
  CASE
    WHEN status IN ('SUBMITTED', 'ASSIGNED', 'UNDER_REVIEW', 'ADDITIONAL_INFO_REQUIRED') THEN estimated_amount
    WHEN status = 'APPROVED' THEN approved_amount
    ELSE 0
  END
)
```
Closed claims (`REJECTED`, `SETTLED`) have 0 outstanding liability.
