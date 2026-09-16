# zero-consumer-web

C 端多租户会员独立启动应用，默认端口为 `8081`。它依赖 `zero-member` 领域模块，
并通过 `zero-tenant-app` 校验租户应用；它不扫描 `zero-system` 的 Controller 或 Service，
`zero-system` 依赖仅用于复用服务端 `sys_client` 会话策略模型。

## 认证接口

- `POST /app/auth/register`：密码注册并登录
- `POST /app/auth/login/password`：账号密码登录
- `POST /app/auth/login/social`：JustAuth 第三方登录，首次登录自动注册会员
- `POST /app/auth/login/wechat-mini-program`：微信小程序登录，首次登录自动注册会员
- `GET /app/auth/social/authorize/{source}`：获取第三方授权地址
- `GET /app/auth/social/providers`：获取已配置的第三方平台
- `POST /app/auth/logout`：退出登录
- `GET /app/member/profile`：当前会员资料

所有登录、注册入口使用平台在“应用接入”中配置的公开 `appId + channel`。服务据此查找
`app_application` 及 `app_application_client`，校验应用、终端绑定、认证策略、
`app:member` 授权范围及所属租户状态，然后在解析出的租户会员空间中完成认证。
终端不能通过参数提交内部 `clientId` 或切换租户。

第三方 OAuth 的客户端密钥由平台统一配置。获取授权地址时传入 `appId` 和 `channel`，服务会将
应用、渠道和登录平台写入 `state`；回调时重新解析绑定，因此授权过程中停用的应用或终端也不能继续登录。
所有租户共用第三方平台配置，但会员数据仍按 `tenant_id` 完全隔离。

`sys_client` 负责授权类型、设备类型和会话时长。一个 App 可以将不同公开渠道码绑定到不同
`sys_client`，多个 App 也可以复用同一认证策略。内部客户端标识由服务端绑定关系解析。

四个容易混淆的标识分别是：

- `appId=app_...`：终端请求使用的公开应用标识，对应 `app_application.app_id`；
- `channel=app|h5|miniapp`：该 App 中配置的公开终端渠道码；
- `tenantId`：由后端根据 `appId` 解析，终端不提交；
- `clientId` / `client_key`：仅用于服务端会话策略，终端不提交。

密码注册示例：

```bash
curl http://localhost:8081/app/auth/register \
  --request POST \
  --header 'Content-Type: application/json' \
  --data '{
    "username": "liaokai",
    "password": "liaokai123",
    "nickname": "akai",
    "mobile": "13652306547",
    "email": "",
    "appId": "app_aabbccddeeff00112233445566778899",
    "channel": "app"
  }'
```

示例中的 `appId` 和 `channel` 需要替换为管理后台“应用接入”页面创建并启用的实际配置，且该应用必须
包含 `app:member` 授权范围。`appId` 是公开标识，不是密钥；`appSecret` 只供可信服务端调用，
不能写入网页、App 或小程序。

微信小程序登录提交平台 `appId`、公开 `channel` 和微信 `code`。在服务端按平台 `appId` 绑定微信配置：

```yaml
consumer:
  wechat:
    mini-programs:
      app_aabbccddeeff00112233445566778899:
        wechat-app-id: wx0000000000000000
        secret: ${WECHAT_MINI_PROGRAM_SECRET}
```

开发环境会从管理端已有的 `zero-admin/src/main/resources/secret-dev-local.yml` 读取数据库连接配置。
如需独立数据库配置，请在当前模块的 `src/main/resources/secret-consumer-local.yml` 中提供
`DB_HOST`、`DB_PORT`、`DB_NAME`、`DB_USERNAME`、`DB_PASSWORD`；本地 Redis 要求认证时还需提供
`REDIS_PASSWORD`。这些设置也可通过环境变量覆盖。本地密钥文件不会进入构建制品。
微信和第三方平台密钥也只能通过该文件、环境变量或配置中心提供。
