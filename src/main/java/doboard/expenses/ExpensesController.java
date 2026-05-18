package doboard.expenses;

import doboard.auth.User;
import doboard.common.connection.SQLConnector;
import doboard.common.session.SessionHandler;
import doboard.common.util.NavigationManager;
import doboard.common.util.Popup;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ExpensesController {
    @FXML private VBox contentArea;
    @FXML private VBox dueBillContainer;
    @FXML private TextField billAmountTextField;
    @FXML private TextField purposeField; // Replaced ComboBox
    @FXML private VBox processedBillContainer;
    @FXML private VBox transactionContainer;

    private final ExpenseService expenseService = new ExpenseService();

    @FXML
    public void initialize() {
        NavigationManager.setTitle("Expenses");
        doboard.common.cache.DormDataCache.getInstance().addListener(this::refreshExpensesUI);
        refreshExpensesUI();
    }

    // Now accepts the splitId to pass into the factory callback
    public void addBillRow(String title, double amt, int splitId) {
        Node row = ExpenseComponentFactory.createDueBill(title, amt, () -> executePayment(splitId));
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
        String purpose = purposeField.getText();

        if (amountStr == null || amountStr.trim().isEmpty() || purpose == null || purpose.trim().isEmpty()) {
            Popup.show("Error", "Please enter amount and purpose.");
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
                purposeField.clear();
                
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

    // Dedicated execution method to handle the inline button callback
    private void executePayment(int splitId) {
        // --- SECURITY LOCK ---
        boolean isLocked = false;
        try (Connection c = SQLConnector.getConnection();
             PreparedStatement s = c.prepareStatement(
                     "SELECT b.title, u.username FROM bill_splits bs " +
                             "JOIN bills b ON bs.bill_id = b.bill_id " +
                             "JOIN users u ON b.paid_by = u.user_id " +
                             "WHERE bs.split_id = ?")) {
            s.setInt(1, splitId);
            ResultSet r = s.executeQuery();
            if (r.next()) {
                String title = r.getString("title").toLowerCase();
                String username = r.getString("username").toLowerCase();

                if (title.contains("rent") || username.equals("admin")) {
                    isLocked = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (isLocked) {
            Popup.show("Access Denied", "Rent and Admin-issued bills can only be settled by the Master Landlord.");
            return;
        }
        // ---------------------

        if (expenseService.markAsPaid(splitId)) {
            doboard.common.cache.DormDataCache cache = doboard.common.cache.DormDataCache.getInstance();
            cache.reload(cache.getDormId(), cache.getCurrentUserId());
            cache.notifyListeners();
        } else {
            Popup.show("Error", "Could not process the payment.");
        }
    }

    @FXML
    private void undoBill(ActionEvent event) {
        User currentUser = SessionHandler.loadSession();
        if (currentUser == null) return;

        int userDormId = expenseService.getDormIdForUser(currentUser.getUser_id());
        if (userDormId == -1) return;

        List<String> paidBills = new ArrayList<>();
        List<Bill> dormBills = expenseService.getDormBills(userDormId);

        for (Bill bill : dormBills) {
            List<BillSplit> splits = expenseService.getSplitsForBill(bill.getBill_id());
            for (BillSplit split : splits) {
                if (split.getUser_id() == currentUser.getUser_id() && split.isPaid()) {
                    paidBills.add("ID: " + split.getSplit_id() + " | " + bill.getTitle() + " | ₱" + String.format("%.2f", split.getAmount()));
                }
            }
        }

        if (paidBills.isEmpty()) {
            Popup.show("Empty Ledger", "You have no completed payments to undo.");
            return;
        }

        ChoiceDialog<String> dialog = new ChoiceDialog<>(paidBills.get(0), paidBills);
        dialog.setTitle("Undo Payment");
        dialog.setHeaderText("Select the payment you want to revert to unpaid:");
        dialog.setContentText("Completed Payment:");

        dialog.showAndWait().ifPresent(selected -> {
            try {
                int splitId = Integer.parseInt(selected.substring(4, selected.indexOf(" |")));

                // --- SECURITY LOCK ---
                boolean isLocked = false;
                try (Connection c = SQLConnector.getConnection();
                     PreparedStatement s = c.prepareStatement(
                             "SELECT b.title, u.username FROM bill_splits bs " +
                                     "JOIN bills b ON bs.bill_id = b.bill_id " +
                                     "JOIN users u ON b.paid_by = u.user_id " +
                                     "WHERE bs.split_id = ?")) {
                    s.setInt(1, splitId);
                    ResultSet r = s.executeQuery();
                    if (r.next()) {
                        String title = r.getString("title").toLowerCase();
                        String username = r.getString("username").toLowerCase();

                        if (title.contains("rent") || username.equals("admin")) {
                            isLocked = true;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (isLocked) {
                    Popup.show("Access Denied", "Rent and Admin-issued records cannot be modified.");
                    return;
                }
                // ---------------------

                if (expenseService.updateSplitStatus(splitId, false)) {
                    doboard.common.cache.DormDataCache cache = doboard.common.cache.DormDataCache.getInstance();
                    cache.reload(cache.getDormId(), cache.getCurrentUserId());
                    cache.notifyListeners();
                }
            } catch (Exception e) {
                Popup.show("Error", "Failed to parse the selected bill.");
            }
        });
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
                        // Pass the clean title and the database split_id for the callback
                        addBillRow(bill.getTitle(), split.getAmount(), split.getSplit_id());
                    } else {
                        addTransaction(bill.getTitle(), bill.getBill_due_date().toString(), split.getAmount());
                        addProcessedBill("Paid ID " + split.getSplit_id() + " on " + bill.getBill_due_date().toString());
                    }
                }
            }
        }
    }
}