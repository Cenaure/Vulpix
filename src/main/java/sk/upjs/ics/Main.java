package sk.upjs.ics;

import sk.upjs.ics.application.ApplicationLauncher;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
  public static void main(String[] args) {
    ApplicationLauncher launcher = new ApplicationLauncher();

    Path applicationPath = Path.of("/home/cenaure/Downloads/Factorio_x64_0.6.4/Factorio_0.6.4.5945/bin/x64/Factorio.exe");

    launcher.runApplication(applicationPath, System.out::println);
  }
}
