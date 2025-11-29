-- Flyway migration (dev) V2: create module, stall, poster_content tables (H2-compatible)

CREATE TABLE IF NOT EXISTS module (
    module_id VARCHAR(255) NOT NULL PRIMARY KEY,
    name VARCHAR(255),
    description VARCHAR(4000),
    assigned_team_id VARCHAR(255),
    exhibition_id VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS stall (
    stall_id VARCHAR(255) NOT NULL PRIMARY KEY,
    name VARCHAR(255),
    description VARCHAR(4000),
    stall_number VARCHAR(255),
    layout VARCHAR(1024),
    module_id VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS poster_content (
    content_id VARCHAR(255) NOT NULL PRIMARY KEY,
    language_code VARCHAR(10),
    poster_media_url VARCHAR(1024),
    content_text VARCHAR(4000),
    stall_id VARCHAR(255)
);

-- Foreign keys (add only if referenced tables exist)
ALTER TABLE IF EXISTS module ADD CONSTRAINT IF NOT EXISTS FK_module_exhibition FOREIGN KEY (exhibition_id) REFERENCES exhibition (exhibition_id);
ALTER TABLE IF EXISTS stall ADD CONSTRAINT IF NOT EXISTS FK_stall_module FOREIGN KEY (module_id) REFERENCES module (module_id);
ALTER TABLE IF EXISTS poster_content ADD CONSTRAINT IF NOT EXISTS FK_postercontent_stall FOREIGN KEY (stall_id) REFERENCES stall (stall_id);
