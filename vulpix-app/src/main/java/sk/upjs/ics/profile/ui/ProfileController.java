package sk.upjs.ics.profile.ui;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import sk.upjs.ics.entities.ApplicationEntity;
import sk.upjs.ics.entities.UserEntity;
import sk.upjs.ics.util.AppContext;

public class ProfileController {
  private final AppContext appContext;

  @FXML private ResourceBundle resources;
  @FXML private Label avatarLetter;
  @FXML private Label usernameLabel;
  @FXML private Label emailLabel;
  @FXML private Label memberSinceLabel;
  @FXML private Label lastLoginLabel;
  @FXML private Label gamesCountLabel;
  @FXML private Label playtimeLabel;
  @FXML private Label collectionsCountLabel;
  @FXML private ListView<ApplicationEntity> topGamesList;

  public ProfileController(AppContext appContext) {
    this.appContext = appContext;
  }

  @FXML
  private void initialize() {
    UserEntity user = appContext.currentUser();

    /// TODO: avatars
    showUserInfo(user);

    List<ApplicationEntity> apps = appContext.applicationDao().getAllByUser(user.id());
    showStatitstics(user, apps);
    showTopGames(apps);
  }

  private String formatMinutes(int minutes) {
    return String.format(resources.getString("time.hm"), minutes / 60, minutes % 60);
  }

  public void showUserInfo(UserEntity user) {
    /// TODO: figure out avatar
    String name = user.username();
    avatarLetter.setText(name == null || name.isBlank() ? "?" : name.substring(0, 1).toUpperCase());

    usernameLabel.setText(name);
    emailLabel.setText(user.email());

    DateTimeFormatter fmt =
        DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(resources.getLocale());
    String never = resources.getString("profile.never");

    memberSinceLabel.setText(user.createdAt() == null ? never : fmt.format(user.createdAt()));

    lastLoginLabel.setText(user.lastLoginAt() == null ? never : fmt.format(user.lastLoginAt()));
  }

  public void showStatitstics(UserEntity user, List<ApplicationEntity> apps) {
    int totalMinutes = apps.stream().mapToInt(app -> app.playedMinutes()).sum();

    gamesCountLabel.setText(String.valueOf(apps.size()));
    playtimeLabel.setText(formatMinutes(totalMinutes));

    int collectionsCount = appContext.collectionDao().getAllByUser(user.id()).size();

    collectionsCountLabel.setText(String.valueOf(collectionsCount));
  }

  public void showTopGames(List<ApplicationEntity> apps) {
    topGamesList.setPlaceholder(new Label(resources.getString("profile.noGames")));
    topGamesList.setCellFactory(
        lv ->
            new ListCell<>() {
              @Override
              protected void updateItem(ApplicationEntity app, boolean empty) {
                super.updateItem(app, empty);
                setText(
                    empty || app == null
                        ? null
                        : app.name() + "  ·  " + formatMinutes(app.playedMinutes()));
              }
            });
    topGamesList
        .getItems()
        .setAll(
            apps.stream()
                .filter(a -> a.playedMinutes() > 0)
                .sorted(
                    Comparator.comparingInt((ApplicationEntity app) -> app.playedMinutes())
                        .reversed())
                .limit(5)
                .toList());
  }

  @FXML
  private void onClose() {
    ((Stage) usernameLabel.getScene().getWindow()).close();
  }
}
