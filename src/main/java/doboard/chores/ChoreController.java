package doboard.chores;

import doboard.auth.User;
import doboard.common.session.SessionHandler;
import doboard.common.util.ComponentFactory;
import doboard.common.util.NavigationManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.util.List;

public class ChoreController {
    @FXML private VBox contentArea;
    @FXML private VBox usersContainer;

    private final ChoreService choreService = new ChoreService();

    @FXML
    public void initialize() {
        NavigationManager.setTitle("Chores");
        
        // Listen to cache updates
        doboard.common.cache.DormDataCache.getInstance().addListener(this::loadLeaderboard);
        
        loadLeaderboard();
    }

    private void loadLeaderboard() {
        usersContainer.getChildren().clear();
        List<ChoreService.LeaderboardEntry> leaderboard = doboard.common.cache.DormDataCache.getInstance().getLeaderboard();
        for(ChoreService.LeaderboardEntry entry : leaderboard){
            addLeaderboardRow(entry.username(), entry.completionCount(), null);
        }
    }

    @FXML
    private void addChore(ActionEvent event) {
        showDialog("/doboard/chores/chore-form-dialog.fxml", "Add Chore", controller -> {
            if (controller instanceof ChoreFormController) {
                ((ChoreFormController) controller).setParentController(this);
                ((ChoreFormController) controller).setChoreToEdit(null);
            }
        });
    }

    @FXML
    private void updateChore(ActionEvent event) {
        showChoreSelectionDialog(chore -> {
            showDialog("/doboard/chores/chore-form-dialog.fxml", "Edit Chore", controller -> {
                if (controller instanceof ChoreFormController) {
                    ((ChoreFormController) controller).setParentController(this);
                    ((ChoreFormController) controller).setChoreToEdit(chore);
                }
            });
        });
    }

    @FXML
    private void removeChore(ActionEvent event) {
        showChoreSelectionDialog(chore -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Delete Chore");
            alert.setHeaderText("Remove Chore Assignment");
            alert.setContentText("Are you sure you want to permanently delete '" + chore.getTitle() + "'?");

            ButtonType confirmBtn = new ButtonType("Delete", ButtonBar.ButtonData.OK_DONE);
            ButtonType cancelBtn = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(confirmBtn, cancelBtn);

            alert.showAndWait().ifPresent(response -> {
                if (response == confirmBtn) {
                    ChoreDAO choreDAO = new ChoreDAO();
                    choreDAO.unassignAll(chore.getChore_id());
                    if (choreDAO.delete(chore.getChore_id())) {
                        doboard.common.cache.DormDataCache cache = doboard.common.cache.DormDataCache.getInstance();
                        cache.reload(cache.getDormId(), cache.getCurrentUserId());
                        cache.notifyListeners();
                    }
                }
            });
        });
    }

    private void showChoreSelectionDialog(java.util.function.Consumer<Chore> onChoreSelected) {
        showDialog("/doboard/chores/select-chore-dialog.fxml", "Select Chore", controller -> {
            if (controller instanceof SelectChoreDialogController) {
                ((SelectChoreDialogController) controller).setOnChoreSelected(onChoreSelected);
            }
        });
    }

    private void showDialog(String fxmlPath, String title, java.util.function.Consumer<Object> controllerConfigurer) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(contentArea.getScene().getWindow());

            // --- THESE TWO LINES FIX THE UGLY WINDOWS BAR ---
            dialogStage.initStyle(StageStyle.TRANSPARENT);

            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            // ------------------------------------------------

            try {
                scene.getStylesheets().add(getClass().getResource("/styles/styles.css").toExternalForm());
            } catch (Exception e) {}

            dialogStage.setScene(scene);

            Object controller = loader.getController();
            if (controllerConfigurer != null) {
                controllerConfigurer.accept(controller);
            }

            dialogStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addLeaderboardRow(String username, int count, String imagePath) {
        Node row = ComponentFactory.createLeaderboardRow(username, count, imagePath);
        if (row != null) usersContainer.getChildren().add(row);
    }

    @FXML public void markAsDone(ActionEvent actionEvent) {}
}