# Prompt 01: System Architecture & Decomposition

## User Instruction
Decompose the Chubb APAC Take-Home Assessment requirements into a clean, testable system architecture. 
Evaluate whether this should be built as Microservices or a Modular Monolith, given the 3-5 hour time-box 
and the domain requirements (motor and property claims across six APAC markets).

## AI Recommendation
- Initial suggestion: 3 microservices (Claims Service, Assessment Service, Analytics Service).

## Engineer Challenge & Override
- **Challenge:** Creating separate microservice projects introduces network hops, distributed transaction complexity, and multi-container debugging overhead that distracts from the core domain assessment.
- **Decision:** Override to a **Modular Monolith** with strict logical boundaries (`api`, `domain`, `service`, `infrastructure`, `security`). 
- **Advantage:** Single deployment, shared transactions, instant startup in under 5 minutes for examiners, while preserving clear interfaces for future microservice extraction.
