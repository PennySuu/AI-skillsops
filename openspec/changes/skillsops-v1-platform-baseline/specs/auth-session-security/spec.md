# 能力：认证、会话与安全（auth-session-security）

## ADDED Requirements

### Requirement: 用户可通过账号密码注册

系统 SHALL 允许新用户使用用户名与密码完成注册；**SHALL NOT** 在 V1 要求邮箱验证码。

#### Scenario: 注册成功

- **WHEN** 客户端提交合法的用户名与密码且用户名唯一
- **THEN** 系统创建用户并返回成功响应（如用户标识），且密码 MUST 仅在后端以 BCrypt/Argon2 等形式存储（与《后端技术评审》一致）

#### Scenario: 注册失败（用户名冲突）

- **WHEN** 用户名已存在
- **THEN** 系统返回可映射的业务错误码与提示，不泄露用户是否存在以外的敏感信息

### Requirement: 用户可通过账号密码登录并建立会话

系统 SHALL 在校验通过后建立会话，会话凭证 MUST 通过 **HttpOnly Cookie** 下发与携带；**SHALL NOT** 要求前端将登录凭证持久化到 LocalStorage。

#### Scenario: 登录成功

- **WHEN** 用户凭正确账号密码登录
- **THEN** 系统写入 HttpOnly Cookie 会话，并返回用户档案与过期信息（若接口设计包含 `expireAt` 等字段）

#### Scenario: 登录失败

- **WHEN** 凭据错误
- **THEN** 系统返回 `401` 及错误码（如 `AUTH_INVALID_CREDENTIALS`），前端可展示统一错误提示

### Requirement: 用户可主动退出并销毁会话

系统 SHALL 提供登出能力（如 `POST /v1/auth/logout` 或与后端最终契约等价路径），在登出后服务端会话 MUST 失效，并清理/覆盖会话 Cookie；前端 MUST 清空登录态并跳转登录页或公共入口。

#### Scenario: 退出成功

- **WHEN** 已登录用户执行退出操作
- **THEN** 服务端销毁会话并返回成功响应，客户端后续访问受保护资源返回未认证结果

### Requirement: 会话过期与未认证访问处理

系统 SHALL 在会话无效或过期时返回 `401` 及明确错误码（如 `AUTH_TOKEN_EXPIRED`）；前端 MUST 引导重新登录并保留 `returnUrl`（与前端评审一致）。

#### Scenario: Token 过期

- **WHEN** 用户持有过期会话访问受保护资源
- **THEN** 系统返回 `401 AUTH_TOKEN_EXPIRED`（或等价约定），客户端跳转登录页并可在登录后回到原任务页面

### Requirement: 写操作 CSRF 防护（上线硬门禁）

系统 MUST 对 **POST/PUT/PATCH/DELETE** 等变更类请求执行 CSRF 校验（双提交 Cookie 或等价方案）；校验失败 SHALL 映射为 `403 AUTH_CSRF_INVALID`（与后端评审清单一致）。

#### Scenario: CSRF 校验失败

- **WHEN** 变更请求缺少或携带无效 CSRF Token
- **THEN** 系统拒绝请求并返回 `403` 与对应错误码

### Requirement: RBAC 与资源归属授权

系统 SHALL 支持全局角色 **USER** 与 **ADMIN**；作者编辑权限 MUST 通过 Skill 资源归属（`author_id`）判定，**SHALL NOT** 引入单独 `AUTHOR` 全局角色（与产品冻结结论一致）。

#### Scenario: 非作者尝试编辑他人 Skill

- **WHEN** 非资源归属用户尝试修改他人 Skill
- **THEN** 系统返回 `403 PERMISSION_DENIED`，且前端对编辑入口隐藏或直接链访问进入 403 页（与产品异常场景一致）

#### Scenario: 普通用户访问管理员能力

- **WHEN** 非 ADMIN 访问管理员路由或接口
- **THEN** 系统拒绝并返回权限错误；前端隐藏管理入口并无权访问页

### Requirement: 统一 API 响应与错误码映射

系统 SHALL 使用统一响应结构 `{ success, code, message, data }`，HTTP 状态码与错误码映射 MUST 与 `openspec/config.yaml` 及后端评审一致（如 `401`→认证类、`403`→`PERMISSION_DENIED`、`404`→`RESOURCE_NOT_FOUND`、`422`→校验失败、`429`→限流等）。

#### Scenario: 参数校验失败

- **WHEN** 请求参数不满足 Bean Validation/业务校验
- **THEN** 系统返回 `422` 与 `VALIDATION_FAILED`（或领域细化码），`message` 可供前端展示

### Requirement: 关键写操作幂等键

系统 SHALL 支持客户端为关键写操作提供 `Idempotency-Key`（与后端评审：安装、审核、评分等）；冲突时返回可识别的幂等冲突错误（如 `409 IDEMPOTENCY_CONFLICT`）。

#### Scenario: 幂等冲突

- **WHEN** 相同键重复用于冲突中的并发或重复提交场景（按服务端定义）
- **THEN** 系统返回 `409` 及约定错误码，不造成重复副作用

### Requirement: 请求可观测性请求头

系统 SHALL 接受并回传 `X-Request-ID`（客户端生成 UUID）；SHOULD 在响应中提供 `X-Response-Time` 等耗时头（与 `config.yaml` API 规范一致）。

#### Scenario: 携带 Request ID

- **WHEN** 请求包含 `X-Request-ID`
- **THEN** 响应回传相同 ID 以便前后端与监控关联
