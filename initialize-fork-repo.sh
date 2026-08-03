#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
ORIGIN_URL="${1:-}"
if [ ! -d .git ]; then
  git init -b main
fi
if ! git remote get-url upstream >/dev/null 2>&1; then
  git remote add upstream https://github.com/Hamza417/Inure.git
fi
if [ -n "$ORIGIN_URL" ]; then
  if git remote get-url origin >/dev/null 2>&1; then
    git remote set-url origin "$ORIGIN_URL"
  else
    git remote add origin "$ORIGIN_URL"
  fi
fi
git status --short
printf 'UPSTREAM=%s\n' "$(git remote get-url upstream)"
if git remote get-url origin >/dev/null 2>&1; then
  printf 'ORIGIN=%s\n' "$(git remote get-url origin)"
fi
