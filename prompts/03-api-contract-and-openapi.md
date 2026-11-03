# Prompt 03: API Contract & OpenAPI Specification

## User Instruction
Generate an OpenAPI 3.0 specification (`claims-api.yaml`) establishing the strict contract between the Spring Boot backend and the Angular frontend before writing any implementation code.

## Key Endpoints Defined
- `POST /api/v1/auth/login` & `GET /api/v1/auth/me`
- `POST /api/v1/claims` (FNOL Submission)
- `GET /api/v1/claims` & `GET /api/v1/claims/{id}`
- `POST /api/v1/claims/{id}/assign`
- `POST /api/v1/claims/{id}/review`
- `POST /api/v1/claims/{id}/request-info`
- `POST /api/v1/claims/{id}/submit-info`
- `POST /api/v1/claims/{id}/assess`
- `POST /api/v1/claims/{id}/settle`
- `GET /api/v1/dashboard/metrics` & `GET /api/v1/dashboard/liability`

## Postman Collection
Exported matching Postman Collection `chubb-claims.postman_collection.json` with sample environment variables for examiner validation.
