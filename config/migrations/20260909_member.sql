-- C 端会员表迁移。
-- 用于已有 PostgreSQL 数据库；执行前请确认目标数据库并做好备份。

-- ----------------------------
-- C 端会员表（与后台 sys_user 分离）
-- ----------------------------
create table if not exists app_member
(
    member_id       int8          not null,
    tenant_id       varchar(20)   default '000000'::varchar not null,
    username        varchar(30)   not null,
    nickname        varchar(30)   not null,
    mobile          varchar(20)   default null::varchar,
    email           varchar(100)  default null::varchar,
    avatar          varchar(500)  default null::varchar,
    password        varchar(100)  default null::varchar,
    status          char          default '0'::bpchar not null,
    del_flag        char          default '0'::bpchar not null,
    register_source varchar(64)   default 'password'::varchar not null,
    login_ip        varchar(128)  default null::varchar,
    login_date      timestamp,
    remark          varchar(500)  default null::varchar,
    create_dept     int8,
    create_by       int8,
    create_time     timestamp,
    update_by       int8,
    update_time     timestamp,
    constraint "pk_app_member" primary key (member_id)
);

create unique index if not exists uk_app_member_tenant_username
    on app_member (tenant_id, username) where del_flag = '0';
create unique index if not exists uk_app_member_tenant_mobile
    on app_member (tenant_id, mobile) where del_flag = '0' and mobile is not null;
create unique index if not exists uk_app_member_tenant_email
    on app_member (tenant_id, email) where del_flag = '0' and email is not null;
create index if not exists idx_app_member_tenant_status
    on app_member (tenant_id, status) where del_flag = '0';

comment on table app_member is 'C端会员表';
comment on column app_member.member_id is '会员ID';
comment on column app_member.tenant_id is '租户编号';
comment on column app_member.username is '会员登录账号';
comment on column app_member.password is 'BCrypt密码摘要，第三方注册会员可为空';
comment on column app_member.register_source is '首次注册来源';
comment on column app_member.status is '账号状态（0正常 1停用）';
comment on column app_member.del_flag is '删除标志（0存在 1删除）';

-- ----------------------------
-- C 端会员第三方身份表
-- ----------------------------
create table if not exists app_member_social
(
    social_id  int8          not null,
    tenant_id  varchar(20)   default '000000'::varchar not null,
    member_id  int8          not null,
    auth_id    varchar(320)  not null,
    source     varchar(64)   not null,
    open_id    varchar(255)  not null,
    union_id   varchar(255)  default null::varchar,
    username   varchar(100)  default null::varchar,
    nickname   varchar(100)  default null::varchar,
    avatar     varchar(500)  default null::varchar,
    del_flag   char          default '0'::bpchar not null,
    create_dept int8,
    create_by   int8,
    create_time timestamp,
    update_by   int8,
    update_time timestamp,
    constraint "pk_app_member_social" primary key (social_id)
);

create unique index if not exists uk_app_member_social_tenant_auth
    on app_member_social (tenant_id, auth_id) where del_flag = '0';
create unique index if not exists uk_app_member_social_member_source
    on app_member_social (tenant_id, member_id, source) where del_flag = '0';
create index if not exists idx_app_member_social_member
    on app_member_social (tenant_id, member_id) where del_flag = '0';

comment on table app_member_social is 'C端会员第三方身份绑定表';
comment on column app_member_social.auth_id is '来源与平台用户标识组成的稳定唯一键';
comment on column app_member_social.source is '第三方平台或微信小程序来源';
comment on column app_member_social.open_id is '第三方平台用户标识';

-- ----------------------------
-- 后台会员管理菜单与权限
-- ----------------------------
insert into sys_menu values
    ('124', '会员管理', '1', '12', 'member', 'system/member/index', '', '1', '0', 'C', '0', '0',
     'system:member:list', 'ze-user', 103, 1, now(), null, null, 'C端会员管理菜单')
on conflict (menu_id) do nothing;

insert into sys_menu values
    ('1066', '会员查询', '124', '1', '#', '', '', '1', '0', 'F', '0', '0',
     'system:member:query', '#', 103, 1, now(), null, null, ''),
    ('1067', '会员状态修改', '124', '2', '#', '', '', '1', '0', 'F', '0', '0',
     'system:member:edit', '#', 103, 1, now(), null, null, '')
on conflict (menu_id) do nothing;

insert into sys_role_menu (role_id, menu_id) values
    (3, 124),
    (3, 1066),
    (3, 1067)
on conflict (role_id, menu_id) do nothing;
