# CampusOverflow · 校园问答与声誉平台

TeamX（cs2412-team04）《软件工程》课程项目。基于 Spring Boot + React 的校园问答社区：
提问、回答、采纳、投票、评论、悬赏、声誉与徽章、中文搜索与相关问题、通知、管理员认证与账号管理。

## 1. 技术栈

| 层 | 技术 |
| --- | --- |
| 后端 | Java 21、Spring Boot 3、Spring Security、Spring Data JPA、Flyway、Spring Session |
| 前端 | React 18、TypeScript、Vite、React Router |
| 数据 | MySQL 8.0.39（ngram 中文全文索引）、Redis 7（会话、限流、视图计数） |
| 构建 | Maven 多模块、JUnit 5、AssertJ、JaCoCo、ArchUnit、Testcontainers |
| 交付 | Docker Compose、Nginx、Prometheus、Grafana、GitHub Actions |

后端是模块化单体，按限界上下文分为 `co-identity`、`co-qa`、`co-reputation`、`co-discovery`，
每个上下文内部再分 domain / application / infrastructure / interfaces / api。领域层不依赖框架。

## 2. 五分钟跑起来（本地开发）

### 2.1 前置条件

- JDK 21
- Maven 3.9+
- Node.js 20+ / npm
- Docker Desktop（MySQL、Redis、集成测试都需要）

### 2.2 起中间件

在仓库根目录：

```bash
docker compose -f deploy/docker-compose.yml up -d mysql redis
```

默认端口：MySQL `3306`、Redis `6379`。如果本机已有 MySQL 占用 3306，先停掉本机服务，或修改 compose 的端口映射。

### 2.3 启动后端（demo profile）

```bash
cd backend
mvn -pl co-bootstrap -am -DskipTests package
java -jar co-bootstrap/target/campus-overflow.jar --spring.profiles.active=demo
```

也可以在 IntelliJ 里直接运行 `CampusOverflowApplication`，或用
`mvn -pl co-bootstrap spring-boot:run -Dspring-boot.run.profiles=demo`
（后者要求依赖已经在本地 Maven 仓库中）。

- 后端：<http://localhost:8080>
- Swagger：<http://localhost:8080/swagger-ui.html>
- 健康检查：<http://localhost:8080/actuator/health>

`demo` profile 会由 `DemoDataSeeder` 写入演示账号、课程、问题和悬赏；重复启动会跳过已存在的数据。
如果只想启动空库，把 profile 换成 `dev`。

### 2.4 启动前端

```bash
cd frontend
npm install
npm run dev
```

打开终端打印的地址，通常是 <http://localhost:5173/>。

如果 `localhost:5173` 被解析到 IPv6 而连不上，改用 <http://127.0.0.1:5173/>；Vite 已把 `/api` 代理到 `http://localhost:8080`。

## 3. 演示账号

`demo` profile 下密码统一为 `Campus2026`：

| 账号 | 角色 | 昵称 |
| --- | --- | --- |
| `admin01` | 管理员 ADMIN | 系统管理员 |
| `T2001` | 教师 TEACHER | 王老师 |
| `TA3001` | 助教 TA | 李助教 |
| `2023001` | 学生 STUDENT | 小明 |
| `2023002` | 学生 STUDENT | 小红 |
| `2023003` | 学生 STUDENT | 小刚 |

管理员登录后，右上角导航会出现「管理」，进入：

```text
http://localhost:5173/admin
```

可查看待认证学生、完成认证、创建教职工账号、调整角色。非管理员看不到入口，直接访问 URL 会显示“无权限访问”。

### 3.1 自定义账号提权（不用 demo 时）

学生可以在登录页「注册」自助注册；教师/管理员没有自助入口。先用学生流程注册 `T2001` / `admin01`，
再进 MySQL 提升角色并标记为已认证：

```bash
docker exec -it co-mysql mysql -ucampus -pcampus_dev_pwd campus_overflow
```

```sql
UPDATE id_user SET role = 'TEACHER', verified = TRUE WHERE username = 'T2001';
UPDATE id_user SET role = 'ADMIN',   verified = TRUE WHERE username = 'admin01';
```

改完退出并重新登录，因为会话里缓存了 Actor。

## 4. 测试与质量门禁

### 4.1 后端

```bash
cd backend
mvn test                 # 单元测试
mvn verify               # 单元测试 + 架构检查 + 覆盖率门禁 + 集成测试（需要 Docker）
```

- 领域层覆盖率门槛：`backend/pom.xml` 中 `domain.coverage.minimum=0.70`；
- 架构约束：`mvn test -pl co-bootstrap -Dtest=ArchitectureTest`；
- 集成测试用 Testcontainers 启动 MySQL 8.0.39 与 Redis 7-alpine，必须保证 Docker 可用。

### 4.2 前端

```bash
cd frontend
npm run typecheck
npm run build
```

### 4.3 CI

GitHub Actions 的 `CI` 流水线包含后端编译、Sprint 范围单元测试、ArchUnit、覆盖率门禁、
集成测试（容错阶段）和前端类型检查/构建。发布到 `main` 的 PR 还会构建前后端镜像。

## 5. Docker 一键部署

完整环境（MySQL + Redis + 后端 + 前端 Nginx）：

```bash
docker compose -f deploy/docker-compose.yml up -d --build
```

访问：

| 服务 | 地址 |
| --- | --- |
| 前端 | <http://localhost/> |
| 后端 API | <http://localhost:8080/> |
| Swagger | <http://localhost:8080/swagger-ui.html> |
| 健康检查 | <http://localhost:8080/actuator/health> |

加上监控（Prometheus + Grafana）：

```bash
docker compose -f deploy/docker-compose.yml --profile observability up -d --build
```

| 服务 | 地址 | 默认凭据 |
| --- | --- | --- |
| Prometheus | <http://localhost:9090/> | — |
| Grafana | <http://localhost:3000/> | `admin` / `admin`（可用 `GRAFANA_PASSWORD` 覆盖） |

常用环境变量：

```text
DB_PASSWORD         MySQL campus 用户密码
MYSQL_ROOT_PASSWORD MySQL root 密码
SPRING_PROFILES    后端 profile，默认 prod,demo
CO_FIELD_KEY       字段加密密钥，生产必须替换
COOKIE_SECURE      HTTPS 部署时设为 true
GRAFANA_PASSWORD   Grafana 管理员密码
```

停止：

```bash
docker compose -f deploy/docker-compose.yml down
# 连数据卷一起清掉：docker compose -f deploy/docker-compose.yml down -v
```

> `SPRING_PROFILES` 默认包含 `demo`，会写入演示数据；正式部署时去掉 `demo`。

## 6. 项目结构

```text
backend/
  co-shared-kernel/        共享内核（Actor、异常、事件、分页）
  co-identity/             身份、课程、角色、管理员
  co-qa/                   问题、回答、投票、评论、标签
  co-reputation/           声誉账本、徽章、排行榜、悬赏
  co-discovery/            搜索投影、全文检索、相关问题、通知
  co-bootstrap/            启动模块、安全配置、demo 数据
frontend/                  React + Vite 前端
deploy/                    Dockerfile、Compose、Nginx、Prometheus、Grafana
docs/
  adr/                     架构决策记录
  ddd/                     上下文映射、事件风暴、统一语言
  scrum/                   Sprint 计划、评审、回顾、燃尽图
  final/                   最终答辩材料与用户手册
  qa/                      测试计划与缺陷台账
```

## 7. 文档入口

- 用户手册：[`docs/final/user-guide.md`](docs/final/user-guide.md)
- 最终答辩材料：[`docs/final/README.md`](docs/final/README.md)
- 架构决策：[`docs/adr/README.md`](docs/adr/README.md)
- DDD 设计：[`docs/ddd/`](docs/ddd/)
- Scrum 记录：[`docs/scrum/`](docs/scrum/)
- 测试计划与缺陷：[`docs/qa/`](docs/qa/)
- 发布记录：[`docs/release/`](docs/release/)、[`docs/reports/`](docs/reports/)

## 8. 常见问题

| 现象 | 处理 |
| --- | --- |
| `localhost:5173` 拒绝连接 | 确认 `npm run dev` 的终端还开着；改用终端打印的地址，或 `http://127.0.0.1:5173/` |
| 登录 403 / CSRF 校验失败 | 刷新页面重新取 `XSRF-TOKEN`；不要手工构造 Cookie |
| 搜索单字返回 422 `DIS-1001` | 中文 ngram 最小长度为 2，改用两个及以上字符 |
| 中文搜索无结果 | 必须使用 compose 里的 MySQL（`ngram_token_size=2`），并等待 outbox 投影完成 |
| 声誉或通知没变化 | 走事务性发件箱异步投递，检查 `outbox_event.last_error` |
| `mvn verify` 失败 | 确认 Docker 可用；Testcontainers 需要拉取 MySQL/Redis 镜像 |
| demo 启动报 `UnsupportedOperationException` | 使用最新 `develop`；S3 收口后业务代码已实现 |
| 管理员入口看不到 | 当前账号必须是 `ADMIN`；学生账号不会显示「管理」，直接访问 `/admin` 会显示无权限 |
