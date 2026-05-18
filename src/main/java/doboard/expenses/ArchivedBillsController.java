package doboard.expenses;

import doboard.auth.User;
import doboard.common.connection.SQLConnector;
import doboard.common.session.SessionHandler;
import doboard.common.util.NavigationManager;
import doboard.common.util.Popup;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

public class ArchivedBillsController {

    @FXML private VBox logsContainer;
    private final ExpenseService expenseService = new ExpenseService();

    @FXML
    public void initialize() {
        NavigationManager.setTitle("Archived Bills");
        loadArchivedBills();
    }

    private void loadArchivedBills() {
        User currentUser = SessionHandler.loadSession();
        if (currentUser == null) return;

        int userDormId = expenseService.getDormIdForUser(currentUser.getUser_id());
        if (userDormId == -1) return;

        List<Bill> dormBills = expenseService.getDormBills(userDormId);

        // Clear the container first so lines don't stack duplicates on reload
        logsContainer.getChildren().clear();

        for (Bill bill : dormBills) {
            List<BillSplit> splits = expenseService.getSplitsForBill(bill.getBill_id());

            for (BillSplit split : splits) {
                if (split.getUser_id() == currentUser.getUser_id() && split.isPaid()) {

                    // Pass the lambda callback as the 4th parameter
                    Node logRow = ExpenseComponentFactory.createTransactionItem(
                            bill.getTitle(),
                            bill.getBill_due_date().toString(),
                            split.getAmount(),
                            () -> executeArchiveUndo(split.getSplit_id()) // 4th Parameter added here
                    );

                    if (logRow != null) {
                        logsContainer.getChildren().add(logRow);
                    }
                }
            }
        }
    }

    private void executeArchiveUndo(int splitId) {
        // --- SECURITY LOCK CHECK ---
        boolean isLocked = false;
        String securityQuery = "SELECT b.title, u.username FROM bill_splits bs " +
                "JOIN bills b ON bs.bill_id = b.bill_id " +
                "JOIN users u ON b.paid_by = u.user_id " +
                "WHERE bs.split_id = ?";

        try (Connection c = SQLConnector.getConnection();
             PreparedStatement s = c.prepareStatement(securityQuery)) {
            s.setInt(1, splitId);
            try (ResultSet r = s.executeQuery()) {
                if (r.next()) {
                    String title = r.getString("title").toLowerCase();
                    String username = r.getString("username").toLowerCase();
                    if (title.contains("rent") || username.equals("admin")) {
                        isLocked = true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (isLocked) {
            Popup.show("Access Denied", "Rent and Admin-issued records cannot be modified.");
            return;
        }
        // ----------------------------

        // Update the record to unpaid in the DB using your existing expenseService setup
        if (expenseService.updateSplitStatus(splitId, false)) {
            // Sync your friend's global cache engine state
            doboard.common.cache.DormDataCache cache = doboard.common.cache.DormDataCache.getInstance();
            cache.reload(cache.getDormId(), cache.getCurrentUserId());

            // Reload this specific view scene to make the un-archived row disappear instantly
            loadArchivedBills();

            // Notify other active background listeners (like the main Expenses dashboard) to update too
            cache.notifyListeners();
        } else {
            Popup.show("Error", "Could not process the archive reversal request.");
        }
    }
}