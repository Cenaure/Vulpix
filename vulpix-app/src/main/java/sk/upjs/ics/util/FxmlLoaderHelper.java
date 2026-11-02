package sk.upjs.ics.util;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Callback;

public final class FxmlLoaderHelper {

  private FxmlLoaderHelper() {}

  public static void open(String fxml, Callback<Class<?>, Object> controllerFactory, Stage stage) {
    try {
      FXMLLoader loader =
          new FXMLLoader(FxmlLoaderHelper.class.getResource("/fxml/" + fxml), bundle());
      loader.setControllerFactory(controllerFactory);

      Scene scene = new Scene(loader.load());

      scene
          .getStylesheets()
          .add(FxmlLoaderHelper.class.getResource("/css/app.css").toExternalForm());
      stage.setScene(scene);
      stage.show();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public static ResourceBundle bundle() {
    return ResourceBundle.getBundle("lang", Locale.getDefault());
  }
}
