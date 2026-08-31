-- 系统监控核心模块：在线用户、缓存管理
-- 可重复执行；只补充/更新菜单，不向普通角色自动授予全局监控权限。

insert into sys_menu (
    menu_id, menu_name, parent_id, order_num, path, component, query_param,
    is_frame, is_cache, menu_type, visible, status, perms, icon,
    create_dept, create_by, create_time, update_by, update_time, remark
) values (
    2, '系统监控', 0, 3, 'monitor', null, '',
    '1', '0', 'M', '0', '0', '', 'monitor',
    103, 1, now(), null, null, '系统监控目录'
)
on conflict (menu_id) do update set
    menu_name = excluded.menu_name,
    parent_id = excluded.parent_id,
    order_num = excluded.order_num,
    path = excluded.path,
    component = excluded.component,
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
) values
    (109, '在线用户', 2, 1, 'online', 'monitor/online/index', '',
     '1', '0', 'C', '0', '0', 'monitor:online:list', 'online',
     103, 1, now(), null, null, '在线用户菜单'),
    (113, '缓存管理', 2, 2, 'cache', 'monitor/cache/index', '',
     '1', '0', 'C', '0', '0', 'monitor:cache:list', 'redis',
     103, 1, now(), null, null, '缓存管理菜单')
on conflict (menu_id) do update set
    menu_name = excluded.menu_name,
    parent_id = excluded.parent_id,
    order_num = excluded.order_num,
    path = excluded.path,
    component = excluded.component,
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
    (1046, '在线查询', 109, 1, '#', '', '', '1', '0', 'F', '0', '0',
     'monitor:online:query', '#', 103, 1, now(), null, null, ''),
    (1047, '批量强退', 109, 2, '#', '', '', '1', '0', 'F', '0', '0',
     'monitor:online:batchLogout', '#', 103, 1, now(), null, null, ''),
    (1048, '单条强退', 109, 3, '#', '', '', '1', '0', 'F', '0', '0',
     'monitor:online:forceLogout', '#', 103, 1, now(), null, null, ''),
    (1630, '缓存查询', 113, 1, '#', '', '', '1', '0', 'F', '0', '0',
     'monitor:cache:list', '#', 103, 1, now(), null, null, ''),
    (1631, '缓存清理', 113, 2, '#', '', '', '1', '0', 'F', '0', '0',
     'monitor:cache:clear', '#', 103, 1, now(), null, null, '')
on conflict (menu_id) do update set
    menu_name = excluded.menu_name,
    parent_id = excluded.parent_id,
    order_num = excluded.order_num,
    perms = excluded.perms,
    status = excluded.status,
    update_by = 1,
    update_time = now();
