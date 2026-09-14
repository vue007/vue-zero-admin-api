-- App 合作客户表及管理功能迁移。
-- 用于已有 PostgreSQL 数据库；执行前请确认目标数据库并做好备份。


begin;

create table if not exists app_partner
(
    partner_id     int8          not null,
    tenant_id      varchar(20)   default '000000'::varchar not null,
    partner_code   varchar(32)   not null,
    partner_name   varchar(100)  not null,
    credit_code    varchar(32)   default null::varchar,
    contact_name   varchar(50)   default null::varchar,
    contact_phone  varchar(30)   default null::varchar,
    contact_email  varchar(100)  default null::varchar,
    address        varchar(255)  default null::varchar,
    status         char          default '0'::bpchar not null,
    del_flag       char          default '0'::bpchar not null,
    remark         varchar(500)  default null::varchar,
    create_dept    int8,
    create_by      int8,
    create_time    timestamp,
    update_by      int8,
    update_time    timestamp,
    constraint "pk_app_partner" primary key (partner_id)
);

create unique index if not exists uk_app_partner_tenant_code
    on app_partner (tenant_id, partner_code) where del_flag = '0';
create unique index if not exists uk_app_partner_tenant_credit_code
    on app_partner (tenant_id, credit_code)
    where del_flag = '0' and credit_code is not null and credit_code <> '';
create index if not exists idx_app_partner_tenant_status
    on app_partner (tenant_id, status) where del_flag = '0';

comment on table app_partner is '租户合作客户（商户）表';
comment on column app_partner.partner_id is '合作客户ID';
comment on column app_partner.tenant_id is '所属租户编号';
comment on column app_partner.partner_code is '租户内唯一的合作客户编码';
comment on column app_partner.partner_name is '合作客户或商户名称';
comment on column app_partner.credit_code is '统一社会信用代码';
comment on column app_partner.status is '状态（0正常 1停用）';
comment on column app_partner.del_flag is '删除标志（0存在 1删除）';

insert into sys_menu values
    ('7', 'App管理', '0', '3', 'app', null, '', '1', '0', 'M', '0', '0',
     '', 'el-cellphone', 103, 1, now(), null, null, 'C端及App功能管理目录')
on conflict (menu_id) do nothing;

insert into sys_menu values
    ('125', '合作客户管理', '7', '2', 'partner', 'app/partner/index', '', '1', '0', 'C', '0', '0',
     'app:partner:list', 'el-office-building', 103, 1, now(), null, null, '租户合作客户管理菜单')
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
    ('1068', '合作客户查询', '125', '1', '#', '', '', '1', '0', 'F', '0', '0',
     'app:partner:query', '#', 103, 1, now(), null, null, ''),
    ('1069', '合作客户新增', '125', '2', '#', '', '', '1', '0', 'F', '0', '0',
     'app:partner:add', '#', 103, 1, now(), null, null, ''),
    ('1070', '合作客户修改', '125', '3', '#', '', '', '1', '0', 'F', '0', '0',
     'app:partner:edit', '#', 103, 1, now(), null, null, ''),
    ('1071', '合作客户删除', '125', '4', '#', '', '', '1', '0', 'F', '0', '0',
     'app:partner:remove', '#', 103, 1, now(), null, null, '')
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
    (3, 125),
    (3, 1068),
    (3, 1069),
    (3, 1070),
    (3, 1071)
on conflict (role_id, menu_id) do nothing;

update sys_tenant_package
set menu_ids = concat_ws(',',
    nullif(menu_ids, ''),
    case when position(',7,' in ',' || coalesce(menu_ids, '') || ',') = 0 then '7' end,
    case when position(',125,' in ',' || coalesce(menu_ids, '') || ',') = 0 then '125' end,
    case when position(',1068,' in ',' || coalesce(menu_ids, '') || ',') = 0 then '1068' end,
    case when position(',1069,' in ',' || coalesce(menu_ids, '') || ',') = 0 then '1069' end,
    case when position(',1070,' in ',' || coalesce(menu_ids, '') || ',') = 0 then '1070' end,
    case when position(',1071,' in ',' || coalesce(menu_ids, '') || ',') = 0 then '1071' end
)
where package_id = 1;

insert into sys_role_menu (role_id, menu_id)
select role.role_id, menus.menu_id
from sys_role role
inner join sys_tenant tenant on tenant.tenant_id = role.tenant_id
inner join sys_tenant_package package on package.package_id = tenant.package_id
cross join (values (7::int8), (125::int8), (1068::int8), (1069::int8), (1070::int8), (1071::int8)) menus(menu_id)
where role.role_key = 'admin'
  and role.del_flag = '0'
  and position(',125,' in ',' || coalesce(package.menu_ids, '') || ',') > 0
on conflict (role_id, menu_id) do nothing;

commit;

