# Prompt 10: Candidate Technical Audit & Submission Readiness Verification

## User Instruction
Perform a comprehensive technical audit against the Chubb APAC Engineering Take-Home Assessment brief, the candidate video instructions, and enterprise production guidelines:
1. Verify the 7-step Claims Lifecycle State Machine rigorously rejects illegal transitions (e.g., `SUBMITTED -> SETTLED`).
2. Verify cross-tenant isolation (claimants can only access their own claims) and market-level jurisdiction boundaries for claims officers.
3. Verify optimistic locking (`@Version`) prevents race conditions when multiple officers attempt to claim or assess the same record.
4. Verify multi-currency statutory exposure calculations group reserves by market currency (SGD, HKD, AUD, THB, MYR, IDR) without naive cross-currency summing.
5. Verify complete documentation:
   - Root `README.md` with architectural diagrams and <5 minute run instructions.
   - Root `NOTES.md` capturing candidate reflections and trade-offs.
   - `AI_WORKING_JOURNAL.md` (and mirrored in `.claude/ai-journal.md` and `.antigravity/ai-journal.md`).
   - OpenAPI 3.0 specification (`openapi/claims-api.yaml`) and Postman collection.
   - Comprehensive test suite covering domain logic, concurrency, security, and REST contracts.
