package doboard.auth;

import doboard.common.util.Popup;
import doboard.common.util.SceneLoader;
import doboard.common.util.StageUtil;
import doboard.common.util.CustomTitleBar; // Added import
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox; // Added import
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class RegisterController {
    @FXML private StackPane rootPane;
    @FXML private ImageView backgroundView;
    @FXML private HBox topNavBar; // Added reference for the title bar
    @FXML private TextField emailField;
    @FXML private TextField fullNameField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button registerButton;

    private final CustomTitleBar titleBar = new CustomTitleBar(); // Added title bar manager

    @FXML
    private void initialize(){
        backgroundView.fitWidthProperty().bind(rootPane.widthProperty());
        backgroundView.fitHeightProperty().bind(rootPane.heightProperty());
    }
    @FXML
    private void goLogin(ActionEvent event){
        Stage stage = StageUtil.getStage(event);
        SceneLoader.loadScene(stage, RegisterController.class, "login-view.fxml", "Login");
    }

    @FXML
    private void handleRegister(ActionEvent event){
        String emailInput = emailField.getText();
        String fullNameInput = fullNameField.getText();
        String usernameInput = usernameField.getText();
        String passwordInput = passwordField.getText();

        if (emailInput.isEmpty() || fullNameInput.isEmpty() || usernameInput.isEmpty() || passwordInput.isEmpty()) {
            Popup.show("Registration Failed", "Please fill in all fields.");
            return;
        }

        if (!emailInput.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            Popup.show("Registration Failed", "Please enter a valid email address.");
            return;
        }

        User newUser = new User(usernameInput, emailInput, fullNameInput, passwordInput);

        boolean isRegistered = UserDAO.Register(newUser);
        if(!isRegistered){
            Popup.show("Registration Failed", "Username may already exist or database error.");
            return;
        }

        goLogin(event);
    }
}
