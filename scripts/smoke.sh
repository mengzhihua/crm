#!/usr/bin/env bash
set -euo pipefail
base="${BASE_URL:-http://localhost:8080}"
json(){ curl -fsS -H 'Content-Type: application/json' "$@"; }
login(){
  json -X POST "$base/api/auth/login" -d "$1"
}
admin_token=$(login '{"username":"admin","password":"admin123"}' | jq -r '.data.token')
manager_token=$(login '{"username":"manager","password":"123456"}' | jq -r '.data.token')
test "$admin_token" != null -a "$manager_token" != null
auth_json(){ curl -fsS -H 'Content-Type: application/json' -H "Authorization: Bearer $admin_token" "$@"; }
manager_json(){ curl -fsS -H 'Content-Type: application/json' -H "Authorization: Bearer $manager_token" "$@"; }
product_code="SMOKE-$(date +%s)"
product=$(auth_json -X POST "$base/api/products" -d '{"code":"'"$product_code"'","name":"冒烟产品","unit":"套","listPrice":10000,"active":true}')
product_id=$(jq -r '.data.id' <<<"$product")
pricebook_id=$(auth_json "$base/api/pricebooks?page=1&size=100" | jq -r '.data.records[] | select(.standard == true) | .id' | head -n 1)
entry=$(auth_json -X POST "$base/api/pricebooks/$pricebook_id/entries" -d '{"productId":'"$product_id"',"unitPrice":10000,"active":true}')
test "$product_id" != null -a "$pricebook_id" != null
lead=$(auth_json -X POST "$base/api/leads" -d '{"name":"冒烟客户联系人","company":"冒烟科技'"$(date +%s)"'","phone":"13800000000","source":"WEB"}')
id=$(jq -r '.data.id' <<<"$lead"); test "$id" != null
converted=$(auth_json -X POST "$base/api/leads/$id/convert" -d '{"createOpportunity":true,"opportunityName":"冒烟商机","amount":10000}')
account=$(jq -r '.data.accountId' <<<"$converted"); opp=$(jq -r '.data.opportunityId' <<<"$converted")
test "$account" != null -a "$opp" != null
items=$(auth_json -X PUT "$base/api/opportunities/$opp/items" -d '{"items":[{"productId":'"$product_id"',"priceBookEntryId":'"$(jq -r '.data.id' <<<"$entry")"',"quantity":10,"unitPrice":10000,"discountRate":0}]}')
jq -e '.data[0].totalPrice == 100000' <<<"$items" >/dev/null
opportunity=$(auth_json "$base/api/opportunities/$opp")
jq -e '.data.amount == 100000' <<<"$opportunity" >/dev/null
quote=$(auth_json -X POST "$base/api/quotes/from-opportunity/$opp")
quote_id=$(jq -r '.data.id' <<<"$quote")
auth_json -X PUT "$base/api/quotes/$quote_id/discount" -d '{"discountRate":25}' >/dev/null
submitted=$(auth_json -X POST "$base/api/quotes/$quote_id/submit")
jq -e '.data.status == "IN_REVIEW"' <<<"$submitted" >/dev/null
approval_id=$(manager_json "$base/api/approvals?mine=true&page=1&size=100" | jq -r '.data.records[] | select(.targetId == '"$quote_id"') | .id' | head -n 1)
manager_json -X PUT "$base/api/approvals/$approval_id/approve" -d '{"comment":"审批通过"}' >/dev/null
accepted=$(auth_json -X POST "$base/api/quotes/$quote_id/accept")
total=$(jq -r '.data.totalAmount' <<<"$accepted")
opportunity=$(auth_json "$base/api/opportunities/$opp")
jq -e --argjson total "$total" '.data.amount == $total' <<<"$opportunity" >/dev/null
auth_json -X PUT "$base/api/opportunities/$opp/stage" -d '{"stage":"CLOSED_WON"}' >/dev/null
case_json=$(auth_json -X POST "$base/api/cases" -d '{"subject":"冒烟工单","description":"全链路测试","priority":"HIGH","type":"PROBLEM","origin":"WEB","accountId":'"$account"'}')
case_id=$(jq -r '.data.id' <<<"$case_json"); test "$case_id" != null
auth_json -X POST "$base/api/cases/$case_id/comments" -d '{"author":"冒烟用户","content":"已收到","internal":false}' >/dev/null
auth_json -X PUT "$base/api/cases/$case_id/status" -d '{"status":"IN_PROGRESS"}' >/dev/null
auth_json -X PUT "$base/api/cases/$case_id/status" -d '{"status":"RESOLVED","solution":"已处理"}' >/dev/null
auth_json -X PUT "$base/api/cases/$case_id/status" -d '{"status":"CLOSED"}' >/dev/null
summary=$(auth_json "$base/api/dashboard/summary")
jq -e '.data.newLeadCount != null and .data.pipeline != null and .data.openCaseCount != null' <<<"$summary" >/dev/null
echo "冒烟测试通过：登录、产品价格手册、商机行项目、报价审批、工单与仪表盘"
