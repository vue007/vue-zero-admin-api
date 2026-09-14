# 数据库初始化脚本

本目录只用于初始化空的 PostgreSQL 数据库。已有数据库必须使用
`config/migrations/` 下的增量脚本，不能重新执行这些初始化文件。

## 目录划分

1. `core.sql`
   - 创建所有 `sys_*` 核心表；
   - 初始化主租户 `000000`；
   - 初始化部门、后台用户、岗位、角色、菜单、字典、参数、公告和 PC 客户端等基础数据。
   - 日志、第三方绑定和 OSS 对象等运行数据只建表，不写入伪造记录。
2. `tenants/example-tenant.sql`
   - 初始化示例租户“XXX有限责任公司”（租户号 `000001`）；
   - 包含套餐、组织、岗位、后台账号、角色、数据范围、字典、参数、示例会员和示例合作客户；
   - 只用于本地开发和演示环境。
3. `apps/member.sql`
   - 初始化 C 端会员模块的 `app_member`、`app_member_social`；
   - 初始化 App 客户端、App 管理目录、会员菜单和权限。
4. `apps/partner.sql`
   - 初始化当前租户自己的合作客户（商户）表 `app_partner`；
   - 初始化合作客户菜单和增删改查权限。
5. `apps/application.sql`
   - 初始化租户 App 接入表 `app_application`；
   - 初始化“租户管理/App接入管理”和“App管理/应用接入”两套菜单及权限。

其他脚本：

- `apps/demo.sql`：旧版代码生成/数据权限演示表。当前没有对应运行模块，默认不执行。
- `extensions/snail-job.sql`：SnailJob 独立扩展服务的 `sj_*` 表和种子，按需单独执行。

## 新库执行方式

默认入口会按实际依赖顺序执行“核心 → 会员 App → 合作客户 App → 示例租户 → 应用接入”：

```bash
psql -v ON_ERROR_STOP=1 -d zero_admin -f config/init.sql
```

`config/init.sql` 使用 psql 的 `\ir` 相对包含指令。如果使用 DataGrip、DBeaver
或其他不识别该指令的客户端，请依次执行：

1. `config/init/core.sql`
2. `config/init/apps/member.sql`
3. `config/init/apps/partner.sql`
4. `config/init/tenants/example-tenant.sql`
5. `config/init/apps/application.sql`

只需要核心平台时，仅执行第一项；不要执行示例租户脚本。每个脚本都有独立事务，
任一脚本失败时会回滚该部分。

## 可选扩展

需要 SnailJob 时，在完成核心初始化后单独执行：

```bash
psql -v ON_ERROR_STOP=1 -d zero_admin -f config/init/extensions/snail-job.sql
```

该脚本不是幂等迁移，不能重复执行。初始化前应确认数据库名称和连接目标；
生产环境不要导入示例租户或演示数据，并应替换所有开发默认凭据。
