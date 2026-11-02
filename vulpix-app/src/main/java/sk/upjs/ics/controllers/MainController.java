package sk.upjs.ics.controllers;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import sk.upjs.ics.entities.ApplicationEntity;
import sk.upjs.ics.entities.CollectionEntity;
import sk.upjs.ics.util.AppContext;

public class MainController {
  private final AppContext appContext;

  @FXML private ResourceBundle resources;
  @FXML private ListView<CollectionEntity> collectionsList;
  @FXML private Label userLabel;
  @FXML private TextField searchField;
  @FXML private FlowPane gamesPane;
  @FXML private Label emptyLabel;
  @FXML private VBox detailPane;
  @FXML private Label detailName;
  @FXML private Label detailPlayed;
  @FXML private Label detailAdded;
  @FXML private Button playButton;

  private CollectionEntity selectedCollection;
  private List<ApplicationEntity> currentApps = new ArrayList<>();
  private ApplicationEntity selectedApp;

  public MainController(AppContext appContext) {
    this.appContext = appContext;
  }

  @FXML
  private void initialize() {
    var user = appContext.currentUser();

    userLabel.setText(user.username());
    collectionsList.getItems().setAll(appContext.collectionDao().getAllByUser(user.id()));

    collectionsList
        .getSelectionModel()
        .selectedItemProperty()
        .addListener(
            (obs, oldVal, newVal) -> {
              if (newVal == null) return;
              selectedCollection = newVal;
              loadApplications();
            });

    searchField.textProperty().addListener((obs, oldVal, newVal) -> refresh());

    loadApplications();
  }

  private void loadApplications() {
    UUID userId = appContext.currentUser().id();
    if (selectedCollection == null) {
      currentApps = appContext.applicationDao().getAllByUser(userId);
    } else {
      currentApps = appContext.collectionDao().getApplications(selectedCollection.id());
    }
    refresh();
  }

  private void refresh() {
    String query = searchField.getText().trim().toLowerCase();

    gamesPane.getChildren().clear();
    boolean selectedVisible = false;

    for (ApplicationEntity app : currentApps) {
      if (!app.name().toLowerCase().contains(query)) {
        continue;
      }

      Node card = createCard(app);

      if (selectedApp != null && selectedApp.id().equals(app.id())) {
        card.getStyleClass().add("selected");
        selectedVisible = true;
      }

      gamesPane.getChildren().add(card);
    }

    if (!selectedVisible) {
      selectedApp = null;
    }

    showDetail(selectedApp);
    emptyLabel.setVisible(gamesPane.getChildren().isEmpty());
  }

  private Node createCard(ApplicationEntity app) {
    String initial = app.name().isBlank() ? "?" : app.name().substring(0, 1).toUpperCase();

    Label letter = new Label(initial);
    letter.getStyleClass().add("cover-letter");

    // TODO: check whether there is an actual cover or  placeholder is needed
    StackPane cover = new StackPane(letter);
    cover.getStyleClass().add("cover-placeholder");
    cover.setPrefSize(150, 200);
    cover.setMinSize(150, 200);

    Label name = new Label(app.name());
    name.setWrapText(true);
    name.setMaxWidth(150);

    Label played = new Label(formatPlayed(app.playedMinutes()));
    played.getStyleClass().add("muted");

    VBox card = new VBox(8, cover, name, played);
    card.getStyleClass().add("game-card");
    card.setPrefWidth(174);

    card.setOnMouseClicked(
        e -> {
          selectedApp = app;
          gamesPane.getChildren().forEach(node -> node.getStyleClass().remove("selected"));
          card.getStyleClass().add("selected");
          showDetail(app);
        });

    return card;
  }

  private void showDetail(ApplicationEntity app) {
    boolean visible = app != null;
    detailPane.setVisible(visible);
    detailPane.setManaged(visible);
    if (!visible) {
      return;
    }
    detailName.setText(app.name());
    detailPlayed.setText(formatPlayed(app.playedMinutes()));
    String addedDate =
        DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
            .withLocale(resources.getLocale())
            .format(app.addedAt());

    detailAdded.setText(resources.getString("main.added") + " " + addedDate);
  }

  private String formatPlayed(int minutes) {
    return String.format(resources.getString("main.played"), minutes / 60, minutes % 60);
  }

  @FXML
  private void onShowAll() {
    selectedCollection = null;
    collectionsList.getSelectionModel().clearSelection();
    loadApplications();
  }

  @FXML
  private void onAddGame() {
    appContext.showAddApplication(() -> loadApplications());
  }

  @FXML
  private void onShowProfile() {
    appContext.showProfile();
  }

  @FXML
  private void onPlay() {
    // TODO: commit suicide
    System.out.println("Play: " + selectedApp.name());
  }

  @FXML
  private void onLogout() {
    appContext.logout();
  }

  @FXML
  private void onCloseDetail() {
    selectedApp = null;
    gamesPane.getChildren().forEach(n -> n.getStyleClass().remove("selected"));
    showDetail(null);
  }
}
