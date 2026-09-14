-- 示例租户初始化：XXX有限责任公司
-- 依赖 config/init/core.sql、config/init/apps/member.sql 和 partner.sql。
-- 租户号 000001；所有示例后台账号及示例会员的初始密码均为 666666。
-- 仅用于本地开发和演示环境，不要在生产环境执行。

begin;

-- XXX有限责任公司标准租户套餐：开放企业日常管理功能，不开放租户管理功能
with recursive package_menu as (
    select menu_id
    from sys_menu
    where menu_id in (1, 7)
    union all
    select menu.menu_id
    from sys_menu menu
    inner join package_menu parent on menu.parent_id = parent.menu_id
)
insert into sys_tenant_package (
    package_id, package_name, menu_ids, remark, menu_check_strictly, status,
    del_flag, create_dept, create_by, create_time, update_by, update_time
)
select 1, '标准企业套餐', string_agg(menu_id::varchar, ',' order by menu_id),
       'XXX有限责任公司企业标准套餐', true, '0', '0', 103, 1, now(), null, null
from package_menu
where menu_id not in (
    102, 1013, 1014, 1015, 1016,
    108, 500, 501, 1040, 1041, 1042, 1043, 1044, 1045, 1050,
    123, 1061, 1062, 1063, 1064, 1065,
    133, 1620, 1621, 1622, 1623
);

-- XXX有限责任公司租户主体
insert into sys_tenant (
    id, tenant_id, contact_user_name, contact_phone, company_name,
    license_number, address, intro, domain, remark, package_id, expire_time,
    account_count, status, del_flag, create_dept, create_by, create_time,
    update_by, update_time
)
values (
    2, '000001', '管理员', '15888888888', 'XXX有限责任公司',
    null, null, null, null, null, 1, null,
    -1, '0', '0', 103, 1, now(), null, null
);

-- 公司部门：总经办、技术部、市场部、财务部、人事行政部
insert into sys_dept (
    dept_id, tenant_id, parent_id, ancestors, dept_name, dept_category,
    order_num, leader, phone, email, status, del_flag, create_dept, create_by,
    create_time, update_by, update_time
)
values
    (2001, '000001', 0,    '0',         'XXX有限责任公司', null, 0, 2001, '15888888888', null, '0', '0', 103, 1, now(), null, null),
    (2002, '000001', 2001, '0,2001',    '总经办',          null, 1, null,  '15888888888', null, '0', '0', 103, 1, now(), null, null),
    (2003, '000001', 2001, '0,2001',    '技术部',          null, 2, 2002, '15888888888', null, '0', '0', 103, 1, now(), null, null),
    (2004, '000001', 2001, '0,2001',    '市场部',          null, 3, 2003, '15888888888', null, '0', '0', 103, 1, now(), null, null),
    (2005, '000001', 2001, '0,2001',    '财务部',          null, 4, 2004, '15888888888', null, '0', '0', 103, 1, now(), null, null),
    (2006, '000001', 2001, '0,2001',    '人事行政部',      null, 5, 2005, '15888888888', null, '0', '0', 103, 1, now(), null, null);

-- 公司岗位
insert into sys_post (
    post_id, tenant_id, dept_id, post_code, post_category, post_name,
    post_sort, status, create_dept, create_by, create_time, update_by,
    update_time, remark
)
values
    (2001, '000001', 2001, 'ceo',            null, '总经理',       1, '0', 103, 1, now(), null, null, ''),
    (2002, '000001', 2003, 'tech_manager',   null, '技术部经理',   2, '0', 103, 1, now(), null, null, ''),
    (2003, '000001', 2003, 'developer',      null, '开发工程师',   3, '0', 103, 1, now(), null, null, ''),
    (2004, '000001', 2004, 'sales_manager',  null, '市场部经理',   4, '0', 103, 1, now(), null, null, ''),
    (2005, '000001', 2005, 'accountant',     null, '财务专员',     5, '0', 103, 1, now(), null, null, ''),
    (2006, '000001', 2006, 'hr',             null, '人事专员',     6, '0', 103, 1, now(), null, null, '');

-- 公司角色：管理员、技术部经理、市场部经理、普通员工
insert into sys_role (
    role_id, tenant_id, role_name, role_key, role_sort, data_scope,
    menu_check_strictly, dept_check_strictly, status, del_flag, create_dept,
    create_by, create_time, update_by, update_time, remark
)
values
    (2001, '000001', '管理员',       'admin',         1, '1', true, true, '0', '0', 103, 1, now(), null, null, '租户管理员'),
    (2002, '000001', '技术部经理',   'tech_manager',  2, '4', true, true, '0', '0', 103, 1, now(), null, null, '技术部及以下数据权限'),
    (2003, '000001', '市场部经理',   'sales_manager', 3, '4', true, true, '0', '0', 103, 1, now(), null, null, '市场部及以下数据权限'),
    (2004, '000001', '普通员工',     'employee',     4, '5', true, true, '0', '0', 103, 1, now(), null, null, '仅本人数据权限');

-- 公司账号（所有初始化账号密码均为 666666）
insert into sys_user (
    user_id, tenant_id, dept_id, user_name, nick_name, user_type, email,
    phonenumber, sex, avatar, password, status, del_flag, login_ip, login_date,
    create_dept, create_by, create_time, update_by, update_time, remark
)
values
    (2001, '000001', 2001, 'admin',         '系统管理员',   'sys_user', 'admin@xxx.com',         '15888888888', '0', null, '$2a$10$b8yUzN0C71sbz.PhNOCgJe.Tu1yWC3RNrTyjSQ8p1W0.aaUXUJ.Ne', '0', '0', '127.0.0.1', now(), 103, 1, now(), null, null, '租户管理员'),
    (2002, '000001', 2003, 'tech_manager',  '技术部经理',   'sys_user', 'tech@xxx.com',          '15888888889', '0', null, '$2a$10$b8yUzN0C71sbz.PhNOCgJe.Tu1yWC3RNrTyjSQ8p1W0.aaUXUJ.Ne', '0', '0', '127.0.0.1', now(), 103, 1, now(), null, null, ''),
    (2003, '000001', 2004, 'sales_manager', '市场部经理',   'sys_user', 'sales@xxx.com',         '15888888890', '0', null, '$2a$10$b8yUzN0C71sbz.PhNOCgJe.Tu1yWC3RNrTyjSQ8p1W0.aaUXUJ.Ne', '0', '0', '127.0.0.1', now(), 103, 1, now(), null, null, ''),
    (2004, '000001', 2005, 'finance',       '财务专员',     'sys_user', 'finance@xxx.com',       '15888888891', '0', null, '$2a$10$b8yUzN0C71sbz.PhNOCgJe.Tu1yWC3RNrTyjSQ8p1W0.aaUXUJ.Ne', '0', '0', '127.0.0.1', now(), 103, 1, now(), null, null, ''),
    (2005, '000001', 2006, 'hr',            '人事专员',     'sys_user', 'hr@xxx.com',            '15888888892', '0', null, '$2a$10$b8yUzN0C71sbz.PhNOCgJe.Tu1yWC3RNrTyjSQ8p1W0.aaUXUJ.Ne', '0', '0', '127.0.0.1', now(), 103, 1, now(), null, null, ''),
    (2006, '000001', 2003, 'developer',     '开发工程师',   'sys_user', 'developer@xxx.com',     '15888888893', '0', null, '$2a$10$b8yUzN0C71sbz.PhNOCgJe.Tu1yWC3RNrTyjSQ8p1W0.aaUXUJ.Ne', '0', '0', '127.0.0.1', now(), 103, 1, now(), null, null, '');

-- 示例 C 端会员（密码 666666）
insert into app_member (
    member_id, tenant_id, username, nickname, mobile, email, avatar, password,
    status, del_flag, register_source, login_ip, login_date, remark,
    create_dept, create_by, create_time, update_by, update_time
) values (
    2001, '000001', 'example_member', '示例会员', '13900000001',
    'member@xxx.com', null,
    '$2a$10$b8yUzN0C71sbz.PhNOCgJe.Tu1yWC3RNrTyjSQ8p1W0.aaUXUJ.Ne',
    '0', '0', 'password', '127.0.0.1', now(), 'XXX有限责任公司示例会员',
    2001, 2001, now(), null, null
);

-- 示例合作客户：该商户归 XXX有限责任公司租户所有
insert into app_partner (
    partner_id, tenant_id, partner_code, partner_name, credit_code,
    contact_name, contact_phone, contact_email, address, status, del_flag,
    remark, create_dept, create_by, create_time, update_by, update_time
) values (
    2001, '000001', 'PARTNER001', '示例合作商户', '91440101MA00000001',
    '张经理', '13700000001', 'partner@xxx.com', '广州市天河区示例路 1 号',
    '0', '0', 'XXX有限责任公司维护的示例合作客户',
    2001, 2001, now(), null, null
);

-- 账号与角色、岗位关联
insert into sys_user_role (user_id, role_id) values
    (2001, 2001),
    (2002, 2002),
    (2003, 2003),
    (2004, 2004),
    (2005, 2004),
    (2006, 2004);

insert into sys_user_post (user_id, post_id) values
    (2001, 2001),
    (2002, 2002),
    (2003, 2004),
    (2004, 2005),
    (2005, 2006),
    (2006, 2003);

insert into sys_role_dept (role_id, dept_id) values
    (2001, 2001),
    (2002, 2003),
    (2003, 2004);

-- 管理员拥有套餐内全部企业管理权限
with recursive package_menu as (
    select menu_id
    from sys_menu
    where menu_id in (1, 7)
    union all
    select menu.menu_id
    from sys_menu menu
    inner join package_menu parent on menu.parent_id = parent.menu_id
)
insert into sys_role_menu (role_id, menu_id)
select 2001, menu_id
from package_menu
where menu_id not in (
    102, 1013, 1014, 1015, 1016,
    108, 500, 501, 1040, 1041, 1042, 1043, 1044, 1045, 1050,
    123, 1061, 1062, 1063, 1064, 1065,
    133, 1620, 1621, 1622, 1623
);

-- 部门经理仅可查看和维护本部门业务数据
insert into sys_role_menu (role_id, menu_id)
select role_id, menu_id
from (values (2002::int8), (2003::int8)) roles(role_id)
cross join (values
    (1::int8), (100::int8), (1001::int8), (1003::int8),
    (103::int8), (1017::int8), (1019::int8),
    (104::int8), (1021::int8),
    (107::int8), (1036::int8)
) menus(menu_id);

-- 复制默认租户字典和参数配置，保持租户数据隔离
insert into sys_dict_type (
    dict_id, tenant_id, dict_name, dict_type, create_dept, create_by,
    create_time, update_by, update_time, remark
)
select 100000 + dict_id, '000001', dict_name, dict_type, 103, 1,
       now(), null, null, remark
from sys_dict_type
where tenant_id = '000000';

insert into sys_dict_data (
    dict_code, tenant_id, dict_sort, dict_label, dict_value, dict_type,
    css_class, list_class, is_default, create_dept, create_by, create_time,
    update_by, update_time, remark
)
select 200000 + dict_code, '000001', dict_sort, dict_label, dict_value, dict_type,
       css_class, list_class, is_default, 103, 1, now(), null, null, remark
from sys_dict_data
where tenant_id = '000000';

insert into sys_config (
    config_id, tenant_id, config_name, config_key, config_value, config_type,
    create_dept, create_by, create_time, update_by, update_time, remark
)
select 300000 + config_id, '000001', config_name, config_key, config_value,
       config_type, 103, 1, now(), null, null, remark
from sys_config
where tenant_id = '000000';

commit;
