# 现有数据库迁移

本目录用于升级已有 PostgreSQL 数据库。新建空库请执行 `config/init.sql`，
不要把初始化脚本和迁移脚本混合执行。

迁移按依赖关系建议使用以下顺序：

1. `tenant_package.sql`
2. `social_login.sql`
3. `member.sql`
4. `app_management_menu.sql`（兼容执行过旧版会员迁移的数据库）
5. `partner.sql`
6. `tenant_application.sql`
7. `notice_log.sql`
8. `monitor_core.sql`
9. `external_consoles.sql`
10. `tencent_cos.sql`

这些文件面向不同历史状态，执行前应先备份并核对目标库。新增迁移不能只改
`config/migrations/`；还应把最终 schema 和种子同步到对应的初始化模块，
保证“新库初始化”和“旧库升级”得到一致结果。

`tenant_application.sql` 会把尚无终端绑定的旧 App 迁移到默认消费者认证策略
（`sys_client.id=2`、`client_key=app`）和公开渠道码 `app`。如果旧环境曾通过
`CONSUMER_CLIENT_ID` 改用其他认证客户端，请在升级前确认对应 `sys_client`，并在迁移后
将 `app_application_client.auth_client_id` 调整为原策略；新版 Consumer 不再读取该环境变量。
