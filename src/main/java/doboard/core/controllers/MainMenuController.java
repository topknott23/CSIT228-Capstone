package doboard.core.controllers;

import doboard.core.util.SceneLoader;
import doboard.core.util.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class MainMenuController {

    @FXML private Label welcomeLabel;

    @FXML
    private void initialize() {
        if (UserSession.getInstance().getCurrentUser() != null) {
            String name = UserSession.getInstance().getCurrentUser().getUsername();
            if (welcomeLabel != null) {
                welcomeLabel.setText("Welcome back, " + name + "!");
            }
        }
    }

    @FXML
    private void goToDormitory(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        SceneLoader.loadScene(stage, "/com/doboard/view/dormitory-view.fxml", "DoBoard - My Dorm");
    }

    @FXML
    private void goToChores(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        SceneLoader.loadScene(stage, "/com/doboard/view/chores-view.fxml", "DoBoard - Chores");
    }

    @FXML
    private void goToBills(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        SceneLoader.loadScene(stage, "/com/doboard/view/bills-view.fxml", "DoBoard - Bills");
    }

    @FXML
    private void logout(ActionEvent event) {
        UserSession.getInstance().clearSession();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        SceneLoader.loadScene(stage, "/com/doboard/view/login-view.fxml", "DoBoard - Login");
    }
}
