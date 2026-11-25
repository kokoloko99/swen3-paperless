package at.fhtw.swen3.paperless_ocr_worker;
import io.minio.*;
import java.io.ByteArrayInputStream;
import okhttp3.Headers;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
class MinioDocumentStorageTest {
    MinioClient client=mock(MinioClient.class);
    DocumentStorage storage=new MinioDocumentStorage(client,"documents");
    @Test void fetchesOriginalByObjectKey() throws Exception {
        var response=spy(new GetObjectResponse(new Headers.Builder().build(),"documents",null,"id",new ByteArrayInputStream(new byte[]{1,2,3})));
        when(client.getObject(any())).thenReturn(response);
        assertThat(storage.load("id")).containsExactly(1,2,3);
        var arg=org.mockito.ArgumentCaptor.forClass(GetObjectArgs.class);
        verify(client).getObject(arg.capture());
        assertThat(arg.getValue().object()).isEqualTo("id");
        assertThat(arg.getValue().bucket()).isEqualTo("documents");
        verify(response).close();
    }
    @Test void wrapsDownloadFailure() throws Exception {
        when(client.getObject(any())).thenThrow(new java.io.IOException("offline"));
        assertThatThrownBy(() -> storage.load("id")).isInstanceOf(ProcessingException.class)
            .hasCauseInstanceOf(java.io.IOException.class);
    }
}
