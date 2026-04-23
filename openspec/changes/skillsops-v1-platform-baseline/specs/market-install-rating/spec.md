# 能力：市场、安装与评分（market-install-rating）

## ADDED Requirements

### Requirement: 市场仅展示已上架 Skill

系统 SHALL 在市场列表与检索中**仅返回已审核上架**的 Skill；未审核通过 Skill MUST NOT 对市场用户可见（与产品冻结结论一致）。

#### Scenario: 用户浏览市场

- **WHEN** 已登录用户访问市场列表
- **THEN** 返回结果仅包含已上架 Skill，并支持分类、关键词、排序、分页

### Requirement: 市场列表检索与分页排序

系统 SHALL 提供 `GET /v1/market/skills`，支持 `page`、`size`、分类、`q`（或等价关键词参数）、`sort`（与后端评审一致），响应分页结构包含 `page`、`size`、`total`、`items`。

#### Scenario: 关键词搜索触发刷新

- **WHEN** 用户输入关键词并触发查询
- **THEN** 列表从第 1 页刷新且保留其他筛选条件的行为由前端实现；服务端返回匹配结果集

### Requirement: Skill 详情聚合查询

系统 SHALL 提供 `GET /v1/market/skills/{skillId}`，聚合展示所需字段：基础信息、当前/最新版本、评分摘要、安装量、版本历史入口数据等（以后端 DTO 为准）。

#### Scenario: 加载详情成功

- **WHEN** 请求已上架 Skill 的详情
- **THEN** 返回完整详情 DTO；前端展示安装区、版本历史、评论区与侧栏操作（与产品信息架构一致）

#### Scenario: 下架 Skill 对市场不可见

- **WHEN** Skill 已下架且用户**未曾安装**该 Skill
- **THEN** 市场不可见；详情返回下架状态并禁用安装与新评分，且安装相关接口返回 `409 SKILL_OFFLINE_NOT_INSTALLABLE`

### Requirement: 安装为「复制命令」且不执行本地脚本

系统 SHALL NOT 在平台内直接执行安装脚本；用户通过前端复制安装命令后在本地终端执行（与产品冻结结论一致）。

#### Scenario: 用户请求安装命令

- **WHEN** 已登录用户对**可安装状态**的 Skill 请求安装命令
- **THEN** 系统返回形如 `npx skills add <signed-git-url>` 的命令文本，且链接为**短时签名**、**短 TTL**、**单次可用**（signed-git-url），**禁止**长期明文 Token

### Requirement: 安装命令签发接口

系统 SHALL 提供 `POST /v1/skills/{skillId}/install-command`，要求 `Idempotency-Key`；返回 `command` 与 `urlExpireAt`（与后端评审）；且 MUST 在校验 Skill 状态为已上架后方可签发。

#### Scenario: 已下架不可安装（含历史版本）

- **WHEN** Skill 已下架
- **THEN** 系统 MUST 拒绝安装命令签发（含历史版本场景），并返回与评审一致的错误码（如 `409 SKILL_OFFLINE_NOT_INSTALLABLE`）

#### Scenario: 记录安装行为

- **WHEN** 安装命令流程成功（按后端设计：记录命令签发与安装记录）
- **THEN** 系统维护 `install_record`（一人一技能一条，更新版本覆盖），并支撑「我的安装」与「仅安装后可评分」判定

### Requirement: 我的安装列表

系统 SHALL 提供 `GET /v1/users/me/installs`，支持分页，返回安装版本、最新版本、可更新标记、下架标记等（与产品「我的安装」模块一致）。

#### Scenario: 已下架仍可见但不可安装

- **WHEN** 用户曾安装某 Skill 后该 Skill 被下架
- **THEN** 「我的安装」仍展示记录并标注「已下架」，且禁用安装相关动作（与产品异常场景一致）

### Requirement: 评分与评论（先安装、一人一评可改、评论可选）

系统 SHALL 提供 `PUT /v1/skills/{skillId}/ratings`；用户 MUST 已安装后才可评分；每位用户对每 Skill **仅一条**评分记录但**可更新**；评分 1–5；**允许仅打分不评论**（评论为空或可选字段）。

#### Scenario: 未安装用户尝试评分

- **WHEN** 未安装用户提交评分
- **THEN** 系统返回 `422 RATING_REQUIRES_INSTALL`（与后端评审）

#### Scenario: 已安装用户更新评分

- **WHEN** 已安装用户再次提交评分
- **THEN** 系统更新该用户评分并刷新聚合均分与评论列表；前端可做乐观更新与回滚（与前端评审）

### Requirement: 详情页指标近实时刷新口径

系统 SHALL 满足产品口径：详情页**安装量、平均评分**在写入后 **`<= 5s`** 内可见刷新；榜单与趋势类数据允许秒级最终一致。

#### Scenario: 安装或评分写入后读详情

- **WHEN** 用户完成安装记录更新或评分写入后短时间内读取详情
- **THEN** 详情展示的安装量/均分与后端聚合策略一致，且满足上述时效口径（可通过缓存失效 + 回查实现，见设计文档）

### Requirement: 前端交互与安全提示

前端 MUST 在复制安装命令成功后提示用户：**命令含敏感凭据或签名链接、勿分享**；文案 SHOULD 与产品文档语义一致并避免暗示「长期 API Token」若实际为短时签名（与产品/安全验收一致）。

#### Scenario: 复制成功提示

- **WHEN** 用户复制安装命令成功
- **THEN** 展示 Toast/提示，且包含防分享风险提示
