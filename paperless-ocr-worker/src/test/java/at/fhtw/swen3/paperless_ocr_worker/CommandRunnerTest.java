package at.fhtw.swen3.paperless_ocr_worker;
import java.nio.file.*;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.assertj.core.api.Assertions.*;
class CommandRunnerTest {
    @TempDir Path temp;
    CommandRunner runner=new CommandRunner();
    String javaCommand() { return Path.of(System.getProperty("java.home"),"bin","java").toString(); }
    @Test void runsCommandWithoutShell() throws Exception {
        Path log=temp.resolve("out");
        runner.run(List.of(javaCommand(),"-version"),log,Duration.ofSeconds(20));
        assertThat(Files.readString(log)).contains("version");
    }
    @Test void reportsNonZeroExit() {
        assertThatThrownBy(() -> runner.run(List.of(javaCommand(),"--invalid-option"),
                temp.resolve("out"),Duration.ofSeconds(20))).isInstanceOf(java.io.IOException.class);
    }
    @Test void timesOut() {
        assertThatThrownBy(() -> runner.run(List.of(javaCommand(),"-version"),
                temp.resolve("out"),Duration.ZERO)).isInstanceOf(java.io.IOException.class);
    }
}
