# ADR 0002 — Keep the refund domain and application core framework-independent

- **Status:** Accepted
- **Date:** 2026-11-15

## Context

The application uses LangGraph4j for outer workflow orchestration and LangChain4j (including its experimental Agentic module) for bounded agent collaboration. The financial domain must not inherit lifecycle, serialization, model-provider or framework-specific concerns from either library.

## Decision

Create separate `refund-domain` and `refund-application` Maven modules. The domain contains typed business concepts and a pure eligibility policy. The application module coordinates inbound use cases with outbound ports and depends on the domain only. Framework integrations remain in dedicated adapters or orchestration modules introduced in later phases.

## Consequences

- Domain policy can be compiled and tested without loading either agent framework.
- Framework upgrades cannot directly change money, eligibility or approval semantics.
- Ports define the information required from payment and refund-history adapters.
- Positive eligibility is a recommendation to enter approval only; it is not financial authorization or a reservation.
- Atomic refund reservation and idempotent execution remain separate concerns for later phases.
