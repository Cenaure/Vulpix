package sk.upjs.ics;

import javafx.application.Application;
import javafx.stage.Stage;
import sk.upjs.ics.collection.dao.CollectionDao;
import sk.upjs.ics.library.dao.ApplicationDao;
import sk.upjs.ics.library.dao.ApplicationSpecificationsDao;
import sk.upjs.ics.library.dao.InMemoryApplicationDao;
import sk.upjs.ics.util.AppContext;

public class Main extends Application {
  //  public static void main(String[] args) {
  //        ApplicationLauncher launcher = new ApplicationLauncher();
  //
  //        Path applicationPath =
  //            Path.of(
  //
  //     "/home/cenaure/Downloads/Factorio_x64_0.6.4/Factorio_0.6.4.5945/bin/x64/Factorio.exe");
  //
  //        RunnerSpecifications s = new RunnerSpecifications(null, null);
  //
  //        launcher.runApplication(applicationPath, System.out::println, s);
  //    launch(args);
  //  }

  @Override
  public void start(Stage primaryStage) throws Exception {
    primaryStage.setTitle("Vulpix");
    var appDao = new InMemoryApplicationDao();

    ApplicationDao applicationDao = daoFactory.INSTANCE.applicationDao();
    CollectionDao collectionDao = daoFactory.INSTANCE.collectionDao();
    ApplicationSpecificationsDao specificationsDao =
        daoFactory.INSTANCE.applicationSpecificationsDao();

    AppContext ctx =
        new AppContext(
            primaryStage, new MockAuthService(), applicationDao, collectionDao, specificationsDao);
    ctx.showLogin();
  }
}
