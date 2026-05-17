package doboard.dorm;

import doboard.auth.User;
import doboard.common.session.SessionHandler;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class DormSetupController {

    @FXML private VBox createSection;
    @FXML private HBox orSeparator;
    @FXML private TextField dormNameField;
    @FXML private TextField joinCodeField;
    @FXML private Label statusLabel;
    @FXML private Button cancelButton;

    private final DormService dormService = new DormService();
    private Runnable onSuccessCallback;

    @FXML
    public void initialize() {
        User currentUser = SessionHandler.loadSession();

        // --- HARDCODED ADMIN CHECK ---
        // If the username is exactly "admin", reveal the Create tools!
        if (currentUser != null && currentUser.getUsername().equalsIgnoreCase("admin")) {
            createSection.setVisible(true);
            createSection.setManaged(true);
            orSeparator.setVisible(true);
            orSeparator.setManaged(true);
        }
    }

    public void setOnSuccess(Runnable callback) {
        this.onSuccessCallback = callback;
    }

    @FXML
    private void handleCreateDorm(ActionEvent event) {
        String name = dormNameField.getText().trim();
        User currentUser = SessionHandler.loadSession();

        if (name.isEmpty()) {
            showStatus("Please provide a name for your dorm.", true);
            return;
        }

        DormService.DormOperationResult result = dormService.createDorm(name, currentUser);

        if (result.isSuccess()) {
            triggerSuccess();
        } else {
            showStatus(result.getMessage(), true);
        }
    }

    @FXML
    private void handleJoinDorm(ActionEvent event) {
        String code = joinCodeField.getText().trim().toUpperCase();
        User currentUser = SessionHandler.loadSession();

        if (code.isEmpty()) {
            showStatus("Please enter a 6-digit join code.", true);
            return;
        }

        DormService.DormOperationResult result = dormService.joinDormWithCode(code, currentUser);

        if (result.isSuccess()) {
            triggerSuccess();
        } else {
            showStatus(result.getMessage(), true);
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    private void triggerSuccess() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
        if (onSuccessCallback != null) {
            onSuccessCallback.run();
        }
    }

    private void showStatus(String message, boolean isError) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: " + (isError ? "#e74c3c" : "#2ecc71") + "; -fx-font-size: 12px; -fx-font-weight: bold;");
    }
}