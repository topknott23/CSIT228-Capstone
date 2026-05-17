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
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class ChoreController {
    @FXML private VBox contentArea;
    @FXML private VBox usersContainer; // Container for "Monthly Chores Done" list

    private final ChoreService choreService = new ChoreService();

    @FXML
    public void initialize() {
        NavigationManager.setTitle("Chores");
        loadLeaderboard();
    }

    private void loadLeaderboard() {
        // Get current user from session
        User currentUser = SessionHandler.loadSession();
        if (currentUser == null) {
            System.err.println("No user session found");
            return;
        }

        // Get dorm_id for current user
        int dormId = choreService.getDormIdForUser(currentUser.getUser_id());
        if (dormId == -1) {
            System.err.println("User not in any dorm");
            return;
        }

        usersContainer.getChildren().clear();

        List<ChoreService.LeaderboardEntry> leaderboard = choreService.getSortedLeaderboard(dormId);

        for(ChoreService.LeaderboardEntry entry : leaderboard){
            addLeaderboardRow(entry.username(), entry.completionCount(), null);
        }
    }

    @FXML
    private void addChore(ActionEvent event) {
        showDialog("/doboard/chores/chore-form-dialog.fxml", "Add Chore", controller -> {
            if (controller instanceof ChoreFormController) {
                ((ChoreFormController) controller).setParentController(this);
                ((ChoreFormController) controller).setChoreToEdit(null); // Passing null triggers ADD mode
            }
        });
    }

    @FXML
    private void updateChore(ActionEvent event) {
        showChoreSelectionDialog(chore -> {
            showDialog("/doboard/chores/chore-form-dialog.fxml", "Edit Chore", controller -> {
                if (controller instanceof ChoreFormController) {
                    ((ChoreFormController) controller).setParentController(this);
                    ((ChoreFormController) controller).setChoreToEdit(chore); // Passing a chore triggers EDIT mode
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
                        loadLeaderboard();
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
            dialogStage.setTitle(title);
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(contentArea.getScene().getWindow());

            Scene scene = new Scene(root);
            dialogStage.setScene(scene);

            // Configure the controller
            Object controller = loader.getController();
            if (controllerConfigurer != null) {
                controllerConfigurer.accept(controller);
            }

            dialogStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Helpers
    public void addLeaderboardRow(String username, int count, String imagePath) {
        Node row = ComponentFactory.createLeaderboardRow(username, count, imagePath);

        if (row != null) {
            usersContainer.getChildren().add(row);
        }
    }

    @FXML
    public void markAsDone(ActionEvent actionEvent) {
        //TODO: Implement marking a chore as done
    }
}