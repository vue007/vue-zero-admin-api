-- C 端会员业务模块初始化
-- 依赖 config/init/core.sql；使用示例租户时必须先执行本脚本。
-- 包含会员表、App 管理菜单、会员权限和 App 客户端种子。

begin;

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
-- App 客户端
-- ----------------------------
insert into sys_client (
    id, client_id, client_key, client_secret, grant_type, device_type,
    active_timeout, timeout, status, del_flag, create_dept, create_by,
    create_time, update_by, update_time
) values (
    2, '428a8310cd442757ae699df5d894f051', 'app', 'app123',
    'password,sms,social', 'android', 1800, 604800, '0', '0',
    103, 1, now(), 1, now()
)
on conflict (id) do update set
    client_id = excluded.client_id,
    client_key = excluded.client_key,
    grant_type = excluded.grant_type,
    device_type = excluded.device_type,
    active_timeout = excluded.active_timeout,
    timeout = excluded.timeout,
    status = excluded.status,
    del_flag = excluded.del_flag,
    update_by = 1,
    update_time = now();

-- ----------------------------
-- App 管理目录、会员管理菜单与权限
-- ----------------------------
insert into sys_menu values
    ('7', 'App管理', '0', '3', 'app', null, '', '1', '0', 'M', '0', '0',
     '', 'el-cellphone', 103, 1, now(), null, null, 'C端及App功能管理目录')
on conflict (menu_id) do nothing;

update sys_menu set order_num = 4 where menu_id = 2;

insert into sys_menu values
    ('124', '会员管理', '7', '1', 'member', 'app/member/index', '', '1', '0', 'C', '0', '0',
     'app:member:list', 'ze-users', 103, 1, now(), null, null, '会员管理菜单')
on conflict (menu_id) do update set
    menu_name = excluded.menu_name,
    parent_id = excluded.parent_id,
    order_num = excluded.order_num,
    path = excluded.path,
    component = excluded.component,
    perms = excluded.perms,
    icon = excluded.icon,
    remark = excluded.remark;

insert into sys_menu values
    ('1066', '会员查询', '124', '1', '#', '', '', '1', '0', 'F', '0', '0',
     'app:member:query', '#', 103, 1, now(), null, null, ''),
    ('1067', '会员状态修改', '124', '2', '#', '', '', '1', '0', 'F', '0', '0',
     'app:member:edit', '#', 103, 1, now(), null, null, '')
on conflict (menu_id) do update set
    menu_name = excluded.menu_name,
    parent_id = excluded.parent_id,
    order_num = excluded.order_num,
    perms = excluded.perms,
    status = excluded.status,
    update_by = 1,
    update_time = now();

insert into sys_role_menu (role_id, menu_id) values
    (3, 7),
    (3, 124),
    (3, 1066),
    (3, 1067)
on conflict (role_id, menu_id) do nothing;

-- 默认企业套餐开放 App 管理及 C 端用户功能。
update sys_tenant_package
set menu_ids = concat_ws(',',
    nullif(menu_ids, ''),
    case when position(',7,' in ',' || coalesce(menu_ids, '') || ',') = 0 then '7' end,
    case when position(',124,' in ',' || coalesce(menu_ids, '') || ',') = 0 then '124' end,
    case when position(',1066,' in ',' || coalesce(menu_ids, '') || ',') = 0 then '1066' end,
    case when position(',1067,' in ',' || coalesce(menu_ids, '') || ',') = 0 then '1067' end
)
where package_id = 1;

-- 套餐已开放 C 端用户功能时，为该租户管理员补齐目录、菜单及按钮权限。
insert into sys_role_menu (role_id, menu_id)
select role.role_id, menus.menu_id
from sys_role role
inner join sys_tenant tenant on tenant.tenant_id = role.tenant_id
inner join sys_tenant_package package on package.package_id = tenant.package_id
cross join (values (7::int8), (124::int8), (1066::int8), (1067::int8)) menus(menu_id)
where role.role_key = 'admin'
  and role.del_flag = '0'
  and position(',124,' in ',' || coalesce(package.menu_ids, '') || ',') > 0
on conflict (role_id, menu_id) do nothing;

commit;
