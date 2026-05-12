package doboard.dorm;

import doboard.auth.User;
import doboard.common.session.SessionHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;

public class DormController {

    @FXML private TextField dormNameField;
    @FXML private TextField joinCodeField;
    @FXML private Label statusLabel;

    private final DormMemberDAO dormMemberDAO = new DormMemberDAO();

    /**
     * Logic for the "Create" button
     */
    @FXML
    private void handleCreateDorm() {
        String name = dormNameField.getText().trim();
        User currentUser = getSessionUser(); // Replace with your actual session logic

        if (name.isEmpty()) {
            showStatus("Please provide a name for your dorm.", true);
            return;
        }

        Dorm result = dormMemberDAO.createDormAsOwner(name, currentUser);

        if (result != null) {
            showStatus("Dorm created! Code: " + result.getJoin_code(), false);
            dormNameField.clear();
        } else {
            showStatus("Error: Could not create dorm. Are you already in one?", true);
        }
    }

    /**
     * Logic for the "Join" button
     */
    @FXML
    private void handleJoinDorm() {
        String code = joinCodeField.getText().trim().toUpperCase();
        User currentUser = getSessionUser();

        if (code.isEmpty()) {
            showStatus("Please enter a join code.", true);
            return;
        }

        boolean success = dormMemberDAO.joinDorm(code, currentUser);

        if (success) {
            showStatus("Successfully joined!", false);
            joinCodeField.clear();
            // Here you would typically load the main dashboard scene
        } else {
            showStatus("Invalid code or you are already a member of a dorm.", true);
        }
    }

    /**
     * Helper to update the UI status message
     */
    private void showStatus(String message, boolean isError) {
        statusLabel.setText(message);
        statusLabel.setTextFill(isError ? Color.FIREBRICK : Color.CHARTREUSE.darker());
    }

    /**
     * Placeholder for session management.
     * In a real app, this returns the User object of the person logged in.
     */
    private User getSessionUser() {
        User loadedUser = SessionHandler.loadSession();
        assert loadedUser != null;
        System.out.println("SESSION LOADED FOR USER: " + loadedUser.getUsername() + " ID: " + loadedUser.getUser_id());
        return loadedUser;
    }
}