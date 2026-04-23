# SkillsOps V1 技术设计（对齐四份基线文档）

## Context

- **业务**：团队内 Skill 资产「可发现、可判断、可安装、可迭代、可运营」。V1 闭环：作者发布 → 管理员审核 → 上架 → 用户市场发现 → 复制安装命令 → 安装记录与评分回流 → 运营统计。
- **文档来源**：`doc/产品与交互文档.md`、`doc/后端技术评审文档.md`、`doc/前端技术评审文档.md`、`openspec/config.yaml`。
- **约束**：API-first；`/v1`；统一响应 `{ success, code, message, data }`；分页 `page/size`；会话 **HttpOnly Cookie**；写操作 **CSRF 硬门禁**；关键写操作 **`Idempotency-Key`**；MySQL + Redis；后端 **Java 17 + Spring Boot 3.3.x + MyBatis**；前端 **Vue 3 + TS + Vite + Pinia + Vue Router + Axios + TanStack Query + Naive UI**。

## Goals / Non-Goals

**Goals:**

- 以**模块化单体**交付 V1（单 Spring Boot 进程，领域分包），预留 Outbox/MQ 驱动的异步统计与缓存失效。
- 市场读路径、详情聚合、安装与评分写路径达到评审中的性能与 RT 目标（列表/详情/写入 P95 阈值见后端评审）。
- 前端实现评审中的路由表、Query 缓存键与失效策略、MSW 联调与 CI 门禁（lint、typecheck、test）。

**Non-Goals（与产品一致）：**

- 企业 OpenID、支付、外部公开市场。
- 平台内自动执行安装脚本；命令内长期明文 Token。
- V1 强制 WebSocket/SSE 实时推送（以写后失效 + 回查满足详情近实时口径）。

## Decisions

### D1. 架构：模块化单体 + Redis +（可选）Kafka

- **决策**：V1 采用**模块化单体**；Redis 用于会话、市场/详情缓存、限流计数等；消息队列 **Kafka 优先**（或 RocketMQ 二选一，与运维现状匹配）。
- **理由**：降低早期分布式复杂度，与《后端技术评审》一致。
- **备选**：直接拆微服务——运维与联调成本高，延后到 V2+。

### D2. 数据模型与一致性

- **决策**：核心表：`user`、`category`、`skill`、`skill_version`、`install_record`、`rating`、`audit_record`（及聚合/统计表如 `skill_stat_agg`）；本地事务用 `@Transactional`；安装计数、均分、运营看板通过 **Outbox + MQ** 最终一致，`event_id` 幂等消费。
- **理由**：与后端评审 ER 与一致性章节一致。

### D3. 鉴权与 CSRF

- **决策**：生产主链路以 **HttpOnly Cookie** 为会话载体；**所有写操作**携带 CSRF Token；`Authorization: Bearer` 仅作调试兜底（以后端评审为准）。
- **理由**：与产品安全验收、后端 4.4 一致。

### D4. 安装：短时签名 URL + 幂等

- **决策**：`POST /v1/skills/{skillId}/install-command` 返回 `npx skills add <signed-git-url>`；链接短 TTL、单次可用；服务端记录签发与消费状态；依赖 `Idempotency-Key`。
- **理由**：产品冻结「禁止长期明文 Token」与后端时序设计。

### D5. 前端数据层：Axios + TanStack Query

- **决策**：Axios 统一实例（`baseURL`、`timeout`、`withCredentials: true`），拦截器注入 `X-Request-ID`、CSRF、幂等键；Vue Query 管理 `['market', ...]`、`['skill-detail', skillId]`、`['my-installs', ...]` 等缓存键；安装/评分/审核成功后按前端评审失效集合刷新。
- **理由**：与《前端技术评审》第 5、7 章一致。

### D6. UI 与样式

- **决策**：**Naive UI** + 业务组件（`SkillCard`、`SkillInstallPanel`、`RatingEditor` 等）；**Sass + CSS Modules** + 全局 Design Tokens；图表 **ECharts**；日期 **dayjs**。
- **理由**：与前端评审选型一致。

### D7. `openspec/config.yaml` 作为规范母本

- **决策**：实现与代码评审 MUST 遵守 `config.yaml` 中 **shared（安全、错误码、代码风格、测试）**、**api（REST、响应 envelope、分页过滤、版本兼容）**、**backend（分层、MyBatis、Redis key、可观测性）**、**frontend（目录结构、组件规范、API 封装、Pinia、监控）**、**doc（文档与变更同步）** 的约束。
- **理由**：用户明确要求四文档写入 OpenSpec；`config.yaml` 是项目级 SDD 约束的集中声明。

### D8. 规范冲突显式处理：密码传输与存储

- **`openspec/config.yaml`** 表述包含：「密码传输：前端 SHA-256 加盐 → 后端 BCrypt/Argon2 存储」。
- **《后端技术评审》§6.5** 表述为：HTTPS 传输 + 后端 BCrypt/Argon2，**「前端不做自定义哈希」**。
- **裁决（用于实现，避免分叉）**：V1 **以实现《后端技术评审》为准**——不在前端做自定义密码哈希；强制 HTTPS；后端存储使用 BCrypt/Argon2。后续应**修订 `config.yaml`** 相应条目与后端评审对齐（本变更记录在案，满足「四文档内容已写入 OpenSpec」同时不隐藏冲突）。

### D9. 一致性优先级：与三份评审文档逐条一致

- **决策**：`skillsops-v1-platform-baseline` 的实现范围、行为、接口、页面与异常处理，MUST 与以下文档逐条一致：`doc/产品与交互文档.md`、`doc/后端技术评审文档.md`、`doc/前端技术评审文档.md`。
- **规则**：
  - 文档中已定义的功能 SHALL 全部实现，不得遗漏（如注册/登录/退出、审核闭环、安装命令复制、评分、运营统计）。
  - 文档未定义的功能 SHALL NOT 在 V1 自行扩展。
  - 出现歧义时，必须先更新 OpenSpec 规格并获得确认，再进入实现。
- **理由**：确保交付内容与评审结果一模一样，避免实现偏航。

## 补充设计摘录（与评审文档对齐）

### 后端模块边界（逻辑）

`auth`、`skill`、`review`、`version`、`market`、`install`、`rating`、`category`、`ops`；模块间通过 Service 接口调用，**禁止跨模块直访 DAO**。

### 核心 REST 接口清单（摘自后端评审，实现时以 OpenAPI 为准）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/v1/auth/register` | 注册 |
| POST | `/v1/auth/login` | 登录写 Cookie |
| POST | `/v1/skills` | 创建草稿（`resourceUrl` 必填） |
| PATCH | `/v1/skills/{skillId}` | 编辑 |
| POST | `/v1/skills/{skillId}/submit-review` | 提交审核 |
| GET | `/v1/reviews/pending` | 待审核 |
| POST | `/v1/reviews/{reviewId}/approve` | 通过 |
| POST | `/v1/reviews/{reviewId}/reject` | 拒绝（理由 10–200） |
| POST | `/v1/skills/{skillId}/versions` | 新版本 |
| GET | `/v1/market/skills` | 市场列表 |
| GET | `/v1/market/skills/{skillId}` | 详情 |
| POST | `/v1/skills/{skillId}/install-command` | 安装命令 |
| GET | `/v1/users/me/installs` | 我的安装 |
| PUT | `/v1/skills/{skillId}/ratings` | 评分 |
| GET/POST/PUT/PATCH | `/v1/admin/categories`… | 分类管理 |
| GET | `/v1/admin/ops/dashboard` | 运营统计 |

### 关键错误码（示例）

`AUTH_INVALID_CREDENTIALS`、`AUTH_TOKEN_EXPIRED`、`AUTH_CSRF_INVALID`、`PERMISSION_DENIED`、`RESOURCE_NOT_FOUND`、`VALIDATION_FAILED`、`SKILL_VERSION_CONFLICT`、`SKILL_RESOURCE_URL_REQUIRED`、`RATING_REQUIRES_INSTALL`、`SKILL_OFFLINE_NOT_INSTALLABLE`、`IDEMPOTENCY_CONFLICT`、`RATE_LIMIT_EXCEEDED` 等——完整映射见后端评审 §4.3 与 `config.yaml` shared.error_codes。

### 前端路由与导航（摘自前端评审）

- 公共：`/login`、`/register`、404。
- 登录：`/market` 默认首页；`/skills/:skillId` 详情。
- 工作台：`/workspace/published`、`/workspace/installed`、管理员 `/workspace/reviews`、`/workspace/categories`、`/workspace/ops`。
- 守卫：未登录 `returnUrl`；`meta.roles` 管理员校验。

### 交互与视觉（摘自产品文档）

- Desktop First，主内容最大宽 1200px；主色 `#2F6BFF`；栅格与间距体系按产品 §3。
- 列表骨架屏、错误块重试、审核拒绝/下架二次确认与表单项校验行为按产品 §2.4。

### 缓存与 Redis Key 形态

- 后端评审建议：`skillsops:{domain}:{bizKey}`；市场列表 TTL 30–60s、详情 30s、会话 30min 等。
- `config.yaml` 亦要求 Redis Key `{业务域}:{子模块}:{标识}` 与 TTL 约束——实现时取二者并集语义。

## Risks / Trade-offs

- **[Risk] 近实时指标 vs 缓存抖动** → 详情 `<=5s` 用短 TTL + 写后失效；榜单/趋势允许最终一致；监控缓存命中与回源率。
- **[Risk] MySQL 模糊搜索性能** → V1 索引 + `LIKE`；数据量上升后引入全文/ES（后端评审已预案）。
- **[Risk] 审核中编辑竞态** → 服务端强制 `pending` 拒绝写入 + 前端禁用（双保险）。
- **[Risk] 配置与评审表述不一致** → 已记录 D8，优先后端评审；修订 `config.yaml` 跟进。

## Migration Plan

- 数据库变更通过 Migration 管理，支持回滚（`config.yaml` backend 数据库规则）。
- 发布：`dev/test/staging/prod` profiles；Actuator health；优雅停机；灰度发布策略见前端/后端评审。

## Open Questions

- 产品文档中安装成功提示语与「短时签名、无长期 Token」的**用户可见文案**是否需统一修订（不影响接口，仅文案）。
- 运营统计 TopN 与趋势口径（自然日 vs 滚动窗口）是否在联调前完全冻结（前后端评审均提到待确认项）。
