# vue-zero-admin-api

## 启动项目
### 安装数据库环境
1. 安装postgresql数据库并添加数据库用户，创建zero_admin database
2. 在仓库根目录添加 `secret-dev-local.yml`（该文件会被 Git 和 Maven 构建忽略）
   ```yml
   SECRET:
     DB:
       HOST: localhost # 数据库地址
       PORT: 5432 # 数据库端口
       USERNAME: root # 数据库账户
       PASSWORD: root123 # 数据库账户密码
   justauth:
     # 无法直连 GitHub 时按 source 配置代理；本机端口以代理软件实际监听值为准。
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
 
3. 执行初始化sql config/init.sql

第三方登录的数据库迁移、身份平台配置和回调流程见 [docs/social-login.md](docs/social-login.md)。

### 本地 HTTPS

默认开发链路由前端开发服务器提供 HTTPS，并将 `/api` 代理到本服务的
`http://localhost:8080`。因此正常本地开发不需要为后端生成证书，也不会因为换电脑缺少
`.cert-local` 文件而启动失败。

只有需要直接访问 `https://localhost:8080` 时，才启用可选的 `https` profile：

```bash
# 首次使用先安装 mkcert，并执行一次 mkcert -install
mkdir -p .cert-local
mkcert -cert-file .cert-local/localhost.pem \
  -key-file .cert-local/localhost-key.pem localhost 127.0.0.1 ::1
SPRING_PROFILES_ACTIVE=dev,https mvn -pl zero-admin -am spring-boot:run
```

若同时通过前端开发服务器访问这个 HTTPS 后端，在前端仓库的 `.env.development.local`
中设置 `API_PROXY_TARGET=https://localhost:8080/`；未设置时前端默认代理到后端 HTTP。

从其他工作目录启动时，可通过 `LOCAL_HTTPS_CERTIFICATE` 和 `LOCAL_HTTPS_PRIVATE_KEY`
指定两个 PEM 文件的绝对 `file:` 路径。证书和私钥只保存在已被 Git 忽略的
`.cert-local/`，不得提交。

## 安装redis环境
1. 安装redis
2. ```$ redis-cli```
3. ```> AUTH redis123```
4. ```> config set requirepass redis123```
