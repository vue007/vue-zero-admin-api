# 第三方登录与账号绑定

本系统的社会化认证能力基于 RuoYi-Vue-Plus 6.X 的 `ruoyi-common-social`，已适配为：

- `zero-base-social` 公共模块；
- Apache Shiro + Redis 会话；
- 按 `tenant_id` 隔离的 `sys_social` 绑定关系；
- 前端 `/social/callback` 回调页、登录入口和个人中心绑定管理。

当前依赖为 `io.github.windtool:JustAuth:3.0.1`。内置支持的 source 以
`SocialUtils#getAuthRequest` 为准，包括 GitHub、Gitee、Gitea、GitLab、钉钉、QQ、微信、企业微信、
Microsoft、MaxKey 和 TopIAM 等。

## 数据库

新库执行 `config/init.sql`。已有库只执行：

```text
config/migrations/20260908_social_login.sql
```

迁移会为早期手工创建但缺少租户列的 `sys_social` 补上 `tenant_id`，并增加“租户 + 第三方账号”和
“租户 + 用户 + 平台”唯一索引。执行唯一索引前，如已有重复绑定数据，应先人工核对并清理。

## 身份平台配置

第三方客户端 ID 和密钥属于部署密钥，不要提交到仓库。开发环境请写入已忽略的
`zero-admin/src/main/resources/secret-dev-local.yml`，生产环境使用部署平台的密钥配置。例如：

```yaml
justauth:
  # 无法直连对应平台时，可按 source 配置 HTTP 或 SOCKS 代理。
  http-config:
    timeout: 30000
    proxy:
      github:
        type: HTTP
        hostname: 127.0.0.1
        port: 7897
  type:
    github:
      client-id: ${SOCIAL_GITHUB_CLIENT_ID}
      client-secret: ${SOCIAL_GITHUB_CLIENT_SECRET}
      redirect-uri: https://127.0.0.1:3030/social/callback?source=github
      scopes:
        - read:user
        - user:email
```

`http-config.proxy` 的键与登录 `source` 一致，端口需填写代理软件实际监听端口；可以只为需要代理的平台配置。
未配置代理的平台仍直接连接。生产环境若能直连身份平台，应省略对应代理项。

身份平台后台登记的回调地址必须与 `redirect-uri` 完全一致。生产环境请替换为前端 HTTPS 地址，例如
`https://admin.example.com/social/callback?source=github`。

配置了非空 `client-id` 和 `redirect-uri` 的平台会由 `GET /auth/social/providers` 返回，并自动显示在登录页和个人中心。

## 接口流程

1. 前端调用 `GET /auth/binding/{source}` 获取授权地址，后端把租户号与随机 state 一起签入 OAuth state。
2. 身份平台跳转到 `/social/callback`。
3. 未登录场景以 `grantType=social` 调用 `POST /auth/login`；已登录绑定场景调用 `POST /auth/social/callback`。
4. 个人中心通过 `GET /system/social/list` 展示绑定摘要，通过 `DELETE /auth/unlock/{id}` 解绑当前用户自己的账号。

`accessToken`、`refreshToken`、`idToken` 等第三方敏感令牌不会序列化到前端。
