-- 租户套餐管理模块
-- 可重复执行；补齐套餐表、租户管理目录、套餐菜单及按钮权限。

begin;

create table if not exists sys_tenant_package
(
    package_id          int8,
    package_name        varchar(20)   default ''::varchar,
    menu_ids            varchar(3000) default ''::varchar,
    remark              varchar(200)  default ''::varchar,
    menu_check_strictly bool          default true,
    status              char          default '0'::bpchar,
    del_flag            char          default '0'::bpchar,
    create_dept         int8,
    create_by           int8,
    create_time         timestamp,
    update_by           int8,
    update_time         timestamp,
    constraint pk_sys_tenant_package primary key (package_id)
);

comment on table sys_tenant_package is '租户套餐表';
comment on column sys_tenant_package.package_id is '租户套餐id';
comment on column sys_tenant_package.package_name is '套餐名称';
comment on column sys_tenant_package.menu_ids is '关联菜单id';
comment on column sys_tenant_package.remark is '备注';
comment on column sys_tenant_package.menu_check_strictly is '菜单树选择项是否父子关联';
comment on column sys_tenant_package.status is '状态（0正常 1停用）';
comment on column sys_tenant_package.del_flag is '删除标志（0代表存在 1代表删除）';

alter table sys_tenant
    add column if not exists package_id int8;

comment on column sys_tenant.package_id is '租户套餐编号';

insert into sys_menu (
    menu_id, menu_name, parent_id, order_num, path, component, query_param,
    is_frame, is_cache, menu_type, visible, status, perms, icon,
    create_dept, create_by, create_time, update_by, update_time, remark
) values (
    6, '租户管理', 0, 2, 'tenant', null, '',
    '1', '0', 'M', '0', '0', '', 'chart',
    103, 1, now(), null, null, '租户管理目录'
)
on conflict (menu_id) do update set
    menu_name = excluded.menu_name,
    parent_id = excluded.parent_id,
    order_num = excluded.order_num,
    path = excluded.path,
    menu_type = excluded.menu_type,
    visible = excluded.visible,
    status = excluded.status,
    icon = excluded.icon,
    update_by = 1,
    update_time = now(),
    remark = excluded.remark;

insert into sys_menu (
    menu_id, menu_name, parent_id, order_num, path, component, query_param,
    is_frame, is_cache, menu_type, visible, status, perms, icon,
    create_dept, create_by, create_time, update_by, update_time, remark
) values (
    122, '租户套餐管理', 6, 2, 'tenantPackage', 'tenant/tenantPackage/index', '',
    '1', '0', 'C', '0', '0', 'system:tenantPackage:list', 'form',
    103, 1, now(), null, null, '租户套餐管理菜单'
)
on conflict (menu_id) do update set
    menu_name = excluded.menu_name,
    parent_id = excluded.parent_id,
    order_num = excluded.order_num,
    path = excluded.path,
    component = excluded.component,
    menu_type = excluded.menu_type,
    visible = excluded.visible,
    status = excluded.status,
    perms = excluded.perms,
    icon = excluded.icon,
    update_by = 1,
    update_time = now(),
    remark = excluded.remark;

insert into sys_menu (
    menu_id, menu_name, parent_id, order_num, path, component, query_param,
    is_frame, is_cache, menu_type, visible, status, perms, icon,
    create_dept, create_by, create_time, update_by, update_time, remark
) values
    (1611, '租户套餐查询', 122, 1, '#', '', '', '1', '0', 'F', '0', '0',
     'system:tenantPackage:query', '#', 103, 1, now(), null, null, ''),
    (1612, '租户套餐新增', 122, 2, '#', '', '', '1', '0', 'F', '0', '0',
     'system:tenantPackage:add', '#', 103, 1, now(), null, null, ''),
    (1613, '租户套餐修改', 122, 3, '#', '', '', '1', '0', 'F', '0', '0',
     'system:tenantPackage:edit', '#', 103, 1, now(), null, null, ''),
    (1614, '租户套餐删除', 122, 4, '#', '', '', '1', '0', 'F', '0', '0',
     'system:tenantPackage:remove', '#', 103, 1, now(), null, null, ''),
    (1615, '租户套餐导出', 122, 5, '#', '', '', '1', '0', 'F', '0', '0',
     'system:tenantPackage:export', '#', 103, 1, now(), null, null, '')
on conflict (menu_id) do update set
    menu_name = excluded.menu_name,
    parent_id = excluded.parent_id,
    order_num = excluded.order_num,
    perms = excluded.perms,
    status = excluded.status,
    update_by = 1,
    update_time = now();

commit;
