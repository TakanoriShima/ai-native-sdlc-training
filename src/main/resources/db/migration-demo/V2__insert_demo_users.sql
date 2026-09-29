INSERT INTO users (name, email, password_hash, role, enabled, created_at, updated_at)
VALUES
    ('デモ営業', 'sales@example.com', '{bcrypt}$2a$10$AP/MdUnZJk6OjR59puG2d.R7rbbY04y/Gk8KbxWMklqCn1E823aye', 'SALES', TRUE, NOW(), NOW()),
    ('デモ営業責任者', 'sales-manager@example.com', '{bcrypt}$2a$10$AP/MdUnZJk6OjR59puG2d.R7rbbY04y/Gk8KbxWMklqCn1E823aye', 'SALES_MANAGER', TRUE, NOW(), NOW()),
    ('デモ作業部門責任者', 'work-manager@example.com', '{bcrypt}$2a$10$AP/MdUnZJk6OjR59puG2d.R7rbbY04y/Gk8KbxWMklqCn1E823aye', 'WORK_MANAGER', TRUE, NOW(), NOW()),
    ('デモ作業担当者', 'worker@example.com', '{bcrypt}$2a$10$AP/MdUnZJk6OjR59puG2d.R7rbbY04y/Gk8KbxWMklqCn1E823aye', 'WORKER', TRUE, NOW(), NOW()),
    ('デモ経理', 'accounting@example.com', '{bcrypt}$2a$10$AP/MdUnZJk6OjR59puG2d.R7rbbY04y/Gk8KbxWMklqCn1E823aye', 'ACCOUNTING', TRUE, NOW(), NOW()),
    ('デモ管理者', 'admin@example.com', '{bcrypt}$2a$10$AP/MdUnZJk6OjR59puG2d.R7rbbY04y/Gk8KbxWMklqCn1E823aye', 'ADMIN', TRUE, NOW(), NOW());
