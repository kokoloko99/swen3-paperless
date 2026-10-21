ALTER TABLE document ADD COLUMN created_at TIMESTAMP;
ALTER TABLE document ADD COLUMN updated_at TIMESTAMP;
UPDATE document SET created_at = uploaded_at, updated_at = uploaded_at;

ALTER TABLE comment ADD COLUMN updated_at TIMESTAMP;
UPDATE comment SET updated_at = created_at;