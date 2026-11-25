package at.fhtw.swen3.paperless_rest.document.storage;
import io.minio.*;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
class MinioDocumentStorageTest {
    MinioClient client=mock(MinioClient.class);
    at.fhtw.swen3.paperless_rest.document.service.DocumentStorage storage=new MinioDocumentStorage(client,"documents");
    @Test void createsMissingBucketAndStoresOriginal() throws Exception {
        storage.store("id",new byte[]{1},"application/pdf");
        verify(client).makeBucket(any(MakeBucketArgs.class));
        var captor=org.mockito.ArgumentCaptor.forClass(PutObjectArgs.class);
        verify(client).putObject(captor.capture());
        assertThat(captor.getValue().object()).isEqualTo("id");
        assertThat(captor.getValue().contentType()).isEqualTo("application/pdf");
    }
    @Test void reusesBucket() throws Exception {
        when(client.bucketExists(any())).thenReturn(true);
        storage.store("id",new byte[]{1},"image/png");
        verify(client,never()).makeBucket(any()); verify(client).putObject(any());
    }
    @Test void wrapsStorageFailure() throws Exception {
        when(client.bucketExists(any())).thenThrow(new java.io.IOException("down"));
        assertThatThrownBy(() -> storage.store("id",new byte[]{1},"image/png"))
            .isInstanceOf(DocumentStorageException.class).hasCauseInstanceOf(java.io.IOException.class);
        verify(client,never()).putObject(any());
    }
}
