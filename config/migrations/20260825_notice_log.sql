-- 通知公告与日志管理模块迁移
-- 现有 sys_notice、sys_oper_log、sys_logininfor 表及权限菜单已由 init.sql 创建，
-- 本迁移仅将日志菜单组件路径调整为当前前端的文件路由结构。

begin;

update sys_menu
set component = 'system/log/operlog/index',
    update_time = now()
where menu_id = 500
  and component is distinct from 'system/log/operlog/index';

update sys_menu
set component = 'system/log/logininfor/index',
    update_time = now()
where menu_id = 501
  and component is distinct from 'system/log/logininfor/index';

commit;
