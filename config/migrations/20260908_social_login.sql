-- 社会化登录表迁移。
-- 可用于已有 PostgreSQL 数据库；新库请直接执行 config/init.sql。

create table if not exists sys_social
(
    id                 int8          not null,
    tenant_id          varchar(20)   default '000000'::varchar not null,
    user_id            int8          not null,
    auth_id            varchar(255)  not null,
    source             varchar(255)  not null,
    open_id            varchar(255)  default null::varchar,
    user_name          varchar(30)   not null,
    nick_name          varchar(30)   default ''::varchar,
    email              varchar(255)  default ''::varchar,
    avatar             varchar(500)  default ''::varchar,
    access_token       varchar(2000) not null,
    expire_in          int8          default null,
    refresh_token      varchar(2000) default null::varchar,
    access_code        varchar(255)  default null::varchar,
    union_id           varchar(255)  default null::varchar,
    scope              varchar(255)  default null::varchar,
    token_type         varchar(255)  default null::varchar,
    id_token           varchar(2000) default null::varchar,
    mac_algorithm      varchar(255)  default null::varchar,
    mac_key            varchar(255)  default null::varchar,
    code               varchar(255)  default null::varchar,
    oauth_token        varchar(255)  default null::varchar,
    oauth_token_secret varchar(255)  default null::varchar,
    create_dept        int8,
    create_by          int8,
    create_time        timestamp,
    update_by          int8,
    update_time        timestamp,
    del_flag           char          default '0'::bpchar,
    constraint "pk_sys_social" primary key (id)
);

-- 兼容早期按上游原表手工建表、但缺少租户列的环境。
alter table sys_social
    add column if not exists tenant_id varchar(20) default '000000'::varchar;
update sys_social set tenant_id = '000000' where tenant_id is null;
alter table sys_social alter column tenant_id set default '000000'::varchar;
alter table sys_social alter column tenant_id set not null;

create unique index if not exists uk_sys_social_tenant_auth
    on sys_social (tenant_id, auth_id);
create unique index if not exists uk_sys_social_tenant_user_source
    on sys_social (tenant_id, user_id, source);

comment on table  sys_social           is '社会化关系表';
comment on column sys_social.id        is '主键';
comment on column sys_social.tenant_id is '租户编号';
comment on column sys_social.user_id   is '用户ID';
comment on column sys_social.auth_id   is '平台+平台唯一id';
comment on column sys_social.source    is '用户来源';
