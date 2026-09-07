#!/usr/bin/env bash
set -euo pipefail
base="${BASE_URL:-http://localhost:8080}"
json(){ curl -fsS -H 'Content-Type: application/json' "$@"; }
lead=$(json -X POST "$base/api/leads" -d '{"name":"冒烟客户联系人","company":"冒烟科技","phone":"13800000000","source":"WEB"}')
id=$(jq -r '.data.id' <<<"$lead"); test "$id" != null
converted=$(json -X POST "$base/api/leads/$id/convert" -d '{"createOpportunity":true,"opportunityName":"冒烟商机","amount":10000}')
account=$(jq -r '.data.accountId' <<<"$converted"); opp=$(jq -r '.data.opportunityId' <<<"$converted")
test "$account" != null -a "$opp" != null
json -X PUT "$base/api/opportunities/$opp/stage" -d '{"stage":"CLOSED_WON"}' >/dev/null
case_json=$(json -X POST "$base/api/cases" -d '{"subject":"冒烟工单","description":"全链路测试","priority":"HIGH","type":"PROBLEM","origin":"WEB","accountId":'"$account"'}')
case_id=$(jq -r '.data.id' <<<"$case_json"); test "$case_id" != null
json -X POST "$base/api/cases/$case_id/comments" -d '{"author":"冒烟用户","content":"已收到","internal":false}' >/dev/null
json -X PUT "$base/api/cases/$case_id/status" -d '{"status":"IN_PROGRESS"}' >/dev/null
json -X PUT "$base/api/cases/$case_id/status" -d '{"status":"RESOLVED","solution":"已处理"}' >/dev/null
json -X PUT "$base/api/cases/$case_id/status" -d '{"status":"CLOSED"}' >/dev/null
summary=$(json "$base/api/dashboard/summary")
jq -e '.data.newLeadCount != null and .data.pipeline != null and .data.openCaseCount != null' <<<"$summary" >/dev/null
echo "冒烟测试通过：线索转化、商机推进、工单评论与关闭、仪表盘汇总"
