-- 将已经执行过旧版会员迁移的数据库调整为独立的 App 管理菜单，
-- 同时把曾被误改成“合作客户管理”的系统用户菜单恢复为“用户管理”。
-- 可重复执行；仅调整菜单层级及补齐已有授权的父目录权限。

begin;

insert into sys_menu values
    ('7', 'App管理', '0', '3', 'app', null, '', '1', '0', 'M', '0', '0',
     '', 'el-cellphone', 103, 1, now(), null, null, 'C端及App功能管理目录')
on conflict (menu_id) do update set
    menu_name = excluded.menu_name,
    parent_id = excluded.parent_id,
    order_num = excluded.order_num,
    path = excluded.path,
    component = excluded.component,
    icon = excluded.icon,
    remark = excluded.remark;

update sys_menu set order_num = 4 where menu_id = 2;

update sys_menu set
    menu_name = '用户管理',
    remark = '用户管理菜单'
where menu_id = 100;

update sys_config set
    config_name = '用户管理-账号初始密码'
where config_key = 'sys.user.initPassword';

update sys_menu set
    menu_name = '会员管理',
    parent_id = 7,
    order_num = 1,
    path = 'member',
    component = 'app/member/index',
    perms = 'app:member:list',
    icon = 'ze-users',
    remark = '会员管理菜单'
where menu_id = 124;

update sys_menu set
    menu_name = '会员查询',
    perms = 'app:member:query'
where menu_id = 1066;

update sys_menu set
    menu_name = '会员状态修改',
    perms = 'app:member:edit'
where menu_id = 1067;

-- 已经拥有 C 端用户菜单的角色同时获得 App 管理父目录。
insert into sys_role_menu (role_id, menu_id)
select role_id, 7
from sys_role_menu
where menu_id = 124
on conflict (role_id, menu_id) do nothing;

-- 已经包含 C 端用户菜单的套餐补上父目录，保证授权树完整。
update sys_tenant_package
set menu_ids = concat_ws(',', nullif(menu_ids, ''), '7')
where position(',124,' in ',' || coalesce(menu_ids, '') || ',') > 0
  and position(',7,' in ',' || coalesce(menu_ids, '') || ',') = 0;

-- 旧库的默认企业套餐可能还没有包含会员迁移新增的菜单，统一幂等补齐。
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
