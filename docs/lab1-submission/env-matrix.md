# Lab 1 · 开发环境版本汇总（角色卡 E3）

> 团队：TeamX　项目：CampusOverflow　对应实验：Lab 1 团队组建与协作工具链（Sprint 0）
> 对应提交物：Lab 1【环境与前置准备】——Git 2.40+、JDK 21、Node.js 20+、Docker Desktop
> 负责人：**房国栋（Dev-2 前端与验证）**　验收标准：**5 份齐，且每份含 `mvn clean test` 结果，不能只截版本号**
> 采集口径：同一仓库版本（`main`）、同一命令、同一周内完成；截图必须带终端提示符与执行路径

---

## 1. 采集方法（不要手工复制版本号）

仓库内提供两条等价脚本，运行后直接打印一行 Markdown，粘贴进第 2 节表格即可：

```powershell
# Windows (PowerShell)
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/dev-env/collect-env.ps1 -Name 房国栋 -GitHub 2177023844
```

```bash
# Linux / macOS / Git Bash
bash scripts/dev-env/collect-env.sh 房国栋 2177023844
```

脚本会依次探测 `java`、`mvn`、`node`、`docker`、`git`，缺失的项输出 `not installed`，不会中途退出。

> ⚠️ 踩坑记录：`.ps1` 脚本刻意写成**纯 ASCII**。Windows PowerShell 5.1 在没有 BOM 的情况下按 ANSI（GBK）读取 `.ps1`，脚本里一旦出现中文就会解析失败（`意外的标记`）。
> 如果你要改成中文提示，必须把文件另存为 **UTF-8 with BOM**；用 `pwsh` 7 则不受影响。

采集完成后同时截图（同一终端窗口，先跑脚本再跑测试，一张图两件事）：

```bash
cd backend
mvn clean test          # 结果决定第 3 节怎么填，红也要截，见 3.2
```

---

## 2. 环境版本汇总表

| 成员 | GitHub | OS | JDK | Maven | Node.js | Docker | 采集日期 | 截图 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 房国栋 | `2177023844` | Windows 11（`10.0.26200`） | 21（JetBrains Runtime，`E:\IntelliJ IDEA 2025.3.3\jbr`） | 3.9.11 | v24.18.1 / npm 11.16.0 | ⬜ 未安装 | 2026-10-29 | ⬜ |
| 魏志国 | `2123345397-star` | ⬜ | ⬜ | ⬜ | ⬜ | ⬜ | ⬜ | ⬜ |
| 陈明哲 | `cmz666-BOT` | ⬜ | ⬜ | ⬜ | ⬜ | ⬜ | ⬜ | ⬜ |
| 刘俊琒 | `Liujunfeng520` | ⬜ | ⬜ | ⬜ | ⬜ | ⬜ | ⬜ | ⬜ |
| 青岑 | `qing963666` | ⬜ | ⬜ | ⬜ | ⬜ | ⬜ | ⬜ | ⬜ |

图例：✅ 达标　⚠️ 版本不符但已记录　⬜ 待采集

**达标线（Lab 1 明确要求）**

| 工具 | 要求 | 判定方式 |
| --- | --- | --- |
| Git | 2.40+ | `git --version` 主版本号 ≥ 2.40 |
| JDK | 21 | `java -version` 显示 `21.x`；`javac -version` 与 `java` 必须同版本 |
| Maven | 3.9+ | `mvn -v` 首行 `Apache Maven 3.9.x` |
| Node.js | 20+ | `node -v` ≥ v20 |
| Docker | Desktop 可运行 | `docker version` 能连上 daemon（`docker info` 不报错） |

> JDK 必须 21：后端 `pom.xml` 以 Java 21 编译，用 17 或 8 会在 `mvn clean install` 阶段报 `invalid target release: 21`。

---

## 3. `mvn clean test` 取证（E3 的关键一半）

### 3.1 当前团队状态（2026-10-28 复查）

后端按 Lab 4 的 TDD 策略**先落了测试、领域实现留桩**（`co-qa-domain` 中的 `TODO(S2)`，负责人青岑）。因此 `backend/` 整体 `mvn clean test` 现阶段**预期为红**。这不是环境问题，交付时必须把"红的原因"和"绿的部分"一起给出，否则老师会误判为环境没配好。

**实测数据（2026-10-29，房国栋，JDK 21 / Maven 3.9.11）**：

| 项目 | 结果 |
| --- | --- |
| 构建结果 | `BUILD FAILURE`，停在 `co-qa-domain` 的 surefire |
| 测试统计 | `Tests run: 18, Failures: 2, Errors: 13`（3 通过） |
| 失败根因 | `java.lang.UnsupportedOperationException: TODO(S2)：MarkdownBody.ofQuestion 尚未实现` |
| 失败用例分布 | `QuestionTest$Posting` 1、`$Accepting` 5、`$EditingAndDeleting` 3、`AnswerTest` 3、`VoteAndCommentTest` 3 |
| 已通过模块 | `co-shared-kernel` 2 个、`co-identity-domain` 9 个（含 `CourseTest` 3 个） |
| 原始日志 | [`logs/mvn-test-fangguodong.log`](logs/mvn-test-fangguodong.log)（79 KB） |

### 3.2 交付格式：红 + 绿 两张证据

**(a) 整体结果（红，说明原因）**

```bash
cd backend
mvn clean test -DfailIfNoTests=false 2>&1 | tee ../docs/lab1-submission/logs/mvn-test-$(whoami).log
```

截图中要能看到：`BUILD FAILURE`、失败的测试类，以及失败原因是 `UnsupportedOperationException` 或 `TODO(S2)` 桩未实现——而不是 `Could not resolve dependencies` 或 `No compiler is provided`（那才是环境问题）。

**(b) 已实现模块（绿，证明环境可用）**

```bash
cd backend
mvn -pl co-shared-kernel -am install -DskipTests     # 先装依赖
mvn -pl co-shared-kernel test                        # 单独跑，应全绿
```

> 踩坑记录：`mvn -pl co-qa test` 只会构建聚合 POM、不会进叶子模块；正确做法是先 `-pl <叶子模块> -am install -DskipTests`，再进该模块 `mvn test`。

### 3.3 结果登记表

| 成员 | 整体 `mvn clean test` | 失败模块 / 原因 | 已实现模块 `mvn test` | 日志 / 截图 |
| --- | --- | --- | --- | --- |
| 房国栋 | ❌ 红（预期） | `co-qa-domain`：`TODO(S2)：MarkdownBody.ofQuestion 尚未实现`；18 个用例中 2 失败 13 错误 | ✅ `co-shared-kernel` 2、`co-identity-domain` 9 全绿 | [`logs/mvn-test-fangguodong.log`](logs/mvn-test-fangguodong.log) |
| 魏志国 | ⬜ | ⬜ | ⬜ | ⬜ |
| 陈明哲 | ⬜ | ⬜ | ⬜ | ⬜ |
| 刘俊琒 | ⬜ | ⬜ | ⬜ | ⬜ |
| 青岑 | ⬜ | ⬜ | ⬜ | ⬜ |

---

## 4. 截图命名与存放

统一放在 `docs/lab1-submission/screenshots/`，命名规则：

| 文件 | 内容 | 要求 |
| --- | --- | --- |
| `env-<拼音>.png` | 版本采集脚本输出 | 终端提示符、`$` 命令与输出同屏 |
| `mvn-test-<拼音>.png` | `mvn clean test` 结果 | 含 `BUILD FAILURE` / `BUILD SUCCESS` 行 |
| `mvn-test-green-<拼音>.png` | 已实现模块 `mvn test` 全绿 | 含 `Tests run: N, Failures: 0` |

示例：`env-fangguodong.png`、`mvn-test-fangguodong.png`。

---

## 5. E3 关闭条件（逐条打勾才算交付）

- [ ] 5 份 `env-<拼音>.png` 齐备，且 JDK 均显示 21
- [ ] 5 份 `mvn-test-<拼音>.png` 齐备（红也要交，需注明原因）
- [ ] 至少 1 份"已实现模块全绿"证据，证明环境可用
- [ ] 本页第 2、3 节表格无空白 ⬜
- [ ] 版本差异（如有）已写入下方"已知差异"并说明处置

## 6. 已知差异与处置

| 成员 | 差异 | 处置 | 状态 |
| --- | --- | --- | --- |
| 房国栋 | `git` 未加入系统 PATH（使用 `D:\Program Files\Git\cmd\git.exe`） | 已加入 PATH；本文档命令仍保留绝对路径兜底 | ⚠️ 已记录 |
| 房国栋 | 命令行 `java` 默认指向 JDK **1.8.0_481**（系统 PATH 里 Oracle `javapath` 排在前面），但 IDEA/JBR 的 21 可用；Maven 实际已用 21（编译日志 `release 21` 且无 `invalid target release`） | 截图前执行 `$env:JAVA_HOME="E:\IntelliJ IDEA 2025.3.3\jbr"; $env:Path="$env:JAVA_HOME\bin;"+$env:Path`；长期方案见文末"PATH 修正" | ✅ 已解决（临时） |
| 房国栋 | Docker Desktop 未安装 | E3 记录为"未安装"；容器化在 Lab 8 前安装，届时补第二次截图 | ⬜ 待补 |

### PATH 修正（长期，一次到位）

1. `Win + R` → `sysdm.cpl` → 高级 → 环境变量；
2. 系统变量 `Path` 中把 `E:\IntelliJ IDEA 2025.3.3\jbr\bin` 移到最前，或删除 `Oracle\Java\javapath`、`java8path` 两条；
3. 新建系统变量 `JAVA_HOME` = `E:\IntelliJ IDEA 2025.3.3\jbr`；
4. 重开终端，`java -version` 应为 `21.x`。

---

## 7. 与其他提交物的关系

- 与角色卡 **E4 身份绑定核查** 配套：[`identity-audit.md`](identity-audit.md)（版本对了、人也要对得上）。
- 与 Lab 1 总索引：[`README.md`](README.md)。
