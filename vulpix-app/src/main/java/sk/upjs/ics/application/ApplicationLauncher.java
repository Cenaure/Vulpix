package sk.upjs.ics.application;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;
import sk.upjs.ics.application.runners.*;

public class ApplicationLauncher {

  public static IApplicationRunner retrieveRunner(
      Path forApplication, RunnerSpecifications specifications) {
    String os = System.getProperty("os.name");
    System.out.println(os);

    RunnerKind kind =
        os.contains("Linux")
            ? isWindowsExecutable(forApplication)
                ? RunnerKind.LINUX_PROTON
                : RunnerKind.LINUX_NATIVE
            : RunnerKind.WINDOWS_NATIVE;

    return switch (kind) {
      case RunnerKind.LINUX_NATIVE -> new LinuxNativeApplicationRunner(specifications);
      case RunnerKind.LINUX_PROTON -> new LinuxProtonApplicationRunner(specifications);
      // case RunnerKind.WINDOWS_NATIVE -> new WindowsApplicationRunner(specifications);
      default -> null;
    };
  }

  public void runApplication(
      Path applicationPath, Consumer<String> stageConsumer, RunnerSpecifications specifications)
      throws RunnerException {
    if (!Files.exists(applicationPath))
      throw new RunnerException(RunnerExceptionCodes.APPLICATION_PATH_INVALID, applicationPath);

    IApplicationRunner runner = retrieveRunner(applicationPath, specifications);
    if (runner == null) throw new RunnerException(RunnerExceptionCodes.NOT_EXECUTABLE);

    try {
      System.out.println("RunnerKind=" + runner.kind());
      runner.run(applicationPath, line -> getCurrentStage(line, stageConsumer));
    } catch (IOException e) {
      throw new RunnerException(RunnerExceptionCodes.APPLICATION_PATH_INVALID, e, applicationPath);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RunnerException(RunnerExceptionCodes.APPLICATION_APPLICATION_INTERRUPTED, e);
    }
  }

  private void getCurrentStage(String message, Consumer<String> stageConsumer) {
    if (message.contains("Downloading GE-PROTON")) {
      stageConsumer.accept("Downloading GE-PROTON");
    } else if (message.contains("Running")) {
      stageConsumer.accept("Running");
    }
  }

  private static boolean isWindowsExecutable(Path filePath) {
    byte[] bytes;

    try (InputStream inputStream = Files.newInputStream(filePath); ) {
      bytes = inputStream.readNBytes(2);
    } catch (IOException e) {
      throw new RunnerException(RunnerExceptionCodes.APPLICATION_PATH_INVALID, e, filePath);
    }

    // Windows executable starts with 'MZ'
    // https://en.wikipedia.org/wiki/DOS_MZ_executable
    return bytes.length == 2 && bytes[0] == 'M' && bytes[1] == 'Z';
  }
}
