CREATE TABLE document (
    id UUID PRIMARY KEY,
    filename VARCHAR(255),
    content_type VARCHAR(255),
    size BIGINT NOT NULL,
    uploaded_at TIMESTAMP
);
