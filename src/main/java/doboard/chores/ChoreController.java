package doboard.chores;

import doboard.auth.User;
import doboard.auth.UserDAO;
import doboard.common.session.SessionHandler;
import doboard.common.util.ComponentFactory;
import doboard.common.util.NavigationManager;
import doboard.dorm.DormMemberDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ChoreController {
    @FXML private VBox contentArea;
    @FXML private VBox usersContainer; // Container for "Monthly Chores Done" list

    private ChoreDAO choreDAO = new ChoreDAO();
    private ChoreAssignmentDAO choreAssignmentDAO = new ChoreAssignmentDAO();
    private DormMemberDAO dormMemberDAO = new DormMemberDAO();

    @FXML
    public void initialize() {
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
        int dormId = dormMemberDAO.getDormIdByUserId(currentUser.getUser_id());
        if (dormId == -1) {
            System.err.println("User not in any dorm");
            return;
        }

        // Get chore completion counts
        Map<Integer, Integer> completionCounts = choreDAO.getChoreCompletionCounts(dormId);

        // Get all users in the dorm
        List<User> dormUsers = UserDAO.getUsersByIds(List.copyOf(completionCounts.keySet()));

        // Clear existing leaderboard
        usersContainer.getChildren().clear();

        // Add leaderboard rows sorted by completion count (descending)
        dormUsers.stream()
                .sorted((u1, u2) -> completionCounts.get(u2.getUser_id()) - completionCounts.get(u1.getUser_id()))
                .forEach(user -> {
                    int count = completionCounts.get(user.getUser_id());
                    addLeaderboardRow(user.getUsername(), count, null); // null for default image
                });
    }

    @FXML private void goDashboard(ActionEvent event) {NavigationManager.loadView(getClass(), "/doboard/dashboard/content-view.fxml");}
    @FXML private void goExpenses(ActionEvent event) {NavigationManager.loadView(getClass(), "/doboard/expenses/expenses-view.fxml");}
    @FXML private void goSignals(ActionEvent event) {NavigationManager.loadView(getClass(), "/doboard/signals/signals-view.fxml");}

    @FXML
    private void addChore(ActionEvent event) {
        showDialog("/doboard/chores/add-chore-dialog.fxml", "Add Chore", controller -> {
            if (controller instanceof AddChoreDialogController) {
                ((AddChoreDialogController) controller).setParentController(this);
            }
        });
    }

    @FXML
    private void updateChore(ActionEvent event) {
        // First show chore selection dialog
        showChoreSelectionDialog(chore -> {
            // Then show edit dialog for selected chore
            showDialog("/doboard/chores/edit-chore-dialog.fxml", "Edit Chore", controller -> {
                if (controller instanceof EditChoreDialogController) {
                    ((EditChoreDialogController) controller).setParentController(this);
                    ((EditChoreDialogController) controller).setChoreToEdit(chore);
                }
            });
        });
    }

    @FXML
    private void removeChore(ActionEvent event) {
        // First show chore selection dialog
        showChoreSelectionDialog(chore -> {
            // Then show delete confirmation dialog
            showDialog("/doboard/chores/delete-chore-dialog.fxml", "Delete Chore", controller -> {
                if (controller instanceof DeleteChoreDialogController) {
                    ((DeleteChoreDialogController) controller).setParentController(this);
                    ((DeleteChoreDialogController) controller).setChoreToDelete(chore);
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