#!/usr/bin/env bash
# CampusOverflow · 提交作者审计（Lab 1 / 角色卡 E4）
# 用法：bash scripts/dev-env/audit-authors.sh 2177023844
# 输出：所有提交作者的邮箱与提交数，标出可能未与 GitHub 账号绑定的邮箱

set -u

GITHUB="${1:-}"

if ! command -v git >/dev/null 2>&1; then
    echo "[ERROR] 未找到 git，请先安装或加入 PATH"
    exit 1
fi

if ! git rev-parse --show-toplevel >/dev/null 2>&1; then
    echo "[ERROR] 当前目录不是 git 仓库，请先 cd 到 CampusOverflow 根目录"
    exit 1
fi

echo "仓库：$(git rev-parse --show-toplevel)"
echo ""
echo "| 提交数 | 作者名 | 邮箱 | noreply? | 判断 |"
echo "| --- | --- | --- | --- | --- |"

git log --all --format='%ae|%an' \
  | awk -F'|' 'NF>=2 { print $1 "\t" $2 }' \
  | sort | uniq -c | sort -rn \
  | while read -r count email name; do
        case "$email" in
            *.noreply.github.com) noreply="是";   verdict="OK" ;;
            noreply@github.com)    noreply="—";   verdict="Web 端提交（正常）" ;;
            *)                     noreply="否";   verdict="需确认是否已在 GitHub 验证" ;;
        esac
        printf '| %s | %s | %s | %s | %s |\n' "$count" "$name" "$email" "$noreply" "$verdict"
    done

echo ""
echo "=== 本机身份 ==="
echo "user.name  : $(git config --global user.name)"
echo "user.email : $(git config --global user.email)"
echo "最近一次提交作者 : $(git log -1 --format='%an <%ae>')"

if [ -n "$GITHUB" ]; then
    echo ""
    echo "=== 建议填写（noreply 形式，需在 GitHub 账号 Emails 中确认） ==="
    echo "git config --global user.name  \"$(git config --global user.name)\""
    echo "git config --global user.email \"${GITHUB}+${GITHUB}@users.noreply.github.com\""
fi

echo ""
echo "判定标准与整改步骤见 docs/lab1-submission/identity-audit.md"
