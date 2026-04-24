# SkillsOps V1 一致性追踪矩阵（全量版）

用于保证 `skillsops-v1-platform-baseline` 的实现与以下文档可审计一致：

- `doc/产品与交互文档.md`
- `doc/后端技术评审文档.md`
- `doc/前端技术评审文档.md`

## 1. 使用规则

- 每条“可实现要求”都必须映射到：`OpenSpec Requirement`、`后端实现点`、`前端实现点`、`测试用例`、`Task Ref`。
- 状态仅允许：`未开始` / `进行中` / `已完成` / `阻塞`。
- 任一条目若实现点或测试为空，不得标记为 `已完成`。
- 若文档冲突：先改 OpenSpec（spec/design/tasks）再改代码。
- 本表是发布门禁输入之一；发布前需保证无“未映射”条目。
- **可执行门禁**：仓库根目录运行 `node scripts/verify-traceability.mjs`，用于校验「tasks 勾选」与「本表 Task Ref + 状态」是否一致；**Apply/CI 必须跑通**（见 `tasks.md` 顶部说明）。

### 如何阅读「Task Ref」（与 tasks 的绑定方式）

- **文件路径**：`openspec/changes/skillsops-v1-platform-baseline/tasks.md`
- **列名**：下表倒数第二列 **`Task Ref`**
- **编号含义**：与 `tasks.md` 正文里 **`- [ ] x.y` / `x.y.z` 前缀完全一致**。例如 `3.1` 对应该文件中 **「## 3. …」** 小节下的 **`- [ ] 3.1 …`**；`1.1.3` 对应该文件中 **「### 1.1 …」** 下的 **`- [ ] 1.1.3 …`**。
- **多条任务**：同一需求可能对应多个勾选项，用英文逗号分隔（如 `4.3,4.5,4.6`）。

## 2. 追踪表（按功能域）

| ID | 来源文档 | 来源条目 | OpenSpec 映射 | 后端实现点 | 前端实现点 | 测试用例 | Task Ref | 状态 |
|---|---|---|---|---|---|---|---|---|
| AUTH-001 | 产品 | 账号+密码注册，无邮箱验证码 | `auth-session-security` 注册 | `POST /v1/auth/register` | `/register` 表单 | BE: 注册成功/冲突；FE: 校验；E2E | 3.1,3.3,3.5,3.6 | 已完成 |
| AUTH-002 | 产品/后端 | 登录并写 Cookie 会话 | `auth-session-security` 登录会话 | `POST /v1/auth/login` + HttpOnly Cookie | 登录页 + `authStore` | BE: Cookie 写入；FE: 登录态 | 3.1,3.3,3.4,3.5 | 已完成 |
| AUTH-003 | 产品/后端 | 退出（登出）能力 | `auth-session-security` 退出 | `POST /v1/auth/logout` | 顶部菜单退出 | BE: 登出后 401；E2E 登出链路 | 3.1,3.3,3.5 | 已完成 |
| AUTH-004 | 产品/前端 | Token 过期回登录并保留 returnUrl | `auth-session-security` 会话过期 | 401 `AUTH_TOKEN_EXPIRED` | 路由守卫 + 回跳 | FE 守卫单测；E2E 回跳 | 3.3,3.4,3.6 | 已完成 |
| AUTH-005 | 后端 | CSRF 为上线硬门禁 | `auth-session-security` CSRF | 写接口 CSRF 校验 | axios 注入 CSRF | BE: 缺失403；E2E 缺头拒绝 | 3.1,3.5 | 已完成 |
| AUTH-006 | 产品 | 角色 USER/ADMIN，作者按归属判定 | `auth-session-security` RBAC | 403 权限拦截 | 菜单裁剪 + 403 页 | FE 非管理员拦截；BE 权限测试 | 3.4,4.5,4.6 | 已完成 |
| AUTH-007 | 后端/config | 统一响应 envelope + 错误码映射 | `auth-session-security` 响应规范 | `ApiResponse` + `ControllerAdvice` | 统一错误处理器 | BE 响应契约测试 | 1.1.3,2.1,2.3 | 已完成 |
| AUTH-008 | 后端 | `X-Request-ID` / `Idempotency-Key` | `auth-session-security` 可观测性与幂等 | 请求头接入与校验 | axios 拦截器注入 | FE 请求头单测；BE 幂等冲突 | 2.2,5.2,8.1 | 进行中 |
| MKT-001 | 产品 | 市场仅展示已上架 Skill | `market-install-rating` 市场可见性 | `GET /v1/market/skills` 过滤 published | 市场列表展示 | BE 列表过滤；E2E 可见性 | 5.1,5.4,5.6 | 已完成 |
| MKT-002 | 产品/后端 | 市场筛选+排序+分页 | `market-install-rating` 列表检索 | page/size/category/q/sort | 筛选条、分页组件 | 契约参数测试；FE 查询状态测试 | 2.1,5.1,5.4 | 已完成 |
| MKT-003 | 产品/后端 | 详情聚合（版本/评分/安装） | `market-install-rating` 详情查询 | `GET /v1/market/skills/{skillId}` | 详情页模块渲染 | BE DTO 集成；FE 组件渲染 | 5.1,5.4,5.6 | 已完成 |
| MKT-004 | 产品 | 安装仅复制命令，不执行脚本 | `market-install-rating` 安装方式 | 安装命令签发接口 | 复制命令按钮 | FE clipboard 测试 | 5.2,5.5,5.6 | 已完成 |
| MKT-005 | 产品/后端 | 安装命令短时签名、短TTL、单次可用 | `market-install-rating` 安装命令签发 | `POST /v1/skills/{skillId}/install-command` | 复制结果提示 | BE 过期/重复消费测试 | 5.2,5.5,5.6 | 已完成 |
| MKT-006 | 产品 | 下架后市场不可见 | `market-install-rating` 下架可见性 | 下架状态过滤 | 列表不可见分支 | E2E 下架后不可见 | 5.1,5.4,5.6 | 已完成 |
| MKT-007 | 产品/后端 | 下架后已安装可查看但不可装历史版本 | `market-install-rating` + `skill-lifecycle-review` | 安装接口返回 `409 SKILL_OFFLINE_NOT_INSTALLABLE` | 我的安装显示“已下架”、禁安装 | BE 错误码测试；FE 按钮禁用 | 5.2,5.3,5.5,6.3 | 已完成 |
| MKT-008 | 产品 | 详情安装量/均分写后 <=5s | `market-install-rating` 近实时口径 | 写后失效+回查 | 详情页刷新策略 | E2E 轮询断言 | 6.1,6.2,6.3,8.4 | 进行中 |
| RAT-001 | 产品 | 仅安装后可评分 | `market-install-rating` 评分前置条件 | `422 RATING_REQUIRES_INSTALL` | 评分入口拦截 | BE 未安装拒绝；FE 引导 | 6.1,6.2,6.3 | 已完成 |
| RAT-002 | 产品 | 一人一评，可修改 | `market-install-rating` 评分更新 | `uk_user_skill` + update | 评分编辑覆盖 | BE upsert；FE 回显更新 | 6.1,6.2,6.3 | 已完成 |
| RAT-003 | 产品 | 评分允许仅打分不评论 | `market-install-rating` 评分规则 | comment 可空 | comment 非必填 | BE 空comment；FE 表单 | 6.1,6.2,6.3 | 已完成 |
| LIFE-001 | 产品/后端 | 状态机 draft/pending/published/offline | `skill-lifecycle-review` 状态机 | 状态机约束与迁移 | 状态 Tag 显示 | BE 状态迁移单测 | 4.1,4.6 | 已完成 |
| LIFE-002 | 产品 | 审核中不可编辑 | `skill-lifecycle-review` 审核中禁改 | PATCH 拒绝 | 编辑入口禁用提示 | BE 拒绝写入；FE 禁用态 | 4.1,4.2,4.4,4.6 | 已完成 |
| LIFE-003 | 后端 | 创建 Skill 草稿 resourceUrl 必填 | `skill-lifecycle-review` 创建与校验 | `POST /v1/skills` + 422 | 创建表单必填校验 | BE 422 测试；FE 校验 | 4.2,4.4,4.6 | 已完成 |
| LIFE-004 | 后端 | 提交审核接口 | `skill-lifecycle-review` 提交审核 | `POST /v1/skills/{skillId}/submit-review` | 提交审核动作 | 集成测试状态变更 | 4.2,4.4,4.6 | 已完成 |
| LIFE-005 | 产品/后端 | 待审核列表 | `skill-lifecycle-review` 审核队列 | `GET /v1/reviews/pending` | `/workspace/reviews` 列表 | FE 列表渲染；BE 分页 | 4.3,4.5,4.6 | 已完成 |
| LIFE-006 | 产品/后端 | 审核通过 | `skill-lifecycle-review` 审核通过 | `POST /v1/reviews/{reviewId}/approve` | 审核通过按钮 | BE published；FE 刷新 | 4.3,4.5,4.6 | 已完成 |
| LIFE-007 | 产品/后端 | 审核拒绝且理由 10-200 | `skill-lifecycle-review` 审核拒绝 | `POST /v1/reviews/{reviewId}/reject` | 拒绝理由弹窗校验 | BE 长度校验；FE 表单校验 | 4.3,4.5,4.6 | 已完成 |
| LIFE-008 | 产品/后端 | 发布新版本，版本号唯一 | `skill-lifecycle-review` 版本管理 | `POST /v1/skills/{skillId}/versions` + 409 | 发布版本弹窗 | BE 冲突测试；FE 错误提示 | 4.2,4.4,4.6 | 已完成 |
| LIFE-009 | 后端 | 已上架 Skill 新版本默认无需复审 | `skill-lifecycle-review` 免复审策略 | 版本发布直接生效 | 前端可更新标记 | 集成测试流程 | 4.2,4.6,5.3 | 已完成 |
| LIFE-010 | 产品 | 管理员下架需原因+二次确认 | `skill-lifecycle-review` 下架 | 下架接口记录 reason | 下架确认弹窗 | BE reason 校验；FE 弹窗 | 4.3,4.5,4.6 | 已完成 |
| WS-001 | 产品/前端 | 工作台信息架构（发布/安装/审核/分类/统计） | `workspace-admin-operations` 路由结构 | 权限接口支持 | 菜单与侧边栏 | FE 路由快照测试 | 4.5,7.3 | 进行中 |
| WS-002 | 前端 | `/market` 默认首页 | `workspace-admin-operations` 默认首页 | 登录后会话可用 | 登录成功跳转 `/market` | E2E 登录跳转 | 3.3,3.6 | 已完成 |
| WS-003 | 前端 | `/workspace/reviews` 管理员路由 | `workspace-admin-operations` 路由守卫 | 403 权限返回 | `meta.roles` 守卫 | FE 守卫单测 | 3.4,4.5 | 已完成 |
| WS-004 | 前端 | 菜单与路由同源（menu.config.ts） | `workspace-admin-operations` 菜单一致性 | 无 | 统一菜单配置 | FE 配置一致性测试 | 2.2,3.4,7.3 | 进行中 |
| WS-005 | 产品 | 我的发布页（状态/编辑/提交/发布） | `workspace-admin-operations` 我的发布 | 相关 API 支撑 | `/workspace/published` 页面 | FE 页面流测试 | 4.4,4.6 | 已完成 |
| WS-006 | 产品 | 我的安装页（版本、可更新、下架标） | `workspace-admin-operations` 我的安装 | `GET /v1/users/me/installs` | `/workspace/installed` 页面 | FE 渲染测试 | 5.3,5.5,5.6 | 已完成 |
| CAT-001 | 产品/后端 | 分类管理 CRUD + 启停 | `workspace-admin-operations` 分类管理 | `/v1/admin/categories*` | `/workspace/categories` | BE CRUD 集成；FE 表单 | 7.1,7.3,7.4 | 进行中 |
| CAT-002 | 产品 | 分类名唯一 | `workspace-admin-operations` 分类唯一性 | DB 唯一约束 + 409/422 | 重复名提示 | BE 重复名测试；FE 提示 | 7.1,7.3,7.4 | 进行中 |
| OPS-001 | 产品 | 运营统计日/周/月，默认 7/30 天 | `workspace-admin-operations` 运营统计 | `GET /v1/admin/ops/dashboard` | 时间粒度切换 | BE 参数测试；FE 联动 | 7.2,7.3,7.4 | 未开始 |
| OPS-002 | 产品/前端 | 指标卡 + 趋势 + TopN + 活跃作者 | `workspace-admin-operations` 运营看板 | 聚合查询/DTO | ECharts 页面 | FE 图表适配测试 | 7.2,7.3,7.4 | 未开始 |
| OPS-003 | 产品/前端 | 局部错误不影响其他模块 | `workspace-admin-operations` 降级 | 局部接口错误可返回 | 组件局部错误态 | FE 局部降级测试 | 7.3,7.4 | 未开始 |
| API-001 | 后端/config | RESTful + `/v1` + 统一响应 | `auth-session-security` + 其他 specs | OpenAPI + Controller | `api/client` 封装 | 契约测试 | 2.1,2.2,2.3 | 已完成 |
| API-002 | 后端/config | 请求头：`X-Request-ID`、`Idempotency-Key`、CSRF | `auth-session-security` | 统一拦截器/过滤器 | axios 请求拦截器 | 头注入与校验测试 | 2.2,3.1,5.2 | 已完成 |
| API-003 | 后端 | 异常码映射（401/403/409/422/429） | `auth-session-security` + 其他 specs | 错误码枚举与抛出 | 错误消息映射 | BE 异常映射测试 | 1.1.3,2.1,3.5 | 已完成 |
| ENV-001 | 你的新增要求 | 后端至少 dev/prod 两套配置 | `tasks` 环境要求 | `application-dev/prod.yml` | 无 | 启动验证 | 1.1.4 | 已完成 |
| ENV-002 | 你的新增要求 | 前端至少 development/production 两套配置 | `tasks` 环境要求 | 无 | `.env.development/.env.production` | 构建验证 | 1.2.2,1.2.3 | 已完成 |
| ENV-003 | 你的新增要求 | Java 包名禁止 example | `tasks` 包名要求 | `com.skillsops` 包路径 | 无 | 静态检查 | 1.1.2 | 已完成 |
| ENV-004 | 你的新增要求 | 本地启动自动初始化表结构 | `tasks` Flyway 要求 | Flyway auto-migrate | 无 | `@SpringBootTest` 验证 | 1.1.5,1.3.1 | 已完成 |
| NFR-001 | 前端/后端评审 | CI 门禁（lint/typecheck/test/build, mvn verify） | `tasks` CI 要求 | Maven verify | FE pipeline | CI 结果 | 1.3.4 | 已完成 |
| NFR-002 | 后端评审 | 可观测性（Actuator/Prometheus/日志） | `design` D7 + `tasks` | Micrometer + JSON 日志 | 前端错误上报对接 | 监控冒烟 | 8.1,8.2 | 未开始 |
| NFR-003 | 前端评审 | 错误监控与 Web Vitals | `tasks` 非功能 | 无 | Sentry + 指标采集 | FE 冒烟 | 8.2 | 未开始 |
| NFR-004 | 产品/前端评审 | 错误态/空态/重试一致交互 | `market-install-rating` + `workspace-admin-operations` | 错误码语义 | BaseErrorBlock/BaseEmpty | FE 组件与 E2E | 5.4,7.3 | 进行中 |

## 3. 覆盖率检查（手动）

- 三文档可实现要求是否全部入表：`已复核`（矩阵见 §2；新需求须先改 OpenSpec 再增行）
- OpenSpec Requirement 是否均有至少 1 条映射：`是`
- 所有接口路径是否有对应条目：`是`
- 是否绑定 `tasks.md` 编号：`是`

## 4. 与 OpenSpec 联动

- OpenSpec 变更：`openspec/changes/skillsops-v1-platform-baseline/`
- 关键文件：`proposal.md`、`design.md`、`tasks.md`、`specs/*/spec.md`
- 建议在每次完成 `tasks` 小节后，批量更新本表对应状态与测试证据链接。
