CREATE TABLE comment (
    id UUID PRIMARY KEY,
    document_id UUID NOT NULL REFERENCES document(id),
    author VARCHAR(255),
    text VARCHAR(1000),
    created_at TIMESTAMP
);

CREATE INDEX idx_comment_document_id ON comment(document_id);
