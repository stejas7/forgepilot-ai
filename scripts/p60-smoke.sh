#!/usr/bin/env bash
set -Eeuo pipefail

BASE_URL="${1:-${FORGEPILOT_BASE_URL:-https://forgepilot-ai.duckdns.org}}"
BASE_URL="${BASE_URL%/}"

printf 'P60 smoke target: %s\n' "$BASE_URL"

health="$(curl --connect-timeout 4 --max-time 10 -fsS "$BASE_URL/actuator/health")"
printf '%s' "$health" | grep -q '"status":"UP"'

providers="$(curl --connect-timeout 4 --max-time 10 -fsS "$BASE_URL/api/auth/providers")"
printf '%s' "$providers" | grep -q '"id":"google"'
printf '%s' "$providers" | grep -q '"id":"github"'

root_code="$(curl --connect-timeout 4 --max-time 10 -sS -o /dev/null -w '%{http_code}' "$BASE_URL/")"
case "$root_code" in 200|302) ;; *) echo "Unexpected root status: $root_code"; exit 1;; esac

for provider in google github; do
  code="$(curl --connect-timeout 4 --max-time 10 -sS -o /dev/null -w '%{http_code}' "$BASE_URL/oauth2/authorization/$provider")"
  case "$code" in 301|302|303|307|308) ;; *) echo "OAuth $provider did not redirect: HTTP $code"; exit 1;; esac
done

printf 'P60 smoke PASS: health, public entry and Google/GitHub OAuth starts are reachable.\n'
