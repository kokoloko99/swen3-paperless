package at.fhtw.swen3.paperless_rest.document.storage;

import java.io.ByteArrayInputStream;
import io.minio.*;
import io.minio.errors.ErrorResponseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MinioDocumentStorage implements at.fhtw.swen3.paperless_rest.document.service.DocumentStorage {
    private final MinioClient client;
    private final String bucket;
    public MinioDocumentStorage(MinioClient client, @Value("${paperless.minio.bucket}") String bucket) {
        this.client = client;
        this.bucket = bucket;
    }
    @Override
    public void store(String id, byte[] content, String contentType) {
        try {
            if (!client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())) {
                try {
                    client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                } catch (ErrorResponseException ex) {
                    if (!"BucketAlreadyOwnedByYou".equals(ex.errorResponse().code())) throw ex;
                }
            }
            try (var stream = new ByteArrayInputStream(content)) {
                client.putObject(PutObjectArgs.builder().bucket(bucket).object(id)
                        .contentType(contentType).stream(stream, content.length, -1).build());
            }
        } catch (Exception ex) {
            throw new DocumentStorageException("Could not store original document", ex);
        }
    }
}
