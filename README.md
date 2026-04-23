# SkillsOps 全栈项目（OpenSpec 规范驱动）

基于 `openspec/config.yaml` 的前后端全栈项目规范说明。  
本项目采用 API-first + SDD（Spec-Driven Development）方式，先定义规范与契约，再实现后端与前端，确保可追溯、可审计、可协作。

## 1. 项目定位

- 开发模式：API-first + SDD
- 目标：统一前后端契约、响应结构、版本管理与向后兼容策略
- 输出要求：规范、设计、任务与变更文档保持同步
- 文档语言：中文

## 2. 技术栈

### 后端

- Java 17
- Spring Boot 3.3.x
- MyBatis
- Maven
- MySQL 8.0+
- Redis 7.0+

### 前端

- Vue 3
- TypeScript
- Vite
- ESLint
- Node.js 20.19+

### 文档与规范

- Markdown
- OpenSpec（`openspec/`）

## 3. 目录约定

```text
skillsops/
├─ backend/          # 后端工程（Spring Boot + MyBatis）
├─ frontend/         # 前端工程（Vue3 + TS + Vite）
├─ doc/              # 需求、变更、影响说明文档
└─ openspec/         # SDD 规范与变更定义
```

> 当前仓库以规范文档为主，若尚未初始化 `backend/`、`frontend/`，请先按本 README 建立目录与工程骨架。

## 4. 开发流程（推荐）

1. 在 `openspec/specs/` 编写或更新 API/业务规范
2. 明确版本与兼容策略（`/v1`、`/v2`）
3. 按规范实现后端接口与前端对接
4. 同步更新 `doc/` 中的变更说明（原因、影响范围、迁移说明）
5. 补齐单元、集成、E2E 测试并验证通过

## 5. API 设计基线

### 路径与版本

- 使用复数资源名：`/v1/users`、`/v1/orders/{orderId}/items`
- 统一版本前缀：`/v1/`、`/v2/`
- 避免动词，特殊动作采用子资源（如取消订单）

### 统一响应结构

```json
{
  "success": true,
  "code": "OK",
  "message": "success",
  "data": {}
}
```

- `success=true` 时，`data` 为业务数据
- `success=false` 时，`code` 为错误码，`message` 为用户提示

### 分页/排序/过滤

- 分页：`page`、`size`
- 排序：`sort=createdAt,desc`
- 过滤：`status=active`、`price[gte]=100&price[lte]=500`、`q=关键词`

### 关键请求头

- `Content-Type: application/json`
- `X-Request-ID: <uuid>`
- `Idempotency-Key: <uuid>`（关键 POST 操作）
- `Authorization: Bearer <token>` 或 Cookie

## 6. 后端实现规范（摘要）

- 分层：`Controller -> Service -> Repository`
- Controller 仅做参数校验、转发与响应封装，禁止承载业务逻辑
- 异常统一由 `@ControllerAdvice` 处理
- MyBatis 强制参数化（`#{}`），禁止 `${}` 拼接用户输入
- SQL 禁止 `SELECT *`，显式字段并关注索引与 N+1
- 生产配置建议使用 `dev/test/staging/prod` Profiles
- 启用健康检查：`/actuator/health`，支持优雅停机

## 7. 前端实现规范（摘要）

- 目录建议：`views/`、`components/`、`composables/`、`stores/`、`api/`、`types/`、`utils/`
- 组件默认使用 `<script setup lang="ts">`
- TypeScript 开启 `strict: true`，禁止 `any` 与 `@ts-ignore`
- Axios 实例统一处理拦截器与响应 envelope 解析
- API 与类型定义与后端 DTO 保持一致
- 关键错误码按状态码分类处理（401/403/404/422/429/500）

## 8. 安全规范（必须）

- 全链路 HTTPS
- 密码传输前端加盐哈希，后端 BCrypt/Argon2 存储
- Session 使用 HTTPOnly Cookie，禁止 LocalStorage 存储敏感凭证
- 防御 XSS/CSRF/SQL 注入（参数化 SQL）
- 禁止硬编码密钥，敏感配置使用环境变量或配置中心
- 日志与异常中敏感数据必须脱敏

## 9. 错误码与状态码约定

- 错误码命名：`MODULE_ERROR_TYPE`（如 `AUTH_TOKEN_EXPIRED`）
- 常用通用码：`VALIDATION_FAILED`、`RESOURCE_NOT_FOUND`、`PERMISSION_DENIED`、`OPERATION_FAILED`
- 状态码映射示例：
  - `401` -> `AUTH_*`
  - `403` -> `PERMISSION_DENIED`
  - `404` -> `RESOURCE_NOT_FOUND`
  - `422` -> `VALIDATION_FAILED`
  - `429` -> `RATE_LIMIT_EXCEEDED`
  - `500` -> `SYSTEM_*`

## 10. 测试要求

- 单元测试覆盖率 >= 70%，核心模块 >= 85%
- 后端：JUnit + Mockito
- 前端：Vitest + `@vue/test-utils`
- 集成测试覆盖 API、数据库、缓存
- E2E 覆盖关键用户流程（Playwright/Cypress）

## 11. 初始化建议（工程尚未创建时）

```powershell
# 1) 创建目录
mkdir backend, frontend, doc, openspec\specs

# 2) 后端初始化（示例）
# 使用 Spring Initializr 生成 Maven + Java17 + Spring Boot 3.3.x 工程到 backend/

# 3) 前端初始化（示例）
cd frontend
npm create vite@latest . -- --template vue-ts
npm install
```

## 12. 文档与协作约定

- 规范更新必须同步文档与变更记录
- 文档需说明 API 版本、兼容策略、废弃说明
- 代码变更前先更新规范，变更后回填 `doc/` 影响范围
- 前后端类型定义保持同步，契约一致
