# Admin 监控与任务调度中心

本项目参照 RuoYi-Vue-Plus 6.x，将两个控制台作为独立 Spring Boot 服务接入：

- `zero-extend/zero-monitor-admin`：Spring Boot Admin，HTTP 端口 `9090`，控制台路径 `/admin`。
- `zero-extend/zero-snailjob-server`：SnailJob 2.0.2，HTTP 端口 `8800`、客户端通信端口 `17888`，控制台路径 `/snail-job`。
- `zero-modules/zero-job`：主应用中的 SnailJob 客户端及示例执行器 `systemHealthJob`。

## 1. 初始化数据库

已有业务数据库先执行菜单迁移：

```sql
\i config/migrations/20260826_external_consoles.sql
```

首次启用 SnailJob 时，再执行一次完整的调度表初始化脚本：

```sql
\i config/snail-job-postgres.sql
```

该脚本不是幂等迁移，不要重复执行。它会创建 `sj_*` 表，并创建
`local`、`dev`、`prod` 三个命名空间及 `zero_admin_group` 客户端组。

初始化后的 SnailJob 控制台账号为 `admin / admin`。首次登录后应立即修改密码。

开发环境从仓库根目录启动时，SnailJob 会优先读取环境变量
`SNAIL_JOB_DB_URL`、`SNAIL_JOB_DB_USERNAME`、`SNAIL_JOB_DB_PASSWORD`；未设置时会复用
`zero-admin/src/main/resources/secret-dev-local.yml` 中的 `SECRET.DB.*`。该本地密钥文件被
Git 忽略，不应提交。

## 2. 构建与启动独立服务

```bash
mvn -pl zero-extend/zero-monitor-admin -am -Pdev package
mvn -pl zero-extend/zero-snailjob-server -am -Pdev package

MONITOR_USERNAME=zero MONITOR_PASSWORD='replace-this-password' \
  java -jar zero-extend/zero-monitor-admin/target/zero-monitor-admin.jar

SNAIL_JOB_DB_URL='jdbc:postgresql://localhost:5432/zero_admin' \
SNAIL_JOB_DB_USERNAME='postgres' \
SNAIL_JOB_DB_PASSWORD='replace-this-password' \
  java -jar zero-extend/zero-snailjob-server/target/zero-snailjob-server.jar
```

生产环境必须通过环境变量提供数据库凭据及监控账号密码，不要把真实凭据提交到 YAML。

## 3. 让主应用注册到两个中心

主应用默认关闭两个可选客户端。启动 `zero-admin-web.jar` 时按需设置：

```bash
MONITOR_CLIENT_ENABLED=true \
MONITOR_USERNAME=zero \
MONITOR_PASSWORD='replace-this-password' \
SNAIL_JOB_ENABLED=true \
SNAIL_JOB_TOKEN='replace-with-sj_group_config-token' \
java -jar zero-admin/target/zero-admin-web.jar
```

`SNAIL_JOB_TOKEN` 必须与 `sj_group_config.token` 中当前命名空间、
`zero_admin_group` 对应的值完全一致。初始化脚本中的值只是开发占位符，启用前应同时修改数据库和环境变量。

## 4. 前端与反向代理

开发服务器已经代理：

- `/admin` → `http://localhost:9090/admin`
- `/snail-job` → `http://localhost:8800/snail-job`

生产环境也必须保持这两个同源路径。例如 Nginx：

```nginx
location /admin/ {
    proxy_pass http://127.0.0.1:9090/admin/;
    proxy_set_header Host $host;
    proxy_set_header X-Forwarded-Proto $scheme;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
}

location /snail-job/ {
    proxy_pass http://127.0.0.1:8800/snail-job/;
    proxy_set_header Host $host;
    proxy_set_header X-Forwarded-Proto $scheme;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
}
```

如部署路径不同，分别调整前端的 `VITE_APP_MONITOR_URL` 与
`VITE_APP_SNAIL_JOB_URL` 后重新构建。

## 安全说明

- 主应用的 `/actuator/**` 已从 Shiro 会话认证中分离，但仍由独立 Basic Auth 过滤器保护。
- Admin 监控与 SnailJob 控制台各自保留独立认证；前端 iframe 不携带主系统管理员凭据。
- 不应将 `9090`、`8800` 或 `17888` 直接暴露到公网；优先使用内网、防火墙及 HTTPS 反向代理。
