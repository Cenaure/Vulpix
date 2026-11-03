package sk.upjs.ics._common.ui;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
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

      List<String> stylesheets =
          List.of(
              FxmlLoaderHelper.class.getResource("/css/theme.css").toExternalForm(),
              FxmlLoaderHelper.class.getResource("/css/components.css").toExternalForm(),
              FxmlLoaderHelper.class.getResource("/css/utilities.css").toExternalForm(),
              FxmlLoaderHelper.class.getResource("/css/components/button.css").toExternalForm(),
              FxmlLoaderHelper.class.getResource("/css/components/field.css").toExternalForm());

      scene.getStylesheets().addAll(stylesheets);

      stage.setMinWidth(1024);
      stage.setMinHeight(576);

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
