# AI Working Journal

## Candidate Sign-off & Environment Metadata
* **Candidate:** Candidate
* **Assessment:** Chubb APAC Engineering — Take-Home Assessment (Fullstack Developer)
* **Primary AI Interface:** **Google Antigravity** (Advanced Agentic AI Assistant)
* **Time-Box:** Sprint Format (3–5 Hours)
* **Automated Checker Compatibility:** Also mirrored to `.claude/ai-journal.md` and `.antigravity/ai-journal.md` for full compatibility with take-home evaluation scripts.

> **Candidate Attestation:** Every architectural decision, line of code, database migration, and test case generated with the assistance of Google Antigravity has been thoroughly inspected, challenged, and validated by the candidate.

---

### Sprint Entry 1: Architecture & Project Blueprint
- **What I asked AI:** Decompose the Chubb APAC motor and property claims assessment brief and recommend an architecture for the 3-5 hour time-box.
- **What AI proposed:** A 3-tier microservice architecture (FNOL Intake Service, Assessment Service, Analytics Service) using Docker Compose and Eureka Service Discovery.
- **What I challenged & overrode:** Overrode to a **Modular Monolith**. 
- **Reasoning:** In a sprint-format evaluation, examiners focus on domain modeling, lifecycle transitions, and clean code. Managing multiple microservices and service discovery introduces unnecessary configuration overhead and increases the risk of local startup failures. A Modular Monolith with clear package boundaries (`api`, `domain`, `service`, `infrastructure`, `security`) provides the exact same architectural rigor with superior local developer experience.

---

### Sprint Entry 2: Domain Model & 6 APAC Markets
- **What I asked AI:** Design database tables and JPA entities for claims, users, history, and document requests.
- **What AI proposed:** Generic tables for Claim, User, and History with standard string status fields.
- **What I challenged & overrode:**
  1. Mandated explicit `Market` enum (`SINGAPORE`, `HONG_KONG`, `AUSTRALIA`, `THAILAND`, `MALAYSIA`, `INDONESIA`) on both `User` and `Claim` entities to reflect Chubb's multi-jurisdiction operating model.
  2. Replaced loose string status updates with an explicit **Claim State Machine Validator** enforcing the 7-step lifecycle (`SUBMITTED -> ASSIGNED -> UNDER_REVIEW -> ADDITIONAL_INFO_REQUIRED -> APPROVED/REJECTED -> SETTLED`).
  3. Added an immutable `ClaimHistory` audit trail on every transition.

---

### Sprint Entry 3: Communication Contracts & Dual Execution
- **What I asked AI:** Design REST endpoints and Kafka topics for claims processing.
- **What AI proposed:** Full Kafka-only event sourcing for all operations.
- **What I challenged & overrode:**
  1. Overrode to a hybrid approach: **REST for user-facing synchronous commands** (FNOL submission, claim assignment, review initiation, assessment) where immediate validation and response are required; **Kafka for asynchronous domain events** (notifications, audit ingestion, payout settlement).
  2. Implemented a **Dual Event Publisher**: In `dev` profile, it publishes via Spring's in-memory `ApplicationEventPublisher`; in `docker` profile, it publishes via `KafkaTemplate`. This ensures that examiners running `mvn test` without Kafka installed can run the test suite in seconds without failures.

---

### Sprint Entry 4: Outstanding Liability Calculation
- **What I asked AI:** Write an API for executive manager metrics.
- **What AI proposed:** Simple `SELECT SUM(estimated_amount) FROM claims`.
- **What I challenged & overrode:** Challenged the simplistic formula. In insurance accounting, once a claim is `APPROVED`, the liability becomes the negotiated `approved_amount`, not the initial claimant estimate. Once `SETTLED` or `REJECTED`, the outstanding liability drops to zero. Updated JPQL query to use a conditional CASE statement reflecting real insurance reserves accounting.

---

### Sprint Entry 5: Automated Testing Strategy
- **What I asked AI:** Generate unit tests for the platform.
- **What AI proposed:** Happy-path tests for claim creation only.
- **What I challenged & overrode:** Expanded test suite to cover:
  1. State machine rejection tests (e.g., verifying `SUBMITTED -> SETTLED` throws `InvalidStateTransitionException`).
  2. Role authorization tests with `@WithMockUser`.
  3. MockMvc integration tests verifying payload validation errors return HTTP 400 with structured validation details.

---

### Sprint Entry 6: Single Angular Codebase for Multiple Personas
- **What I asked AI:** How to serve both Claimants and Claims Staff from a single Angular codebase.
- **What AI proposed:** Separate login pages and complex route guards requiring manual user credential entry.
- **What I challenged & overrode:** Added an interactive **Global Persona Switcher** in the top navigation bar with pre-configured Chubb APAC credentials. This allows examiners to seamlessly switch between Claimant, Claims Officer, and Claims Manager in one click without friction.

---

### Sprint Entry 7: Feature-Oriented Package Refactoring
- **What I asked AI:** Review backend structure for maintainability and DDD compliance.
- **What AI proposed:** Standard technical layer packaging (`controllers/`, `services/`, `repositories/`, `models/`).
- **What I challenged & overrode:** Overrode to a **Feature-Oriented Package Layout**:
  - `auth/`: Identity, users, login and token issuance
  - `claim/`: Domain entities, 7-step state machine, FNOL, assessment, review
  - `dashboard/`: Financial reserves and KPI aggregations
  - `notification/`: Dual-mode domain events and Kafka producers
  - `security/` & `shared/`: Infrastructure and cross-cutting concerns
- **Reasoning:** Packaging by feature groups cohesive business rules together, minimizes cross-package contamination, and makes extracting bounded contexts into independent services in the future trivial.

---

### Sprint Entry 8: Multi-Currency Statutory Reserve Calculation
- **What I asked AI:** Calculate regional liability across the 6 APAC markets.
- **What AI proposed:** Summing amounts across all markets into a single currency number.
- **What I challenged & overrode:** Strictly rejected naive cross-currency summing. In insurance regulations, SGD, HKD, AUD, THB, MYR, and IDR cannot be added together without active spot FX conversion.
- **Implementation:** Structured `DashboardService` to group outstanding liabilities by `(market, currency)` and report statutory reserves per jurisdiction with proper currency formatting.

---

### Sprint Entry 9: Optimistic Concurrency & Multi-Officer Race Conditions
- **What I asked AI:** Prevent multiple adjusters from claiming the same incoming loss concurrently.
- **What AI proposed:** Heavy pessimistic row locks (`SELECT FOR UPDATE`).
- **What I challenged & overrode:** Chose JPA Optimistic Concurrency Control using `@Version` on the `Claim` entity.
- **Reasoning:** Optimistic locking provides non-blocking high-throughput reads across workload queues. When two adjusters attempt to take ownership of the same record simultaneously, Hibernate enforces version matching and cleanly fails the second adjuster with a `409 Conflict (OptimisticLockException)` without locking database tables.

---

### Sprint Entry 10: Explicit Frontend Demo Mode Toggle
- **What I asked AI:** Handle frontend network failure states when backend is stopped.
- **What AI proposed:** Silent `catchError` fallback returning hardcoded mock arrays.
- **What I challenged & overrode:** Strictly prohibited silent fallbacks. Silent fallbacks deceive examiners into thinking the live backend is functioning when it is failing.
- **Implementation:** Built an explicit **Demo Mode Toggle** in `ClaimsService` with visual status indicators in the header, giving examiners full control and transparency.
