package sk.upjs.ics;

import javafx.application.Application;
import javafx.stage.Stage;
import sk.upjs.ics.application.memoryDaos.MemoryApplicationDao;
import sk.upjs.ics.application.memoryDaos.MemoryCollectionDao;

public class Main extends Application {
  public static void main(String[] args) {
    //    ApplicationLauncher launcher = new ApplicationLauncher();
    //
    //    Path applicationPath =
    //        Path.of(
    //
    // "/home/cenaure/Downloads/Factorio_x64_0.6.4/Factorio_0.6.4.5945/bin/x64/Factorio.exe");
    //
    //    RunnerSpecifications s = new RunnerSpecifications(null, null);
    //
    //    launcher.runApplication(applicationPath, System.out::println, s);
    launch(args);
  }

  @Override
  public void start(Stage primaryStage) throws Exception {
    primaryStage.setTitle("Login");
    var appDao = new MemoryApplicationDao();

    AppContext ctx =
        new AppContext(
            primaryStage, new MockAuthService(), appDao, new MemoryCollectionDao(appDao));

    ctx.showLogin();
  }
}
