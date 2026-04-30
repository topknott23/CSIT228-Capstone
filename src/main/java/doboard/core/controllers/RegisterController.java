package doboard.core.controllers;

import doboard.core.util.Database;
import doboard.core.util.SceneLoader;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RegisterController {
    @FXML private StackPane rootPane;
    @FXML private ImageView backgroundView;
    @FXML private TextField emailField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button registerButton;

    @FXML
    private void initialize(){
        backgroundView.fitWidthProperty().bind(rootPane.widthProperty());
        backgroundView.fitHeightProperty().bind(rootPane.heightProperty());
    }

    @FXML
    private void goLogin(ActionEvent event){
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        SceneLoader.loadScene(stage, "/com/doboard/view/login-view.fxml", "TEST");
    }

    @FXML
    private void handleRegister(ActionEvent event){
        String email = emailField.getText();
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Registration Failed", "Please fill in all fields.");
            return;
        }

        String sql = "INSERT INTO users (username, email, password) VALUES (?, ?, ?)";

        try {
            Connection conn = Database.getInstance().getConnection();
            if (conn == null) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Could not connect to the database.");
                return;
            }
            
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setString(2, email);
            pstmt.setString(3, password); // Note: In a real app, hash this password!

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Registration Successful", "Account created! You can now log in.");
                goLogin(event);
            }
            
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "An error occurred. Username or Email might already be in use.");
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
