package doboard.core.controllers;

import doboard.core.dao.ChoreDAO;
import doboard.core.dao.DormDAO;
import doboard.core.models.Chore;
import doboard.core.models.Dorm;
import doboard.core.models.Frequency;
import doboard.core.models.User;
import doboard.core.util.SceneLoader;
import doboard.core.util.UserSession;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

public class ChoresController {

    @FXML private ListView<String> choresListView;
    @FXML private VBox adminPanel;
    
    @FXML private TextField titleField;
    @FXML private TextField descField;
    @FXML private ComboBox<String> frequencyBox;
    @FXML private DatePicker dueDatePicker;
    @FXML private ComboBox<User> assigneeBox;

    private DormDAO dormDAO;
    private ChoreDAO choreDAO;
    private Dorm currentDorm;

    @FXML
    private void initialize() {
        dormDAO = new DormDAO();
        choreDAO = new ChoreDAO();
        
        User user = UserSession.getInstance().getCurrentUser();
        if (user != null) {
            currentDorm = dormDAO.getUserDorm(user.getUser_id());
            if (currentDorm != null) {
                boolean isAdmin = dormDAO.isUserAdmin(user.getUser_id(), currentDorm.getDorm_id());
                if (isAdmin) {
                    adminPanel.setVisible(true);
                    adminPanel.setManaged(true);
                    setupAdminPanel();
                }
                loadChores();
            }
        }
    }

    private void setupAdminPanel() {
        frequencyBox.setItems(FXCollections.observableArrayList("DAILY", "WEEKLY", "MONTHLY"));
        
        List<User> members = dormDAO.getDormMembers(currentDorm.getDorm_id());
        assigneeBox.setItems(FXCollections.observableArrayList(members));
        
        // Custom rendering for User objects in ComboBox
        assigneeBox.setCellFactory(param -> new ListCell<User>() {
            @Override
            protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getUsername());
                }
            }
        });
        assigneeBox.setButtonCell(assigneeBox.getCellFactory().call(null));

        // Auto-calculate due date based on frequency
        frequencyBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                java.time.LocalDate now = java.time.LocalDate.now();
                switch (newVal) {
                    case "DAILY": dueDatePicker.setValue(now.plusDays(1)); break;
                    case "WEEKLY": dueDatePicker.setValue(now.plusWeeks(1)); break;
                    case "MONTHLY": dueDatePicker.setValue(now.plusMonths(1)); break;
                }
            }
        });
    }

    private void loadChores() {
        int userId = UserSession.getInstance().getCurrentUser().getUser_id();
        List<String> summaries = choreDAO.getChoreSummariesForUser(currentDorm.getDorm_id(), userId);
        choresListView.getItems().clear();
        choresListView.getItems().addAll(summaries);
    }

    @FXML
    private void handleCreateChore() {
        if (titleField.getText().isEmpty() || frequencyBox.getValue() == null || dueDatePicker.getValue() == null || assigneeBox.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Missing Fields", "Please fill in all fields.");
            return;
        }

        boolean success = choreDAO.createChore(
            currentDorm.getDorm_id(),
            titleField.getText(),
            descField.getText() == null ? "" : descField.getText(),
            frequencyBox.getValue(),
            dueDatePicker.getValue(),
            assigneeBox.getValue().getUser_id()
        );

        if (success) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Chore assigned successfully!");
            titleField.clear();
            descField.clear();
            dueDatePicker.setValue(null);
            loadChores();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to assign chore.");
        }
    }

    // --- NAVIGATION ---
    @FXML private void goToDashboard(ActionEvent event) { navigate(event, "/com/doboard/view/main-menu.fxml", "DoBoard - Dashboard"); }
    @FXML private void goToDormitory(ActionEvent event) { navigate(event, "/com/doboard/view/dormitory-view.fxml", "DoBoard - My Dorm"); }
    @FXML private void goToBills(ActionEvent event) { navigate(event, "/com/doboard/view/bills-view.fxml", "DoBoard - Bills"); }
    
    @FXML
    private void logout(ActionEvent event) {
        UserSession.getInstance().clearSession();
        navigate(event, "/com/doboard/view/login-view.fxml", "DoBoard - Login");
    }

    private void navigate(ActionEvent event, String fxml, String title) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        SceneLoader.loadScene(stage, fxml, title);
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
