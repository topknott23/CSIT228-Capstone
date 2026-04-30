package doboard.core.controllers;

import doboard.core.dao.BillDAO;
import doboard.core.dao.DormDAO;
import doboard.core.models.Bill;
import doboard.core.models.Dorm;
import doboard.core.models.User;
import doboard.core.util.SceneLoader;
import doboard.core.util.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

public class BillsController {

    @FXML private ListView<String> billsListView;
    @FXML private VBox adminPanel;
    
    @FXML private TextField titleField;
    @FXML private TextField amountField;
    @FXML private DatePicker dueDatePicker;

    private DormDAO dormDAO;
    private BillDAO billDAO;
    private Dorm currentDorm;

    @FXML
    private void initialize() {
        dormDAO = new DormDAO();
        billDAO = new BillDAO();
        
        User user = UserSession.getInstance().getCurrentUser();
        if (user != null) {
            currentDorm = dormDAO.getUserDorm(user.getUser_id());
            if (currentDorm != null) {
                boolean isAdmin = dormDAO.isUserAdmin(user.getUser_id(), currentDorm.getDorm_id());
                if (isAdmin) {
                    adminPanel.setVisible(true);
                    adminPanel.setManaged(true);
                }
                loadBills();
            }
        }
    }

    private void loadBills() {
        int userId = UserSession.getInstance().getCurrentUser().getUser_id();
        List<String> summaries = billDAO.getUserBillSummaries(currentDorm.getDorm_id(), userId);
        billsListView.getItems().clear();
        billsListView.getItems().addAll(summaries);
    }

    @FXML
    private void handleCreateBill() {
        if (titleField.getText().isEmpty() || amountField.getText().isEmpty() || dueDatePicker.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Missing Fields", "Please fill in all fields.");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountField.getText());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Amount", "Please enter a valid number for the amount.");
            return;
        }

        boolean success = billDAO.createBillAndSplit(
            currentDorm.getDorm_id(),
            titleField.getText(),
            amount,
            dueDatePicker.getValue()
        );

        if (success) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Bill successfully created and split among all members!");
            titleField.clear();
            amountField.clear();
            dueDatePicker.setValue(null);
            loadBills();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to create bill. Are there members in the dorm?");
        }
    }

    // --- NAVIGATION ---
    @FXML private void goToDashboard(ActionEvent event) { navigate(event, "/com/doboard/view/main-menu.fxml", "DoBoard - Dashboard"); }
    @FXML private void goToDormitory(ActionEvent event) { navigate(event, "/com/doboard/view/dormitory-view.fxml", "DoBoard - My Dorm"); }
    @FXML private void goToChores(ActionEvent event) { navigate(event, "/com/doboard/view/chores-view.fxml", "DoBoard - Chores"); }
    
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
