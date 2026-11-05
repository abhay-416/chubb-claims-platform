# Chubb APAC Claims Platform - Entity Relationship (ER) Diagram

## 1. Domain Data Model Overview

The database schema is engineered with relational integrity, statutory multi-currency support, auditability, and optimistic concurrency control.

```mermaid
erDiagram
    USERS ||--o{ CLAIMS : "claimant submitted"
    USERS ||--o{ CLAIMS : "officer assigned"
    CLAIMS ||--o{ CLAIM_HISTORY : "tracks status changes"
    CLAIMS ||--o{ CLAIM_INFO_REQUESTS : "contains questionnaires"
    CLAIMS ||--o{ OUTBOX_EVENTS : "emits domain events"

    USERS {
        bigint id PK
        varchar email UK
        varchar password
        varchar name
        varchar role "CLAIMANT, CLAIMS_OFFICER, MANAGER"
        varchar assigned_market "SINGAPORE, HONG_KONG, AUSTRALIA, THAILAND, MALAYSIA, INDONESIA, APAC_REGIONAL"
        timestamp created_at
    }

    CLAIMS {
        bigint id PK
        varchar claim_number UK "e.g. CLM-SG-2026-1001"
        varchar policy_number
        varchar claim_type "MOTOR, PROPERTY"
        varchar market "SINGAPORE, HONG_KONG, AUSTRALIA, THAILAND, MALAYSIA, INDONESIA"
        varchar currency "SGD, HKD, AUD, THB, MYR, IDR"
        varchar status "SUBMITTED, ASSIGNED, UNDER_REVIEW, ADDITIONAL_INFO_REQUIRED, APPROVED, REJECTED, SETTLED"
        date incident_date
        text incident_description
        decimal estimated_amount
        decimal approved_amount
        text rejection_reason
        text settlement_reference
        bigint claimant_id FK
        bigint assigned_officer_id FK
        bigint version "Optimistic Locking (@Version)"
        timestamp created_at
        timestamp updated_at
    }

    CLAIM_HISTORY {
        bigint id PK
        bigint claim_id FK
        varchar old_status
        varchar new_status
        varchar changed_by
        text notes
        timestamp changed_at
    }

    CLAIM_INFO_REQUESTS {
        bigint id PK
        bigint claim_id FK
        text requested_info
        varchar requested_by
        text claimant_response
        varchar status "PENDING, ANSWERED"
        timestamp requested_at
        timestamp responded_at
    }

    OUTBOX_EVENTS {
        bigint id PK
        varchar aggregate_type "CLAIM"
        varchar aggregate_id
        varchar event_type "CLAIM_CREATED, CLAIM_ASSIGNED, CLAIM_DECIDED, CLAIM_SETTLED"
        jsonb payload
        varchar status "PENDING, PUBLISHED, FAILED"
        timestamp created_at
        timestamp processed_at
    }
```

---

## 2. Key Architectural Guarantees

### A. Optimistic Concurrency Control (`version`)
- The `CLAIMS.version` column maps directly to Spring Data JPA's `@Version`.
- When multiple claims adjusters attempt to take ownership of or assess an unassigned claim concurrently, Hibernate generates an `UPDATE ... WHERE id = ? AND version = ?`.
- Conflicting simultaneous updates automatically fail with `OptimisticLockException` (`409 Conflict`), preventing lost updates and duplicate assignments.

### B. Multi-Jurisdiction Currency Rigor (`market` + `currency`)
- Every claim explicitly stores its ISO 4217 statutory currency code (`SGD`, `HKD`, `AUD`, `THB`, `MYR`, `IDR`).
- Financial reserve and exposure calculations group by `(market, currency)` preventing mathematically invalid summing of different fiat currencies without spot exchange conversions.

### C. Complete Immutability & Auditability (`CLAIM_HISTORY`)
- Any transition in `CLAIMS.status` automatically generates a non-destructive audit log entry in `CLAIM_HISTORY` capturing `old_status`, `new_status`, timestamp, and identity of the actor.

### D. Transactional Outbox Pattern (`OUTBOX_EVENTS`)
- Prevents dual-write inconsistencies between the relational database transaction and external Kafka messaging brokers.
