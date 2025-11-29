-- Flyway migration: create module, stall, and poster_content tables (SQL Server compatible)

CREATE TABLE module (
    module_id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255),
    description VARCHAR(MAX),
    assigned_team_id VARCHAR(255),
    exhibition_id VARCHAR(255)
);

CREATE TABLE stall (
    stall_id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255),
    description VARCHAR(MAX),
    stall_number VARCHAR(255),
    layout VARCHAR(MAX),
    module_id VARCHAR(255)
);

CREATE TABLE poster_content (
    content_id VARCHAR(255) PRIMARY KEY,
    language_code VARCHAR(10),
    poster_media_url VARCHAR(1024),
    content_text VARCHAR(MAX),
    stall_id VARCHAR(255)
);

ALTER TABLE module ADD CONSTRAINT FK_module_exhibition FOREIGN KEY (exhibition_id) REFERENCES exhibition (exhibition_id);
ALTER TABLE stall ADD CONSTRAINT FK_stall_module FOREIGN KEY (module_id) REFERENCES module (module_id);
ALTER TABLE poster_content ADD CONSTRAINT FK_postercontent_stall FOREIGN KEY (stall_id) REFERENCES stall (stall_id);
