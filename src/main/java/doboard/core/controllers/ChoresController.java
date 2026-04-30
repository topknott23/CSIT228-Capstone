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

import doboard.core.models.ChoreDisplay;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

public class ChoresController {

    @FXML private ListView<ChoreDisplay> choresListView;
    @FXML private ListView<ChoreDisplay> completedChoresListView;
    @FXML private VBox adminPanel;
    
    @FXML private TextField titleField;
    @FXML private TextField descField;
    @FXML private ComboBox<String> frequencyBox;
    @FXML private DatePicker dueDatePicker;
    @FXML private ComboBox<User> assigneeBox;

    private DormDAO dormDAO;
    private ChoreDAO choreDAO;
    private Dorm currentDorm;
    private boolean isAdmin;

    @FXML
    private void initialize() {
        dormDAO = new DormDAO();
        choreDAO = new ChoreDAO();
        
        User user = UserSession.getInstance().getCurrentUser();
        if (user != null) {
            currentDorm = dormDAO.getUserDorm(user.getUser_id());
            if (currentDorm != null) {
                isAdmin = dormDAO.isUserAdmin(user.getUser_id(), currentDorm.getDorm_id());
                if (isAdmin) {
                    adminPanel.setVisible(true);
                    adminPanel.setManaged(true);
                    setupAdminPanel();
                }
                setupListView();
                choreDAO.cleanupOldCompletedChores(); // Auto-delete overdue completed chores
                loadChores();
            }
        }
    }

    private void setupListView() {
        choresListView.setCellFactory(param -> createChoreCell());
        if (completedChoresListView != null) {
            completedChoresListView.setCellFactory(param -> createChoreCell());
        }
    }

    private ListCell<ChoreDisplay> createChoreCell() {
        return new ListCell<ChoreDisplay>() {
            @Override
            protected void updateItem(ChoreDisplay chore, boolean empty) {
                super.updateItem(chore, empty);

                if (empty || chore == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    HBox root = new HBox(10);
                    root.setAlignment(Pos.CENTER_LEFT);
                    root.setStyle("-fx-padding: 10px; -fx-background-color: #0E7CB5; -fx-background-radius: 5px;");

                    String displayText = chore.getTitle() + " - Due: " + chore.getDueDate() + " [" + chore.getStatus() + "]";
                    if (isAdmin) {
                        displayText += " (Assigned to: " + chore.getAssigneeName() + ")";
                    }

                    Label choreLabel = new Label(displayText);
                    choreLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
                    HBox.setHgrow(choreLabel, Priority.ALWAYS);
                    choreLabel.setMaxWidth(Double.MAX_VALUE);

                    Button doneBtn = new Button("Task done button");
                    doneBtn.setStyle("-fx-background-color: #1A1A1A; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
                    
                    if ("COMPLETED".equals(chore.getStatus())) {
                        doneBtn.setText("Completed");
                        doneBtn.setDisable(true);
                        root.setStyle("-fx-padding: 10px; -fx-background-color: #888888; -fx-background-radius: 5px;");
                    } else {
                        doneBtn.setOnAction(e -> {
                            if (choreDAO.markChoreAsDone(chore.getChoreId())) {
                                doboard.core.util.AppEventBus.getInstance().notifyObservers();
                                loadChores();
                            }
                        });
                    }

                    root.getChildren().addAll(choreLabel, doneBtn);
                    setGraphic(root);
                    setStyle("-fx-background-color: transparent; -fx-padding: 5px 0px;");
                }
            }
        };
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
        List<ChoreDisplay> chores = choreDAO.getChoresForUser(currentDorm.getDorm_id(), userId, isAdmin);
        
        choresListView.getItems().clear();
        if (completedChoresListView != null) completedChoresListView.getItems().clear();

        for (ChoreDisplay c : chores) {
            if ("COMPLETED".equals(c.getStatus())) {
                if (completedChoresListView != null) completedChoresListView.getItems().add(c);
            } else {
                choresListView.getItems().add(c);
            }
        }
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
            doboard.core.util.AppEventBus.getInstance().notifyObservers();
            loadChores();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to assign chore.");
        }
    }

    @FXML
    private void handleBatchAssign() {
        List<User> members = dormDAO.getDormMembers(currentDorm.getDorm_id());
        if (members.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "No members in the dorm to assign tasks to!");
            return;
        }

        // Hardcoded presets
        String[][] presets = {
            {"Wash Dishes", "Clean all dishes in the sink", "DAILY"},
            {"Sweep Floor", "Sweep the common area", "DAILY"},
            {"Refill Water", "Refill the drinking water dispenser", "DAILY"},
            {"Take Out Trash", "Empty the main trash bin", "DAILY"}
        };

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Batch Assign Presets");
        alert.setHeaderText("Distribute Daily Chores");
        alert.setContentText("This will automatically distribute the following daily chores evenly among all " + members.size() + " members:\n\n" +
                "- Wash Dishes\n- Sweep Floor\n- Refill Water\n- Take Out Trash\n\nProceed?");

        java.util.Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            int memberIndex = 0;
            java.time.LocalDate today = java.time.LocalDate.now();
            int successCount = 0;

            for (String[] preset : presets) {
                String title = preset[0];
                String desc = preset[1];
                String freq = preset[2];
                User assignee = members.get(memberIndex);

                boolean success = choreDAO.createChore(
                    currentDorm.getDorm_id(), title, desc, freq, today, assignee.getUser_id()
                );

                if (success) successCount++;
                
                // Move to next member (Round Robin)
                memberIndex = (memberIndex + 1) % members.size();
            }

            showAlert(Alert.AlertType.INFORMATION, "Batch Assigned", "Successfully distributed " + successCount + " chores!");
            doboard.core.util.AppEventBus.getInstance().notifyObservers();
            loadChores();
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
