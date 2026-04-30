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

    @FXML private ListView<doboard.core.models.BillDisplay> billsListView;
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
                setupListView();
                loadBills();
            }
        }
    }

    private void setupListView() {
        billsListView.setCellFactory(param -> new ListCell<doboard.core.models.BillDisplay>() {
            @Override
            protected void updateItem(doboard.core.models.BillDisplay bill, boolean empty) {
                super.updateItem(bill, empty);
                if (empty || bill == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    javafx.scene.layout.HBox root = new javafx.scene.layout.HBox(10);
                    root.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    root.setStyle("-fx-padding: 10px; -fx-background-color: #E25D5D; -fx-background-radius: 5px;");

                    String displayText = String.format("%s - Total: $%.2f | You Owe: $%.2f (Due: %s)", 
                        bill.getTitle(), bill.getTotalAmount(), bill.getAmountOwed(), bill.getDueDate());
                    
                    Label billLabel = new Label(displayText);
                    billLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
                    javafx.scene.layout.HBox.setHgrow(billLabel, javafx.scene.layout.Priority.ALWAYS);
                    billLabel.setMaxWidth(Double.MAX_VALUE);

                    Button payBtn = new Button("Pay My Share");
                    payBtn.setStyle("-fx-background-color: #1A1A1A; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
                    
                    if (bill.isPaid()) {
                        payBtn.setText("Paid");
                        payBtn.setDisable(true);
                        root.setStyle("-fx-padding: 10px; -fx-background-color: #888888; -fx-background-radius: 5px;");
                    } else {
                        payBtn.setOnAction(e -> {
                            if (billDAO.markBillAsPaid(bill.getSplitId())) {
                                doboard.core.util.AppEventBus.getInstance().notifyObservers();
                                loadBills();
                            }
                        });
                    }

                    root.getChildren().addAll(billLabel, payBtn);
                    setGraphic(root);
                    setStyle("-fx-background-color: transparent; -fx-padding: 5px 0px;");
                }
            }
        });
    }

    private void loadBills() {
        int userId = UserSession.getInstance().getCurrentUser().getUser_id();
        List<doboard.core.models.BillDisplay> bills = billDAO.getUserBills(currentDorm.getDorm_id(), userId);
        billsListView.getItems().clear();
        billsListView.getItems().addAll(bills);
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
