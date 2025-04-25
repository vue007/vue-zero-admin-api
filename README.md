# vue-zero-admin-api

## 启动项目
### 安装数据库环境
1. 安装postgresql数据库并添加数据库用户，创建zero_admin database
2. 在[zero-admin/src/main/resources](zero-admin/src/main/resources) 目录下添加 secret-dev-local.yml
   ```yml
   SECRET: 
    DB:
    HOST: localhost # 数据库地址
    PORT: 5432 # 数据库端口
    USERNAME: root # 数据库账户
    PASSWORD: root123 # 数据库账户密码
   ```
3. 执行初始化sql config/init.sql

## 安装redis环境
1. 安装redis
2. ```$ redis-cli```
3. ```> config set requirepass redis123```

## 