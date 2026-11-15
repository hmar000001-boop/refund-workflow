# Phase 1 — Domain and application core

**Status:** implementation prepared; awaiting GitHub Actions verification. Phase 0 was previously confirmed green by the user.

## Scope delivered

- New framework-independent `refund-domain` Maven module.
- New `refund-application` Maven module depending only on `refund-domain`.
- Currency-safe `Money` value object with no implicit rounding.
- Typed refund, payment, customer and request identifiers.
- Immutable payment snapshots, refund requests and refund-history records.
- Deterministic `RefundEligibilityPolicy` with typed result codes.
- `EvaluateRefundRequestUseCase` and an application service using injected ports and `Clock`.
- Unit tests for monetary invariants, eligibility outcomes, history accounting and application-port behavior.
- CI architecture-boundary check that rejects framework imports/dependencies from domain/application.

## Safety semantics

`ELIGIBLE_FOR_APPROVAL` is not an approval, financial authorization, reservation or execution instruction. No write port or payment mutation exists in Phase 1.

Pending and successful refunds reduce the remaining refundable balance. Failed and cancelled records do not. If refund history has a payment-reference or currency inconsistency, the service does not expose a potentially misleading remaining balance and routes the case to manual review. A payment already marked fully refunded has zero refundable balance.

## Verification performed in the preparation environment

- Main Java sources compiled with `javac --release 21 -Xlint:all -Werror`.
- Parent and module POM files are well-formed XML (checked separately).
- Architecture-boundary script passes.
- Shell syntax checks pass.
- Maven/Surefire tests have not been executed in this environment because Maven is not installed and external Maven Central access is unavailable.

## CI acceptance gate

1. `bash scripts/check-core-boundaries.sh` passes.
2. `./mvnw -B --no-transfer-progress clean verify` passes across all three modules.
3. Domain and application unit tests pass.
4. Existing Phase 0 cross-framework integration tests remain green.

No chapter 19 prose has been started. The next phase must not begin until this gate passes.

## Additional local checks

- 21 new domain/application JUnit test methods are present. Their source files compile against temporary local JUnit API stubs matching the APIs used; this is a source-compatibility check only, not a compilation against the real JUnit artifact or a JUnit execution.
- Four JDK-only smoke scenarios passed: available-balance computation with pending/succeeded/failed history, suspected-fraud manual review, fully-refunded payment rejection, and application-service orchestration with a fixed clock.
- A local `./mvnw clean verify` attempt was blocked before Maven started because this environment could not resolve `repo.maven.apache.org`. The authoritative JUnit and reactor result must therefore come from GitHub Actions.
