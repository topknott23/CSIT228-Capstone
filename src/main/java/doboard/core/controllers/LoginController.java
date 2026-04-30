package doboard.core.controllers;

import doboard.core.util.SceneLoader;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button signInButton;
    @FXML private StackPane rootPane;
    @FXML private ImageView backgroundView;

    @FXML
    private void initialize(){
        backgroundView.fitHeightProperty().bind(rootPane.heightProperty());
        backgroundView.fitWidthProperty().bind(rootPane.widthProperty());
    }

    @FXML
    private void goRegister(ActionEvent event){
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        SceneLoader.loadScene(stage, "/com/doboard/view/register-view.fxml", "TEST");
    }

    @FXML
    private void handleSignIn(ActionEvent event) {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert(javafx.scene.control.Alert.AlertType.ERROR, "Login Failed", "Please enter both email and password.");
            return;
        }

        doboard.core.dao.UserDAO userDAO = new doboard.core.dao.UserDAO();
        doboard.core.models.User loggedInUser = userDAO.authenticateUser(email, password);

        if (loggedInUser != null) {
            // Save user to session
            doboard.core.util.UserSession.getInstance().setCurrentUser(loggedInUser);
            
            // Go to Main Menu
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            SceneLoader.loadScene(stage, "/com/doboard/view/main-menu.fxml", "DoBoard - Dashboard");
        } else {
            showAlert(javafx.scene.control.Alert.AlertType.ERROR, "Login Failed", "Invalid email or password.");
        }
    }

    private void showAlert(javafx.scene.control.Alert.AlertType alertType, String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
