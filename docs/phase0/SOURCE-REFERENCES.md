# Phase 0 — Official Java source references

Research is restricted to the Java documentation, tagged source and Maven metadata of **LangGraph4j** and **LangChain4j**. No Python framework documentation or Python implementation is part of this project.

## LangGraph4j 1.9.9

- Project documentation: https://langgraph4j.github.io/langgraph4j/
- Tagged source and examples: https://github.com/langgraph4j/langgraph4j/tree/v1.9.9
- Parent POM (declared LangChain4j alignment): https://raw.githubusercontent.com/langgraph4j/langgraph4j/v1.9.9/pom.xml
- `StateGraph`: https://raw.githubusercontent.com/langgraph4j/langgraph4j/v1.9.9/langgraph4j-core/src/main/java/org/bsc/langgraph4j/StateGraph.java
- `CompiledGraph`: https://raw.githubusercontent.com/langgraph4j/langgraph4j/v1.9.9/langgraph4j-core/src/main/java/org/bsc/langgraph4j/CompiledGraph.java
- `NodeAction`: https://raw.githubusercontent.com/langgraph4j/langgraph4j/v1.9.9/langgraph4j-core/src/main/java/org/bsc/langgraph4j/action/NodeAction.java
- `GraphInput`: https://raw.githubusercontent.com/langgraph4j/langgraph4j/v1.9.9/langgraph4j-core/src/main/java/org/bsc/langgraph4j/GraphInput.java

## LangChain4j 1.23.0 and experimental Agentic 1.23.0-beta33

- Java documentation: https://docs.langchain4j.dev/
- Agentic tutorial, pinned to 1.23.0 documentation source: https://github.com/langchain4j/langchain4j/blob/1.23.0/docs/docs/tutorials/agents.md
- Agentic module POM: https://raw.githubusercontent.com/langchain4j/langchain4j/1.23.0/langchain4j-agentic/pom.xml
- Tagged source: https://github.com/langchain4j/langchain4j/tree/1.23.0

The Agentic tutorial explicitly describes `langchain4j-agentic` as experimental and subject to change. This project deliberately includes it, isolates its use behind an adapter, pins the beta version, and treats API compatibility tests as a release gate.

## Version-alignment interpretation

The LangGraph4j 1.9.9 parent POM declares LangChain4j 1.23.0 and the beta line 1.23.0-beta33. The LangChain4j Agentic 1.23.0 POM inherits beta33 and declares a dependency on LangChain4j 1.23.0. This is documentary alignment, not proof that the complete application compiles or behaves correctly. The Maven build and integration tests must establish that.
