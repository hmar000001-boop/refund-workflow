# ADR-0001: Phase 0 framework version alignment

- Status: **Candidate — not accepted until the clean Maven build passes**
- Date: 2026-11-15

## Context

The compatibility spike must run LangGraph4j and LangChain4j Agentic in the same Java 21 application. `langchain4j-agentic` is experimental and must be included deliberately.

## Documentary evidence

The LangGraph4j `v1.9.9` parent POM declares:

- `langchain4j.version = 1.23.0`
- `langchain4j.beta = 1.23.0-beta33`

The LangChain4j `langchain4j-agentic` POM at tag `1.23.0` has parent version `1.23.0-beta33` and depends on `langchain4j` `1.23.0`.

This is a strong alignment signal from the two projects' own Maven metadata, but it does not prove every runtime combination works in our application.

## Candidate versions

| Component | Candidate |
|---|---:|
| JDK | Temurin 21 |
| LangGraph4j core | 1.9.9 |
| LangChain4j | 1.23.0 |
| LangChain4j Agentic | 1.23.0-beta33 |
| Maven Wrapper | 3.3.2 |
| Maven distribution | 3.9.11 |

## Decision

Use the candidate versions in Phase 0 only. Do not call them a verified compatible baseline until all of the following pass from a clean checkout:

1. `./mvnw -B clean verify` resolves and compiles every dependency.
2. The LangGraph4j two-node graph test passes.
3. The LangChain4j Agentic sequential composition test passes with deterministic `ChatModel` implementations and no provider credentials.
4. A LangGraph4j node invokes the Agentic workflow and the final graph state contains the expected result.
5. `./mvnw dependency:tree` is inspected for accidental version mediation or duplicate incompatible LangChain4j artifacts.

If the tests fail, isolate the incompatibility and test the maintained LangGraph4j 1.8.x line before changing the LangChain4j pair. Do not add exclusions or overrides without a reproducible reason and regression test.

## Consequences

The Agentic dependency remains isolated in the future `refund-langchain4j` adapter module. Graph state receives a validated DTO/string result; `AgenticScope` is not stored in graph state. LangGraph4j remains the only owner of the long-running refund lifecycle.
