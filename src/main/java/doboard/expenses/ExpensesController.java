package doboard.expenses;

import doboard.auth.User;
import doboard.common.session.SessionHandler;
import doboard.common.util.NavigationManager;
import doboard.common.util.Popup;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.VBox;

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
        
        doboard.common.cache.DormDataCache.getInstance().addListener(this::refreshExpensesUI);
        
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
                
                doboard.common.cache.DormDataCache cache = doboard.common.cache.DormDataCache.getInstance();
                cache.reload(cache.getDormId(), cache.getCurrentUserId());
                cache.notifyListeners();
            } else {
                Popup.show("Error", "Failed to create bill splits.");
            }

        } catch (NumberFormatException e) {
            Popup.show("Error", "Invalid amount entered. Please enter a valid number.");
        }
    }

    @FXML
    private void markAsPaid(ActionEvent event){
        String input = Popup.showInput("Mark as Paid", "Enter Split ID:", "e.g. 12");

        if (input != null && !input.trim().isEmpty()) {
            try {
                int id = Integer.parseInt(input.trim());
                if(expenseService.updateSplitStatus(id, true)){
                    doboard.common.cache.DormDataCache cache = doboard.common.cache.DormDataCache.getInstance();
                    cache.reload(cache.getDormId(), cache.getCurrentUserId());
                    cache.notifyListeners();
                }
            } catch (NumberFormatException ignored) {
                Popup.show("Error", "Please enter a valid numeric ID.");
            }
        }
    }

    @FXML
    private void undoBill(ActionEvent event){
        String input = Popup.showInput("Undo Bill", "Enter Split ID to revert to unpaid:", "e.g. 12");

        if (input != null && !input.trim().isEmpty()) {
            try {
                int id = Integer.parseInt(input.trim());
                if(expenseService.updateSplitStatus(id, false)){
                    doboard.common.cache.DormDataCache cache = doboard.common.cache.DormDataCache.getInstance();
                    cache.reload(cache.getDormId(), cache.getCurrentUserId());
                    cache.notifyListeners();
                }
            } catch (NumberFormatException ignored) {
                Popup.show("Error", "Please enter a valid numeric ID.");
            }
        }
    }

    private void refreshExpensesUI() {
        dueBillContainer.getChildren().clear();
        processedBillContainer.getChildren().clear();
        transactionContainer.getChildren().clear();

        doboard.common.cache.DormDataCache cache = doboard.common.cache.DormDataCache.getInstance();
        if (cache.getCurrentUserId() == -1) return;

        List<Bill> dormBills = cache.getBills();

        for (Bill bill : dormBills) {
            List<BillSplit> splits = cache.getSplitsForBill(bill.getBill_id());

            for (BillSplit split : splits) {
                if (split.getUser_id() == cache.getCurrentUserId()) {
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

}