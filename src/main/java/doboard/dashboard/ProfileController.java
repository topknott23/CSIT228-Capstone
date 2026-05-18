package doboard.dashboard;

import doboard.auth.User;
import doboard.common.session.SessionHandler;
import doboard.common.util.NavigationManager;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class ProfileController {
    @FXML private TextField usernameField;
    @FXML private TextField fullNameField;
    @FXML private TextField emailField;

    @FXML
    public void initialize() {
        NavigationManager.setTitle("Profile");

        User user = SessionHandler.loadSession();
        if (user != null) {
            usernameField.setText(user.getUsername());
            fullNameField.setText(user.getFull_name());
            emailField.setText(user.getEmail());
        }
    }
}