package sk.upjs.ics;

import javafx.stage.Stage;
import lombok.Setter;
import sk.upjs.ics.application.controllers.LoginController;
import sk.upjs.ics.application.controllers.MainController;
import sk.upjs.ics.auth.AuthResult;
import sk.upjs.ics.auth.AuthService;
import sk.upjs.ics.daos.ApplicationDao;
import sk.upjs.ics.daos.CollectionDao;
import sk.upjs.ics.entities.UserEntity;

public class AppContext {
  private final Stage stage;
  private final AuthService authService;
  private final ApplicationDao applicationDao;
  private final CollectionDao collectionDao;
  @Setter private AuthResult session;

  public AppContext(
      Stage stage,
      AuthService authService,
      ApplicationDao applicationDao,
      CollectionDao collectionDao) {
    this.stage = stage;
    this.authService = authService;
    this.applicationDao = applicationDao;
    this.collectionDao = collectionDao;
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
    if (session != null) authService.logout(session.refreshToken());
    session = null;
    showLogin();
  }

  public void showMain() {
    FxmlLoaderHelper.open("main.fxml", type -> new MainController(this), stage);
  }
}
