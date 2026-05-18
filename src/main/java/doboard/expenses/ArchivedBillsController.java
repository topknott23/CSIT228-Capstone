package doboard.expenses;

import doboard.auth.User;
import doboard.common.session.SessionHandler;
import doboard.common.util.NavigationManager;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
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

        // Fetch all bills for the dorm without needing new SQL queries
        List<Bill> dormBills = expenseService.getDormBills(userDormId);

        for (Bill bill : dormBills) {
            List<BillSplit> splits = expenseService.getSplitsForBill(bill.getBill_id());

            for (BillSplit split : splits) {
                // Filter for the current user AND ensure it is already paid
                if (split.getUser_id() == currentUser.getUser_id() && split.isPaid()) {

                    // Re-use your existing UI factory for a clean look
                    Node logRow = ExpenseComponentFactory.createTransactionItem(
                            bill.getTitle(),
                            bill.getBill_due_date().toString(),
                            split.getAmount()
                    );

                    if (logRow != null) {
                        logsContainer.getChildren().add(logRow);
                    }
                }
            }
        }
    }
}