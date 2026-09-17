# 设备配置社区实施方案

## 1. 目标与边界

在现有 Zero Admin、Consumer Web 与 Web Hub 上实现一个面向设备配置的社区闭环：用户登录后可以发布、浏览、收藏、下载并导入设备配置；后台可以审核、精选和下架内容。

首期只管理结构化配置，不把固件、PCB、财务报表等大文件塞进社区配置表。后续文件中心复用现有 OSS 能力，在线文档编辑再单独接入 ONLYOFFICE 等服务。

Web Hub 当前使用的公开应用标识：

```text
app_33ca1942d6d24f62b089bc1a4e886343
```

## 2. 已确认的领域协议

社区配置只允许以下四种 `kind`：

```ts
type CommunityAssetKind = 'dpi' | 'macro' | 'lighting' | 'profile'
```

- `lighting` 不再拆成鼠标灯光、键盘灯光等冗余类型，使用 `deviceType` 区分。
- `profile` 是可组合配置，可包含或引用 DPI、宏、灯光等能力。
- `deviceType` 独立表达 `mouse | keyboard | gamepad | headset | speaker | any`。
- 发布版本不可原地覆盖；修改已发布内容时创建新版本，以便回滚、审计与兼容性判断。

## 3. 总体架构

| 项目 | 责任 |
| --- | --- |
| `vue-zero-admin-api/zero-community` | 社区领域、JSON 校验、版本、收藏、下载计数、审核状态机 |
| `vue-zero-admin-api/zero-consumer-web` | C 端社区接口；从会员会话取得租户、应用和作者身份 |
| `vue-zero-admin-api/zero-admin` | 后台审核、精选、下架接口 |
| `vue-zero-admin` | 社区审核列表与详情 JSON 预览 |
| `web-hub` | 社区浏览、发布、收藏、下载，以及配置导入本地 Profile |

数据继续使用 PostgreSQL：可搜索、排序和授权的字段用普通列；版本配置内容使用 `jsonb`。当前无需 MongoDB。

所有社区查询至少按 `tenant_id + application_id` 隔离。C 端的 `applicationId`、会员 ID 与租户 ID只能来自受信任的登录会话，不能由请求体指定。

## 4. 核心数据模型

### `community_asset`

社区内容聚合根：标题、摘要、封面、`kind`、`device_type`、可见性、审核状态、当前发布版本、精选标记、收藏数与下载数。

### `community_asset_release`

不可变发布版本：版本号、协议版本、来源平台/产品/能力版本、`jsonb payload`、哈希、大小、更新说明和发布时间。

### `community_asset_compatibility`

发布版本兼容范围：平台、产品、设备类型和能力版本。首期下载/导入前按精确产品匹配，避免错误配置写入硬件。

### `community_favorite`

会员收藏关系，唯一键为租户、应用、内容和会员。收藏接口保持幂等。

## 5. 状态机

```text
draft -> pending -> published
                 -> rejected
published -> hidden
rejected -> draft (作者修改后重新提交)
```

- 作者只能编辑自己的 `draft/rejected` 内容。
- `pending` 等待后台审核。
- 只有 `published` 会出现在社区公开列表。
- 下架使用 `hidden`，保留历史与审计记录。

## 6. 接口设计

### Web Hub / C 端

- `GET /app/community/feed`：已发布内容分页、搜索和筛选。
- `GET /app/community/assets/{assetId}`：内容和当前版本详情。
- `GET /app/community/me/assets`：我的发布。
- `GET /app/community/me/favorites`：我的收藏。
- `POST /app/community/assets`：创建草稿及首个版本。
- `PUT /app/community/assets/{assetId}`：修改自己的草稿。
- `POST /app/community/assets/{assetId}/submit`：提交审核。
- `POST /app/community/assets/{assetId}/favorite` / `DELETE ...`：收藏/取消收藏。
- `POST /app/community/releases/{releaseId}/download`：记录下载并返回可导入版本。

### 管理后台

- `GET /app/community/list`：按租户、应用、类型、设备和状态筛选。
- `GET /app/community/{assetId}`：审核详情。
- `PUT /app/community/{assetId}/review`：通过或驳回。
- `PUT /app/community/{assetId}/feature`：精选/取消精选。
- `PUT /app/community/{assetId}/hide`：下架。

权限串统一为 `app:community:list/query/review/feature/hide`。

## 7. 配置校验与安全

- 请求只接受 `dpi | macro | lighting | profile`，灯光类型通过 `deviceType` 区分。
- `payload` 必须是 JSON 对象，单版本首期限制为 1 MiB。
- DPI 校验档位数组、索引、数量和数值范围。
- 宏动作必须来自 Web Hub `MacroAction` 白名单，并限制动作数量、延迟和循环次数。
- 灯光校验模式、亮度、速度、方向和颜色格式。
- Profile 校验子项/引用的 kind、设备范围和协议版本。
- 下载后仍由 Web Hub 做一次本地协议和设备兼容校验；不兼容时不允许直接写设备。
- 标题、摘要等展示字段不信任 HTML；后台 JSON 只以纯文本预览。

## 8. 分阶段交付

### 阶段一：社区最小闭环

完成 `zero-community`、数据库迁移、C 端接口、后台审核页与 Web Hub 社区页；打通“本地预设 -> 发布 -> 审核 -> 下载 -> 导入为云端预设”。首期 UI 以 DPI 为主要实测对象，但协议完整保留四种 kind。

### 阶段二：能力完善

补充宏、灯光和组合 Profile 的专用发布表单、严格 JSON Schema、版本对比、兼容设备矩阵、标签与举报。

### 阶段三：文件中心

在社区之外建立统一文件元数据与版本模块，二进制继续走 OSS/COS/S3。覆盖固件、PCB/Gerber、BOM、生产资料、财务附件，使用业务对象引用文件，不在数据库保存大文件。

### 阶段四：在线编辑和工程协作

- 文档/表格：接入 ONLYOFFICE，增加编辑锁、版本快照和权限回调。
- PCB/固件：建立工程版本、构建产物、审批与发布流水线，不与社区 Profile JSON 混为一个模型。
- 财务文件：单独权限域、审计日志、水印和下载控制。

## 9. 首期验收链路

1. Web Hub 用户通过现有弹窗登录。
2. 选择本地 DPI 预设并创建社区草稿，提交审核。
3. 后台在社区管理页查看 JSON 与兼容信息，通过审核并可设为精选。
4. 另一会员浏览、收藏并下载内容。
5. Web Hub 校验 `kind/deviceType/productCode/schemaVersion` 后导入为本地可编辑副本；记录来源内容与版本 ID。
6. 不兼容设备、越权编辑、重复收藏和非法 JSON 均被拒绝或幂等处理。

