package doboard.chores;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class DeleteChoreDialogController {

    @FXML private Label confirmationLabel;
    @FXML private Button cancelButton;
    @FXML private Button deleteButton;

    private ChoreController parentController;
    private ChoreDAO choreDAO = new ChoreDAO();
    private ChoreAssignmentDAO choreAssignmentDAO = new ChoreAssignmentDAO();
    private Chore choreToDelete;

    public void setChoreToDelete(Chore chore) {
        this.choreToDelete = chore;
        confirmationLabel.setText("Are you sure you want to delete the chore \"" +
                                chore.getTitle() + "\"?\n\nThis action cannot be undone.");
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        closeDialog();
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        if (choreToDelete != null) {
            // Remove all user assignments first
            choreAssignmentDAO.unassignAll(choreToDelete.getChore_id());

            // Delete the chore
            if (choreDAO.delete(choreToDelete.getChore_id())) {
                // Refresh parent controller's leaderboard
                if (parentController != null) {
                    parentController.initialize();
                }
                closeDialog();
            } else {
                // Show error alert
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Failed to delete chore");
                alert.showAndWait();
            }
        }
    }

    private void closeDialog() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    public void setParentController(ChoreController parentController) {
        this.parentController = parentController;
    }
}