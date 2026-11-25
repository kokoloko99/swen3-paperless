package at.fhtw.swen3.paperless_ocr_worker;

import java.io.IOException;
import java.nio.file.*;
import java.time.Duration;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TesseractTextExtractor implements TextExtractor {
    private final CommandRunner runner;
    private final String languages;
    public TesseractTextExtractor(CommandRunner runner, @Value("${paperless.ocr.languages}") String languages) {
        this.runner = runner;
        this.languages = languages;
    }
    @Override
    public String extract(byte[] content, String contentType) {
        Path directory = null;
        try {
            directory = Files.createTempDirectory("paperless-ocr-");
            Path input = directory.resolve("input");
            Files.write(input, content);
            List<Path> images;
            if ("application/pdf".equals(contentType)) {
                runner.run(List.of("pdftoppm", "-r", "200", "-png", input.toString(),
                        directory.resolve("page").toString()), directory.resolve("pdf.log"), Duration.ofMinutes(2));
                try (var paths = Files.list(directory)) {
                    images = paths.filter(p -> p.getFileName().toString().matches("page-\\d+\\.png"))
                            .sorted(Comparator.comparingInt(TesseractTextExtractor::pageNumber)).toList();
                }
                if (images.isEmpty()) throw new IOException("PDF contains no renderable pages");
            } else {
                images = List.of(input);
            }
            StringJoiner text = new StringJoiner("\n");
            for (Path image : images) {
                Path output = directory.resolve("result");
                runner.run(List.of("tesseract", image.toString(), output.toString(), "-l", languages),
                        directory.resolve("ocr.log"), Duration.ofMinutes(2));
                text.add(Files.readString(directory.resolve("result.txt")).strip());
            }
            return text.toString();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new ProcessingException("OCR interrupted", ex);
        } catch (IOException ex) {
            throw new ProcessingException("Could not extract document text", ex);
        } finally {
            if (directory != null) {
                try (var paths = Files.walk(directory)) {
                    for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) Files.deleteIfExists(path);
                } catch (IOException ex) {
                    org.slf4j.LoggerFactory.getLogger(getClass()).warn("Could not remove OCR temporary files", ex);
                }
            }
        }
    }
    private static int pageNumber(Path path) {
        String name = path.getFileName().toString();
        return Integer.parseInt(name.substring(5, name.length() - 4));
    }
}
