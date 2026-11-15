#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
DOMAIN_POM="$ROOT_DIR/refund-domain/pom.xml"
APPLICATION_POM="$ROOT_DIR/refund-application/pom.xml"
DOMAIN_SRC="$ROOT_DIR/refund-domain/src/main/java"
APPLICATION_SRC="$ROOT_DIR/refund-application/src/main/java"

fail() {
  printf 'Architecture boundary violation: %s\n' "$1" >&2
  exit 1
}

if grep -Eqi 'langchain4j|langgraph4j|refund-application' "$DOMAIN_POM"; then
  fail "refund-domain must not depend on frameworks or the application module"
fi

if grep -Eqi 'langchain4j|langgraph4j' "$APPLICATION_POM"; then
  fail "refund-application must not depend on LangChain4j or LangGraph4j"
fi

if grep -R -n -E 'import (dev\.langchain4j|org\.bsc\.langgraph4j)' "$DOMAIN_SRC" "$APPLICATION_SRC"; then
  fail "domain and application source must remain framework-independent"
fi

if grep -R -n -E 'import dev\.refundworkflow\.application' "$DOMAIN_SRC"; then
  fail "domain source must not depend on application source"
fi

printf 'Core architecture boundaries: PASS\n'
printf 'refund-domain -> no framework/application dependencies\n'
printf 'refund-application -> domain only; no framework dependencies\n'
