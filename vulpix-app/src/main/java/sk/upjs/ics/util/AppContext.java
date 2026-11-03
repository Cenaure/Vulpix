package sk.upjs.ics.util;

import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import lombok.Setter;
import sk.upjs.ics._common.ui.FxmlLoaderHelper;
import sk.upjs.ics.auth.AuthResult;
import sk.upjs.ics.auth.AuthService;
import sk.upjs.ics.auth.ui.LoginController;
import sk.upjs.ics.collection.dao.CollectionDao;
import sk.upjs.ics.entities.UserEntity;
import sk.upjs.ics.library.dao.ApplicationDao;
import sk.upjs.ics.library.dao.ApplicationSpecificationsDao;
import sk.upjs.ics.library.ui.AddApplicationController;
import sk.upjs.ics.library.ui.LibraryController;
import sk.upjs.ics.profile.ui.ProfileController;

public class AppContext {
  private final Stage stage;
  private final AuthService authService;
  private final ApplicationDao applicationDao;
  private final CollectionDao collectionDao;
  private final ApplicationSpecificationsDao specificationsDao;
  @Setter private AuthResult session;

  public AppContext(
      Stage stage,
      AuthService authService,
      ApplicationDao applicationDao,
      CollectionDao collectionDao,
      ApplicationSpecificationsDao specificationsDao) {
    this.stage = stage;
    this.authService = authService;
    this.applicationDao = applicationDao;
    this.collectionDao = collectionDao;
    this.specificationsDao = specificationsDao;
  }

  public AuthService authService() {
    return authService;
  }

  public ApplicationDao applicationDao() {
    return applicationDao;
  }

  public CollectionDao collectionDao() {
    return collectionDao;
  }

  public UserEntity currentUser() {
    return session.user();
  }

  public void showLogin() {
    FxmlLoaderHelper.open("login.fxml", type -> new LoginController(this), stage);
  }

  public void logout() {
    AuthResult old = session;
    session = null;
    if (old != null) {
      Thread t = new Thread(() -> authService.logout(old.accessToken()), "logout");
      t.setDaemon(true);
      t.start();
    }
    showLogin();
  }

  public void sessionExpired() {
    if (session == null) return;
    session = null;
    showLogin();
  }

  public void showMain() {
    FxmlLoaderHelper.open("main.fxml", type -> new LibraryController(this), stage);
  }

  public void showProfile() {
    openDialog("profile.fxml", "profile.title", type -> new ProfileController(this));
  }

  public void showAddApplication(Runnable onSaved) {
    openDialog(
        "add-application.fxml", "add.title", type -> new AddApplicationController(this, onSaved));
  }

  private void openDialog(String fxml, String titleKey, Callback<Class<?>, Object> factory) {
    Stage dialog = new Stage();
    dialog.initOwner(stage);
    dialog.initModality(Modality.WINDOW_MODAL);
    dialog.setTitle(FxmlLoaderHelper.bundle().getString(titleKey));
    FxmlLoaderHelper.open(fxml, factory, dialog);
  }

  public ApplicationSpecificationsDao specificationsDao() {
    return specificationsDao;
  }
}
