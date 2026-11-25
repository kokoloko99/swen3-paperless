package at.fhtw.swen3.paperless_ocr_worker;
import io.minio.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
@Component
public class MinioDocumentStorage implements DocumentStorage {
    private final MinioClient client;
    private final String bucket;
    public MinioDocumentStorage(MinioClient client, @Value("${paperless.minio.bucket}") String bucket) {
        this.client = client; this.bucket = bucket;
    }
    @Override
    public byte[] load(String id) {
        try (var stream = client.getObject(GetObjectArgs.builder().bucket(bucket).object(id).build())) {
            return stream.readAllBytes();
        } catch (Exception ex) {
            throw new ProcessingException("Could not load original document from MinIO", ex);
        }
    }
}
