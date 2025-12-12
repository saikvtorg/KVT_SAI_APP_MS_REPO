-- Canonical (SQL Server) migration: create exhibition table

CREATE TABLE exhibition (
    exhibition_id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(MAX),
    start_date DATE,
    end_date DATE,
    location VARCHAR(255),
    status VARCHAR(100)
);

