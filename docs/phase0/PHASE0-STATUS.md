# Phase 0 status — 2026-11-15

## Implemented in source

- Java 21 Maven reactor parent and one `refund-phase0-spike` module.
- Pinned candidate versions for LangGraph4j 1.9.9, LangChain4j 1.23.0 and `langchain4j-agentic` 1.23.0-beta33.
- LangGraph4j graph state and a three-node graph (`prepare` → `agentic-review` → `finish`); the middle node invokes the Agentic workflow.
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

**Build verification in progress. Phase 0 is not complete until the corrected CI run passes.** The POM files pass local XML validation and `mvnw` passes POSIX shell syntax validation. These checks do not establish the final corrected Java runtime behavior.

### First GitHub Actions result

The supplied run established that:

- Maven 3.9.11 launched successfully on Java 21.
- Both framework dependencies resolved sufficiently for compilation and test execution.
- All six production Java sources and the test source compiled successfully.
- The standalone LangChain4j Agentic sequential-composition test passed.
- The LangGraph4j integration test reached the Agentic node and returned its review, but failed its lifecycle-event assertion.

The defect was in the graph definition, not in framework compatibility: the graph declared `agentic-review` → `END`, while the test expected a `graph-finished` event from a node that did not exist. The source now defines an explicit `finish` node and routes `agentic-review` → `finish` → `END`. The test assertion is intentionally unchanged so it continues to verify the complete lifecycle. A new CI run is required to verify the correction.

The run also emitted an SLF4J no-provider warning. It was not responsible for the test failure; logging-provider selection remains a separate configuration decision.

## Required exit gate

Run from a clean checkout on a network-enabled Java 21 environment:

```bash
./mvnw -B --no-transfer-progress clean verify
./mvnw -B --no-transfer-progress -pl refund-phase0-spike dependency:tree -Dincludes=org.bsc.langgraph4j:*,dev.langchain4j:*
```

Then run `Phase0Application` from the IDE after the test suite passes. It uses deterministic fake `ChatModel` implementations and requires no provider credentials.

The gate requires all of the following:

1. The wrapper downloads and launches Maven 3.9.11.
2. Every pinned dependency resolves without conflicting LangChain4j versions or unexpected mediation.
3. All Java sources compile with `--release 21`.
4. The Agentic sequential composition test passes with deterministic model doubles.
5. The LangGraph4j graph invokes the Agentic workflow, executes the explicit `finish` node, and returns all three expected lifecycle events.
6. The runnable smoke application completes successfully.

If the tests fail, isolate and reproduce the incompatibility before changing versions. Do not add dependency exclusions or overrides without a regression test.

**Do not begin Chapter 19 until this exit gate is green.**
