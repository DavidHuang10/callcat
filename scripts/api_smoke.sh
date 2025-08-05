#!/usr/bin/env bash
set -euo pipefail

# CallCat API comprehensive smoke + logic tests
# Prints each curl command, status codes, and pretty JSON bodies
# Requires: curl, jq

BASE_URL=${BASE_URL:-http://localhost:8080}
EMAIL=${EMAIL:-david.huang@duke.edu}
PASSWORD=${PASSWORD:-NewPassword123}
NEW_PASSWORD=${NEW_PASSWORD:-EvenBetterPass456}

if ! command -v jq >/dev/null 2>&1; then
  echo "This script requires 'jq'. Install it (e.g., brew install jq)" >&2
  exit 1
fi

cyan='\033[36m'; green='\033[32m'; yellow='\033[33m'; red='\033[31m'; reset='\033[0m'

LAST_STATUS=""; LAST_BODY=""

request() {
  local method=$1 url=$2 data=${3:-} token=${4:-}
  local tmp
  tmp=$(mktemp)
  local cmd=(curl -sS -X "$method" "$url" -H "Content-Type: application/json")
  [[ -n "$token" ]] && cmd+=(-H "Authorization: Bearer $token")
  [[ -n "$data" ]] && cmd+=(-d "$data")

  echo -e "${yellow}>>> ${cmd[*]}${reset}"
  local code
  code=$("${cmd[@]}" -w "%{http_code}" -o "$tmp") || true
  LAST_STATUS="$code"
  LAST_BODY=$(cat "$tmp"); rm -f "$tmp"
  echo -e "Status: $code"
  if echo "$LAST_BODY" | jq -e . >/dev/null 2>&1; then
    echo "$LAST_BODY" | jq -C .
  else
    echo "$LAST_BODY"
  fi
}

assert_status() {
  local expected=$1
  if [[ "$LAST_STATUS" != "$expected" ]]; then
    echo -e "${red}ASSERT FAIL: expected HTTP $expected, got $LAST_STATUS${reset}" >&2
    exit 1
  fi
  echo -e "${green}ASSERT OK: HTTP $expected${reset}"
}

assert_status_one_of() {
  local opts=($@)
  for o in "${opts[@]}"; do [[ "$LAST_STATUS" == "$o" ]] && { echo -e "${green}ASSERT OK: HTTP $o${reset}"; return; }; done
  echo -e "${red}ASSERT FAIL: expected one of: ${opts[*]}, got $LAST_STATUS${reset}" >&2
  exit 1
}

assert_jq_eq() {
  local jq_path=$1 expected=$2
  local val
  val=$(echo "$LAST_BODY" | jq -r "$jq_path" 2>/dev/null || echo "__jq_error__")
  if [[ "$val" == "$expected" ]]; then
    echo -e "${green}ASSERT OK: $jq_path == $expected${reset}"
  else
    echo -e "${red}ASSERT FAIL: $jq_path expected '$expected', got '$val'${reset}" >&2
    exit 1
  fi
}

section() { echo -e "\n${cyan}== $* ==${reset}"; }

section "1) Send verification code"
request POST "$BASE_URL/api/auth/send-verification" "{\"email\":\"$EMAIL\"}"
assert_status 200
assert_jq_eq .success true

read -r -p $'Enter the 6-digit verification code printed in your Spring Boot logs: ' CODE

section "2) Verify email with WRONG code"
request POST "$BASE_URL/api/auth/verify-email" "{\"email\":\"$EMAIL\",\"code\":\"000000\"}"
assert_status 400

section "3) Verify email with CORRECT code"
request POST "$BASE_URL/api/auth/verify-email" "{\"email\":\"$EMAIL\",\"code\":\"$CODE\"}"
assert_status 200
assert_jq_eq .success true

section "4) Register user"
request POST "$BASE_URL/api/auth/register" "{\"email\":\"$EMAIL\",\"password\":\"$PASSWORD\",\"firstName\":\"David\",\"lastName\":\"Huang\"}"
assert_status 200
TOKEN_REG=$(echo "$LAST_BODY" | jq -r .token)
assert_jq_eq .email "$EMAIL"

section "5) Duplicate register should fail"
request POST "$BASE_URL/api/auth/register" "{\"email\":\"$EMAIL\",\"password\":\"$PASSWORD\",\"firstName\":\"David\",\"lastName\":\"Huang\"}"
assert_status 400

section "6) Login and capture JWT"
request POST "$BASE_URL/api/auth/login" "{\"email\":\"$EMAIL\",\"password\":\"$PASSWORD\"}"
assert_status 200
TOKEN=$(echo "$LAST_BODY" | jq -r .token)
if [[ -z "${TOKEN:-}" || "$TOKEN" == "null" ]]; then echo "Token missing after login" >&2; exit 1; fi

section "7) Access profile WITHOUT token (expect 401/403)"
request GET "$BASE_URL/api/user/profile"
assert_status_one_of 401 403

section "8) Get profile WITH token"
request GET "$BASE_URL/api/user/profile" "" "$TOKEN"
assert_status 200
assert_jq_eq .email "$EMAIL"

section "9) Update profile (first/last name only)"
request PUT "$BASE_URL/api/user/profile" '{"firstName":"David","lastName":"Huang"}' "$TOKEN"
assert_status 200
assert_jq_eq .fullName "David Huang"

section "10) Get preferences (should exist; defaults may be UTC/true)"
request GET "$BASE_URL/api/user/preferences" "" "$TOKEN"
assert_status 200

section "11) Update preferences"
request PUT "$BASE_URL/api/user/preferences" '{"timezone":"America/New_York","emailNotifications":true,"voiceId":"voice_123","systemPrompt":"You are a helpful AI assistant for CallCat."}' "$TOKEN"
assert_status 200
assert_jq_eq .timezone "America/New_York"

section "12) Change password with WRONG current (expect 400)"
request POST "$BASE_URL/api/user/change-password" "{\"currentPassword\":\"WrongPass123\",\"newPassword\":\"$NEW_PASSWORD\"}" "$TOKEN"
assert_status 400

section "13) Change password with CORRECT current"
request POST "$BASE_URL/api/user/change-password" "{\"currentPassword\":\"$PASSWORD\",\"newPassword\":\"$NEW_PASSWORD\"}" "$TOKEN"
assert_status 200

section "14) Login with NEW password"
request POST "$BASE_URL/api/auth/login" "{\"email\":\"$EMAIL\",\"password\":\"$NEW_PASSWORD\"}"
assert_status 200
NEW_TOKEN=$(echo "$LAST_BODY" | jq -r .token)
if [[ -z "${NEW_TOKEN:-}" || "$NEW_TOKEN" == "null" ]]; then echo "New token missing after login" >&2; exit 1; fi

section "15) Logout (blacklist token)"
request POST "$BASE_URL/api/auth/logout" "{\"token\":\"$NEW_TOKEN\"}"
assert_status 200

section "16) Access profile with BLACKLISTED token (expect 401/403)"
request GET "$BASE_URL/api/user/profile" "" "$NEW_TOKEN"
assert_status_one_of 401 403

echo -e "\n${green}All tests completed.${reset}"


