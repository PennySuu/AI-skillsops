CREATE TABLE IF NOT EXISTS review_record (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    skill_id BIGINT NOT NULL,
    status VARCHAR(16) NOT NULL,
    reason VARCHAR(200) NULL,
    submitted_by BIGINT NOT NULL,
    reviewed_by BIGINT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_review_status (status),
    INDEX idx_review_skill (skill_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
