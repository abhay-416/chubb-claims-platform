# Architecture Decision Records (ADRs)

This document formalizes the key technical and design trade-offs made for the Chubb APAC Claims Management Platform.

---

## ADR 01: Modular Monolith vs Distributed Microservices

### Context
Insurance claim processing involves tight consistency boundaries between policy submission, identity verification, document requests, and reserve accounting across 6 APAC markets. A microservices architecture introduces network latency, distributed transactions (Sagas/2PC), and high operational complexity.

### Decision
Implement a **Feature-Oriented Modular Monolith** inside Spring Boot 3.x with strict domain package isolation:
- `auth`: Identity & JWT token issuance
- `claim`: Core claims lifecycle, state machine, and adjusters
- `dashboard`: Financial reserve analytics & exposure calculation
- `notification`: Cross-boundary events & Kafka integration
- `security` & `shared`: Infrastructure, error handling, cross-cutting concerns

### Consequences
- **Positive:** Zero distributed transaction overhead; sub-millisecond local service calls; compile-time boundary enforcement; effortless deployment and testing.
- **Negative:** Shared process space; requires discipline to avoid circular dependencies between packages.

---

## ADR 02: Synchronous REST vs Asynchronous Kafka Domain Events

### Context
The candidate assessment requires real-time workflow coordination between claimants and adjusters, as well as downstream event streaming to external systems (reinsurance, accounting, notifications).

### Decision
Adopt a **Hybrid Architecture**:
1. **Synchronous REST APIs** for operational user workflows where immediate state confirmation is required (FNOL submission, assignment, claim assessment).
2. **Asynchronous Apache Kafka Events** (`chubb.apac.claims.events`) for downstream consumers and decoupling.
3. Pluggable `EventPublisher` interface supporting in-process Spring ApplicationEvents (`dev` profile) and Kafka Template (`docker` profile).

### Consequences
- **Positive:** Interactive UI receives instant HTTP status codes; downstream analytics and legacy systems receive immutable event streams.
- **Negative:** Requires handling network failure scenarios when communicating with Kafka brokers (mitigated by transactional outbox table).

---

## ADR 03: Multi-Currency Statutory Liability Exposure Modeling

### Context
Chubb APAC operates across 6 distinct jurisdictions with different legal currencies:
- Singapore: `SGD`
- Hong Kong: `HKD`
- Australia: `AUD`
- Thailand: `THB`
- Malaysia: `MYR`
- Indonesia: `IDR`

Summing these amounts into a single arbitrary figure without real-time FX spot rates violates insurance statutory reserve regulations.

### Decision
Calculate financial exposure strictly grouped by statutory currency and market:
- Open claims (`SUBMITTED`, `ASSIGNED`, `UNDER_REVIEW`, `ADDITIONAL_INFO_REQUIRED`) contribute their `estimatedAmount`.
- Approved claims (`APPROVED`) contribute their finalized `approvedAmount`.
- Closed claims (`SETTLED`, `REJECTED`) contribute `0.00` active reserve liability.
- Results are reported per market with currency symbols and counts.

### Consequences
- **Positive:** Complies with regulatory solvency standards; avoids fake currency conversions; clear auditable figures for regional managers.

---

## ADR 04: Optimistic Locking for Claim Assignment & Assessment

### Context
In high-volume regional claim centers, multiple officers might view the unassigned pool simultaneously and click "Assign to Me" or submit concurrent assessment decisions on the same claim.

### Decision
Implement JPA Optimistic Concurrency Control using `@Version private Long version;` on the `Claim` entity.

### Consequences
- **Positive:** No database table or row lock contention during reads; first write wins, subsequent conflicting writes fail immediately with `409 Conflict (OptimisticLockException)`.
- **Negative:** The client must refresh the record if a concurrent modification occurred.

---

## ADR 05: Explicit Demo Mode vs Silent Mock Fallback in Frontend

### Context
Frontend applications often implement fallback logic where failed HTTP requests silently fall back to mock data. In an engineering assessment, this masks backend outages, authentication bugs, or network configuration errors.

### Decision
Implement an **Explicit Demo Mode**:
- `ClaimsService.isDemoMode` defaults to `false` (live API connection).
- If the backend is unreachable, the UI surfaces clear error banners.
- Examiners can toggle Demo Mode on demand with visual status badges in the header.

### Consequences
- **Positive:** Examiners have complete transparency regarding whether they are testing live Spring Boot endpoints or mock fixtures.
- **Negative:** Slightly more frontend state management for mock data toggling.

---

## ADR 06: Zero-Setup Dual Profile Configuration (H2 vs PostgreSQL)

### Context
Examiners evaluating take-home assignments need the platform to boot and run reliably in under 5 minutes without mandatory Docker daemon or external database setup.

### Decision
Configure Spring Boot dual profiles:
1. `dev` (Default): Uses in-memory H2 database (with PostgreSQL syntax compatibility) and in-memory Spring ApplicationEvents. Starts in ~3 seconds with zero external dependencies.
2. `docker`: Uses PostgreSQL 16 container, Apache Kafka KRaft cluster, and containerized Angular frontend via `docker compose up --build`.

### Consequences
- **Positive:** Guaranteed out-of-the-box runnability in any evaluator environment.
