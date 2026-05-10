package doboard.auth;

import doboard.common.session.SessionHandler;
import doboard.common.util.Popup;
import doboard.common.util.SceneLoader;
import doboard.common.util.StageUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

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
        Stage stage = StageUtil.getStage(event);
        SceneLoader.loadScene(stage, LoginController.class, "register-view.fxml", "Register");
    }

    @FXML
    private void handleSignIn(ActionEvent event) {
        String usernameInput = emailField.getText();
        String passwordInput = passwordField.getText();

        if(usernameInput.isEmpty() || passwordInput.isEmpty()){
            System.out.println("Login failed: Please enter both username and password.");
            return;
        }


        // 1. Call UserDAO.login(username, password) to verify credentials.
        User loggedInUser = UserDAO.Login(usernameInput, passwordInput);

        // 2. If the user is found, call SessionHandler.saveSession(user) to persist the login.
        if(loggedInUser == null){
            Popup.show("Login failed!", "Login failed: Username or password is incorrect.");
            return;
        }
        SessionHandler.saveSession(loggedInUser);
        Popup.show("Success", "Login Successful! Welcome back, " + loggedInUser.getUsername() + ".");

        // 3. Trigger the scene switch to the Dashboard/Main view.
        Stage stage = StageUtil.getStage(event);
        SceneLoader.loadScene(stage, LoginController.class, "/doboard/dashboard/dashboard-view.fxml", "DoBoard - Dashboard");
    }

}
