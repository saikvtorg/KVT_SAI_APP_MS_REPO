-- H2-compatible migration for user tables

CREATE TABLE app_user_profile (
    user_id VARCHAR(36) PRIMARY KEY,
    full_name VARCHAR(255),
    email VARCHAR(255),
    phone VARCHAR(50),
    preferred_language VARCHAR(50),
    country VARCHAR(100),
    address VARCHAR(2000)
);

CREATE TABLE user_feedback (
    feedback_id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36),
    exhibition_id VARCHAR(255),
    comments VARCHAR(2000),
    rating INT,
    created_at TIMESTAMP
);

CREATE TABLE user_quiz_result (
    result_id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36),
    module_id VARCHAR(255),
    result INT,
    total_marks INT,
    percentage DOUBLE,
    points INT,
    taken_at TIMESTAMP
);

ALTER TABLE user_feedback ADD CONSTRAINT FK_feedback_user FOREIGN KEY (user_id) REFERENCES app_user_profile (user_id);
ALTER TABLE user_quiz_result ADD CONSTRAINT FK_quiz_user FOREIGN KEY (user_id) REFERENCES app_user_profile (user_id);
