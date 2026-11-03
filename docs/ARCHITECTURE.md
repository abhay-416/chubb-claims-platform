# Chubb APAC Claims Platform - Architecture & Design Document

## 1. System Overview
The Chubb APAC Claims Platform is a high-performance, modular insurance processing engine designed for motor and property portfolios across six Asia-Pacific markets: **Singapore, Hong Kong, Australia, Thailand, Malaysia, and Indonesia**.

```mermaid
graph TD
    subgraph Frontend [Angular Single Page Application]
        CP[Claimant Portal - FNOL & Tracker]
        OP[Officer Workload Queue & Assessment]
        MP[Manager Real-Time Exposure Analytics]
        RS[Global Role Switcher]
    end

    subgraph Backend [Spring Boot Modular Monolith]
        subgraph API_Layer [REST API Layer]
            AC[AuthController]
            CC[ClaimController]
            DC[DashboardController]
            GEH[GlobalExceptionHandler]
        end

        subgraph Security [Spring Security 6]
            JWT[JwtAuthenticationFilter]
            RBAC[Role-Based Authorization]
        end

        subgraph Core_Services [Business Service Layer]
            CS[ClaimServiceImpl]
            SM[ClaimStateMachine Validator]
            DS[DashboardService - Liability Calculation]
        end

        subgraph Persistence [Data Layer]
            CR[ClaimRepository]
            CHR[ClaimHistoryRepository]
            CIR[ClaimInfoRequestRepository]
            UR[UserRepository]
        end

        subgraph Messaging [Dual Event Subsystem]
            EP[EventPublisher Interface]
            SEP[Spring In-Memory EventPublisher]
            KEP[KafkaEventPublisher]
        end
    end

    subgraph Infrastructure [Data Stores & Brokers]
        H2[(H2 In-Memory DB - Dev Profile)]
        PG[(PostgreSQL 16 - Docker Profile)]
        KF[Apache Kafka Cluster]
    end

    CP -->|REST / JWT| CC
    OP -->|REST / JWT| CC
    MP -->|REST / JWT| DC
    RS -->|REST / JWT| AC

    API_Layer --> Security
    Security --> Core_Services
    Core_Services --> SM
    Core_Services --> Persistence
    Core_Services --> Messaging

    Persistence --> H2
    Persistence --> PG
    Messaging --> SEP
    Messaging --> KEP
    KEP --> KF
```

---

## 2. Claim Lifecycle Sequence Diagram
The state machine strictly validates all transitions according to Chubb APAC insurance underwriting guidelines:

```mermaid
sequenceDiagram
    autonumber
    actor Claimant as Policyholder (Claimant)
    participant API as Spring Boot Claims API
    participant SM as ClaimStateMachine
    participant DB as PostgreSQL / H2
    participant EventBus as Event Publisher (Kafka)
    actor Officer as Claims Officer

    Claimant->>API: POST /api/v1/claims (FNOL Details)
    API->>DB: Persist Claim (Status = SUBMITTED)
    API->>DB: Record Audit History
    API->>EventBus: Publish ClaimCreatedEvent
    API-->>Claimant: 201 Created (Claim #CLM-SG-2026-1001)

    Officer->>API: POST /api/v1/claims/1/assign
    API->>SM: validateTransition(SUBMITTED, ASSIGNED)
    SM-->>API: Valid Transition
    API->>DB: Update Claim (Status = ASSIGNED, Officer = Sarah)
    API->>EventBus: Publish ClaimAssignedEvent
    API-->>Officer: 200 OK

    Officer->>API: POST /api/v1/claims/1/review
    API->>SM: validateTransition(ASSIGNED, UNDER_REVIEW)
    API->>DB: Update Claim (Status = UNDER_REVIEW)
    API-->>Officer: 200 OK

    alt Additional Information Needed
        Officer->>API: POST /api/v1/claims/1/request-info
        API->>SM: validateTransition(UNDER_REVIEW, ADDITIONAL_INFO_REQUIRED)
        API->>DB: Status = ADDITIONAL_INFO_REQUIRED, Record Request
        API-->>Officer: 200 OK
        Claimant->>API: POST /api/v1/claims/1/submit-info
        API->>SM: validateTransition(ADDITIONAL_INFO_REQUIRED, UNDER_REVIEW)
        API->>DB: Status = UNDER_REVIEW, Mark Info Resolved
        API-->>Claimant: 200 OK
    end

    Officer->>API: POST /api/v1/claims/1/assess (APPROVE, Amount = $4500)
    API->>SM: validateTransition(UNDER_REVIEW, APPROVED)
    API->>DB: Status = APPROVED, approvedAmount = $4500
    API->>EventBus: Publish ClaimStatusChangedEvent
    API-->>Officer: 200 OK

    Officer->>API: POST /api/v1/claims/1/settle
    API->>SM: validateTransition(APPROVED, SETTLED)
    API->>DB: Status = SETTLED
    API->>EventBus: Publish ClaimSettledEvent (Payout Integration)
    API-->>Officer: 200 OK
```

---

## 3. Communication Strategy: REST vs Kafka Deep-Dive

| Interaction Pattern | Protocol | Use Case | Architectural Justification |
| :--- | :--- | :--- | :--- |
| **Synchronous Command** | REST / JSON | User Login, FNOL Claim Submission, Self-Assignment | Requires immediate client feedback, HTTP status codes, and atomic validation before continuing. |
| **Synchronous Query** | REST / JSON | View Claim Detail, Outstanding Liability Metrics | High-frequency, immediate read operations required for interactive UI dashboards. |
| **Asynchronous Event** | Apache Kafka | `ClaimCreatedEvent`, `ClaimStatusChangedEvent` | Decouples long-running side-effects (fraud detection scoring, customer SMS/email notifications, data warehouse analytics). |
| **Financial Disbursement** | Apache Kafka | `ClaimSettledEvent` | Triggers downstream financial ledger and core banking payout systems with guaranteed delivery. |

---

## 4. Multi-Market Exposure Aggregation Formula
The executive dashboard calculates real-time liability exposure by aggregating active claim amounts across all 6 markets:

$$\text{Outstanding Liability} = \sum_{c \in \text{Claims}} \begin{cases} c.\text{estimatedAmount} & \text{if } c.\text{status} \in \{\text{SUBMITTED, ASSIGNED, UNDER\_REVIEW, ADDITIONAL\_INFO\_REQUIRED}\} \\ c.\text{approvedAmount} & \text{if } c.\text{status} = \text{APPROVED} \\ 0 & \text{if } c.\text{status} \in \{\text{SETTLED, REJECTED}\} \end{cases}$$
