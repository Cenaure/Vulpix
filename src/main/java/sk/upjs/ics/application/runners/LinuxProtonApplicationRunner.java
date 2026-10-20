package sk.upjs.ics.application.runners;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor
public class LinuxProtonApplicationRunner implements IApplicationRunner {
  @NonNull private final RunnerSpecifications _specifications;

  public final RunnerKind kind() {
    return RunnerKind.LINUX_PROTON;
  }
  ;

  @Override
  public void run(Path applicationPath, Consumer<String> logConsumer)
      throws RunnerException, IOException, InterruptedException {
    if (!Files.exists(applicationPath))
      throw new RunnerException(RunnerExceptionCodes.APPLICATION_PATH_INVALID, applicationPath);

    System.out.println("Windows executable");
    int exitCode = this.umuRun(applicationPath, Path.of("./umu-run.log"), logConsumer);
    System.out.println("Exit code: " + exitCode);
  }

  /**
   * Executes application using the "umu-run" command, writes output to a log file, and invokes consumer
   */
  private int umuRun(Path applicationPath, Path logPath, Consumer<String> logConsumer)
      throws IOException, InterruptedException {
    ProcessBuilder pb = new ProcessBuilder().command("umu-run", applicationPath.toString());

    Map<String, String> environments = pb.environment();

    if (this._specifications.protonPath() != null && !this._specifications.protonPath().isEmpty())
      environments.put("PROTONPATH", this._specifications.protonPath());
    if (this._specifications.winePrefix() != null && !this._specifications.winePrefix().isEmpty())
      environments.put("WINEPREFIX", this._specifications.winePrefix());

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
}
