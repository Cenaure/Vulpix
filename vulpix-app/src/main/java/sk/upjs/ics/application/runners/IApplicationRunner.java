package sk.upjs.ics.application.runners;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Consumer;

public interface IApplicationRunner {
  RunnerKind kind();

  RunnerSpecifications _specifications = null;

  void run(Path applicationPath, Consumer<String> logConsumer)
      throws RunnerException, IOException, InterruptedException;
}
