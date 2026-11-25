-- Löscht beim Entfernen eines Dokuments automatisch alle zugehörigen Kommentare.
ALTER TABLE comment DROP CONSTRAINT comment_document_id_fkey;
ALTER TABLE comment ADD CONSTRAINT comment_document_id_fkey
    FOREIGN KEY (document_id) REFERENCES document(id) ON DELETE CASCADE;
