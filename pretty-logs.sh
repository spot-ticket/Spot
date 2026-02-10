#!/bin/bash
set -euo pipefail

NAMESPACE="${1:-spot}"
MODE="${2:-follow}"   # follow | once

SERVICES=("spot-user" "spot-store" "spot-order" "spot-payment" "spot-gateway")
# Check jq dependency
if ! command -v jq &> /dev/null; then
  echo "[WARN] jq is not installed. Installing jq..."
  brew install jq
fi

FORMAT_JQ='
  . as $line
  | (fromjson? // $line) as $x
  | if ($x|type) == "object" then
      ("[" + $svc + "] "
       + (($x."@timestamp" // $x.timestamp // $x.time // "")|tostring)
       + " ["
       + (($x.thread_name // $x.thread // "")|tostring)
       + "] "
       + (($x.level // $x."log.level" // "")|tostring)
       + " "
       + (($x.logger_name // $x.logger // "")|tostring)
       + " - "
       + (($x.message // $x.msg // "")|tostring)
      )
      + (if ((($x.level // $x."log.level" // "")|tostring) == "ERROR") and ($x.stack_trace? != null) then
           "\n" + ($x.stack_trace|tostring)
         else "" end)
    else
      ("[" + $svc + "] " + ($x|tostring))
    end
  + "\n"
'


usage() {
  echo "Usage:"
  echo "  $0 [namespace] [once|follow]"
  echo ""
  echo "Examples:"
  echo "  $0                 # spot namespace, follow all services"
  echo "  $0 spot            # follow all services in spot"
  echo "  $0 spot once       # one-time logs for all services"
  echo "  $0 monitoring      # follow all services in monitoring (if names exist)"
}

if [[ "${1:-}" == "--help" || "${1:-}" == "-h" ]]; then
  usage
  exit 0
fi

echo "[INFO] Namespace: $NAMESPACE"
echo "[INFO] Mode: $MODE"
echo "[INFO] Services: ${SERVICES[*]}"
echo ""

run_one() {
  local svc="$1"

  if [[ "$MODE" == "once" ]]; then
    kubectl -n "$NAMESPACE" logs "deploy/$svc" \
      | jq -Rr --arg svc "$svc" "$FORMAT_JQ"
  else
    kubectl -n "$NAMESPACE" logs -f "deploy/$svc" \
      | jq -Rr --arg svc "$svc" "$FORMAT_JQ"
  fi
}

pids=()

for svc in "${SERVICES[@]}"; do
  echo "[INFO] Attaching: deploy/$svc"
  run_one "$svc" &
  pids+=("$!")
done

cleanup() {
  echo ""
  echo "[INFO] Stopping log tails..."
  for pid in "${pids[@]}"; do
    kill "$pid" 2>/dev/null || true
  done
}
trap cleanup INT TERM

wait
