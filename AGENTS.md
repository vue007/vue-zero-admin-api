# 项目协作指南

## 仓库边界与事实来源

- 本仓库是后端 `vue-zero-admin-api`；相邻的 `../vue-zero-admin` 是 Vue 前端。接口、登录、动态菜单或权限模型变更通常需要同步检查两个仓库。
- 前端仓库的 `Thesis.pdf` 用于理解业务目标和早期设计，`config/init.sql` 用于空库初始化。实现冲突时以当前 Java 源码和配置为第一事实来源，其次是 `init.sql`，最后才是论文。
- 论文描述的是旧版本（Spring Boot 3.4、Undertow、Sa-Token/JWT、Caffeine + Redis）。当前代码已经升级为 Spring Boot 4.1、Tomcat、Apache Shiro 和 Redis 会话；不要复制论文中的旧认证示例。

## 产品与领域模型

- 系统是多租户企业后台基础服务，核心能力包括租户/套餐、用户、部门、岗位、菜单、角色、字典、参数、公告、日志、OSS、客户端授权、个人资料和认证。
- 权限分三层：租户隔离、RBAC 功能权限、角色数据范围。超级管理员可跨租户管理；普通租户数据必须保持隔离。
- 核心关系是 `user -> user_role -> role -> role_menu -> menu`；岗位通过 `user_post` 关联用户；自定义数据范围通过 `role_dept` 关联部门。
- `sys_role.data_scope`：`1` 全部、`2` 自定义、`3` 本部门、`4` 本部门及以下、`5` 仅本人。任何列表或详情接口都不能只做功能权限而漏掉数据范围检查。

## 当前技术架构

- Java 26、Spring Boot 4.1.1、Spring MVC/Tomcat、Maven 多模块。
- PostgreSQL 18；MyBatis/MyBatis-Plus + XML Mapper；应用侧 `ASSIGN_ID` 生成主键；dynamic-datasource 管理数据源。
- Apache Shiro 3 负责认证与角色/权限注解授权；Shiro session 和在线信息存 Redis/Redisson。
- MapStruct Plus 负责 Entity/BO/VO 转换；Lombok、Hutool 为常用基础工具。
- 横切能力位于 `zero-base`：Web、MyBatis、租户、Shiro、安全、Redis、日志、幂等、限流、加密、Excel、OSS、SSE、国际化等。

## Maven 模块职责

- `zero-admin/`：可运行 Web 应用，入口为 `com.zero.admin.AdminApplication`；包含认证、注册、验证码和应用配置，产物为 `zero-admin-web.jar`。
- `zero-modules/zero-system/`：系统业务模块，按 Controller -> Service -> Mapper 分层，包含 Entity、BO、VO 与 Mapper XML。
- `zero-base/zero-base-core/`：通用模型、常量、异常和工具；其他 `zero-base-*` 模块提供单一横切能力。
- 新业务优先放入独立 `zero-modules/<module>`，通用且与业务无关的能力才进入 `zero-base`。不要让 `zero-base` 反向依赖业务模块。

## HTTP、认证与响应契约

- 服务默认监听 `8080`，前端开发代理会移除 `/api` 前缀后访问本服务。
- 普通响应统一使用 `R<T>`：`{ code, msg, data }`，成功码 `200`、默认失败码 `500`、警告码 `601`。分页使用 `TableDataInfo<T>`，其中 `data` 为 `{ rows, total }`。
- 登录入口是 `POST /auth/login`。请求至少包含 `clientId`、`grantType`、`tenantId` 和相应认证载荷；密码登录还包含用户名、密码及启用验证码时的 `code/uuid`。
- `clientId` 必须命中启用的 `sys_client` 且允许所请求的 `grantType`。前端当前 PC 客户端 ID 必须与 `config/init.sql` 的种子保持一致。
- 登录成功返回的 `access_token` 是 Shiro session ID。后续请求使用 `Authorization: Bearer <token>`；`TokenWebSessionManager` 从该头读取会话，`clientid` 请求头用于客户端识别与日志。
- 公开路径集中在 `ShiroConfig`（登录、验证码、注册、租户列表、第三方绑定等），其他路径经 `RestAuthFilter` 认证。新增匿名接口时显式更新并审查过滤链，不能通过宽泛通配符放开业务接口。
- `GlobalExceptionHandler` 负责统一业务、参数和运行时异常响应。Controller 返回 `R`/`TableDataInfo`，不要自行发明另一套响应 envelope。

## 授权、租户与数据权限

- Controller 功能权限使用 Shiro `@RequiresPermissions("system:<resource>:<action>")`，租户/菜单等超管能力叠加 `@RequiresRoles`。权限串必须与 `sys_menu.perms` 和前端授权入口保持一致。
- 多租户默认开启。带租户数据的实体继承 `TenantEntity`，由租户插件自动加 `tenant_id` 条件；跨租户操作只能通过既有 `TenantHelper`/动态租户机制完成。
- `application.yml` 的租户排除表是全局共享或纯关系表，包括 `sys_menu`、`sys_tenant`、`sys_tenant_package`、角色/用户关系表、`sys_client`、`sys_oss_config`。修改此列表属于安全敏感变更，必须检查所有读写路径。
- 数据范围通过 Mapper 方法上的 `@DataPermission`/`@DataColumn` 注入。新增用户、部门、岗位、角色相关查询时，参考现有 Mapper 标注；需要绕过时只在明确的系统内部操作中使用 `DataPermissionHelper.ignore`，并把范围缩到最小。
- 超级管理员、租户管理员和普通用户的边界由 `LoginHelper`、角色 key 与租户服务共同决定；禁止只根据前端参数或用户提交的 `tenantId` 判断权限。

## 数据库约定与 `init.sql`

- `config/init.sql` 面向新建 PostgreSQL 数据库，创建租户、套餐、部门、用户、岗位、角色、菜单、关系表、字典、参数、操作/登录日志、公告、客户端及测试表，并写入默认租户 `000000`、管理员、角色、菜单和 PC/App 客户端种子。
- 初始化脚本不是可重复迁移：大量种子是普通 `INSERT`，`sys_client` 甚至没有 `IF NOT EXISTS`。只对空库执行；已有环境必须使用有版本、可回滚或幂等的迁移脚本，不能直接重跑整个文件。
- 脚本没有创建数据库本身；README 预期 `zero_admin`，而当前 Docker Compose 默认创建 `postgres`。实际库名由本地 secret 配置决定，初始化前先确认目标库，绝不能凭默认值执行。
- 表未声明数据库外键，完整性由 Service 校验和关系表维护。删除用户、部门、角色、菜单、租户时必须沿用现有前置检查和关联清理，不能只删除主表记录。
- MyBatis-Plus 全局主键为 `ASSIGN_ID`，SQL 没有 sequence/identity。新增表默认使用 `int8`/`Long` 主键并由应用生成，除非全链路明确改为数据库自增。
- 租户业务表通常含 `tenant_id`；可审计实体含 `create_dept/create_by/create_time/update_by/update_time`；支持逻辑删除的表含 `del_flag` 且实体用 `@TableLogic`。保持 `0` 正常/存在、`1` 停用/删除的既有语义。
- 部门树使用 `parent_id + ancestors`，菜单树使用 `parent_id`。修改树节点必须维护祖先链、禁止循环，并检查子节点和关联对象。
- `sys_dict_type` 对 `(tenant_id, dict_type)` 有唯一索引；新增字典类型时按租户保证唯一。
- 代码含 `SysOss`、`SysOssConfig`、`SysSocial` 等实体，但当前 `init.sql` 未覆盖其全部表。启用相关模块前先核对数据库 schema，不要假定只执行该脚本即可支持所有后端能力。

## 业务代码实现约定

- 新增 CRUD 时沿用同领域结构：`domain/<Entity>`、`domain/bo/<Entity>Bo`、`domain/vo/<Entity>Vo`、Mapper、Service 接口/实现、Controller；使用 `MapstructUtils`/`BaseMapperPlus`，避免在 Controller 手工拼装持久化对象。
- Controller 只做 HTTP 绑定、校验、权限和响应；事务、唯一性、树/关系维护放 Service；复杂 SQL 放 Mapper XML 或明确的 Mapper 方法。
- 写操作使用 Jakarta Validation 与校验分组；管理操作按现有模式添加 `@Log`，高风险重复提交接口考虑 `@RepeatSubmit`，限流/加密仅按现有基础模块契约使用。
- 列表参数使用 BO + `PageQuery`，分页返回 `TableDataInfo`。Entity 不直接作为对外响应或写入模型。
- 认证策略通过 `IAuthStrategy` 扩展，当前实现有 password、social、xcx。新增 grant type 时同步：策略 Bean、`sys_client.grant_type`、授权类型字典、登录校验、前端请求和测试。
- 所有缓存 key 和会话操作复用 `CacheConstants`、`RedisUtils`、Shiro session DAO；不要另建无法统一失效的认证缓存。

## 配置与本地运行

- Maven profile 有 `local`、`dev`（默认）和 `prod`，会替换 `spring.profiles.active`；配置文件名和 profile 必须对应。
- 本地数据库凭据放 `zero-admin/src/main/resources/secret-dev-local.yml`，该模式已被 `.gitignore` 忽略。不要把数据库、Redis、OSS、第三方登录或部署凭据写入被跟踪的 YAML、SQL、日志或文档。
- 启动依赖为 PostgreSQL 与 Redis；Docker Compose 使用 host network 和 `/docker/...` 绝对挂载，更接近服务器环境，macOS/Windows 本地开发需按平台调整，不能假定开箱即用。
- 仓库只有 Windows 的 `mvnw.cmd`，Unix/macOS 使用已安装且支持 Java 26 的 `mvn`。

```bash
# 默认会跳过测试
mvn -Pdev clean package

# 显式执行 dev 分组测试
mvn -pl zero-admin -am -Pdev -DskipTests=false test

# 构建后启动
java -jar zero-admin/target/zero-admin-web.jar
```

- `skipTests` 在父 POM 默认为 `true`；声称“测试通过”前必须显式设置 `-DskipTests=false`，并说明使用的 profile/tag。
- 修改单模块基础能力时至少运行 `mvn -pl <module> -am test -DskipTests=false`；修改跨模块 API、注解处理器或依赖版本时运行根项目 `clean package`，避免残留生成类影响结果。
- 集成测试若使用 `@SpringBootTest`，需要可连接的 PostgreSQL/Redis 和对应 secret 配置；纯工具逻辑优先编写不依赖外部服务的单元测试。

## 部署与提交卫生

- `zero-admin/Dockerfile` 构建 Java 26 镜像并复制 `zero-admin-web.jar`；GitHub Actions 在 `release/*` 上以 `-P prod` 构建并推送阿里云镜像。修改产物名、Java 版本或 profile 时同步更新 POM、Dockerfile 和 workflow。
- 不提交 `target/`、`logs/`、`spy.log`、本地 secret、数据库数据目录或 IDE 产物。
- Schema、权限、菜单或客户端配置改动在交付说明中明确列出迁移顺序、回滚方式以及前端是否需要同步发布。
