-- Flyway migration (dev): create exhibition table

CREATE TABLE exhibition (
    exhibition_id VARCHAR(100) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(2000),
    start_date DATE,
    end_date DATE,
    location VARCHAR(255),
    status VARCHAR(50)
);

