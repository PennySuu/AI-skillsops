## 变更说明

- 做了什么、为何要做（面向评审与发布记录）

## 自检

- [ ] 已阅读 `openspec/config.yaml` 与相关 `spec.md` / `design.md`
- [ ] 若勾选 `openspec/changes/skillsops-v1-platform-baseline/tasks.md` 中的任务：已同步更新 `doc/traceability-matrix.md` 对应行的 **状态**
- [ ] 仓库根目录执行 `node scripts/verify-traceability.mjs` 通过
- [ ] `backend`：`mvn -B verify`；`frontend`：`npm run lint && npm run typecheck && npm run test && npm run build`
