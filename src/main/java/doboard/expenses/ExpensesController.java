package doboard.expenses;

import doboard.auth.User;
import doboard.common.session.SessionHandler;
import doboard.common.util.ComponentFactory;
import doboard.common.util.NavigationManager;
import doboard.common.util.Popup;
import doboard.dorm.DormMemberDAO;
import doboard.dorm.DormMember;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class ExpensesController {
    @FXML private VBox contentArea;
    @FXML private VBox dueBillContainer;
    @FXML private TextField billAmountTextField;
    @FXML private ComboBox<String> purposeComboBox;
    @FXML private VBox processedBillContainer;
    @FXML private VBox transactionContainer;

    private final ExpenseService expenseService = new ExpenseService();

    @FXML
    public void initialize() {
        NavigationManager.setTitle("Expenses"); // init title
        purposeComboBox.getItems().addAll("Rent", "Electricity", "Water", "Internet", "Groceries", "Other");
        refreshExpensesUI();
    }

    public void addBillRow(String title, double amt) {
        Node row = ExpenseComponentFactory.createDueBill(title, amt);
        if (row != null) dueBillContainer.getChildren().add(row);
    }

    public void addProcessedBill(String date){
        Node row = ExpenseComponentFactory.createProcessedBill(date);
        if(row != null) processedBillContainer.getChildren().add(row);
    }

    public void addTransaction(String title, String date, double amount){
        Node row = ExpenseComponentFactory.createTransactionItem(title, date, amount);
        if(row != null) transactionContainer.getChildren().add(row);
    }

    @FXML
    private void splitBill(ActionEvent event) {
        String amountStr = billAmountTextField.getText();
        String purpose = purposeComboBox.getValue();

        if (amountStr == null || amountStr.trim().isEmpty() || purpose == null) {
            Popup.show("Error", "Please enter amount and select a purpose.");
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);

            if (amount <= 0) {
                Popup.show("Error", "Amount must be greater than zero.");
                return;
            }
            if (amount > 100000) {
                Popup.show("Error", "Amount exceeds maximum allowed value (₱100,000).");
                return;
            }

            User currentUser = SessionHandler.loadSession();

            if(currentUser == null) return;

            int dormId = expenseService.getDormIdForUser(currentUser.getUser_id());
            if(dormId == -1) {
                Popup.show("Error", "User is not assigned to a dorm.");
                return;
            }

            boolean success = expenseService.processBillSplit(dormId, purpose, amount);

            if (success) {
                Popup.show("Success", "Bill structured for: " + purpose + " at ₱" + String.format("%.2f", amount));
                billAmountTextField.clear();
                purposeComboBox.getSelectionModel().clearSelection();
                refreshExpensesUI();
            } else {
                Popup.show("Error", "Failed to create bill splits.");
            }

        } catch (NumberFormatException e) {
            Popup.show("Error", "Invalid amount entered. Please enter a valid number.");
        }
    }

    @FXML
    private void markAsPaid(ActionEvent event){
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Mark as Paid");
        dialog.setHeaderText(null);
        dialog.setContentText("Enter Split ID:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(idStr -> {
            try {
                int id = Integer.parseInt(idStr.trim());
                if(expenseService.updateSplitStatus(id, true)){
                    refreshExpensesUI();
                }
            } catch (NumberFormatException ignored) {}
        });
    }

    @FXML
    private void undoBill(ActionEvent event){
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Undo Bill");
        dialog.setHeaderText(null);
        dialog.setContentText("Enter Split ID to undo:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(idStr -> {
            try {
                int id = Integer.parseInt(idStr.trim());
                if(expenseService.updateSplitStatus(id, false)){
                    refreshExpensesUI();
                }
            } catch (NumberFormatException ignored) {}
        });
    }

    private void refreshExpensesUI() {
        dueBillContainer.getChildren().clear();
        processedBillContainer.getChildren().clear();
        transactionContainer.getChildren().clear();

        User currentUser = SessionHandler.loadSession();
        if (currentUser == null) return;

        int userDormId = expenseService.getDormIdForUser(currentUser.getUser_id());
        if (userDormId == -1) return;

        List<Bill> dormBills = expenseService.getDormBills(userDormId);

        for (Bill bill : dormBills) {
            List<BillSplit> splits = expenseService.getSplitsForBill(bill.getBill_id());

            for (BillSplit split : splits) {
                if (split.getUser_id() == currentUser.getUser_id()) {
                    if (!split.isPaid()) {
                        addBillRow(bill.getTitle() + " (ID: " + split.getSplit_id() + ")", split.getAmount());
                    } else {
                        addTransaction(bill.getTitle(), bill.getBill_due_date().toString(), split.getAmount());
                        addProcessedBill("Paid ID " + split.getSplit_id() + " on " + bill.getBill_due_date().toString());
                        }
                    }
                }
        }
    }

    @FXML private void goDashboard() { NavigationManager.loadView(getClass(), "/doboard/dashboard/content-view.fxml"); }
    @FXML private void goChores() { NavigationManager.loadView(getClass(), "/doboard/chores/chore-view.fxml"); }
    @FXML private void goSignals() { NavigationManager.loadView(getClass(), "/doboard/signals/signals-view.fxml"); }
}