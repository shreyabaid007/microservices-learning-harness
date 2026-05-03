#!/usr/bin/env bash
# Runs on every Claude Stop event. Blocks if the build is red.
set -euo pipefail

ROOT="$(git rev-parse --show-toplevel)"
cd "$ROOT"

if ! output=$(./mvnw -q verify 2>&1); then
  summary=$(printf '%s' "$output" | tail -30 | tr -d '\000-\010\013-\037' | sed 's/\\/\\\\/g; s/"/\\"/g' | tr '\n' ' ')
  printf '{"decision":"block","reason":"mvn verify is red — fix before stopping. Last output: %s"}\n' "$summary"
  exit 1
fi
