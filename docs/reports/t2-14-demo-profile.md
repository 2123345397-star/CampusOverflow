# T2-14 · demo profile 启动与演示数据验证

> 负责人：陈明哲（E）　验证时间：2026-11-15 16:10 / 16:43（Asia/Shanghai）
> 对应验收：`DemoDataSeeder` 能在 `demo` profile 下启动并加载演示数据
> 原始日志：[`t2-14-demo-startup.log`](t2-14-demo-startup.log)

## 一、环境与命令

| 项 | 值 |
| --- | --- |
| OS | Windows 11（`10.0.26200`） |
| JDK | 21.0.10（`C:\Users\HUAWEI\.jdks\ms-21.0.10`） |
| 构建 | Maven 3.9.9 |
| 数据库 | MySQL 8.4（临时实例，端口 3307，`campus_overflow`） |
| 缓存/会话 | Redis 5.0（端口 6379） |
| 启动 | `java -jar backend/co-bootstrap/target/campus-overflow.jar --spring.profiles.active=demo`（`SERVER_PORT=18080`） |
| 等价命令 | `mvn -pl co-bootstrap spring-boot:run -Dspring-boot.run.profiles=demo` |

> 本机没有 Docker，MySQL/Redis 用本地临时实例代替 `docker compose`；不影响 demo profile 的启动与 seeder 逻辑。

## 二、启动日志（首次，含 seeder 初始化）

```text
Started CampusOverflowApplication in 9.592 seconds (process running for 10.061)
DemoDataSeeder : 演示数据初始化完成：管理员 admin01 / 教师 T2001 / 助教 TA3001 / 学生 2023001-2023003，密码 Campus2026
```

## 三、演示数据落库核对

启动后直接查库（`campus_overflow`）：

| 表 | 结果 |
| --- | --- |
| `id_user` | `admin01`(ADMIN)、`T2001`(TEACHER)、`TA3001`(TA)、`2023001`/`2023002`/`2023003`(STUDENT)，均 `verified=1`，`created_at=2026-11-15 08:10:02 UTC` |
| `id_course` | 1 门：`CS2001 数据结构` |
| `qa_question` | 演示问题 3 条（id 1–3，作者分别为 2023001/2023003/2023002） |
| `rep_bounty` | 1 条（演示悬赏） |

## 四、重启幂等

再次以 `demo` 启动：

```text
Started CampusOverflowApplication in 24.071 seconds (process running for 24.47)
DemoDataSeeder : 演示数据已存在，跳过初始化
```

`DemoDataSeeder` 以「演示数据已存在」短路，重复启动不会重复插入。

## 五、说明与边界

- 本次验证的是「能启动 + 有数据」，不覆盖前端页面操作；端到端主链路见 [`scripts/smoke/README.md`](../../scripts/smoke/README.md)（T1-15 冒烟脚本）。
- 无 Redis 时应用仍可启动、seeder 仍可写入，但登录会因 `LoginThrottle`/Session 连接 Redis 失败而 500；因此 demo 验证与冒烟均要求 Redis 可用。
- 日志中 `OutboxRelay` 在无 Redis 时会出现投递告警，属于环境缺失而非启动异常。
