# SkillsOps V1 平台基线（OpenSpec）

## Why

团队需要将 `doc/产品与交互文档.md`、`doc/前端技术评审文档.md`、`doc/后端技术评审文档.md` 与 `openspec/config.yaml` 中已冻结的 V1 范围与约束，统一沉淀为可审计、可联调、可验收的 OpenSpec 变更，作为 API-first / SDD 下的单一事实来源，支撑「发布—审核—上架—安装—评分—运营」闭环落地。

## What Changes

- 建立本变更下的能力规范（spec）：认证与会话安全、市场/安装/评分、Skill 生命周期与审核、工作台与管理端运营能力。
- 建立与本变更配套的设计说明（design）：对齐后端模块化单体、数据与缓存、接口清单与错误码、前端架构与路由/状态/联调策略，并**原文级引用** `openspec/config.yaml` 中的项目规范条目（共享/API/后端/前端/文档）。
- 建立可执行任务列表（tasks）：按依赖顺序拆解实现与联调里程碑。
- 建立“文档一致性矩阵”与门禁：实现 MUST 与三份评审文档逐条一致，已定义功能不得遗漏，未定义功能不得扩展。
- 范围边界（与产品文档一致）：**不包含**企业 OpenID、支付、外部公开市场；市场**仅展示已审核上架** Skill；安装为**复制命令**由用户在终端执行，命令为**短时签名安装链接**（短 TTL、单次可用），**禁止**长期明文 Token；角色为 **USER/ADMIN**，作者能力由资源归属判定。

## Capabilities

### New Capabilities

- `auth-session-security`：注册/登录/会话（HttpOnly Cookie）、CSRF（写操作硬门禁）、RBAC + 资源归属、统一错误码与 401/403 行为、与 `config.yaml` 安全/API 规范对齐。
- `market-install-rating`：市场检索与详情、安装命令签发与安装记录、评分/评论（先安装、一人一评可改、评论可选）、实时性口径（详情写入后 ≤5s；榜单/趋势最终一致）。
- `skill-lifecycle-review`：Skill 状态机（草稿/审核中/已上架/已下架）、作者创建/编辑/提交审核、版本发布与唯一性、审核通过/拒绝（拒绝理由必填）、下架与「已安装仅可查看不可装任何历史版本」。
- `workspace-admin-operations`：工作台「我的发布/我的安装」、管理员待审核/分类管理/运营统计（日/周/月，默认近 7/30 天）、权限化导航与路由。

### Modified Capabilities

- （无）当前 `openspec/specs/` 下尚无已归档基线能力。

## Impact

- **后端**：Java 17 + Spring Boot 3.3.x + MyBatis；MySQL + Redis；模块化单体；Outbox/MQ 用于异步聚合（安装计数、均分、运营统计等）的最终一致。
- **前端**：Vue 3 + TS + Vite + Pinia + Vue Router；Axios（`withCredentials`）+ TanStack Query；Naive UI；MSW 联调；ECharts 运营图表。
- **API**：统一前缀 `/v1`；统一响应 `{ success, code, message, data }`；分页 `page/size`；关键写操作 `Idempotency-Key`；写操作 CSRF。
- **文档与规范**：OpenSpec 产出使用**中文**；`openspec/config.yaml` 为规范母本之一，但其中个别条目与《后端技术评审》存在表述差异（见 `design.md`「规范对齐与差异」），实现阶段需做一次**显式裁决**以免前后端实现分叉。
