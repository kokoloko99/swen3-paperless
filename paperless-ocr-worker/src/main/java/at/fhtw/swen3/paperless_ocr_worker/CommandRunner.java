package at.fhtw.swen3.paperless_ocr_worker;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Component;

@Component
public class CommandRunner {
    public void run(List<String> command, Path log, Duration timeout) throws IOException, InterruptedException {
        Process process = new ProcessBuilder(command).redirectErrorStream(true)
                .redirectOutput(log.toFile()).start();
        try {
            if (!process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS)) {
                throw new IOException("OCR command timed out");
            }
            if (process.exitValue() != 0) throw new IOException("OCR command failed: " + command.getFirst());
        } finally {
            if (process.isAlive()) process.destroyForcibly();
        }
    }
}
