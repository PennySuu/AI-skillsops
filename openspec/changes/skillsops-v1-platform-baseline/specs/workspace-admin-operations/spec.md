# 能力：工作台与管理端运营（workspace-admin-operations）

## ADDED Requirements

### Requirement: 工作台信息架构与路由

前端 SHALL 提供工作台布局：侧边导航包含「我的发布」「我的安装」；管理员可见「待审核」「分类管理」「运营统计」（与产品 Page Map 一致）；路由 MUST 与《前端技术评审》路由表对齐，包括但不限于：

- `/workspace/published` 我的发布
- `/workspace/installed` 我的安装
- `/workspace/reviews` 待审核（ADMIN）
- `/workspace/categories` 分类管理（ADMIN）
- `/workspace/ops` 运营统计（ADMIN）

#### Scenario: 非管理员访问管理员路由

- **WHEN** 普通用户访问 `/workspace/reviews` 等管理员路由
- **THEN** 进入 403 或无权限提示页（与产品/前端评审一致）

### Requirement: 我的发布（作者）

工作台 SHALL 展示作者发布列表：名称、状态、当前版本、更新时间；提供新建、编辑、提交审核、发布新版本入口；审核拒绝理由 MUST 可见（与产品页面详述一致）。

#### Scenario: 审核中禁用编辑入口

- **WHEN** Skill 处于审核中
- **THEN** 列表/详情中编辑入口禁用并展示原因（与 `skill-lifecycle-review` 一致）

### Requirement: 我的安装（用户）

工作台 SHALL 展示安装列表：安装版本、最新版本、可更新标识、下架标识；提供跳转详情与复制最新安装命令（复制行为与安装规范一致，不自动执行）。

#### Scenario: 可更新提示

- **WHEN** 最新版本高于用户安装版本且 Skill 未下架
- **THEN** 列表显示「可更新」强调样式（与产品视觉备注一致）

### Requirement: 分类管理（管理员）

系统 SHALL 提供：

- `GET /v1/admin/categories` 分页列表
- `POST /v1/admin/categories` 新增
- `PUT /v1/admin/categories/{categoryId}` 编辑
- `PATCH /v1/admin/categories/{categoryId}/status` 启停

分类名 MUST 唯一；停用 MUST 二次确认且不影响历史数据展示（与产品交互说明一致）。

#### Scenario: 分类名重复

- **WHEN** 创建或修改为已存在名称
- **THEN** 请求失败并提示重复

### Requirement: 运营统计（管理员）

系统 SHALL 提供 `GET /v1/admin/ops/dashboard`，支持 `granularity` 为 **日/周/月**，默认提供近 **7 天**与近 **30 天**范围（与产品冻结结论）；返回指标卡片、安装趋势、热门 Skill TopN、活跃作者等（与后端评审字段意图一致，具体 DTO 以实现为准）。

#### Scenario: 切换统计粒度联动刷新

- **WHEN** 管理员切换时间粒度或范围
- **THEN** 前端联动刷新各模块；局部模块失败不阻断其他模块（与前端评审「局部降级」）

### Requirement: 路由守卫与菜单一致性

前端 MUST 使用统一菜单/路由配置源（如 `menu.config.ts`）驱动导航与 `meta.roles`，避免菜单可见但路由不可达（与前端评审一致）。

#### Scenario: 登录回跳

- **WHEN** 未登录访问受限页
- **THEN** 跳转 `/login?returnUrl=...` 并在登录后回到原页面

### Requirement: 市场默认首页

登录成功后用户 MUST 默认进入 Skill 市场页 `/market`，除非存在有效的 `returnUrl` 回跳目标（与产品/前端评审一致）。

#### Scenario: 登录成功导航

- **WHEN** 用户登录成功且无其他回跳
- **THEN** 进入 `/market`
