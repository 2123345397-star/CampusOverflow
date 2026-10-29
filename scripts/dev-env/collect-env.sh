#!/usr/bin/env bash
# CampusOverflow · 开发环境版本采集（Lab 1 / 角色卡 E3）
# 用法：bash scripts/dev-env/collect-env.sh 房国栋 2177023844
# 输出：一行可粘贴到 docs/lab1-submission/env-matrix.md 第 2 节表格的 Markdown

set -u

NAME="${1:-（未填姓名）}"
GITHUB="${2:--}"

first_line() {
    # $1 = 命令，其余 = 参数
    local cmd="$1"; shift
    if ! command -v "$cmd" >/dev/null 2>&1; then
        echo "未安装"
        return
    fi
    "$cmd" "$@" 2>&1 | head -n 1 | tr -d '\r'
}

extract() {
    # 从管道文本里抽出版本号：extract '正则'
    sed -nE "s/$1/\1/p" | head -n 1
}

JAVA_RAW=$(first_line java -version)
JAVA_VERSION=$(printf '%s' "$JAVA_RAW" | extract '.*version "([^"]+)".*')
[ -z "$JAVA_VERSION" ] && JAVA_VERSION="${JAVA_RAW:-未安装}"

JAVAC_VERSION=$(first_line javac -version | extract 'javac ([0-9][^ ]*)')
[ -z "$JAVAC_VERSION" ] && JAVAC_VERSION="未安装"

MVN_VERSION=$(first_line mvn -v | extract 'Apache Maven ([0-9][^ ]*)')
[ -z "$MVN_VERSION" ] && MVN_VERSION="未安装"

NODE_VERSION=$(first_line node -v)
NPM_VERSION=$(first_line npm -v)

DOCKER_VERSION=$(first_line docker --version | extract 'Docker version ([^,]+)')
[ -z "$DOCKER_VERSION" ] && DOCKER_VERSION="未安装"

GIT_VERSION=$(first_line git --version | extract 'git version ([^ ]+)')
[ -z "$GIT_VERSION" ] && GIT_VERSION="未安装"

OS_DESC=$(uname -srm 2>/dev/null || echo "unknown")
TODAY=$(date +%F)

echo ""
echo "=== CampusOverflow 开发环境采集 ==="
echo "姓名   : $NAME"
echo "GitHub : $GITHUB"
echo "OS     : $OS_DESC"
echo "JDK    : java $JAVA_VERSION / javac $JAVAC_VERSION"
echo "Maven  : $MVN_VERSION"
echo "Node   : $NODE_VERSION / npm $NPM_VERSION"
echo "Docker : $DOCKER_VERSION"
echo "Git    : $GIT_VERSION"
echo ""
echo "=== 粘贴下面这一行到 env-matrix.md 第 2 节表格 ==="
echo "| $NAME | \`$GITHUB\` | $OS_DESC | $JAVA_VERSION | $MVN_VERSION | $NODE_VERSION | $DOCKER_VERSION | $TODAY | ⬜ |"
echo ""
echo "=== 达标检查（JDK 21 / Maven 3.9+ / Node 20+ / Git 2.40+） ==="

check() {
    # $1 名称, $2 实际值, $3 最低主版本
    local actual="${2:-未安装}"
    local major
    major=$(printf '%s' "$actual" | grep -oE '[0-9]+(\.[0-9]+)?' | head -n 1)
    if [ -z "$major" ]; then
        echo "[FAIL] $1  $actual"
        return
    fi
    if awk -v a="$major" -v b="$3" 'BEGIN { exit !(a >= b) }'; then
        echo "[PASS] $1  $actual"
    else
        echo "[FAIL] $1  $actual （要求 >= $3）"
    fi
}

check "JDK 21"      "$JAVA_VERSION"   21
check "Maven 3.9+"  "$MVN_VERSION"   3.9
check "Node.js 20+" "$NODE_VERSION"  20
check "Git 2.40+"   "$GIT_VERSION"   2.40

echo ""
echo "提示：紧接着执行 cd backend && mvn clean test 并截图，一张图同时覆盖 E3 的版本与测试结果。"
