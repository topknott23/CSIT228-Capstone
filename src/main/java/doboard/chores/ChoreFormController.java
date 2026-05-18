package doboard.chores;

import doboard.auth.User;
import doboard.common.enums.Frequency;
import doboard.common.session.SessionHandler;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class ChoreFormController {

    @FXML private Label dialogTitleLabel;
    @FXML private TextField titleField;
    @FXML private TextField descriptionField;
    @FXML private ComboBox<String> frequencyComboBox;
    @FXML private DatePicker dueDatePicker;

    // Status row is wrapped in a container so we can hide it in ADD mode
    @FXML private VBox statusContainer;
    @FXML private ComboBox<String> statusComboBox;

    @FXML private ListView<String> usersListView;
    @FXML private Button cancelButton;
    @FXML private Button submitButton;

    private ChoreController parentController;
    private final ChoreService choreService = new ChoreService();
    private final ChoreDAO choreDAO = new ChoreDAO(); // Using our unified DAO

    private Chore choreToEdit; // If null = ADD mode, if present = EDIT mode
    private final ObservableList<String> availableUsers = FXCollections.observableArrayList();
    private List<User> dormUsers;

    @FXML
    public void initialize() {
        frequencyComboBox.setItems(FXCollections.observableArrayList("ONCE", "DAILY", "WEEKLY", "MONTHLY"));
        statusComboBox.setItems(FXCollections.observableArrayList("PENDING", "COMPLETE"));
        usersListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        usersListView.setItems(availableUsers);
    }

    public void setParentController(ChoreController parentController) {
        this.parentController = parentController;
    }

    /**
     * Configures whether this form operates as an Add Form or an Edit Form dynamically.
     */
    public void setChoreToEdit(Chore chore) {
        this.choreToEdit = chore;
        loadDormUsers();

        if (choreToEdit != null) {
            // --- EDIT MODE CONFIGURATION ---
            dialogTitleLabel.setText("Edit Chore");
            submitButton.setText("Save Changes");
            statusContainer.setVisible(true);
            statusContainer.setManaged(true);

            titleField.setText(choreToEdit.getTitle());
            descriptionField.setText(choreToEdit.getDescription());
            frequencyComboBox.setValue(choreToEdit.getFrequency().name());
            dueDatePicker.setValue(choreToEdit.getDue_date());
            statusComboBox.setValue(choreToEdit.getStatus().name());

            // Pre-select currently assigned users
            List<Integer> assignedUserIds = choreDAO.getUserIdsByChore(choreToEdit.getChore_id());
            for (Integer userId : assignedUserIds) {
                dormUsers.stream()
                        .filter(u -> u.getUser_id() == userId)
                        .findFirst()
                        .ifPresent(user -> usersListView.getSelectionModel().select(user.getUsername()));
            }
        } else {
            // --- ADD MODE CONFIGURATION ---
            dialogTitleLabel.setText("Add New Chore");
            submitButton.setText("Add Chore");
            statusContainer.setVisible(false);
            statusContainer.setManaged(false);
            dueDatePicker.setValue(LocalDate.now());
        }
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
    private void handleSubmit(ActionEvent event) {
        if (!validateInput()) return;

        User currentUser = SessionHandler.loadSession();
        if (currentUser == null) return;

        int dormId = choreService.getDormIdForUser(currentUser.getUser_id());
        if (dormId == -1) return;

        if (choreToEdit == null) {
            // Execute ADD Flow
            Chore newChore = new Chore(0, dormId, titleField.getText().trim(), descriptionField.getText().trim(),
                    Frequency.valueOf(frequencyComboBox.getValue()), dueDatePicker.getValue(), Chore.Status.PENDING);

            int insertedId = choreDAO.insertAndReturnId(newChore);
            if (insertedId != -1) {
                assignSelectedUsers(insertedId);
                finishAndRefresh();
            } else {
                showAlert("Error", "Failed to add new chore.");
            }
        } else {
            // Execute EDIT Flow
            choreToEdit.setTitle(titleField.getText().trim());
            choreToEdit.setDescription(descriptionField.getText().trim());
            choreToEdit.setFrequency(Frequency.valueOf(frequencyComboBox.getValue()));
            choreToEdit.setDue_date(dueDatePicker.getValue());
            choreToEdit.setStatus(Chore.Status.valueOf(statusComboBox.getValue()));

            if (choreDAO.update(choreToEdit)) {
                choreDAO.unassignAll(choreToEdit.getChore_id());
                assignSelectedUsers(choreToEdit.getChore_id());
                finishAndRefresh();
            } else {
                showAlert("Error", "Failed to updateDorm chore.");
            }
        }
    }

    private void assignSelectedUsers(int choreId) {
        ObservableList<String> selectedUsernames = usersListView.getSelectionModel().getSelectedItems();
        for (String username : selectedUsernames) {
            dormUsers.stream()
                    .filter(u -> u.getUsername().equals(username))
                    .findFirst()
                    .ifPresent(user -> choreDAO.assign(new ChoreAssignment(choreId, user.getUser_id())));
        }
    }

    private void finishAndRefresh() {
        if (parentController != null) {
            parentController.initialize(); // Refresh leaderboard and tables
        }
        closeDialog();
    }

    private boolean validateInput() {
        if (titleField.getText().trim().isEmpty()) { showAlert("Validation Error", "Title is required."); return false; }
        if (descriptionField.getText().trim().isEmpty()) { showAlert("Validation Error", "Description is required."); return false; }
        if (frequencyComboBox.getValue() == null) { showAlert("Validation Error", "Frequency is required."); return false; }
        if (dueDatePicker.getValue() == null) { showAlert("Validation Error", "Due date is required."); return false; }
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
}