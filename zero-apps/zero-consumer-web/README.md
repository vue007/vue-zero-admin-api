# zero-consumer-web

C 端会员独立启动应用，默认端口为 `8081`。它依赖 `zero-member` 领域模块，
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

登录、注册请求的 `clientId` 必须对应启用的 `sys_client`，授权类型必须匹配，
并且 `client_key` 必须在 `consumer.auth.allowed-client-keys` 中。当前初始化脚本中的
App 客户端满足 `client_key=app` 以及 `password,social` 授权类型。

本地敏感配置请写入不提交版本库的 `secret-consumer-local.yml`，至少提供数据库密码；
微信和第三方平台密钥也只能通过该文件、环境变量或配置中心提供。
