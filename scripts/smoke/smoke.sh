#!/usr/bin/env bash
# CampusOverflow 端到端冒烟脚本（T1-15，Linux/macOS/Git Bash 版）
# 一条命令跑通：CSRF → 注册 → 登录 → 发问题 → 另一账号回答 → 投票 → 采纳 → 详情校验。
#
# 依赖：curl；后端已启动（dev 或 demo profile，需 MySQL + Redis）。
# 用法：bash scripts/smoke/smoke.sh [baseUrl]
set -euo pipefail

BASE_URL="${1:-http://localhost:8080}"
BASE_URL="${BASE_URL%/}"
PASSWORD="${PASSWORD:-Passw0rd123}"
SUFFIX="$(date +%m%d%H%M%S)$(printf '%03d' $((RANDOM % 900 + 100)))"
USER_A="smokeA${SUFFIX}"
USER_B="smokeB${SUFFIX}"
NAME_A="冒烟甲${SUFFIX}"
NAME_B="冒烟乙${SUFFIX}"

JAR="$(mktemp)"
trap 'rm -f "$JAR"' EXIT

CSRF_HEADER=""
CSRF_TOKEN=""
START_TS="$(date +%s)"

step() { printf '[%d/9] %s\n' "$1" "$2"; }

fail() {
  printf '\nSMOKE FAIL\n  %s\n' "$1" >&2
  exit 1
}

json_id() { sed -n 's/.*"id"[[:space:]]*:[[:space:]]*\([0-9][0-9]*\).*/\1/p' | head -n 1; }

request() {
  local method="$1" path="$2" data="${3:-}" out payload
  if [ -n "$data" ]; then
    payload="$(mktemp)"
    printf '%s' "$data" > "$payload"
    out="$(curl -sS --fail-with-body -c "$JAR" -b "$JAR" -X "$method" "$BASE_URL$path" \
      -H "Content-Type: application/json; charset=utf-8" \
      -H "${CSRF_HEADER}: ${CSRF_TOKEN}" \
      --data-binary @"$payload")" || { rm -f "$payload"; printf '%s\n' "$out" >&2; return 1; }
    rm -f "$payload"
  else
    out="$(curl -sS --fail-with-body -c "$JAR" -b "$JAR" -X "$method" "$BASE_URL$path" \
      -H "${CSRF_HEADER}: ${CSRF_TOKEN}")" || { printf '%s\n' "$out" >&2; return 1; }
  fi
  printf '%s' "$out"
}

csrf() {
  local body
  body="$(curl -sS --fail-with-body -c "$JAR" -b "$JAR" "$BASE_URL/api/v1/auth/csrf")"
  CSRF_HEADER="$(printf '%s' "$body" | sed -n 's/.*"headerName"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')"
  CSRF_TOKEN="$(awk '$6 == "XSRF-TOKEN" { print $7 }' "$JAR" | tail -n 1)"
  if [ -z "$CSRF_HEADER" ] || [ -z "$CSRF_TOKEN" ]; then
    fail "CSRF 获取失败：$body"
  fi
}

step 1 "获取 CSRF"
csrf

step 2 "注册提问者 ${USER_A}"
REG_A="$(request POST /api/v1/auth/register "{\"username\":\"${USER_A}\",\"displayName\":\"${NAME_A}\",\"email\":\"${USER_A}@example.com\",\"password\":\"${PASSWORD}\",\"college\":\"计算机学院\",\"className\":\"软工2401\"}")"
ID_A="$(printf '%s' "$REG_A" | json_id)"
[ -n "$ID_A" ] || fail "注册提问者失败：$REG_A"

step 3 "登录提问者"
csrf
request POST /api/v1/auth/login "{\"username\":\"${USER_A}\",\"password\":\"${PASSWORD}\"}" >/dev/null

step 4 "发布问题"
csrf
QUESTION="$(request POST /api/v1/questions '{"title":"冒烟测试：红黑树插入后为什么必须旋转？","body":"这是冒烟脚本创建的问题正文，长度需要超过二十个字符才能通过校验。","tags":["冒烟测试","数据结构"]}')"
QUESTION_ID="$(printf '%s' "$QUESTION" | json_id)"
[ -n "$QUESTION_ID" ] || fail "发布问题失败：$QUESTION"

step 5 "注册回答者 ${USER_B}"
csrf
REG_B="$(request POST /api/v1/auth/register "{\"username\":\"${USER_B}\",\"displayName\":\"${NAME_B}\",\"email\":\"${USER_B}@example.com\",\"password\":\"${PASSWORD}\",\"college\":\"计算机学院\",\"className\":\"软工2401\"}")"
ID_B="$(printf '%s' "$REG_B" | json_id)"
[ -n "$ID_B" ] || fail "注册回答者失败：$REG_B"

step 6 "切换登录回答者并提交回答"
csrf
request POST /api/v1/auth/login "{\"username\":\"${USER_B}\",\"password\":\"${PASSWORD}\"}" >/dev/null
csrf
ANSWER="$(request POST "/api/v1/questions/${QUESTION_ID}/answers" '{"body":"这是冒烟脚本提交的回答正文，长度也超过二十个字符，用来验证回答链路。"}')"
ANSWER_ID="$(printf '%s' "$ANSWER" | json_id)"
[ -n "$ANSWER_ID" ] || fail "提交回答失败：$ANSWER"

step 7 "回答者给问题投 +1"
csrf
request PUT /api/v1/votes "{\"targetType\":\"QUESTION\",\"targetId\":${QUESTION_ID},\"value\":1}" >/dev/null

step 8 "切回提问者并采纳回答"
csrf
request POST /api/v1/auth/login "{\"username\":\"${USER_A}\",\"password\":\"${PASSWORD}\"}" >/dev/null
csrf
request PUT "/api/v1/questions/${QUESTION_ID}/accepted-answer" "{\"answerId\":${ANSWER_ID}}" >/dev/null

step 9 "读取详情并校验"
DETAIL="$(request GET "/api/v1/questions/${QUESTION_ID}")"
# 只保留问题级字段，避免 answers[].score 干扰解析
TOP="$(printf '%s' "$DETAIL" | sed 's/"answers".*//')"
printf '%s' "$TOP" | grep -q "\"acceptedAnswerId\"[[:space:]]*:[[:space:]]*${ANSWER_ID}" \
  || fail "采纳未生效：$DETAIL"
SCORE="$(printf '%s' "$TOP" | sed -n 's/.*"score"[[:space:]]*:[[:space:]]*\(-\?[0-9][0-9]*\).*/\1/p')"
[ -n "${SCORE}" ] && [ "${SCORE}" -ge 1 ] || fail "问题分数未因投票增加：${SCORE}"

ELAPSED=$(( $(date +%s) - START_TS ))
printf '\nSMOKE PASS\n'
printf '  用户 A=%s (id=%s)  用户 B=%s (id=%s)\n' "$USER_A" "$ID_A" "$USER_B" "$ID_B"
printf '  问题 id=%s  回答 id=%s  score=%s  acceptedAnswerId=%s\n' \
  "$QUESTION_ID" "$ANSWER_ID" "$SCORE" "$ANSWER_ID"
printf '  耗时 %ss  base=%s\n' "$ELAPSED" "$BASE_URL"
