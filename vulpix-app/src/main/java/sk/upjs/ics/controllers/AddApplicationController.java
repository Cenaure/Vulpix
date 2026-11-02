package sk.upjs.ics.controllers;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ResourceBundle;
import java.util.UUID;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import sk.upjs.ics.entities.ApplicationEntity;
import sk.upjs.ics.entities.ApplicationSpecificationsEntity;
import sk.upjs.ics.entities.CollectionEntity;
import sk.upjs.ics.util.AppContext;

public class AddApplicationController {
  private final AppContext appContext;
  private final Runnable onSaved;

  @FXML private ResourceBundle resources;
  @FXML private TextField nameField;
  @FXML private TextField pathField;
  @FXML private VBox protonBox;
  @FXML private TextField protonVersionField;
  @FXML private TextField winePrefixField;
  @FXML private TextField launchOptionsField;
  @FXML private ListView<CollectionEntity> collectionsList;
  @FXML private CheckBox privateCheck;
  @FXML private Label errorLabel;

  public AddApplicationController(AppContext appContext, Runnable onSaved) {
    this.appContext = appContext;
    this.onSaved = onSaved;
  }

  @FXML
  private void initialize() {

    boolean visible = isWindowsExecutable(pathField.getText());
    protonBox.setVisible(visible);
    protonBox.setManaged(visible);

    pathField
        .textProperty()
        .addListener(
            (obs, oldValue, newValue) -> {
              boolean show = isWindowsExecutable(newValue);
              protonBox.setVisible(show);
              protonBox.setManaged(show);
            });

    collectionsList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    collectionsList.setCellFactory(
        lv ->
            new ListCell<>() {
              @Override
              protected void updateItem(CollectionEntity item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.name());
              }
            });
    collectionsList
        .getItems()
        .setAll(appContext.collectionDao().getAllByUser(appContext.currentUser().id()));
  }

  @FXML
  private void onBrowseExe() {
    FileChooser chooser = new FileChooser();
    chooser.setTitle(resources.getString("add.chooseExe"));
    File file = chooser.showOpenDialog(pathField.getScene().getWindow());
    if (file == null) {
      return;
    }

    pathField.setText(file.getAbsolutePath());
    if (nameField.getText().isBlank()) {
      String fileName = file.getName();
      int dot = fileName.lastIndexOf('.');
      nameField.setText(dot > 0 ? fileName.substring(0, dot) : fileName);
    }
  }

  @FXML
  private void onBrowsePrefix() {
    DirectoryChooser chooser = new DirectoryChooser();
    chooser.setTitle(resources.getString("add.choosePrefix"));
    File dir = chooser.showDialog(winePrefixField.getScene().getWindow());
    if (dir != null) {
      winePrefixField.setText(dir.getAbsolutePath());
    }
  }

  @FXML
  private void onSave() {
    String name = nameField.getText().trim();
    String path = pathField.getText().trim();

    if (name.isEmpty()) {
      showError("add.error.nameRequired");
      return;
    }
    if (path.isEmpty()) {
      showError("add.error.pathRequired");
      return;
    }
    if (!fileExists(path)) {
      showError("add.error.pathInvalid");
      return;
    }

    saveApplication(name, path);
    onSaved.run();
    close();
  }

  private void saveApplication(String name, String path) {
    UUID userId = appContext.currentUser().id();

    ApplicationEntity saved =
        appContext
            .applicationDao()
            .save(
                new ApplicationEntity(
                    null,
                    userId,
                    name,
                    0,
                    null,
                    null,
                    null,
                    null,
                    privateCheck.isSelected(),
                    null));

    boolean proton = isWindowsExecutable(path);

    appContext
        .specificationsDao()
        .save(
            new ApplicationSpecificationsEntity(
                saved.id(),
                path,
                proton ? emptyToNull(protonVersionField.getText()) : null,
                proton ? emptyToNull(winePrefixField.getText()) : null,
                emptyToNull(launchOptionsField.getText())));

    for (CollectionEntity collection : collectionsList.getSelectionModel().getSelectedItems()) {
      appContext.collectionDao().addApplication(collection.id(), saved.id());
    }
  }

  @FXML
  private void onCancel() {
    close();
  }

  private static boolean isWindowsExecutable(String path) {
    return path != null && path.trim().toLowerCase().endsWith(".exe");
  }

  private static boolean fileExists(String path) {
    try {
      return Files.isRegularFile(Path.of(path));
    } catch (InvalidPathException e) {
      return false;
    }
  }

  private static String emptyToNull(String text) {
    return text == null || text.isBlank() ? null : text.trim();
  }

  private void showError(String key) {
    errorLabel.setText(resources.getString(key));
    errorLabel.setVisible(true);
    errorLabel.setManaged(true);
  }

  private void close() {
    ((Stage) nameField.getScene().getWindow()).close();
  }
}
