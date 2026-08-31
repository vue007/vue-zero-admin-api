# 指标、日志与链路追踪

本系统基于 Spring Boot Actuator、Micrometer Prometheus 和 OpenTelemetry 提供统一可观测能力。应用默认采集指标和链路上下文，但默认关闭 OTLP 网络导出（包括 OTLP 指标和链路）；未部署 Collector 时不会产生连接失败噪声。

## 能力清单

- 指标：`/actuator/prometheus` 提供 JVM、Tomcat、线程池、数据库连接池和 HTTP 请求指标。
- 业务指标：登录次数 `zero_auth_login_attempts_total` 与耗时 `zero_auth_login_duration_seconds`，仅使用 `grant_type`、`outcome` 等低基数标签，不包含租户号、用户 ID 或用户名。
- 日志：控制台及文件日志均包含 `traceId`、`spanId`、`requestId`。
- 请求关联：支持透传合法的 `X-Request-Id`；缺失或格式不安全时自动生成，并通过响应头返回。
- 链路追踪：使用 Micrometer Observation 采集，按需通过 OTLP/HTTP 发往 OpenTelemetry Collector、Tempo、Jaeger 等后端。

Actuator 只暴露 `health`、`info`、`metrics`、`prometheus`、`loggers`、`logfile`，并继续由独立 Basic Auth 过滤器保护。

## Prometheus 抓取

请从部署密钥注入 `MONITOR_USERNAME`、`MONITOR_PASSWORD`，不要把生产凭据写入配置库。Prometheus 示例：

```yaml
scrape_configs:
  - job_name: zero-admin
    metrics_path: /actuator/prometheus
    static_configs:
      - targets: ["zero-admin:8080"]
    basic_auth:
      username: ${MONITOR_USERNAME}
      password: ${MONITOR_PASSWORD}
```

人工验证：

```bash
curl -u "$MONITOR_USERNAME:$MONITOR_PASSWORD" http://localhost:8080/actuator/prometheus
```

建议至少配置以下告警：

- `http_server_requests_seconds` 的 5xx 比例和 P95/P99 延迟；
- JVM 堆使用率、GC 暂停时间、线程数；
- 数据库连接池等待连接数与超时；
- `zero_auth_login_attempts_total{outcome="failure"}` 的突增。

## OTLP 链路导出

部署 OpenTelemetry Collector 后设置：

```bash
OTEL_TRACING_ENABLED=true
OTEL_METRICS_ENABLED=false
OTEL_EXPORTER_OTLP_TRACES_ENDPOINT=http://otel-collector:4318/v1/traces
OTEL_EXPORTER_OTLP_METRICS_ENDPOINT=http://otel-collector:4318/v1/metrics
TRACING_SAMPLING_PROBABILITY=0.1
```

采样率取值为 `0.0` 到 `1.0`。生产环境建议从 `0.01` 到 `0.1` 起步；排障窗口可临时提高，恢复后及时调低。不要长期使用全量采样。

跨服务调用时应保留标准 W3C `traceparent`/`tracestate` 请求头；`X-Request-Id` 用于人工检索日志，不能代替 trace context。

## 排障流程

1. 从前端响应头或网关日志取得 `X-Request-Id`。
2. 在应用日志中按 `requestId` 检索，得到同一请求的 `traceId`。
3. 在链路平台按 `traceId` 查看跨服务 Span、耗时和异常。
4. 回到 Prometheus/Grafana 对照同一时间窗口的错误率、延迟、JVM 与连接池指标。

指标标签严禁加入租户号、用户 ID、用户名、URL 查询参数等高基数或敏感信息。需要定位某个租户时，应使用受控日志字段并遵循脱敏和访问审计要求。
