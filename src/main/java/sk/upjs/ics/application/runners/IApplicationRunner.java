package sk.upjs.ics.application.runners;

import java.nio.file.Path;
import java.util.function.Consumer;

public interface IApplicationRunner {
    void run(Path applicationPath, Consumer<String> logConsumer) throws RunnerException;
}
