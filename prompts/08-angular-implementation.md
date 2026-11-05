# Prompt 08: Angular Multi-Role Application & Component Separation

## User Instruction
Structure the Angular frontend into clean, dedicated feature components rather than an overloaded monolithic component:
- `features/claimant/`: FNOL incident report form, active claim tracking, visual status progression bar.
- `features/officer/`: Incoming workload queue, jurisdiction & status filters, review modal, and assessment controls.
- `features/manager/`: Multi-currency statutory liability exposure dashboard across all 6 APAC jurisdictions.

## Explicit Demo Mode
Avoid silent HTTP fallbacks. Implement an explicit `Demo Mode` toggle in `ClaimsService` with visual indicators so examiners clearly know whether the application is hitting the live Spring Boot API or running offline fixtures.
