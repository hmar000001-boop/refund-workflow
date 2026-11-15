# Refund Workflow — Phase 0 compatibility spike

Java-only code-first reference project using **LangGraph4j** and **LangChain4j**, including the experimental `langchain4j-agentic` module.

This phase proves a narrow technical seam before any refund business domain is implemented:

1. A declarative LangChain4j `@Agent` interface is created using a deterministic fake `ChatModel`.
2. Two agents execute in a LangChain4j Agentic sequential workflow.
3. A LangGraph4j graph node invokes that workflow.
4. The final LangGraph4j state contains the Agentic result.

No Python, no other agent framework, no model API key and no real refund gateway are used.

## Requirements

- JDK 21
- Network access to Maven Central on first wrapper/build run

## Run

```bash
./mvnw -B --no-transfer-progress clean verify
```

Run `Phase0Application` from an IDE after the test suite passes. It uses deterministic fake `ChatModel` implementations and requires no provider credentials.

## Version decision

See [ADR-0001](docs/adr/0001-phase0-framework-version-alignment.md). The candidate version pair is based on the LangGraph4j 1.9.9 POM and LangChain4j Agentic 1.23.0-beta33 POM. It is not considered verified until the clean build and integration tests pass.

## Status

See [Phase 0 status](docs/phase0/PHASE0-STATUS.md). The source and CI workflow are prepared, but this authoring container could not execute Maven because Maven is absent and outbound DNS to Maven Central/GitHub failed. Do not treat Phase 0 as complete until CI is green.
