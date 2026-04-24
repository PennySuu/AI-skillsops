# SkillsOps V1 禁止自由扩展清单（out-of-scope）

依据 `openspec/changes/skillsops-v1-platform-baseline/design.md`「Non-Goals」与三份评审文档（`doc/产品与交互文档.md`、`doc/后端技术评审文档.md`、`doc/前端技术评审文档.md`）**未定义**的能力，V1 **不得实现**，避免范围蔓延。

## 明确不做（示例，与 design 对齐）

- 企业 OpenID / 外部 IdP 单点登录（V1 仅账号密码 + 会话 Cookie）。
- 支付、计费、订单商业化能力。
- 对外开放公共 Skill 市场（非团队内场景）。
- 平台内自动执行用户安装脚本；安装命令中的**长期明文 Token**。
- V1 强制 WebSocket / SSE 实时推送（以缓存 TTL + 写后失效满足「近实时」口径）。
- 微服务拆分、多租户 SaaS 控制台（V1 为模块化单体 + 工作台）。

## 变更规则

若产品需要上述能力，须：**先更新 OpenSpec（proposal/design/spec/tasks）并评审通过**，再进入实现；不得在未更新规范的情况下直接加代码。
