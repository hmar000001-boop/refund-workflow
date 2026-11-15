# Phase 0 status — 2026-11-15

## Implemented in source

- Java 21 Maven reactor parent and one `refund-phase0-spike` module.
- Pinned candidate versions for LangGraph4j 1.9.9, LangChain4j 1.23.0 and `langchain4j-agentic` 1.23.0-beta33.
- LangGraph4j graph state and a two-node graph.
- Two declarative `@Agent` interfaces composed with `AgenticServices.sequenceBuilder()`.
- Deterministic `ChatModel` test doubles; no model provider credentials are required.
- Integration test in which a LangGraph4j node invokes the Agentic sequence.
- GitHub Actions workflow running the Maven Wrapper build.
- Version-alignment ADR and official Java source references.

## Environment observed

- JDK: OpenJDK 21.0.12.1.
- Maven: not installed globally.
- Maven Wrapper bootstrap: attempted, but failed before Maven could start because DNS resolution for `repo.maven.apache.org` failed (`curl: (6) Could not resolve host`).

## Execution status

**Not build-verified. Phase 0 is not complete.** The POM files pass local XML validation and `mvnw` passes POSIX shell syntax validation. These checks do not establish Java compilation or runtime compatibility. No successful `clean verify` result is claimed, and GitHub Actions has not yet run.

## Required exit gate

Run from a clean checkout on a network-enabled Java 21 environment:

```bash
./mvnw -B --no-transfer-progress clean verify
./mvnw -B dependency:tree -Dverbose
```

Then run `Phase0Application` from the IDE after the test suite passes. It uses deterministic fake `ChatModel` implementations and requires no provider credentials.

The gate requires all of the following:

1. The wrapper downloads and launches Maven 3.9.11.
2. Every pinned dependency resolves without conflicting LangChain4j versions or unexpected mediation.
3. All Java sources compile with `--release 21`.
4. The Agentic sequential composition test passes with deterministic model doubles.
5. The LangGraph4j graph invokes the Agentic workflow and returns the expected final state.
6. The runnable smoke application completes successfully.

If the tests fail, isolate and reproduce the incompatibility before changing versions. Do not add dependency exclusions or overrides without a regression test.

**Do not begin Chapter 19 until this exit gate is green.**
