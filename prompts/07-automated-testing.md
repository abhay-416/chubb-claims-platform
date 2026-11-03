# Prompt 07: Comprehensive Automated Testing

## User Instruction
Write automated JUnit 5 and MockMvc tests covering:
1. Valid claim submission and lifecycle progression.
2. Rejection of illegal state transitions (e.g., `SUBMITTED` directly to `SETTLED`).
3. Role authorization enforcement (e.g. Claimant cannot approve claims).
4. Real-time liability exposure calculation across markets.

# Prompt 08: Angular Frontend & Dual Role Portals

## User Instruction
Build an Angular Single Page Application serving both user groups:
1. **Claimant Portal:** New FNOL incident submission form, real-time status tracker with timeline steps, additional document/info response modal.
2. **Claims Officer & Manager Portal:** Consolidated claim queue with status filter, claim detail view with action buttons (Assign, Review, Request Info, Assess, Settle), and Manager Real-time Liability Exposure Metrics by market.
3. **Role Switcher:** Global top navigation bar allowing examiners to test both roles with 1 click.
