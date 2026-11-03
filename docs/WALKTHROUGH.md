# Panel Interview Walkthrough Guide (15-20 Min Script & Q&A)

This document is prepared to guide your 15–20 minute walkthrough presentation and defend architectural choices during the panel Q&A.

---

## 1. Presentation Structure (15–20 Minutes)

### Minute 0–3: Understanding the Chubb APAC Domain
- *"Chubb operates across 6 APAC markets processing high volumes of motor and property claims. Prior to this platform, claims were fragmented across inboxes and spreadsheets, leaving policyholders with zero visibility and management unable to compute outstanding liability exposure."*
- *"We set out to build a unified platform serving both Claimants (First Notice of Loss, tracking, info submission) and Claims Staff (workload queue, assessment, manager exposure analytics) through a single Angular application."*

### Minute 3–7: Architecture & Key Decisions
- **Modular Monolith over Microservices:** Explain that while service boundaries are strictly separated (`domain`, `service`, `infrastructure`, `security`), keeping them in a single deployable artifact allows sub-second local startup, zero distributed-transaction complexity, and clean eventual extraction to microservices.
- **REST vs Kafka Strategy:** Explain synchronous commands (immediate validation on submission) vs asynchronous domain events (audit trail, downstream accounting).
- **Dual-Profile Runtime:** Highlight how default H2 in-memory DB enables running the entire test suite in under 30 seconds, while Docker Compose provisions PostgreSQL 16 + Confluent Kafka in one command.

### Minute 7–13: Live Application Demonstration
1. **Claimant Persona:**
   - Log in as `claimant.sg@gmail.com`.
   - Submit a new motor claim in Singapore (`$4,500`).
   - Show initial status `SUBMITTED` and the visual lifecycle tracker.
2. **Claims Officer Persona:**
   - Click the Role Switcher to `officer@chubb.com`.
   - Show the claim appearing in the Workload Queue.
   - Click "Assign to Me" ➔ Status changes to `ASSIGNED`.
   - Click "Commence Assessment" ➔ Status changes to `UNDER_REVIEW`.
   - Demonstrate the State Machine in action: Request additional information ➔ Claimant supplies info ➔ Review resumes.
   - Click "Approve Claim" with approved payout of `$4,200`.
   - Click "Settle Claim" ➔ Status changes to `SETTLED`. Show the immutable audit trail with timestamps.
3. **Manager Persona:**
   - Click the Role Switcher to `manager@chubb.com`.
   - Demonstrate the real-time **Liability Exposure Dashboard** updating dynamically across the 6 APAC markets.

### Minute 13–15: AI Tooling & Working Journal
- Walk the panel through `AI_WORKING_JOURNAL.md` and `prompts/`.
- Highlight areas where AI suggestions were **challenged and overridden** (e.g. rejecting premature microservices, enforcing multi-market modeling, designing accurate insurance reserves formulas).

---

## 2. Panel Q&A Defense ("Why Not X?" Deep-Dive)

### Q1: Why a Modular Monolith instead of Microservices?
> **Answer:** *"In early-stage systems and tight time-boxes, distributed systems create latency, network boundaries, and eventual consistency challenges before domain boundaries are proven. By building a Modular Monolith with strict package encapsulation and domain events, we achieved full testability, single-command deployment, and zero network hops. When traffic scales, the `ClaimService`, `AssessmentService`, and `DashboardService` can be extracted into independent containerized microservices without altering domain logic."*

### Q2: Why both REST and Kafka? What happens if Kafka is down?
> **Answer:** *"User interactions require immediate deterministic feedback: when a claimant submits an FNOL or an officer approves a payout, they must receive immediate validation (HTTP 200/201). Conversely, side-effects like fraud analysis, SMS notifications, and GL accounting integrations are asynchronous. If Kafka experiences downtime, the core database transaction succeeds, and using the Transactional Outbox Pattern, pending events remain safely persisted in PostgreSQL and replay automatically once Kafka recovers."*

### Q3: How do you handle concurrent claim assignments?
> **Answer:** *"We utilize JPA optimistic locking (`@Version` on the Claim entity) and a strict State Machine validator. If two officers attempt to pick up the same `SUBMITTED` claim simultaneously, the first transaction updates the status to `ASSIGNED`; the second transaction encounters a state mismatch (`SUBMITTED -> ASSIGNED` is only valid once) and receives an `OptimisticLockException` or `InvalidStateTransitionException`."*

### Q4: How is Liability Exposure calculated across the 6 markets?
> **Answer:** *"We use an actuarial reserve formula: For open claims in `SUBMITTED`, `ASSIGNED`, `UNDER_REVIEW`, or `ADDITIONAL_INFO_REQUIRED`, we sum the initial claimant `estimatedAmount`. As soon as a claim is `APPROVED`, the liability crystallizes into the negotiated `approvedAmount`. Once a claim transitions to `SETTLED` or `REJECTED`, its outstanding liability drops to zero. This is indexed by `market` to give regional executives real-time exposure figures."*

---

## 3. What would you do with more time? (10 Min Segment)
1. **Transactional Outbox with Debezium CDC:** Replace synchronous event publishing with Debezium log-based Change Data Capture for zero dual-write vulnerabilities.
2. **Document OCR & Automated FNOL Triage:** Integrate AWS Textract or GCP Document AI to automatically extract damage quotes and police reports upon upload.
3. **Localized Currency & Multi-Market Regulatory Rules:** Add currency conversion (SGD, HKD, AUD, THB, MYR, IDR) to a standardized reporting base currency (USD).
4. **Idempotency Keys:** Enforce `Idempotency-Key` headers on `POST /claims` to prevent accidental duplicate submissions during network retries.
