#!/usr/bin/env node
/**
 * 一致性追踪矩阵门禁：tasks.md 勾选状态 与 doc/traceability-matrix.md 中「Task Ref / 状态」必须一致。
 *
 * 规则（对每一行矩阵，状态为「阻塞」整行跳过）：
 * - 解析 Task Ref 为逗号分隔的任务编号（与 tasks.md 中 - [ ] 1.1.1 前缀一致）
 * - 若关联任务全部未勾选 → 行状态须为「未开始」
 * - 若部分勾选 → 须为「进行中」
 * - 若全部勾选 → 须为「已完成」
 *
 * 用法（仓库根目录）：
 *   node scripts/verify-traceability.mjs
 *
 * 退出码：0 通过，1 失败
 */

import fs from "fs";
import path from "path";
import { fileURLToPath } from "url";

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const REPO_ROOT = path.resolve(__dirname, "..");

const TASKS_PATH = path.join(
  REPO_ROOT,
  "openspec",
  "changes",
  "skillsops-v1-platform-baseline",
  "tasks.md",
);
const MATRIX_PATH = path.join(REPO_ROOT, "doc", "traceability-matrix.md");

const ALLOWED_STATUS = new Set(["未开始", "进行中", "已完成", "阻塞"]);

function readUtf8(p) {
  return fs.readFileSync(p, "utf8");
}

/** @returns {Set<string>} */
function parseCompletedTasks(content) {
  const done = new Set();
  const re = /^-\s+\[x\]\s+(\d+(?:\.\d+)*)\b/im;
  for (const line of content.split(/\r?\n/)) {
    const m = line.match(/^-\s+\[x\]\s+(\d+(?:\.\d+)*)\b/i);
    if (m) done.add(m[1]);
  }
  return done;
}

/** @returns {{ header: string[], rows: string[][] }} */
function parseMarkdownTable(content) {
  const lines = content.split(/\r?\n/);
  let i = 0;
  for (; i < lines.length; i++) {
    if (/^\|\s*ID\s*\|/.test(lines[i])) break;
  }
  if (i >= lines.length) {
    throw new Error("未找到以 | ID | 开头的追踪表");
  }
  const headerLine = lines[i];
  const header = headerLine
    .split("|")
    .map((c) => c.trim())
    .filter((c) => c.length > 0);
  i += 1;
  if (i >= lines.length || !/^\|[\s\-:|]+\|$/.test(lines[i].replace(/\s/g, ""))) {
    // 宽松：跳过分隔行
    i += 1;
  }
  const rows = [];
  for (; i < lines.length; i++) {
    const line = lines[i];
    if (!line.trim().startsWith("|")) break;
    const cells = line
      .split("|")
      .map((c) => c.trim())
      .slice(1, -1);
    if (cells.length < 2) continue;
    // 跳过分隔行 |---|---|
    const first = cells[0];
    if (/^-+$/.test(first) || first === "") continue;
    rows.push(cells);
  }
  return { header, rows };
}

function main() {
  if (!fs.existsSync(TASKS_PATH)) {
    console.error("缺少文件:", TASKS_PATH);
    process.exit(1);
  }
  if (!fs.existsSync(MATRIX_PATH)) {
    console.error("缺少文件:", MATRIX_PATH);
    process.exit(1);
  }

  const tasksContent = readUtf8(TASKS_PATH);
  const completed = parseCompletedTasks(tasksContent);

  const matrixContent = readUtf8(MATRIX_PATH);
  const { header, rows } = parseMarkdownTable(matrixContent);

  const idxId = header.indexOf("ID");
  const idxRef = header.indexOf("Task Ref");
  const idxStatus = header.indexOf("状态");
  if (idxId < 0 || idxRef < 0 || idxStatus < 0) {
    console.error("表头缺少 ID / Task Ref / 状态 列:", header.join(", "));
    process.exit(1);
  }

  const errors = [];

  for (const cells of rows) {
    if (cells.length <= Math.max(idxId, idxRef, idxStatus)) continue;
    const rowId = cells[idxId];
    const refRaw = cells[idxRef];
    const status = cells[idxStatus];

    if (!ALLOWED_STATUS.has(status)) {
      errors.push(`[${rowId}] 非法状态「${status}」，允许: ${[...ALLOWED_STATUS].join(", ")}`);
      continue;
    }
    if (status === "阻塞") continue;

    const refs = refRaw
      .split(",")
      .map((s) => s.trim())
      .filter(Boolean);

    if (refs.length === 0) {
      errors.push(`[${rowId}] Task Ref 为空，无法与 tasks 对齐`);
      continue;
    }

    let doneCount = 0;
    for (const r of refs) {
      if (completed.has(r)) doneCount += 1;
    }

    let expected;
    if (doneCount === 0) expected = "未开始";
    else if (doneCount === refs.length) expected = "已完成";
    else expected = "进行中";

    if (status !== expected) {
      errors.push(
        `[${rowId}] 状态为「${status}」，但与 tasks 勾选不一致：Task Ref=[${refs.join(", ")}]，期望「${expected}」（已完成 ${doneCount}/${refs.length}）`,
      );
    }
  }

  if (errors.length) {
    console.error("traceability-matrix 门禁失败：\n");
    for (const e of errors) console.error(" -", e);
    console.error(
      "\n修复方式：同步更新 doc/traceability-matrix.md 的「状态」列，或回退 openspec/.../tasks.md 中过早勾选的 [x]。\n",
    );
    process.exit(1);
  }

  console.log(
    "traceability-matrix 门禁通过（tasks 勾选与矩阵「Task Ref/状态」一致）。已勾选任务数:",
    completed.size,
  );
}

main();
