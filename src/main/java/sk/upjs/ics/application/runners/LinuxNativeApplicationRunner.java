package sk.upjs.ics.application.runners;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class LinuxNativeApplicationRunner implements IApplicationRunner {
  private final RunnerSpecifications _specifications;

  public final RunnerKind kind() {
    return RunnerKind.LINUX_NATIVE;
  }
  ;

  @Override
  public void run(Path applicationPath, Consumer<String> logConsumer)
      throws RunnerException, IOException, InterruptedException {
    if (!Files.exists(applicationPath))
      throw new RunnerException(RunnerExceptionCodes.APPLICATION_PATH_INVALID, applicationPath);

    System.out.println("Windows executable");
    int exitCode = this.nativeRun(applicationPath, Path.of("./native-run.log"), logConsumer);
    System.out.println("Exit code: " + exitCode);
  }

  private int nativeRun(Path applicationPath, Path logPath, Consumer<String> logConsumer)
      throws IOException, InterruptedException {
    if (!isExecutable(applicationPath))
      throw new RunnerException(RunnerExceptionCodes.NOT_EXECUTABLE);

    ProcessBuilder pb = new ProcessBuilder().command(applicationPath.toAbsolutePath().toString());

    pb.redirectErrorStream(true);
    Process process = pb.start();

    try (BufferedReader bufferedReader =
        new BufferedReader(new InputStreamReader(process.getInputStream()))) {
      BufferedWriter logFile =
          Files.newBufferedWriter(logPath, StandardOpenOption.CREATE, StandardOpenOption.APPEND);

      String line;
      while ((line = bufferedReader.readLine()) != null) {
        System.out.println(line);
        logFile.write(line);
        logFile.newLine();
        logConsumer.accept(line);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    // returns exit code
    return process.waitFor();
  }

  private boolean isExecutable(Path applicationPath) {
    return Files.isExecutable(applicationPath);
  }
}
