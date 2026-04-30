package doboard.core.controllers;

import doboard.core.dao.DormDAO;
import doboard.core.models.Dorm;
import doboard.core.models.User;
import doboard.core.util.UserSession;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import java.util.UUID;

public class DormitoryController {

    @FXML private VBox noDormBox;
    @FXML private VBox hasDormBox;
    
    @FXML private TextField createDormNameField;
    @FXML private TextField joinCodeField;
    
    @FXML private Label dormNameLabel;
    @FXML private Label joinCodeDisplayLabel;

    private DormDAO dormDAO;

    @FXML
    private void initialize() {
        dormDAO = new DormDAO();
        refreshView();
    }

    private void refreshView() {
        User user = UserSession.getInstance().getCurrentUser();
        if (user == null) return;

        Dorm currentDorm = dormDAO.getUserDorm(user.getUser_id());
        
        if (currentDorm != null) {
            // User is in a dorm
            noDormBox.setVisible(false);
            noDormBox.setManaged(false);
            hasDormBox.setVisible(true);
            hasDormBox.setManaged(true);
            
            dormNameLabel.setText("Dorm: " + currentDorm.getDorm_name());
            joinCodeDisplayLabel.setText("Invite Code: " + currentDorm.getJoin_code());
        } else {
            // User is NOT in a dorm
            hasDormBox.setVisible(false);
            hasDormBox.setManaged(false);
            noDormBox.setVisible(true);
            noDormBox.setManaged(true);
        }
    }

    @FXML
    private void handleCreateDorm() {
        String name = createDormNameField.getText();
        if (name.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Name", "Please enter a Dorm name.");
            return;
        }

        String code = UUID.randomUUID().toString().substring(0, 6).toUpperCase(); // Short code
        int userId = UserSession.getInstance().getCurrentUser().getUser_id();
        
        Dorm newDorm = dormDAO.createDorm(name, userId, code);
        
        if (newDorm != null) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Dorm Created Successfully!");
            refreshView();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to create Dorm.");
        }
    }

    @FXML
    private void handleJoinDorm() {
        String code = joinCodeField.getText();
        if (code.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Code", "Please enter an invite code.");
            return;
        }

        int userId = UserSession.getInstance().getCurrentUser().getUser_id();
        Dorm joinedDorm = dormDAO.joinDorm(code, userId);

        if (joinedDorm != null) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Successfully joined " + joinedDorm.getDorm_name() + "!");
            refreshView();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Invalid code or you are already a member.");
        }
    }

    @FXML
    private void goToDashboard(javafx.event.ActionEvent event) {
        javafx.stage.Stage stage = (javafx.stage.Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        doboard.core.util.SceneLoader.loadScene(stage, "/com/doboard/view/main-menu.fxml", "DoBoard - Dashboard");
    }

    @FXML
    private void goToChores(javafx.event.ActionEvent event) {
        javafx.stage.Stage stage = (javafx.stage.Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        doboard.core.util.SceneLoader.loadScene(stage, "/com/doboard/view/chores-view.fxml", "DoBoard - Chores");
    }

    @FXML
    private void goToBills(javafx.event.ActionEvent event) {
        javafx.stage.Stage stage = (javafx.stage.Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        doboard.core.util.SceneLoader.loadScene(stage, "/com/doboard/view/bills-view.fxml", "DoBoard - Bills");
    }

    @FXML
    private void logout(javafx.event.ActionEvent event) {
        UserSession.getInstance().clearSession();
        javafx.stage.Stage stage = (javafx.stage.Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        doboard.core.util.SceneLoader.loadScene(stage, "/com/doboard/view/login-view.fxml", "DoBoard - Login");
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
