CREATE TABLE IF NOT EXISTS rating (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    skill_id BIGINT NOT NULL,
    score TINYINT NOT NULL,
    comment VARCHAR(1000) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_rating_user_skill UNIQUE (user_id, skill_id),
    INDEX idx_rating_skill (skill_id),
    INDEX idx_rating_user (user_id),
    CONSTRAINT ck_rating_score CHECK (score >= 1 AND score <= 5)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
