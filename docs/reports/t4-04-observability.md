# T4-04 · 可观测性验证（四指标 + Grafana）

> 负责人：陈明哲（E）　验证时间：2026-11-16 22:00–22:10（Asia/Shanghai）
> 目标：四个面板都有真实数据，且 `co_reputation_mismatches` 能人为造 1 并归零。

## 一、被测环境

| 项 | 值 |
| --- | --- |
| 应用 | `campus-overflow.jar`，`--spring.profiles.active=demo`，端口 18080 |
| JDK | 21.0.10 |
| MySQL | 8.4（临时实例 3307，库 `campus_overflow`） |
| Redis | 5.0（6379） |
| 监控 | `deploy/prometheus.yml` 抓取 `backend:8080/actuator/prometheus`；Grafana 11.2（compose observability profile） |

Actuator 已暴露 `health,info,metrics,prometheus`；为避免 HTTP 直方图默认不输出，`application.yml` 增加了：

```yaml
management:
  metrics:
    distribution:
      percentiles-histogram:
        http.server.requests: true
```

## 二、四个指标（actuator 实测）

```text
$ curl -s localhost:18080/actuator/prometheus | grep -E '^co_(outbox|reputation)'
co_outbox_backlog{application="campus-overflow"} 0.0
co_outbox_dead_letters{application="campus-overflow"} 0.0
co_reputation_mismatches{application="campus-overflow"} 0.0
```

HTTP P95 所需的直方图桶已出现（`/api/v1/questions` 共 69 条 bucket series）：

```text
http_server_requests_seconds_bucket{...,uri="/api/v1/questions",le="0.001"} 0
http_server_requests_seconds_bucket{...,uri="/api/v1/questions",le="0.001048576"} 0
...
```

面板查询：

```promql
histogram_quantile(0.95,
  sum by (le, uri) (rate(http_server_requests_seconds_bucket{uri=~"/api/v1/.*"}[5m]))
) * 1000
```

## 三、对账指标“造 1 归零”实录

```text
1) 选一个余额=流水之和的账户（user 5，reputation=35，ledger_sum=35）
2) UPDATE rep_account SET reputation = reputation + 1 WHERE user_id = 5;
3) 等待定时对账（60s 周期）→ co_reputation_mismatches = 1   （实测 15s 后可见）
4) UPDATE rep_account SET reputation = reputation - 1 WHERE user_id = 5;
5) 等待下一轮 → co_reputation_mismatches = 0                （实测 60s 后归零）
```

指标实现：`ReputationJobs.reconcile()` 每 60s 调用 `ReconciliationService.reconcile()`，把 `List.size()` 写入 Gauge；原为每日 cron，演示不可见，已改为 `fixedDelay=60000`（可通过 `co.reputation.reconcile-interval-ms` 调整）。

## 四、Grafana 四面板

文件（compose 已挂载）：

- [`deploy/grafana/dashboards/campus-overflow.json`](../../deploy/grafana/dashboards/campus-overflow.json)
- [`deploy/grafana/provisioning/datasources/prometheus.yml`](../../deploy/grafana/provisioning/datasources/prometheus.yml)
- [`deploy/grafana/provisioning/dashboards/campus-overflow.yml`](../../deploy/grafana/provisioning/dashboards/campus-overflow.yml)

面板：

| # | 标题 | 指标 | 阈值 |
| --- | --- | --- | --- |
| 1 | 发件箱积压 | `co_outbox_backlog` | >100 黄、>500 红 |
| 2 | 死信事件 | `co_outbox_dead_letters` | >0 红 |
| 3 | 声誉对账不一致 | `co_reputation_mismatches` | >0 红 |
| 4 | 接口 P95 | `http_server_requests_seconds` 直方图 | >300ms 黄、>1s 红 |

启动：

```bash
docker compose -f deploy/docker-compose.yml --profile observability up -d --build
# Prometheus: http://localhost:9090
# Grafana:    http://localhost:3000  (admin / admin，首次登录会要求改密)
# Dashboard:  CampusOverflow / CampusOverflow · 质量与可观测性
```

## 五、结论与边界

- 三项自定义指标均在 `/actuator/prometheus` 实测有值；对账指标完成 1→0 演练；
- HTTP 直方图桶已开启，P95 查询有数据来源；
- 本机无 Docker，未启动 Grafana 容器做 UI 截图；dashboard JSON 与 provisioning 已入库，**在装有 Docker 的机器上按第四节一条命令即可复现四面板**，S4 彩排时补 Grafana 截图。
