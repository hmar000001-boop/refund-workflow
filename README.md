# Refund Workflow

Java-only, code-first reference implementation using LangGraph4j and LangChain4j, including the experimental LangChain4j Agentic module.

## Toolchain

- Java 21
- Maven Wrapper (Maven 3.9.11)
- LangGraph4j 1.9.9
- LangChain4j 1.23.0
- LangChain4j Agentic 1.23.0-beta33

Framework versions remain pinned in the parent POM. The Phase 0 CI build confirmed the compatibility spike and its deterministic integration tests.

## Reactor modules

| Module | Responsibility | Framework dependency |
|---|---|---|
| `refund-domain` | Refund domain model, money value object and pure eligibility policy | None |
| `refund-application` | Refund use case, application result and outbound query ports | Domain only |
| `refund-phase0-spike` | Proven LangGraph4j-to-LangChain4j-Agentic integration | LangGraph4j + LangChain4j |

The domain and application modules must remain independent of orchestration and model frameworks. Framework-specific adapters will be introduced in later phases, outside the domain/application core.

## Phase 1 scope

Phase 1 implements read-only refund eligibility evaluation. A positive eligibility decision means **eligible to enter approval**, not approved and not authorized for execution. No payment or refund mutation is performed in this phase.

The deterministic policy:

- verifies payment and customer references;
- requires a captured payment (a disputed payment routes to manual review);
- validates currency consistency;
- counts pending and successful refunds against the refundable balance;
- excludes failed and cancelled refunds from the reserved balance;
- refuses to infer a safe balance from inconsistent refund history;
- routes duplicate request identifiers and suspected fraud to manual review;
- rejects payments already fully refunded.

The policy does not reserve funds. Atomic reservation and idempotent execution belong to the later execution phase.

## Build and verification

```bash
bash scripts/check-core-boundaries.sh
./mvnw -B --no-transfer-progress clean verify
```

CI runs both checks, then inspects the resolved LangGraph4j/LangChain4j dependency tree and uploads Surefire reports.

## Development rule

Implementation and tests precede chapter prose. Each increment must build and pass its acceptance criteria in CI before its corresponding chapter is written.
