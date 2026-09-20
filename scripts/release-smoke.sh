#!/usr/bin/env bash
set -euo pipefail
DIR="$(cd "$(dirname "$0")" && pwd)"
PORT="${SERVER_PORT:-8091}"
cd "$DIR"
./start.sh > "$DIR/smoke.log" 2>&1 &
PID=$!
cleanup() { kill "$PID" 2>/dev/null || true; wait "$PID" 2>/dev/null || true; }
trap cleanup EXIT
ok=0
for _ in $(seq 1 90); do
  if curl -sf "http://127.0.0.1:${PORT}/" >/dev/null 2>&1; then ok=1; break; fi
  sleep 1
done
if [[ "$ok" != "1" ]]; then
  echo "SMOKE FAIL crm: app did not start on :$PORT"
  tail -n 80 "$DIR/smoke.log" || true
  exit 1
fi
html="$(curl -sS "http://127.0.0.1:${PORT}/")"
echo "$html" | grep -qiE '<html|<div id=.app' || { echo "SMOKE FAIL crm: / is not HTML"; exit 1; }
spa="$(curl -sS -o /tmp/crm-spa.body -w "%{http_code}" "http://127.0.0.1:${PORT}/dashboard")"
test "$spa" = "200"
code="$(curl -sS -o /tmp/crm-probe.body -w "%{http_code}" "http://127.0.0.1:${PORT}/api/leads")"
test "$code" = "200" || { echo "SMOKE FAIL crm: /api/leads HTTP $code"; cat /tmp/crm-probe.body; exit 1; }

echo "SMOKE OK crm :$PORT"
