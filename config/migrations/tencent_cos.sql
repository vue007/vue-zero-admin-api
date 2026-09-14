-- 腾讯云 COS / OSS 文件管理接入
-- 本迁移不包含任何云密钥。执行后在“系统管理 -> 文件管理 -> 配置管理”中新增腾讯云 COS 配置。

begin;

create table if not exists sys_oss
(
    oss_id        int8,
    tenant_id     varchar(20)  default '000000'::varchar,
    file_name     varchar(255) not null,
    original_name varchar(255) not null,
    file_suffix   varchar(20)  default ''::varchar,
    url           varchar(500) not null,
    create_dept   int8,
    create_by     int8,
    create_time   timestamp,
    update_by     int8,
    update_time   timestamp,
    service       varchar(100) not null,
    constraint sys_oss_pk primary key (oss_id)
);

create index if not exists idx_sys_oss_tenant_create_time on sys_oss (tenant_id, create_time desc);
create index if not exists idx_sys_oss_service on sys_oss (service);

create table if not exists sys_oss_config
(
    oss_config_id int8,
    config_key    varchar(100) not null,
    access_key    varchar(255) not null,
    secret_key    varchar(255) not null,
    bucket_name   varchar(255) not null,
    prefix        varchar(255) default ''::varchar,
    endpoint      varchar(255) not null,
    domain        varchar(255) default ''::varchar,
    is_https      char(1)      default 'Y'::bpchar,
    region        varchar(100) default ''::varchar,
    access_policy char(1)      default '0'::bpchar,
    status        char(1)      default '1'::bpchar,
    ext1          varchar(255) default ''::varchar,
    create_dept   int8,
    create_by     int8,
    create_time   timestamp,
    update_by     int8,
    update_time   timestamp,
    remark        varchar(500),
    constraint sys_oss_config_pk primary key (oss_config_id)
);

create unique index if not exists uk_sys_oss_config_key on sys_oss_config (config_key);

insert into sys_menu (
    menu_id, menu_name, parent_id, order_num, path, component, query_param,
    is_frame, is_cache, menu_type, visible, status, perms, icon,
    create_dept, create_by, create_time, update_by, update_time, remark
)
values (133, '文件配置管理', 1, 10, 'oss/config', 'system/oss/config', '',
        '1', '0', 'C', '1', '0', 'system:ossConfig:list', '#',
        103, 1, now(), null, null, '文件管理隐藏子页面')
on conflict (menu_id) do update set
    menu_name = excluded.menu_name,
    parent_id = excluded.parent_id,
    order_num = excluded.order_num,
    path = excluded.path,
    component = excluded.component,
    visible = '1',
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
)
select permission.menu_id, permission.menu_name, 118, permission.order_num, '#', '', '',
       '1', '0', 'F', '0', '0', permission.perms, '#',
       103, 1, now(), null, null, ''
from (values
    (1600::int8, '文件查询', 1, 'system:oss:query'),
    (1601::int8, '文件上传', 2, 'system:oss:upload'),
    (1602::int8, '文件下载', 3, 'system:oss:download'),
    (1603::int8, '文件删除', 4, 'system:oss:remove'),
    (1620::int8, '配置列表', 5, 'system:ossConfig:list'),
    (1621::int8, '配置添加', 6, 'system:ossConfig:add'),
    (1622::int8, '配置编辑', 7, 'system:ossConfig:edit'),
    (1623::int8, '配置删除', 8, 'system:ossConfig:remove')
) as permission(menu_id, menu_name, order_num, perms)
where not exists (select 1 from sys_menu where sys_menu.menu_id = permission.menu_id);

commit;
