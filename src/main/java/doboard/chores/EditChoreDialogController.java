package doboard.chores;

import doboard.auth.User;
import doboard.common.enums.Frequency;
import doboard.common.session.SessionHandler;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class EditChoreDialogController {

    @FXML private TextField titleField;
    @FXML private TextField descriptionField;
    @FXML private ComboBox<String> frequencyComboBox;
    @FXML private DatePicker dueDatePicker;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private ListView<String> usersListView;
    @FXML private Button cancelButton;
    @FXML private Button saveButton;

    private ChoreController parentController;
    private final ChoreService choreService = new ChoreService();
    private final ChoreDAO choreDAO = new ChoreDAO();
    private final ChoreAssignmentDAO choreAssignmentDAO = new ChoreAssignmentDAO();

    private Chore choreToEdit;
    private final ObservableList<String> availableUsers = FXCollections.observableArrayList();
    private List<User> dormUsers;

    @FXML
    public void initialize() {
        // Populate frequency combo box
        frequencyComboBox.setItems(FXCollections.observableArrayList(
                "ONCE", "DAILY", "WEEKLY", "MONTHLY"
        ));

        // Populate status combo box
        statusComboBox.setItems(FXCollections.observableArrayList(
                "PENDING", "COMPLETE"
        ));

        // Set up multi-selection for users
        usersListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    public void setChoreToEdit(Chore chore) {
        this.choreToEdit = chore;

        // Populate form with existing data
        titleField.setText(chore.getTitle());
        descriptionField.setText(chore.getDescription());
        frequencyComboBox.setValue(chore.getFrequency().name());
        dueDatePicker.setValue(chore.getDue_date());
        statusComboBox.setValue(chore.getStatus().name());

        // Load available users and select currently assigned ones
        loadDormUsersAndAssignments();
    }

    private void loadDormUsersAndAssignments() {
        User currentUser = SessionHandler.loadSession();
        if (currentUser == null) return;

        int dormId = choreService.getDormIdForUser(currentUser.getUser_id());
        if (dormId == -1) return;

        // Get all users in the dorm
        dormUsers = choreService.getDormUsers(dormId);
        availableUsers.clear();
        availableUsers.addAll(dormUsers.stream().map(User::getUsername).collect(Collectors.toList()));
        usersListView.setItems(availableUsers);

        List<Integer> assignedUserIds = choreAssignmentDAO.getUserIdsByChore(choreToEdit.getChore_id());
        for (Integer userId : assignedUserIds) {
            User user = dormUsers.stream().filter(u -> u.getUser_id() == userId).findFirst().orElse(null);
            if (user != null) {
                int index = availableUsers.indexOf(user.getUsername());
                if (index >= 0) {
                    usersListView.getSelectionModel().select(index);
                }
            }
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        closeDialog();
    }

    @FXML
    private void handleSave(ActionEvent event) {
        if (!validateInput()) {
            return;
        }


        choreToEdit.setTitle(titleField.getText().trim());
        choreToEdit.setDescription(descriptionField.getText().trim());
        choreToEdit.setFrequency(Frequency.valueOf(frequencyComboBox.getValue()));
        choreToEdit.setDue_date(dueDatePicker.getValue());
        choreToEdit.setStatus(Chore.Status.valueOf(statusComboBox.getValue()));

        // Update chore in database
        if (choreDAO.update(choreToEdit)) {
            // Update user assignments
            // First, remove all existing assignments
            choreAssignmentDAO.unassignAll(choreToEdit.getChore_id());

            // Then, assign selected users
            ObservableList<String> selectedUsers = usersListView.getSelectionModel().getSelectedItems();
            for (String username : selectedUsers) {
                User user = dormUsers.stream()
                        .filter(u -> u.getUsername().equals(username))
                        .findFirst()
                        .orElse(null);
                if (user != null) {
                    ChoreAssignment assignment = new ChoreAssignment(choreToEdit.getChore_id(), user.getUser_id());
                    choreAssignmentDAO.assign(assignment);
                }
            }

            // Refresh parent controller's leaderboard
            if (parentController != null) {
                parentController.initialize();
            }

            closeDialog();
        } else {
            showAlert("Error", "Failed to update chore");
        }
    }

    private boolean validateInput() {
        if (titleField.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Title is required");
            return false;
        }

        if (descriptionField.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Description is required");
            return false;
        }

        if (frequencyComboBox.getValue() == null) {
            showAlert("Validation Error", "Frequency is required");
            return false;
        }

        if (dueDatePicker.getValue() == null) {
            showAlert("Validation Error", "Due date is required");
            return false;
        }

        if (dueDatePicker.getValue().isBefore(LocalDate.now())) {
            showAlert("Validation Error", "Due date cannot be in the past");
            return false;
        }

        if (statusComboBox.getValue() == null) {
            showAlert("Validation Error", "Status is required");
            return false;
        }

        return true;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void closeDialog() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    public void setParentController(ChoreController parentController) {
        this.parentController = parentController;
    }
}