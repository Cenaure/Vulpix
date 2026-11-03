package sk.upjs.ics.auth.ui;

import java.util.ResourceBundle;
import javafx.concurrent.Task;
import javafx.fxml.*;
import javafx.scene.Node;
import javafx.scene.control.*;
import sk.upjs.ics.auth.AuthException;
import sk.upjs.ics.auth.AuthResult;
import sk.upjs.ics.util.AppContext;

public class LoginController {
  private final AppContext appContext;
  private boolean registerMode = false;

  @FXML private TextField usernameField, emailField;
  @FXML private PasswordField passwordField;
  @FXML private Label errorLabel;
  @FXML private Button submitButton;
  @FXML private ProgressIndicator progress;
  @FXML private Hyperlink toggleModeLink;
  @FXML private ResourceBundle resources;

  public LoginController(AppContext context) {
    this.appContext = context;
  }

  @FXML
  private void onToggleMode() {
    registerMode = !registerMode;

    setVisibleManaged(usernameField, registerMode);

    if (registerMode) {
      submitButton.setText(resources.getString("login.register"));
      toggleModeLink.setText(resources.getString("login.toLogin"));
    } else {
      submitButton.setText(resources.getString("login.submit"));
      toggleModeLink.setText(resources.getString("login.toRegister"));
    }

    hideError();
  }

  @FXML
  private void onSubmit() {
    String email = emailField.getText().trim();
    String password = passwordField.getText();
    String username = usernameField.getText().trim();

    if (email.isEmpty() || password.isEmpty()) {
      showError("error.fieldsRequired");
      return;
    }

    if (registerMode && username.isEmpty()) {
      showError("error.fieldsRequired");
      return;
    }

    setBusy(true);

    Task<AuthResult> task =
        new Task<>() {
          @Override
          protected AuthResult call() {
            if (registerMode) {
              return appContext.authService().register(username, email, password);
            } else {
              return appContext.authService().login(email, password);
            }
          }
        };

    task.setOnSucceeded(
        e -> {
          setBusy(false);
          appContext.setSession(task.getValue());
          appContext.showMain();
        });

    task.setOnFailed(
        e -> {
          setBusy(false);

          Throwable ex = task.getException();

          if (ex instanceof AuthException) {
            AuthException authError = (AuthException) ex;
            String key = "error." + authError.getCode().name();
            showError(key);
          } else {
            showError("error.unknown");
          }
        });

    Thread thread = new Thread(task);
    thread.setName("auth");
    thread.start();
  }

  private void setBusy(boolean busy) {
    submitButton.setDisable(busy);
    setVisibleManaged(progress, busy);

    if (busy) {
      hideError();
    }
  }

  private void showError(String key) {
    errorLabel.setText(resources.getString(key));
    setVisibleManaged(errorLabel, true);
  }

  private void hideError() {
    setVisibleManaged(errorLabel, false);
  }

  private static void setVisibleManaged(Node node, boolean value) {
    node.setVisible(value);
    node.setManaged(value);
  }
}
