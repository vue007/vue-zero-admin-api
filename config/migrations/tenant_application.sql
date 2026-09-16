-- 租户 App 接入表、双入口菜单及权限迁移。
-- 可重复执行；执行前请确认目标 PostgreSQL 数据库并做好备份。

begin;

create table if not exists app_application
(
    id                  int8          not null,
    tenant_id           varchar(20)   default '000000'::varchar not null,
    app_name            varchar(100)  not null,
    app_id              varchar(64)   not null,
    secret_hash         varchar(100)  not null,
    scope_codes         varchar(4096) default ''::varchar not null,
    status              char          default '0'::bpchar not null,
    last_access_time    timestamp,
    secret_rotated_time timestamp     not null,
    del_flag            char          default '0'::bpchar not null,
    remark              varchar(500)  default null::varchar,
    create_dept         int8,
    create_by           int8,
    create_time         timestamp,
    update_by           int8,
    update_time         timestamp,
    constraint pk_app_application primary key (id)
);

-- 应用本身不重复保存终端类型，终端渠道通过 app_application_client 显式绑定。
alter table app_application drop column if exists app_type;

create unique index if not exists uk_app_application_app_id
    on app_application (app_id);
create index if not exists idx_app_application_tenant_status
    on app_application (tenant_id, status) where del_flag = '0';
create index if not exists idx_app_application_tenant_created
    on app_application (tenant_id, create_time desc) where del_flag = '0';

comment on table app_application is '租户 App 接入应用表';
comment on column app_application.id is '应用主键';
comment on column app_application.tenant_id is '可信所属租户编号';
comment on column app_application.app_name is '应用名称';
comment on column app_application.app_id is '全局唯一公开应用标识';
comment on column app_application.secret_hash is 'App Secret 的 BCrypt 摘要';
comment on column app_application.scope_codes is '逗号分隔的授权范围';
comment on column app_application.status is '状态（0正常 1停用）';
comment on column app_application.last_access_time is '最后一次凭证校验成功时间';
comment on column app_application.secret_rotated_time is '密钥最近轮换时间';
comment on column app_application.del_flag is '删除标志（0存在 1删除）';

create table if not exists app_application_client
(
    id                  int8         not null,
    tenant_id           varchar(20)  default '000000'::varchar not null,
    application_id      int8         not null,
    auth_client_id      int8         not null,
    channel             varchar(32)  not null,
    status              char         default '0'::bpchar not null,
    del_flag            char         default '0'::bpchar not null,
    create_dept         int8,
    create_by           int8,
    create_time         timestamp,
    update_by           int8,
    update_time         timestamp,
    constraint pk_app_application_client primary key (id)
);

create unique index if not exists uk_app_application_client_channel
    on app_application_client (application_id, channel) where del_flag = '0';
create index if not exists idx_app_application_client_tenant_app
    on app_application_client (tenant_id, application_id) where del_flag = '0';
create index if not exists idx_app_application_client_auth
    on app_application_client (auth_client_id) where del_flag = '0';

comment on table app_application_client is 'App 与认证客户端的终端渠道绑定表';
comment on column app_application_client.id is '绑定主键';
comment on column app_application_client.tenant_id is '可信所属租户编号';
comment on column app_application_client.application_id is 'app_application.id';
comment on column app_application_client.auth_client_id is 'sys_client.id';
comment on column app_application_client.channel is 'App 对外公开的终端渠道码';
comment on column app_application_client.status is '绑定状态（0正常 1停用）';
comment on column app_application_client.del_flag is '删除标志（0存在 1删除）';

-- 仅兼容尚无任何终端绑定的旧应用：默认绑定消费者认证客户端；负数主键与 ASSIGN_ID 空间隔离。
insert into app_application_client (
    id, tenant_id, application_id, auth_client_id, channel, status, del_flag,
    create_dept, create_by, create_time, update_by, update_time
)
select -application.id, application.tenant_id, application.id, auth_client.id, 'app', '0', '0',
       application.create_dept, application.create_by, now(), application.update_by, now()
from app_application application
inner join sys_client auth_client
  on auth_client.id = 2
 and auth_client.client_key = 'app'
 and auth_client.del_flag = '0'
where application.del_flag = '0'
  and not exists (
      select 1
      from app_application_client binding
      where binding.application_id = application.id
        and binding.del_flag = '0'
  )
on conflict (id) do nothing;

insert into sys_menu values
    ('126', 'App接入管理', '6', '3', 'appAccess', 'tenant/appAccess/index', '', '1', '0', 'C', '0', '0',
     'system:tenantApp:list', 'el-key', 103, 1, now(), null, null, '平台跨租户 App 接入管理')
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
    ('1072', 'App接入查询', '126', '1', '#', '', '', '1', '0', 'F', '0', '0', 'system:tenantApp:query', '#', 103, 1, now(), null, null, ''),
    ('1073', 'App接入新增', '126', '2', '#', '', '', '1', '0', 'F', '0', '0', 'system:tenantApp:add', '#', 103, 1, now(), null, null, ''),
    ('1074', 'App接入修改', '126', '3', '#', '', '', '1', '0', 'F', '0', '0', 'system:tenantApp:edit', '#', 103, 1, now(), null, null, ''),
    ('1075', 'App接入状态', '126', '4', '#', '', '', '1', '0', 'F', '0', '0', 'system:tenantApp:status', '#', 103, 1, now(), null, null, ''),
    ('1076', 'App接入重置密钥', '126', '5', '#', '', '', '1', '0', 'F', '0', '0', 'system:tenantApp:resetSecret', '#', 103, 1, now(), null, null, ''),
    ('1077', 'App接入删除', '126', '6', '#', '', '', '1', '0', 'F', '0', '0', 'system:tenantApp:remove', '#', 103, 1, now(), null, null, '')
on conflict (menu_id) do update set
    menu_name = excluded.menu_name,
    parent_id = excluded.parent_id,
    order_num = excluded.order_num,
    perms = excluded.perms,
    status = excluded.status,
    update_by = 1,
    update_time = now();

insert into sys_menu values
    ('127', '应用接入', '7', '3', 'access', 'app/access/index', '', '1', '0', 'C', '0', '0',
     'app:application:list', 'el-key', 103, 1, now(), null, null, '当前租户应用接入管理')
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
    ('1078', '应用接入查询', '127', '1', '#', '', '', '1', '0', 'F', '0', '0', 'app:application:query', '#', 103, 1, now(), null, null, ''),
    ('1079', '应用接入新增', '127', '2', '#', '', '', '1', '0', 'F', '0', '0', 'app:application:add', '#', 103, 1, now(), null, null, ''),
    ('1080', '应用接入修改', '127', '3', '#', '', '', '1', '0', 'F', '0', '0', 'app:application:edit', '#', 103, 1, now(), null, null, ''),
    ('1081', '应用接入状态', '127', '4', '#', '', '', '1', '0', 'F', '0', '0', 'app:application:status', '#', 103, 1, now(), null, null, ''),
    ('1082', '应用接入重置密钥', '127', '5', '#', '', '', '1', '0', 'F', '0', '0', 'app:application:resetSecret', '#', 103, 1, now(), null, null, ''),
    ('1083', '应用接入删除', '127', '6', '#', '', '', '1', '0', 'F', '0', '0', 'app:application:remove', '#', 103, 1, now(), null, null, '')
on conflict (menu_id) do update set
    menu_name = excluded.menu_name,
    parent_id = excluded.parent_id,
    order_num = excluded.order_num,
    perms = excluded.perms,
    status = excluded.status,
    update_by = 1,
    update_time = now();

insert into sys_role_menu (role_id, menu_id)
select role.role_id, menus.menu_id
from sys_role role
cross join (values
    (6::int8), (126::int8),
    (1072::int8), (1073::int8), (1074::int8), (1075::int8),
    (1076::int8), (1077::int8)
) menus(menu_id)
where role.role_key = 'superadmin' and role.del_flag = '0'
on conflict (role_id, menu_id) do nothing;

insert into sys_role_menu (role_id, menu_id) values
    (3, 7), (3, 127),
    (3, 1078), (3, 1079), (3, 1080), (3, 1081), (3, 1082), (3, 1083)
on conflict (role_id, menu_id) do nothing;

-- 标准企业套餐获得租户自助入口，平台入口绝不下发到套餐。
update sys_tenant_package
set menu_ids = concat_ws(',',
    nullif(menu_ids, ''),
    case when position(',7,' in ',' || coalesce(menu_ids, '') || ',') = 0 then '7' end,
    case when position(',127,' in ',' || coalesce(menu_ids, '') || ',') = 0 then '127' end,
    case when position(',1078,' in ',' || coalesce(menu_ids, '') || ',') = 0 then '1078' end,
    case when position(',1079,' in ',' || coalesce(menu_ids, '') || ',') = 0 then '1079' end,
    case when position(',1080,' in ',' || coalesce(menu_ids, '') || ',') = 0 then '1080' end,
    case when position(',1081,' in ',' || coalesce(menu_ids, '') || ',') = 0 then '1081' end,
    case when position(',1082,' in ',' || coalesce(menu_ids, '') || ',') = 0 then '1082' end,
    case when position(',1083,' in ',' || coalesce(menu_ids, '') || ',') = 0 then '1083' end
)
where package_id = 1;

-- 套餐已开放应用接入时，为该租户管理员补齐目录、菜单及按钮权限。
insert into sys_role_menu (role_id, menu_id)
select role.role_id, menus.menu_id
from sys_role role
inner join sys_tenant tenant on tenant.tenant_id = role.tenant_id
inner join sys_tenant_package package on package.package_id = tenant.package_id
cross join (values
    (7::int8), (127::int8),
    (1078::int8), (1079::int8), (1080::int8), (1081::int8),
    (1082::int8), (1083::int8)
) menus(menu_id)
where role.role_key = 'admin'
  and role.del_flag = '0'
  and position(',127,' in ',' || coalesce(package.menu_ids, '') || ',') > 0
on conflict (role_id, menu_id) do nothing;

commit;
