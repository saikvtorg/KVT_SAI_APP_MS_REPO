-- Flyway migration: create exhibition table (H2/SQL compatible)

CREATE TABLE exhibition (
    exhibition_id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    start_date DATE,
    end_date DATE,
    location VARCHAR(255),
    status VARCHAR(100)
);

