-- 基线占位表：保证全新库在 Flyway migrate 后具备可校验的 schema（业务表在后续任务中演进）
CREATE TABLE IF NOT EXISTS skillsops_schema_meta (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    note VARCHAR(128) NOT NULL DEFAULT 'baseline'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
