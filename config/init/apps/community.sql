-- 设备配置社区：内容、不可变版本、兼容范围与收藏。

begin;

create table if not exists community_asset
(
    asset_id           int8          not null,
    tenant_id          varchar(20)   default '000000'::varchar not null,
    application_id     int8          not null,
    owner_member_id    int8          not null,
    title              varchar(120)  not null,
    summary            varchar(1000),
    cover_url          varchar(500),
    kind               varchar(20)   not null,
    device_type        varchar(20)   not null,
    visibility         varchar(20)   default 'public'::varchar not null,
    status             varchar(20)   default 'draft'::varchar not null,
    current_release_id int8,
    featured           char          default '0'::bpchar not null,
    favorite_count     int8          default 0 not null,
    download_count     int8          default 0 not null,
    published_at       timestamp,
    reject_reason      varchar(500),
    del_flag           char          default '0'::bpchar not null,
    create_dept        int8,
    create_by          int8,
    create_time        timestamp,
    update_by          int8,
    update_time        timestamp,
    constraint pk_community_asset primary key (asset_id),
    constraint ck_community_asset_kind check (kind in ('dpi', 'macro', 'lighting', 'profile')),
    constraint ck_community_asset_device check (device_type in ('mouse', 'keyboard', 'gamepad', 'headset', 'speaker', 'any')),
    constraint ck_community_asset_status check (status in ('draft', 'pending', 'published', 'rejected', 'hidden')),
    constraint ck_community_asset_visibility check (visibility in ('public', 'private'))
);

create index if not exists idx_community_asset_feed
    on community_asset (tenant_id, application_id, status, featured desc, published_at desc)
    where del_flag = '0';
create index if not exists idx_community_asset_owner
    on community_asset (tenant_id, application_id, owner_member_id, create_time desc)
    where del_flag = '0';
create index if not exists idx_community_asset_kind_device
    on community_asset (tenant_id, application_id, kind, device_type)
    where del_flag = '0';

create table if not exists community_asset_release
(
    release_id               int8          not null,
    tenant_id                varchar(20)   default '000000'::varchar not null,
    application_id           int8          not null,
    asset_id                 int8          not null,
    version_no               int4          not null,
    schema_version           int4          not null,
    source_platform_code     varchar(64)   not null,
    source_product_code      varchar(128)  not null,
    source_capability_version varchar(64),
    payload                  jsonb         not null,
    payload_hash             varchar(64)   not null,
    payload_size             int4          not null,
    changelog                varchar(500),
    published_at             timestamp,
    del_flag                 char          default '0'::bpchar not null,
    create_dept              int8,
    create_by                int8,
    create_time              timestamp,
    update_by                int8,
    update_time              timestamp,
    constraint pk_community_asset_release primary key (release_id)
);

create unique index if not exists uk_community_release_version
    on community_asset_release (tenant_id, application_id, asset_id, version_no)
    where del_flag = '0';
create index if not exists idx_community_release_asset
    on community_asset_release (tenant_id, application_id, asset_id, create_time desc)
    where del_flag = '0';

create table if not exists community_asset_compatibility
(
    compatibility_id  int8          not null,
    tenant_id         varchar(20)   default '000000'::varchar not null,
    application_id    int8          not null,
    release_id        int8          not null,
    platform_code     varchar(64)   not null,
    product_code      varchar(128)  not null,
    device_type       varchar(20)   not null,
    capability_version varchar(64),
    del_flag          char          default '0'::bpchar not null,
    create_dept       int8,
    create_by         int8,
    create_time       timestamp,
    update_by         int8,
    update_time       timestamp,
    constraint pk_community_asset_compatibility primary key (compatibility_id)
);

create unique index if not exists uk_community_compatibility
    on community_asset_compatibility (tenant_id, application_id, release_id, platform_code, product_code, device_type)
    where del_flag = '0';

create table if not exists community_favorite
(
    favorite_id   int8          not null,
    tenant_id     varchar(20)   default '000000'::varchar not null,
    application_id int8         not null,
    asset_id      int8          not null,
    member_id     int8          not null,
    del_flag      char          default '0'::bpchar not null,
    create_dept   int8,
    create_by     int8,
    create_time   timestamp,
    update_by     int8,
    update_time   timestamp,
    constraint pk_community_favorite primary key (favorite_id)
);

create unique index if not exists uk_community_favorite_member
    on community_favorite (tenant_id, application_id, asset_id, member_id)
    where del_flag = '0';
create index if not exists idx_community_favorite_member_created
    on community_favorite (tenant_id, application_id, member_id, create_time desc)
    where del_flag = '0';

comment on table community_asset is '设备配置社区内容';
comment on table community_asset_release is '设备配置社区不可变发布版本';
comment on table community_asset_compatibility is '社区配置版本兼容范围';
comment on table community_favorite is '社区会员收藏关系';
comment on column community_asset.kind is 'dpi/macro/lighting/profile；lighting 由 device_type 区分设备';
comment on column community_asset_release.payload is '经白名单校验的结构化设备配置 JSON';

insert into sys_menu values
    ('128', '配置社区', '7', '4', 'community', 'app/community/index', '', '1', '0', 'C', '0', '0',
     'app:community:list', 'el-connection', 103, 1, now(), null, null, '设备配置社区审核与运营')
on conflict (menu_id) do update set
    menu_name = excluded.menu_name,
    parent_id = excluded.parent_id,
    order_num = excluded.order_num,
    path = excluded.path,
    component = excluded.component,
    perms = excluded.perms,
    icon = excluded.icon,
    update_by = 1,
    update_time = now(),
    remark = excluded.remark;

insert into sys_menu values
    ('1084', '社区列表', '128', '1', '#', '', '', '1', '0', 'F', '0', '0', 'app:community:list', '#', 103, 1, now(), null, null, ''),
    ('1085', '社区详情', '128', '2', '#', '', '', '1', '0', 'F', '0', '0', 'app:community:query', '#', 103, 1, now(), null, null, ''),
    ('1086', '社区审核', '128', '3', '#', '', '', '1', '0', 'F', '0', '0', 'app:community:review', '#', 103, 1, now(), null, null, ''),
    ('1087', '社区精选', '128', '4', '#', '', '', '1', '0', 'F', '0', '0', 'app:community:feature', '#', 103, 1, now(), null, null, ''),
    ('1088', '社区下架', '128', '5', '#', '', '', '1', '0', 'F', '0', '0', 'app:community:hide', '#', 103, 1, now(), null, null, '')
on conflict (menu_id) do update set
    menu_name = excluded.menu_name,
    parent_id = excluded.parent_id,
    order_num = excluded.order_num,
    perms = excluded.perms,
    status = excluded.status,
    update_by = 1,
    update_time = now();

insert into sys_role_menu (role_id, menu_id) values
    (3, 128), (3, 1084), (3, 1085), (3, 1086), (3, 1087), (3, 1088)
on conflict (role_id, menu_id) do nothing;

update sys_tenant_package
set menu_ids = concat_ws(',',
    nullif(menu_ids, ''),
    case when position(',128,' in ',' || coalesce(menu_ids, '') || ',') = 0 then '128' end,
    case when position(',1084,' in ',' || coalesce(menu_ids, '') || ',') = 0 then '1084' end,
    case when position(',1085,' in ',' || coalesce(menu_ids, '') || ',') = 0 then '1085' end,
    case when position(',1086,' in ',' || coalesce(menu_ids, '') || ',') = 0 then '1086' end,
    case when position(',1087,' in ',' || coalesce(menu_ids, '') || ',') = 0 then '1087' end,
    case when position(',1088,' in ',' || coalesce(menu_ids, '') || ',') = 0 then '1088' end
);

commit;
