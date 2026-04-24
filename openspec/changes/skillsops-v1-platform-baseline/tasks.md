# SkillsOps V1 实现任务清单（生产级 · 垂直切片）

> 依据 `proposal.md`、`design.md` 与 `specs/**/spec.md`。  
> **硬性约束**：Java 根包 **`com.skillsops`**（及子包），**禁止** `com.example` / `org.example` 等占位包名；后端 **dev/prod**、前端 **development/production** 至少两套配置；本地启动 **自动执行 Flyway** 建表/演进；每项业务切片尽量 **后端 + 前端 + 测试** 同时闭环，避免「玩具工程」。

## Apply 门禁：追踪矩阵与 tasks 必须联动

执行 **`/opsx:apply`**（或等价实现流程）时，**每勾选完成一条任务**（`- [ ]` → `- [x]`），必须立刻维护 `doc/traceability-matrix.md`：

1. 在矩阵中找到 **Task Ref** 列包含该任务编号（如 `3.1`、`1.1.3`）的所有行。
2. 按该行 **Task Ref 所列任务** 在 `tasks.md` 中的勾选情况，更新该行的 **状态**：
   - 关联任务**全部未勾选** → `未开始`
   - **部分勾选** → `进行中`
   - **全部勾选** → `已完成`
3. 在仓库根目录执行（**退出码非 0 则禁止继续下一条任务或合并 PR**）：

```bash
node scripts/verify-traceability.mjs
```

脚本规则见 `scripts/verify-traceability.mjs` 文件头注释。Agent 执行 apply 时须在勾选任务后**默认运行**该命令并修复失败项。

---

## 1. 生产级工程基线（后端 / 前端 / 测试）

### 1.1 后端：Maven + Spring Boot 工程骨架

- [x] 1.1.1 在 `backend/` 建立 **Maven** 工程：Java **17**、**Spring Boot 3.3.x**、依赖管理 BOM；启用 `spring-boot-starter-web`、`validation`、`security`（或按设计启用）、`mybatis-spring-boot-starter`、`mysql-connector-j`、`flyway-core`、`spring-boot-starter-data-redis`（按需）、`springdoc-openapi`、`spring-boot-starter-actuator`、`micrometer-registry-prometheus`（按需）
- [x] 1.1.2 约定根包 **`com.skillsops`**，按领域划分子包（如 `com.skillsops.auth`、`com.skillsops.skill`、`com.skillsops.common`）；**禁止**任何 `example` 包路径与默认样例 `Application` 留在 `com.example`
- [x] 1.1.3 提供统一 **`ApiResponse<T>`**、`GlobalExceptionHandler`、`ErrorCode` 枚举，与 `openspec/config.yaml` 错误码命名对齐
- [x] 1.1.4 配置 **多环境**：`application.yml`（公共）+ **`application-dev.yml`** + **`application-prod.yml`**（数据源、Redis、Flyway、日志级别、Cookie `Secure`/`SameSite`、Session 超时等分环境差异）；`dev` 可开启 SQL 调试，`prod` 强制脱敏与精简日志
- [x] 1.1.5 集成 **Flyway**：迁移脚本位于 `backend/src/main/resources/db/migration`；**本地与默认 `dev` profile 启动时自动 `migrate`**；`prod` 仅允许受控迁移（文档说明运维策略）；**禁止**依赖手工执行 SQL 才能启动
- [x] 1.1.6 在仓库根目录 **`README.md`** 中编写**后端**章节：JDK/Maven 版本、**`SPRING_PROFILES_ACTIVE=dev`** 启动方式、必填环境变量清单、Flyway 说明（**不**单独维护 `README-backend.md`）

### 1.2 前端：Vite + Vue 工程骨架

- [x] 1.2.1 在 `frontend/` 建立 **Vite 5 + Vue 3 + TypeScript（strict）+ Vue Router + Pinia**；ESLint（`vue` + `@typescript-eslint`）+ 格式化工具；目录对齐 `config.yaml`（`api/`、`views/`、`components/`、`composables/`、`stores/`）
- [x] 1.2.2 配置 **多环境**：**`.env.development`**、**`.env.production`**，以及 **`.env.example`**（无密钥，仅键名说明）；`VITE_API_BASE_URL` 等在 `import.meta.env` 中类型声明（`env.d.ts`）
- [x] 1.2.3 `vite.config.ts` 按 `mode` 区分 **`server.proxy`（开发）** 与 **生产 `base`/CDN** 策略；构建产物含 `sourcemap` 策略说明（prod 可选关闭以减体积）
- [x] 1.2.4 接入 **Naive UI**、全局样式 Tokens（Sass + CSS Modules 基座）、`layouts/` 默认布局（顶栏 + 内容区）
- [x] 1.2.5 在 **`README.md`** 中编写**前端**章节：Node 版本、**`npm`** 脚本（安装与 `development`/`production` 构建、预览）、环境变量说明（**不**单独维护 `README-frontend.md`；包管理统一 **`npm`**，不用 pnpm）

### 1.3 测试与 CI 基线

- [x] 1.3.1 后端：添加 **`src/test/resources/application-test.yml`**（独立内存库 **或** Testcontainers MySQL）；**`@SpringBootTest`** 冒烟测试验证 **应用上下文加载 + Flyway 在测试环境执行成功**
- [x] 1.3.2 后端：接入 **Surefire**，约定测试命名与 `config.yaml` testing 章节一致；核心工具类与异常映射单元测试样例
- [x] 1.3.3 前端：配置 **Vitest** + `@vue/test-utils`；**示例**：布局或根组件挂载冒烟测试；`package.json` 脚本 `test` / `test:coverage`
- [x] 1.3.4 仓库根目录添加 **CI 工作流**（GitHub Actions / 团队等价物）：**后端** `mvn -B verify`；**前端** `lint + typecheck + test + build`；**并执行** `node scripts/verify-traceability.mjs`；失败即阻断合并

### 1.4 文档一致性矩阵（强制门禁）

- [x] 1.4.1 建立 `doc/traceability-matrix.md`：将三份评审文档拆为“需求条目 ID -> OpenSpec Requirement -> 后端实现点 -> 前端实现点 -> 测试用例 ID”
- [x] 1.4.2 在 CI 中加入一致性检查门禁：矩阵中“未实现/未测试”项不允许合并
- [x] 1.4.3 明确“V1 禁止自由扩展”清单：不在三份评审文档中的功能一律标记为 out-of-scope，不得实现
- [x] 1.4.4 维护 `scripts/verify-traceability.mjs`：与矩阵列定义保持同步；PR 模板或贡献说明中要求勾选任务后必须通过该脚本

---

## 2. API 契约与 OpenAPI（后端 / 前端 / 测试）

- [x] 2.1 **后端**：按 `design.md` 接口表建立 **`/v1`** Controller 壳层 + DTO + Bean Validation；**SpringDoc** 生成 OpenAPI 3；JSON 示例与错误响应 schema 齐全
- [x] 2.2 **前端**：自 OpenAPI **生成或手维护** `types/` 与 `api/client` 封装（axios 实例：`withCredentials`、超时、`X-Request-ID`）；**development** 指向代理，**production** 指向 `VITE_API_BASE_URL`
- [x] 2.3 **测试**：契约快照或 **OpenAPI 校验测试**（如 schemathesis 可选）；前端对 **envelope** 解析做单元测试（mock axios 响应）

---

## 3. 认证、会话与 CSRF（后端 / 前端 / 测试）

- [x] 3.1 **后端**：`POST /v1/auth/register`、`POST /v1/auth/login`、`POST /v1/auth/logout`；密码 **BCrypt/Argon2** 存储；**HttpOnly Session Cookie**；会话超时与注销；**双提交 Cookie 或 Header CSRF**，全部 **POST/PUT/PATCH/DELETE** 校验，`403 AUTH_CSRF_INVALID`
- [x] 3.2 **后端**：`User` 领域模型、Flyway **`Vxxx__user.sql`**、唯一用户名索引；登录失败限流钩子（预留 Sentinel）
- [x] 3.3 **前端**：`/login`、`/register` 页面；表单校验与错误码文案；顶部用户菜单提供“退出登录”；**登录后默认 `/market`**，支持 `returnUrl`；Axios 拦截器附加 **CSRF** 与 **401 → 登录回跳**
- [x] 3.4 **前端**：`authStore`、路由 `beforeEach`：`requiresAuth`、`meta.roles`（USER/ADMIN）
- [x] 3.5 **测试**：后端 **`@WebMvcTest`/`MockMvc`** 或切片测试覆盖注册/登录/登出/CSRF 缺失；**Testcontainers** 集成测试一条完整登录-登出链路
- [x] 3.6 **测试**：前端 Vitest 覆盖 `useAuth` 或路由守卫逻辑（mock Pinia）；**Playwright** 一条用例：注册/登录 → 进入市场（可用 **MSW** 或真实后端 profile=test）

---

## 4. Skill 生命周期、审核与版本（后端 / 前端 / 测试）

- [x] 4.1 **后端**：Flyway 创建 **`skill`、`skill_version`、`audit_record`** 及索引；MyBatis Mapper；状态机 `draft/pending/published/offline`；`pending` **禁止 PATCH**
- [x] 4.2 **后端**：`POST /v1/skills`、`PATCH /v1/skills/{id}`、`POST /v1/skills/{skillId}/submit-review`；`resourceUrl` 必填；`POST /v1/skills/{skillId}/versions` 版本唯一 `409 SKILL_VERSION_CONFLICT`
- [x] 4.3 **后端**：`GET /v1/reviews/pending`、`POST /v1/reviews/{reviewId}/approve`、`POST /v1/reviews/{reviewId}/reject`（理由 10–200）；管理员下架接口（若独立路径，与评审一致）
- [x] 4.4 **前端**：`/workspace/published` 列表、创建/编辑表单、审核中禁用编辑提示、发布版本弹窗、拒绝理由展示
- [x] 4.5 **前端**：`/workspace/reviews` 管理员队列、通过/拒绝弹窗；路由 **ADMIN** 守卫与 403 页
- [x] 4.6 **测试**：后端 Service 单测覆盖状态迁移与非法迁移；集成测试覆盖 **提交审核 → 通过/拒绝**；前端组件测试覆盖 **审核中禁用** 与拒绝表单校验；E2E 覆盖作者与管理员的审核闭环（可选用 **test profile 种子数据**）

---

## 5. 市场、详情、安装命令与我的安装（后端 / 前端 / 测试）

- [x] 5.1 **后端**：`GET /v1/market/skills`（仅 `published`）、分页排序过滤；`GET /v1/market/skills/{id}` 详情聚合；Redis 缓存与失效策略（与 `design.md` 一致）
- [x] 5.2 **后端**：`POST /v1/skills/{id}/install-command`：**短 TTL 签名 URL**、单次消费、`Idempotency-Key`、**offline** 拒绝 `409`；`install_record` Flyway + upsert
- [x] 5.3 **后端**：`GET /v1/users/me/installs`；可更新与下架标记计算
- [x] 5.4 **前端**：`/market` 筛选条、`SkillCard`、分页、骨架屏、空态/错误重试；`/skills/:id` 详情布局、版本时间线、下架 Banner
- [x] 5.5 **前端**：安装面板：生成 UUID **Idempotency-Key** → 调接口 → 写剪贴板 → Toast（提示勿分享，语义与短时签名一致）；`/workspace/installed` 列表与复制最新命令
- [x] 5.6 **测试**：后端集成测试覆盖 **market 不可见 offline**、安装命令过期/重复消费；前端 Vitest 测 `SkillInstallPanel` 状态机；E2E：**市场 → 详情 → 复制命令**（mock 或 testcontainers）

---

## 6. 评分与评论（后端 / 前端 / 测试）

- [x] 6.1 **后端**：Flyway `rating` 表、`uk_user_skill`；`PUT /v1/skills/{id}/ratings`；**未安装** `422 RATING_REQUIRES_INSTALL`；聚合 `avg_rating`/`rating_count` 与缓存失效
- [ ] 6.2 **前端**：`RatingEditor`：仅安装后可提交、评论可选、乐观更新失败回滚；详情页列表刷新与 Query invalidation
- [ ] 6.3 **测试**：后端单测/集成测覆盖 **未安装拦截**、更新覆盖；前端单测覆盖分支；E2E：安装后评分 → 均分更新（**`<=5s` 口径在测试中放宽为轮询断言**）

---

## 7. 分类管理与运营看板（后端 / 前端 / 测试）

- [ ] 7.1 **后端**：Flyway `category`；`GET/POST/PUT/PATCH /v1/admin/categories...`；名称唯一；启停
- [ ] 7.2 **后端**：`GET /v1/admin/ops/dashboard`：granularity **day/week/month**，默认近 **7/30** 天；指标 + 趋势 + TopN + 活跃作者（SQL 或预聚合，按 `design.md`）
- [ ] 7.3 **前端**：`/workspace/categories` CRUD + 启停确认；`/workspace/ops` ECharts 图表与 **局部错误降级**
- [ ] 7.4 **测试**：后端集成测试覆盖 **dashboard 参数校验与空数据**；前端 Vitest 测图表数据适配器；E2E 管理员进入运营页（seed 数据）

---

## 8. 横切能力与非功能（后端 / 前端 / 测试）

- [ ] 8.1 **后端**：Sentinel/Resilience4j 限流占位与关键路径规则；Micrometer **Prometheus** 指标；**结构化日志**（prod JSON）；`X-Request-ID` MDC
- [ ] 8.2 **前端**：全局 **错误边界**、`BaseErrorBlock`、埋点命名规范；**MSW** 开发默认、可配置关闭；**Sentry** SDK（`production` DSN 来自环境变量）
- [ ] 8.3 **测试**：契约 **`config.yaml` 密码策略**：实现与 **`design.md` D8** 一致；新增回归测试确保 **登录请求体不执行前端自定义哈希**
- [ ] 8.4 **测试**：负载或冒烟脚本（可选 JMeter/k6）验证 **市场/详情 P95** 目标；Redis 降级手工演练记录

---

## 9. 联调、发布与 OpenSpec 收尾

- [ ] 9.1 **联调**：前后端 **dev** profile 对齐 Cookie 域、CORS、`SameSite`、CSRF 头；修复联调节点 A–D（见评审文档）
- [ ] 9.2 **发布**：**prod** 构建检查清单：HTTPS、Cookie `Secure`、关闭敏感 Actuator、Flyway **仅迁移不 clean**、前端 **production** 包体与 CDN
- [ ] 9.3 **文档**：根 `README.md` 已汇总前后端启动与运维说明（见 §1.1.6 / §1.2.5）；**环境变量矩阵**（dev/prod）一页说明
- [ ] 9.4 **OpenSpec**：变更保持 `openspec validate skillsops-v1-platform-baseline --type change --strict` 通过；完成后 **`openspec archive`** 合并入 `openspec/specs/`；更新 `doc/` 变更记录

---

## 任务编号说明（便于排期）

- **§1** 完成前不得合并「伪代码」主干；**Flyway + 双环境** 为 **DoD 门槛**。  
- **§2～§7** 为业务垂直切片：每个小节均含 **后端 Flyway/接口、前端页面/组件、自动化测试** 三类子任务，缺一类视为 **未完成**。  
- **§8～§9** 为生产化与非功能收口。
