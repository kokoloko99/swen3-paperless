package at.fhtw.swen3.paperless_rest.document.storage;
import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class MinioConfiguration {
    @Bean
    MinioClient minioClient(@Value("${paperless.minio.url}") String url,
            @Value("${paperless.minio.access-key}") String accessKey,
            @Value("${paperless.minio.secret-key}") String secretKey) {
        return MinioClient.builder().endpoint(url).credentials(accessKey, secretKey).build();
    }
}
