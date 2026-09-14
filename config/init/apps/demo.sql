-- 可选的历史数据权限演示表
-- 当前代码库没有对应 Controller、Mapper 或前端页面，默认初始化入口不会执行本文件。
-- 仅在需要保留旧版代码生成/数据权限示例时，手工对空库执行。

begin;

create table if not exists test_demo
(
    id          int8,
    tenant_id   varchar(20)     default '000000',
    dept_id     int8,
    user_id     int8,
    order_num   int4            default 0,
    test_key    varchar(255),
    value       varchar(255),
    version     int4            default 0,
    create_dept int8,
    create_time timestamp,
    create_by   int8,
    update_time timestamp,
    update_by   int8,
    del_flag    int4            default 0,
    constraint pk_test_demo primary key (id)
);

comment on table test_demo is '测试单表';
comment on column test_demo.id is '主键';
comment on column test_demo.tenant_id is '租户编号';
comment on column test_demo.dept_id is '部门id';
comment on column test_demo.user_id is '用户id';
comment on column test_demo.order_num is '排序号';
comment on column test_demo.test_key is 'key键';
comment on column test_demo.value is '值';
comment on column test_demo.version is '版本';
comment on column test_demo.create_dept  is '创建部门';
comment on column test_demo.create_time is '创建时间';
comment on column test_demo.create_by is '创建人';
comment on column test_demo.update_time is '更新时间';
comment on column test_demo.update_by is '更新人';
comment on column test_demo.del_flag is '删除标志';

create table if not exists test_tree
(
    id          int8,
    tenant_id   varchar(20)     default '000000',
    parent_id   int8            default 0,
    dept_id     int8,
    user_id     int8,
    tree_name   varchar(255),
    version     int4            default 0,
    create_dept int8,
    create_time timestamp,
    create_by   int8,
    update_time timestamp,
    update_by   int8,
    del_flag    integer         default 0,
    constraint pk_test_tree primary key (id)
);

comment on table test_tree is '测试树表';
comment on column test_tree.id is '主键';
comment on column test_tree.tenant_id is '租户编号';
comment on column test_tree.parent_id is '父id';
comment on column test_tree.dept_id is '部门id';
comment on column test_tree.user_id is '用户id';
comment on column test_tree.tree_name is '值';
comment on column test_tree.version is '版本';
comment on column test_tree.create_dept  is '创建部门';
comment on column test_tree.create_time is '创建时间';
comment on column test_tree.create_by is '创建人';
comment on column test_tree.update_time is '更新时间';
comment on column test_tree.update_by is '更新人';
comment on column test_tree.del_flag is '删除标志';

INSERT INTO test_demo VALUES (1, '000000', 102, 4, 1, '测试数据权限', '测试', 0, 103, now(), 1, NULL, NULL, 0);
INSERT INTO test_demo VALUES (2, '000000', 102, 3, 2, '子节点1', '111', 0, 103, now(), 1, NULL, NULL, 0);
INSERT INTO test_demo VALUES (3, '000000', 102, 3, 3, '子节点2', '222', 0, 103, now(), 1, NULL, NULL, 0);
INSERT INTO test_demo VALUES (4, '000000', 108, 4, 4, '测试数据', 'demo', 0, 103, now(), 1, NULL, NULL, 0);
INSERT INTO test_demo VALUES (5, '000000', 108, 3, 13, '子节点11', '1111', 0, 103, now(), 1, NULL, NULL, 0);
INSERT INTO test_demo VALUES (6, '000000', 108, 3, 12, '子节点22', '2222', 0, 103, now(), 1, NULL, NULL, 0);
INSERT INTO test_demo VALUES (7, '000000', 108, 3, 11, '子节点33', '3333', 0, 103, now(), 1, NULL, NULL, 0);
INSERT INTO test_demo VALUES (8, '000000', 108, 3, 10, '子节点44', '4444', 0, 103, now(), 1, NULL, NULL, 0);
INSERT INTO test_demo VALUES (9, '000000', 108, 3, 9, '子节点55', '5555', 0, 103, now(), 1, NULL, NULL, 0);
INSERT INTO test_demo VALUES (10, '000000', 108, 3, 8, '子节点66', '6666', 0, 103, now(), 1, NULL, NULL, 0);
INSERT INTO test_demo VALUES (11, '000000', 108, 3, 7, '子节点77', '7777', 0, 103, now(), 1, NULL, NULL, 0);
INSERT INTO test_demo VALUES (12, '000000', 108, 3, 6, '子节点88', '8888', 0, 103, now(), 1, NULL, NULL, 0);
INSERT INTO test_demo VALUES (13, '000000', 108, 3, 5, '子节点99', '9999', 0, 103, now(), 1, NULL, NULL, 0);

INSERT INTO test_tree VALUES (1, '000000', 0, 102, 4, '测试数据权限', 0, 103, now(), 1, NULL, NULL, 0);
INSERT INTO test_tree VALUES (2, '000000', 1, 102, 3, '子节点1', 0, 103, now(), 1, NULL, NULL, 0);
INSERT INTO test_tree VALUES (3, '000000', 2, 102, 3, '子节点2', 0, 103, now(), 1, NULL, NULL, 0);
INSERT INTO test_tree VALUES (4, '000000', 0, 108, 4, '测试树1', 0, 103, now(), 1, NULL, NULL, 0);
INSERT INTO test_tree VALUES (5, '000000', 4, 108, 3, '子节点11', 0, 103, now(), 1, NULL, NULL, 0);
INSERT INTO test_tree VALUES (6, '000000', 4, 108, 3, '子节点22', 0, 103, now(), 1, NULL, NULL, 0);
INSERT INTO test_tree VALUES (7, '000000', 4, 108, 3, '子节点33', 0, 103, now(), 1, NULL, NULL, 0);
INSERT INTO test_tree VALUES (8, '000000', 5, 108, 3, '子节点44', 0, 103, now(), 1, NULL, NULL, 0);
INSERT INTO test_tree VALUES (9, '000000', 6, 108, 3, '子节点55', 0, 103, now(), 1, NULL, NULL, 0);
INSERT INTO test_tree VALUES (10, '000000', 7, 108, 3, '子节点66', 0, 103, now(), 1, NULL, NULL, 0);
INSERT INTO test_tree VALUES (11, '000000', 7, 108, 3, '子节点77', 0, 103, now(), 1, NULL, NULL, 0);
INSERT INTO test_tree VALUES (12, '000000', 10, 108, 3, '子节点88', 0, 103, now(), 1, NULL, NULL, 0);
INSERT INTO test_tree VALUES (13, '000000', 10, 108, 3, '子节点99', 0, 103, now(), 1, NULL, NULL, 0);

commit;
