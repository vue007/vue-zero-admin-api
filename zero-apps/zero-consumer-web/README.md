# zero-consumer-web

C 端多租户会员独立启动应用，默认端口为 `8081`。它依赖 `zero-member` 领域模块，
但不扫描 `zero-system` 的 Controller 或 Service；`zero-system` 依赖仅用于读取
全局 `sys_client` 和 `sys_tenant` 配置。

## 认证接口

- `POST /app/auth/register`：密码注册并登录
- `POST /app/auth/login/password`：账号密码登录
- `POST /app/auth/login/social`：JustAuth 第三方登录，首次登录自动注册会员
- `POST /app/auth/login/wechat-mini-program`：微信小程序登录，首次登录自动注册会员
- `GET /app/auth/social/authorize/{source}`：获取第三方授权地址
- `GET /app/auth/social/providers`：获取已配置的第三方平台
- `POST /app/auth/logout`：退出登录
- `GET /app/member/profile`：当前会员资料

密码登录、注册及微信小程序登录请求必须携带 `tenantId`。服务会先校验租户状态，
再在该租户的会员空间中完成认证；登录成功后的请求只使用会话中的租户，不能通过参数切换。

第三方 OAuth 的客户端密钥由平台统一配置。获取授权地址时传入 `tenantId`，服务会将租户、
客户端和登录平台写入受 JustAuth 缓存校验的 `state`，回调登录时从该 `state` 恢复可信租户。
所有租户共用第三方应用配置，但会员数据仍按 `tenant_id` 完全隔离。

登录、注册请求的 `clientId` 必须对应启用的 `sys_client`，授权类型必须匹配，
并且 `client_key` 必须在 `consumer.auth.allowed-client-keys` 中。当前初始化脚本中的
App 客户端满足 `client_key=app` 以及 `password,social` 授权类型。

本地敏感配置请写入不提交版本库的 `secret-consumer-local.yml`，至少提供数据库密码；
微信和第三方平台密钥也只能通过该文件、环境变量或配置中心提供。
