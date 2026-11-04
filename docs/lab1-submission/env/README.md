# 五人开发环境截图（E3）

> 对应实验：Lab 1 团队组建与协作工具链（Sprint 0）【环境与前置准备】
> 要求：Git 2.40+、JDK 21、Node.js 20+、Docker Desktop 可运行
> 负责人：房国栋（Dev-2 前端与验证）
> 汇总表：[`../env-matrix.md`](../env-matrix.md)

---

## 1. 为什么单独建这个目录

汇总表 `env-matrix.md` 只放**结论数据**，本目录放**原始证据**（终端截图 + 采集输出文本），
老师要抽查某个人时不用翻聊天记录，直接点开 `env/<拼音>.png` 即可。

命名规则（拼音全部小写、不带空格）：

| 文件 | 内容 |
| --- | --- |
| `env-<拼音>.png` | 终端里 `java -version` / `mvn -v` / `node -v` / `docker --version` 同屏 |
| `env-<拼音>.txt` | `scripts/dev-env/collect-env.*` 打印的那一行 Markdown |
| `mvn-test-<拼音>.png` | `mvn clean test` 结尾（`BUILD SUCCESS` 或 `BUILD FAILURE` + 原因） |

---

## 2. 采集方法（每人在自己机器上跑一次）

```powershell
# Windows
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/dev-env/collect-env.ps1 -Name 房国栋 -GitHub 2177023844
```

```bash
# Linux / macOS / Git Bash
bash scripts/dev-env/collect-env.sh fangguodong 2177023844
```

脚本会打印一行可直接粘进汇总表的 Markdown，并把缺失项标成 `not installed`，不会中途退出。

采集完**紧接着在同一个终端窗口**跑测试并截图，一张图同时覆盖「版本」和「构建结果」：

```bash
cd backend
mvn clean test
```

---

## 3. 五人清单与状态

| 成员 | GitHub | env 截图 | env 文本 | mvn 测试截图 | 采集日期 |
| --- | --- | --- | --- | --- | --- |
| 房国栋 | `2177023844` | ✅ [`env-fangguodong.png`](env-fangguodong.png) | ✅ [`env-fangguodong.txt`](env-fangguodong.txt) | ⬜ 见 [`../screenshots/mvn-test-fangguodong.png`](../screenshots/mvn-test-fangguodong.png) | 2026-10-29 |
| 魏志国 | `2123345397-star` | ⬜ 待补 | ⬜ | ⬜ | |
| 陈明哲 | `cmz666-BOT` | ⬜ 待补 | ⬜ | ⬜ | |
| 刘俊琒 | `Liujunfeng520` | ⬜ 待补 | ⬜ | ⬜ | |
| 青岑 | `qing963666` | ⬜ 待补 | ⬜ | ⬜ | |

图例：✅ 已交　⬜ 待补

> 其余四人补齐后，把对应的 `env-<拼音>.png` / `env-<拼音>.txt` 放进本目录，并把上表改成 ✅，
> 同时更新 [`../env-matrix.md`](../env-matrix.md) 第 2 节的汇总行。

---

## 4. 达标线

| 工具 | 要求 | 判定 |
| --- | --- | --- |
| JDK | 21 | `java -version` 显示 `21.x`，且 `javac -version` 同版本 |
| Maven | 3.9+ | `mvn -v` 首行 `Apache Maven 3.9.x` |
| Node.js | 20+ | `node -v` ≥ v20 |
| Git | 2.40+ | `git --version` ≥ 2.40 |
| Docker | Desktop 可运行 | `docker info` 不报错（`docker --version` 只证明装了） |

---

## 5. 已知差异

| 成员 | 差异 | 处置 |
| --- | --- | --- |
| 房国栋 | 命令行 `java` 默认指向 JDK 1.8（系统 PATH 里 Oracle `javapath` 排在前面），但 Maven 实际使用 JDK 21（编译日志 `release 21`） | 截图前执行 `$env:JAVA_HOME="E:\IntelliJ IDEA 2025.3.3\jbr"; $env:Path="$env:JAVA_HOME\bin;"+$env:Path`；长期方案见 `env-matrix.md` §6 |
| 房国栋 | Docker Desktop 未安装 | 记录为 `not installed`，容器化实验（Lab 8）前补装并补第二次截图 |
