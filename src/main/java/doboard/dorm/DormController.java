package doboard.dorm;

import doboard.auth.User;
import doboard.common.session.SessionHandler;
import doboard.common.util.SceneLoader;
import doboard.common.util.StageUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class DormController {

    @FXML private TextField dormNameField;
    @FXML private TextField joinCodeField;
    @FXML private Label statusLabel;

    private final DormService dormService = new DormService();

    /**
     * Logic for the "Create" button
     */
    @FXML
    private void handleCreateDorm() {
        String name = dormNameField.getText().trim();
        User currentUser = SessionHandler.loadSession();

        if (name.isEmpty()) {
            showStatus("Please provide a name for your dorm.", true);
            return;
        }

        assert currentUser != null;
        DormService.DormOperationResult result = dormService.createDorm(name, currentUser);

        if (result.isSuccess()) {
            showStatus("Dorm created! Code: " + result.getDorm().getJoin_code(), false);
            dormNameField.clear();
        } else {
            showStatus("Error: Could not create dorm. Are you already in one?", true);
        }
    }

    /**
     * Logic for the "Join" button
     */
    @FXML
    private void handleJoinDorm(ActionEvent event) {
        String code = joinCodeField.getText().trim().toUpperCase();
        User currentUser = SessionHandler.loadSession();

        if (code.isEmpty()) {
            showStatus("Please enter a join code.", true);
            return;
        }

        DormService.DormOperationResult result = dormService.joinDormWithCode(code, currentUser);

        if (result.isSuccess()) {
            Dorm joinedDorm = result.getDorm();
            showStatus("Successfully joined dorm:" + joinedDorm.getDorm_name(), false);
            joinCodeField.clear();
            Stage stage = StageUtil.getStage(event);

            SceneLoader.loadScene(stage, getClass(), "/doboard/dashboard/dashboard-view.fxml", "DoBoard - Dorm: " + joinedDorm.getDorm_name());
        } else {
            showStatus("Invalid code or you are already a member of a dorm.", true);
        }
    }

    /**
     * Helper to updateDorm the UI status message
     */
    private void showStatus(String message, boolean isError) {
        statusLabel.setText(message);
        statusLabel.setTextFill(isError ? Color.FIREBRICK : Color.CHARTREUSE.darker());
    }

}