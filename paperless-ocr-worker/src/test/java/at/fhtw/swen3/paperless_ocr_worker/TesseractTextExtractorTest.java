package at.fhtw.swen3.paperless_ocr_worker;
import java.nio.file.*;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
class TesseractTextExtractorTest {
    CommandRunner runner = mock(CommandRunner.class);
    TesseractTextExtractor extractor = new TesseractTextExtractor(runner, "eng+deu");
    Path work;
    @Test void extractsImageAndCleansTemporaryFiles() throws Exception {
        doAnswer(i -> {
            List<String> cmd=i.getArgument(0); work=Path.of(cmd.get(1)).getParent();
            Files.writeString(Path.of(cmd.get(2)+".txt"), "  Hello world  "); return null;
        }).when(runner).run(anyList(), any(), any());
        assertThat(extractor.extract(new byte[]{1}, "image/png")).isEqualTo("Hello world");
        assertThat(work).doesNotExist();
    }
    @Test void rendersAllPdfPagesInNumericOrder() throws Exception {
        doAnswer(i -> {
            List<String> cmd=i.getArgument(0);
            if (cmd.getFirst().equals("pdftoppm")) {
                work=Path.of(cmd.getLast()).getParent();
                Files.write(work.resolve("page-10.png"), new byte[]{1});
                Files.write(work.resolve("page-2.png"), new byte[]{1});
            } else {
                Files.writeString(Path.of(cmd.get(2)+".txt"), Path.of(cmd.get(1)).getFileName().toString());
            } return null;
        }).when(runner).run(anyList(), any(), any());
        assertThat(extractor.extract(new byte[]{1}, "application/pdf")).isEqualTo("page-2.png\npage-10.png");
        assertThat(work).doesNotExist();
    }
    @Test void failsWhenPdfHasNoPages() {
        assertThatThrownBy(() -> extractor.extract(new byte[]{1}, "application/pdf"))
                .isInstanceOf(ProcessingException.class).hasCauseInstanceOf(IOException.class);
    }
    @Test void wrapsCommandFailureAndCleansUp() throws Exception {
        doAnswer(i -> { work=((Path)i.getArgument(1)).getParent(); throw new IOException("failed"); })
                .when(runner).run(anyList(), any(), any());
        assertThatThrownBy(() -> extractor.extract(new byte[]{1}, "image/png")).isInstanceOf(ProcessingException.class);
        assertThat(work).doesNotExist();
    }
    @Test void preservesInterrupt() throws Exception {
        doThrow(new InterruptedException()).when(runner).run(anyList(),any(),any());
        try {
            assertThatThrownBy(() -> extractor.extract(new byte[]{1},"image/png")).isInstanceOf(ProcessingException.class);
            assertThat(Thread.currentThread().isInterrupted()).isTrue();
        } finally { Thread.interrupted(); }
    }
}
