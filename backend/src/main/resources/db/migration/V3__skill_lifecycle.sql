CREATE TABLE IF NOT EXISTS skill (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    author_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    name VARCHAR(128) NOT NULL,
    description VARCHAR(2000) NOT NULL,
    resource_url VARCHAR(2048) NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'draft',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_skill_status (status),
    INDEX idx_skill_author (author_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS skill_version (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    skill_id BIGINT NOT NULL,
    version VARCHAR(32) NOT NULL,
    changelog VARCHAR(1000) NOT NULL,
    resource_url VARCHAR(2048) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_skill_version UNIQUE (skill_id, version),
    INDEX idx_skill_version_skill_id (skill_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS audit_record (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    skill_id BIGINT NOT NULL,
    action VARCHAR(64) NOT NULL,
    actor_id BIGINT NOT NULL,
    actor_role VARCHAR(16) NOT NULL,
    detail VARCHAR(1000) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_audit_skill_id (skill_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
