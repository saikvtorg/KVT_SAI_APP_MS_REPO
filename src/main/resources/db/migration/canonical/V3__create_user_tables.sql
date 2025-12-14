-- Canonical (SQL Server) migration: create user profile, feedback, and quiz_result tables

CREATE TABLE app_user_profile (
    user_id VARCHAR(36) PRIMARY KEY,
    full_name VARCHAR(255),
    email VARCHAR(255) UNIQUE,
    phone VARCHAR(50),
    preferred_language VARCHAR(50),
    country VARCHAR(100),
    address VARCHAR(MAX)
);

CREATE TABLE user_feedback (
    feedback_id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36),
    exhibition_id VARCHAR(255),
    comments VARCHAR(MAX),
    rating INT,
    created_at DATETIME2
);

CREATE TABLE user_quiz_result (
    result_id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36),
    module_id VARCHAR(255),
    result INT,
    total_marks INT,
    percentage FLOAT,
    points INT,
    taken_at DATETIME2
);

ALTER TABLE user_feedback ADD CONSTRAINT FK_feedback_user FOREIGN KEY (user_id) REFERENCES app_user_profile (user_id);
ALTER TABLE user_quiz_result ADD CONSTRAINT FK_quiz_user FOREIGN KEY (user_id) REFERENCES app_user_profile (user_id);
