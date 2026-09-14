-- Zero Admin 新库初始化入口（psql）
-- 只对空 PostgreSQL 数据库执行；已有数据库使用 config/migrations/。
-- \ir 以本文件所在目录解析相对路径，因此可从任意工作目录调用。

\set ON_ERROR_STOP on
\ir init/core.sql
\ir init/apps/member.sql
\ir init/apps/partner.sql
\ir init/tenants/example-tenant.sql
\ir init/apps/application.sql
