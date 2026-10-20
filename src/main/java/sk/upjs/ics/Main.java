package sk.upjs.ics;

import java.nio.file.Path;
import sk.upjs.ics.application.ApplicationLauncher;
import sk.upjs.ics.application.runners.RunnerSpecifications;

public class Main {
  public static void main(String[] args) {
    ApplicationLauncher launcher = new ApplicationLauncher();

    Path applicationPath =
        Path.of(
            "/home/cenaure/Downloads/Factorio_x64_0.6.4/Factorio_0.6.4.5945/bin/x64/Factorio.exe");

    RunnerSpecifications s = new RunnerSpecifications(null, null);

    launcher.runApplication(applicationPath, System.out::println, s);
  }
}
