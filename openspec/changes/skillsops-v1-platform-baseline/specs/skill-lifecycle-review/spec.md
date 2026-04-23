# 能力：Skill 生命周期与审核（skill-lifecycle-review）

## ADDED Requirements

### Requirement: Skill 状态机

系统 SHALL 维护 Skill 生命周期状态至少包含：`draft`（草稿）、`pending`（审核中）、`published`（已上架）、`offline`（已下架）（与后端数据模型一致）。

#### Scenario: 新建 Skill 草稿

- **WHEN** 作者创建 Skill
- **THEN** 系统创建草稿记录，并要求 `resourceUrl` **必填**（承载仓库地址/文档链接，与产品/后端一致）

### Requirement: 审核中禁止编辑

当 Skill 处于 `pending`（审核中）时，系统 MUST 拒绝作者/用户的编辑类写入，并返回可理解错误；前端 MUST 禁用编辑入口并提示「审核中不可编辑」（与产品交互规则一致）。

#### Scenario: 审核中尝试 PATCH

- **WHEN** 作者对审核中 Skill 发起更新
- **THEN** 请求失败并提示等待审核结果

### Requirement: 提交上架审核

系统 SHALL 提供 `POST /v1/skills/{skillId}/submit-review`，将 Skill 从可提交状态转入 `pending` 并进入审核队列。

#### Scenario: 提交审核成功

- **WHEN** 作者提交审核且满足前置校验
- **THEN** Skill 状态更新为审核中，并产生对应审计/审核记录（与后端流程一致）

### Requirement: 管理员审核通过与拒绝

系统 SHALL 提供 `GET /v1/reviews/pending` 待审核列表，以及 `POST /v1/reviews/{reviewId}/approve` 与 `POST /v1/reviews/{reviewId}/reject`；拒绝 MUST 携带理由，且理由长度满足 **10–200** 字（与产品/后端一致）。

#### Scenario: 审核通过上架

- **WHEN** 管理员通过审核
- **THEN** Skill 进入 `published`，市场立即可见（与产品描述一致），并失效相关缓存（实现细节见设计）

#### Scenario: 审核拒绝退回

- **WHEN** 管理员拒绝并填写理由
- **THEN** Skill 退回作者可编辑状态（与业务流程图一致），作者可见拒绝原因并可修订再提交

### Requirement: Skill 内容与版本管理

系统 SHALL 支持 `POST /v1/skills` 创建、`PATCH /v1/skills/{skillId}` 编辑（作者/管理员）、`POST /v1/skills/{skillId}/versions` 发布新版本；版本号在 Skill 维度 MUST 唯一（`uk_skill_version`）。

#### Scenario: 版本号冲突

- **WHEN** 作者提交已存在版本号
- **THEN** 系统返回 `409 SKILL_VERSION_CONFLICT`（与后端评审）

### Requirement: 已上架 Skill 新版本默认无需复审

V1 SHALL 采用「已上架 Skill 发布新版本默认无需复审」策略（与后端已确认决策一致）；同时 MUST 保留审计追踪与管理员下架/治理通道（与后端风险缓解一致）。

#### Scenario: 已上架发布新版本

- **WHEN** 作者对已上架 Skill 发布新版本
- **THEN** 新版本成为最新版本，已安装用户在「我的安装」看到可更新提示（与产品主流程一致）

### Requirement: 下架 Skill

管理员 SHALL 可对违规或风险 Skill 执行下架；下架 MUST 要求原因（产品交互：二次确认 + 下架原因）；下架后市场不可见，已安装用户仅可查看但不可安装任何历史版本（冻结结论）。

#### Scenario: 下架后安装命令拒绝

- **WHEN** Skill 已下架
- **THEN** 安装命令签发与安装动作被拒绝（错误码与评审一致）

### Requirement: 资源地址缺失校验

当 `resourceUrl` 缺失或无效导致不满足发布/提交条件时，系统 SHALL 返回 `422 SKILL_RESOURCE_URL_REQUIRED`（与后端异常清单一致）。

#### Scenario: 提交审核前校验

- **WHEN** 作者未填写资源地址尝试提交关键流程
- **THEN** 请求被校验拒绝并提示
