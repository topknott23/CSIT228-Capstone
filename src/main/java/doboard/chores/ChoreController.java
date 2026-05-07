package doboard.chores;

import doboard.common.util.ComponentFactory;
import doboard.common.util.NavigationManager;
import doboard.common.util.StageUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ChoreController {
    @FXML private VBox contentArea;
    @FXML private VBox usersContainer; // Container for "Monthly Chores Done" list

    @FXML
    public void initialize() {
        // TODO: Load the leaderboard of users and their chore counts
    }

    @FXML private void goDashboard(ActionEvent event) {NavigationManager.loadView(getClass(), "/doboard/dashboard/content-view.fxml");}
    @FXML private void goExpenses(ActionEvent event) {NavigationManager.loadView(getClass(), "/doboard/expenses/expenses-view.fxml");}
    @FXML private void goSignals(ActionEvent event) {NavigationManager.loadView(getClass(), "/doboard/signals/signals-view.fxml");}

    @FXML
    private void addChore(ActionEvent event) {
        // TODO: Open a popup/dialog to input new chore details
        // kamo nlng ani add,update,remove operations, ky kapoy na. Ako lang designan ig human
    }

    @FXML
    private void updateChore(ActionEvent event) {
        // TODO: Implement logic to edit an existing chore (popup/dialog)
    }

    @FXML
    private void removeChore(ActionEvent event) {
        // TODO: Implement logic to delete a chore (popup/dialog)
    }

    // Helpers
    public void addLeaderboardRow(String username, int count, String imagePath) {
        Node row = ComponentFactory.createLeaderboardRow(username, count, imagePath);

        if (row != null) {
            usersContainer.getChildren().add(row);
        }
    }
}