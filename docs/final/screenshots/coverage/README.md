# Discovery domain 覆盖率证据（S3 收口）

> 采集人：青岑　日期：2026-11-17
> 对应角色卡：S34_3「CI 收口」的“覆盖率报告截图”
> 目标模块：`co-discovery-domain`　门槛：`domain.coverage.minimum=0.70`

## 结果

| 指标 | 数值 |
| --- | ---: |
| 行覆盖（LINE COVERED） | 72 |
| 行未覆盖（LINE MISSED） | 20 |
| 行总数 | 92 |
| 行覆盖率 | **78.26%**（72 / 92） |

## 文件

- [`coverage-discovery.png`](coverage-discovery.png)：JaCoCo HTML `target/site/jacoco/index.html` 截图；
- [`coverage-discovery.csv`](coverage-discovery.csv)：同一份报告的 `jacoco.csv`，用于复核行数。

> 截图里 Lines 一列显示的是 `Missed=20` 和 `Total=92`，不是覆盖率百分比；
> 覆盖率按 CSV 的 `LINE_COVERED / (LINE_COVERED + LINE_MISSED) = 72/92 = 78.26%` 计算。

## 复现

在 JDK 21 下运行 discovery domain 测试并生成报告：

```bash
cd backend
mvn -B test -pl co-discovery/co-discovery-domain -am \
  -Djacoco.skip=true \
  -DargLine="-javaagent:$HOME/.m2/repository/org/jacoco/org.jacoco.agent/0.8.12/org.jacoco.agent-0.8.12-runtime.jar=destfile=$PWD/co-discovery/co-discovery-domain/target/jacoco.exec -Djdk.net.URLClassPath.disableClassPathURLCheck=true"

mvn -B org.jacoco:jacoco-maven-plugin:0.8.12:report \
  -pl co-discovery/co-discovery-domain
```

打开：

```text
backend/co-discovery/co-discovery-domain/target/site/jacoco/index.html
```

> Windows 本机如果直接 `mvn verify`，Surefire 的 manifest-JAR/跨盘符问题会让 JaCoCo 跳过执行数据；
> 上面的手动 agent 参数是为了本机取证，CI（Linux）仍用 `mvn verify -Djacoco.skip=false`。
