-- Admin 监控与 SnailJob 调度中心菜单。
-- 可重复执行；控制台均有独立认证，不向普通角色自动授权。

begin;

insert into sys_menu (
    menu_id, menu_name, parent_id, order_num, path, component, query_param,
    is_frame, is_cache, menu_type, visible, status, perms, icon,
    create_dept, create_by, create_time, update_by, update_time, remark
) values
    (117, 'Admin监控', 2, 5, 'admin', 'monitor/admin/index', '',
     '1', '0', 'C', '0', '0', 'monitor:admin:list', 'dashboard',
     103, 1, now(), null, null, 'Admin监控菜单'),
    (120, '任务调度中心', 2, 6, 'snailjob', 'monitor/snailjob/index', '',
     '1', '0', 'C', '0', '0', 'monitor:snailjob:list', 'job',
     103, 1, now(), null, null, 'SnailJob控制台菜单')
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

commit;
