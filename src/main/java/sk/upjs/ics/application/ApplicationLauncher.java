package sk.upjs.ics.application;

import sk.upjs.ics.application.runners.IApplicationRunner;
import sk.upjs.ics.application.runners.LinuxApplicationRunner;

import java.nio.file.Path;
import java.util.function.Consumer;

public class ApplicationLauncher {

    public static IApplicationRunner retrieveRunner() {
        String os = System.getProperty("os.name");

        System.out.println(os);

        if (os.contains("Linux"))
            return new LinuxApplicationRunner();
//        else if (os.contains("Windows"))
//            return new WindowsApplicationRunner();

        return null;
    }

    public void runApplication(Path applicationPath, Consumer<String> stageConsumer) {
        IApplicationRunner runner = retrieveRunner();

        if (runner != null) {
            runner.run(applicationPath, line -> getCurrentStage(line, stageConsumer));
        }
    }

    private void getCurrentStage(String message, Consumer<String> stageConsumer) {
        if (message.contains("Downloading GE-PROTON")) {
            stageConsumer.accept("Downloading GE-PROTON");
        } else if (message.contains("Running")) {
            stageConsumer.accept("Running");
        }
    }
}
