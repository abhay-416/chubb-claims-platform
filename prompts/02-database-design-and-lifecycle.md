# Prompt 02: Database Design & State Machine Lifecycle

## User Instruction
Design the relational database entities and the claim lifecycle state machine for Chubb APAC motor and property claims. Ensure market-awareness for 6 APAC markets and an immutable audit trail.

## Domain Model Design
- `User`: id, name, email, password, role (`CLAIMANT`, `CLAIMS_OFFICER`, `CLAIMS_MANAGER`), market.
- `Claim`: id, claim_number, policy_number, claim_type (`MOTOR`, `PROPERTY`), market (`SINGAPORE`, `HONG_KONG`, `AUSTRALIA`, `THAILAND`, `MALAYSIA`, `INDONESIA`), status, estimated_amount, approved_amount, claimant_id, assigned_officer_id.
- `ClaimHistory`: id, claim_id, old_status, new_status, changed_by, notes, timestamp.
- `ClaimRequest`: id, claim_id, requested_info, requested_by, response, responded_at, status (`PENDING`, `RESOLVED`).
- `ClaimDocument`: id, claim_id, file_name, file_type, file_url, uploaded_at.

## State Machine Rules
1. `SUBMITTED` -> `ASSIGNED` (Officer picks up claim)
2. `ASSIGNED` -> `UNDER_REVIEW` (Officer initiates investigation)
3. `UNDER_REVIEW` -> `ADDITIONAL_INFO_REQUIRED` (Officer requests info)
4. `ADDITIONAL_INFO_REQUIRED` -> `UNDER_REVIEW` (Claimant provides info)
5. `UNDER_REVIEW` -> `APPROVED` or `REJECTED` (Officer decision)
6. `APPROVED` -> `SETTLED` (Disbursement complete)
*All illegal transitions (e.g. `SUBMITTED` -> `SETTLED`) must be rejected with 400 Bad Request.*
