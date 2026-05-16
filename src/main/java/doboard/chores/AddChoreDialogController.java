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

public class AddChoreDialogController {

    @FXML private TextField titleField;
    @FXML private TextField descriptionField;
    @FXML private ComboBox<String> frequencyComboBox;
    @FXML private DatePicker dueDatePicker;
    @FXML private ListView<String> usersListView;
    @FXML private Button cancelButton;
    @FXML private Button addButton;

    private ChoreController parentController;
    private final ChoreService choreService = new ChoreService();
    private final ChoreDAO choreDAO = new ChoreDAO();
    private final ChoreAssignmentDAO choreAssignmentDAO = new ChoreAssignmentDAO();

    private final ObservableList<String> availableUsers = FXCollections.observableArrayList();
    private List<User> dormUsers;

    @FXML
    public void initialize() {
        // Populate frequency combo box
        frequencyComboBox.setItems(FXCollections.observableArrayList(
                "ONCE", "DAILY", "WEEKLY", "MONTHLY"
        ));

        // Load available users in the dorm
        loadDormUsers();

        // Set up multi-selection for users
        usersListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        usersListView.setItems(availableUsers);
    }

    private void loadDormUsers() {
        User currentUser = SessionHandler.loadSession();
        if (currentUser == null) return;

        int dormId = choreService.getDormIdForUser(currentUser.getUser_id());
        if (dormId == -1) return;

        dormUsers = choreService.getDormUsers(dormId);
        availableUsers.clear();
        availableUsers.addAll(dormUsers.stream().map(User::getUsername).collect(Collectors.toList()));
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        closeDialog();
    }

    @FXML
    private void handleAdd(ActionEvent event) {
        if (!validateInput()) {
            return;
        }

        // Get current user and dorm
        User currentUser = SessionHandler.loadSession();
        if (currentUser == null) {
            showAlert("Error", "No user session found");
            return;
        }

        int dormId = choreService.getDormIdForUser(currentUser.getUser_id());
        if (dormId == -1) {
            showAlert("Error", "User not in any dorm");
            return;
        }

        // Create new chore
        Chore chore = new Chore(
                0, // chore_id will be auto-generated
                dormId,
                titleField.getText().trim(),
                descriptionField.getText().trim(),
                Frequency.valueOf(frequencyComboBox.getValue()),
                dueDatePicker.getValue(),
                Chore.Status.PENDING
        );

        // Insert chore
        if (choreDAO.insert(chore)) {
            // Get the chore ID (this is a bit tricky since we don't have a way to get the generated key)
            // For now, we'll find the chore by its details
            List<Chore> chores = choreDAO.findAllByDormId(dormId);
            Chore insertedChore = chores.stream()
                    .filter(c -> c.getTitle().equals(chore.getTitle()) &&
                               c.getDescription().equals(chore.getDescription()))
                    .findFirst()
                    .orElse(null);

            if (insertedChore != null) {
                // Assign selected users to the chore
                ObservableList<String> selectedUsers = usersListView.getSelectionModel().getSelectedItems();
                for (String username : selectedUsers) {
                    User user = dormUsers.stream()
                            .filter(u -> u.getUsername().equals(username))
                            .findFirst()
                            .orElse(null);
                    if (user != null) {
                        ChoreAssignment assignment = new ChoreAssignment(insertedChore.getChore_id(), user.getUser_id());
                        choreAssignmentDAO.assign(assignment);
                    }
                }
            }

            // Refresh parent controller's leaderboard
            if (parentController != null) {
                parentController.initialize();
            }

            closeDialog();
        } else {
            showAlert("Error", "Failed to add chore");
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