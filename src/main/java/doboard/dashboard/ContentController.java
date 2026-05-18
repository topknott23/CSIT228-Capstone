package doboard.dashboard;

import doboard.auth.User;
import doboard.chores.ChoreService;
import doboard.common.session.SessionHandler;
import doboard.common.util.ComponentFactory;
import doboard.common.util.NavigationManager;
import doboard.common.util.Popup;
import doboard.chores.Chore;
import doboard.dorm.Dorm;
import doboard.dorm.DormService;
import doboard.expenses.*;
import doboard.signals.SignalDAO;
import doboard.signals.SignalService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.time.LocalDate;
import java.util.List;

public class ContentController {
    @FXML private VBox contentArea;
    @FXML private VBox systemNotifContainer;
    @FXML private VBox nudgeNotifContainer;
    @FXML private VBox dueChoreContainer;
    @FXML private VBox upcomingChoreContainer;
    @FXML private VBox expensesAlertContainer;

    @FXML private Label balanceValue;
    @FXML private Label kpiChoresVal;
    @FXML private Label kpiBalanceVal;
    @FXML private Label kpiNudgesVal;

    // --- NEW: TENANT DORM INFO LABELS ---
    @FXML private Label dormNameLabel;
    @FXML private Label joinCodeLabel;

    @FXML private Button signalBtnShrtct1;
    @FXML private Button signalBtnShrtct2;
    @FXML private Button signalBtnShrtct3;
    @FXML private Button signalBtnShrtct4;

    private final DormService dormService = new DormService();
    private final ChoreService choreService = new ChoreService();
    private final ExpenseService expenseService = new ExpenseService();
    private final SignalService signalService = new SignalService();
    private Timeline refreshTimeline;

    private User currentUser;
    private int dormId = -1;

    @FXML
    public void initialize() {
        NavigationManager.setTitle("Dashboard");
        currentUser = SessionHandler.loadSession();
        if (currentUser != null) {
            doboard.common.cache.DormDataCache cache = doboard.common.cache.DormDataCache.getInstance();
            dormId = cache.getDormId();

            // --- NEW: FETCH AND INJECT DORM INFO ---
            Dorm dorm = cache.getDorm();
            if (dorm != null) {
                if (dormNameLabel != null) dormNameLabel.setText(dorm.getDorm_name());
                if (joinCodeLabel != null) joinCodeLabel.setText(dorm.getJoin_code());
            }
            
            // Listen for background updates
            cache.addListener(this::refreshDashboard);
        }

        refreshDashboard();
    }

    @FXML private void goSignals(){NavigationManager.switchToTab("SIGNALS");}
    @FXML private void goExpenses(){NavigationManager.switchToTab("EXPENSES");}
    @FXML private void goChores(){NavigationManager.switchToTab("CHORES");}

    // Removed setupHourlyRefresh as polling is now handled by DataSyncService

    public void refreshDashboard() {
        dueChoreContainer.getChildren().clear();
        upcomingChoreContainer.getChildren().clear();
        expensesAlertContainer.getChildren().clear();
        systemNotifContainer.getChildren().clear();
        nudgeNotifContainer.getChildren().clear();

        if (currentUser == null || dormId == -1) return;

        loadChores(dormId);
        loadBillsAndBalance(dormId, currentUser.getUser_id());
        loadNotifications();
    }

    private void loadChores(int dormId) {
        List<Chore> allDormChores = doboard.common.cache.DormDataCache.getInstance().getChores();
        int dueCount = 0;

        for (Chore chore : allDormChores) {
            if (chore.getStatus() == Chore.Status.COMPLETE) continue;

            boolean isDue = !chore.getDue_date().isAfter(LocalDate.now());
            if (isDue) {
                addChore(dueChoreContainer, chore, true);
                dueCount++;
            } else {
                addChore(upcomingChoreContainer, chore, false);
            }
        }

        if (kpiChoresVal != null) kpiChoresVal.setText(String.valueOf(dueCount));

        if (dueCount > 0) {
            addNotification("You have " + dueCount + " pending chore(s) due!", "System", false);
        }
    }

    private void loadBillsAndBalance(int dormId, int userId) {
        double totalBalance = 0.0;
        doboard.common.cache.DormDataCache cache = doboard.common.cache.DormDataCache.getInstance();
        List<Bill> dormBills = cache.getBills();

        for (Bill bill : dormBills) {
            List<BillSplit> splits = cache.getSplitsForBill(bill.getBill_id());
            for (BillSplit split : splits) {
                if (split.getUser_id() == userId && !split.isPaid()) {
                    totalBalance += split.getAmount();
                    addExpenseAlert("Due: " + bill.getTitle() + " - ₱" + String.format("%.2f", split.getAmount()));
                }
            }
        }

        balanceValue.setText(String.format("%.2f", totalBalance));
        if (kpiBalanceVal != null) {
            kpiBalanceVal.setText(String.format("₱%.2f", totalBalance));
        }

        if (totalBalance > 0) {
            addNotification("You have pending bills to pay.", "System", false);
        }
    }

    private void loadNotifications() {
        List<SignalDAO.Signal> signals = doboard.common.cache.DormDataCache.getInstance().getSignals();

        if (kpiNudgesVal != null) kpiNudgesVal.setText(String.valueOf(signals.size()));

        for (SignalDAO.Signal signal : signals) {
            addNotification(
                    signal.senderName() + ": " + signal.complaint(),
                    signal.sentAt(),
                    true
            );
        }
    }

    public void addChore(VBox container, Chore chore, boolean isDue) {
        Node choreNode = DashboardComponentFactory.createChoreItem(
                chore.getTitle(), () -> markChoreAsDone(chore));
        if (choreNode != null) {
            if (!isDue) {
                Node doneBtn = choreNode.lookup("#doneBtn");
                if (doneBtn != null) doneBtn.setVisible(false);
            }
            container.getChildren().add(choreNode);
        }
    }

    private void markChoreAsDone(Chore chore) {
        choreService.completeAndRotateChore(chore);
        // Force an immediate reload and UI updateDorm
        doboard.common.cache.DormDataCache cache = doboard.common.cache.DormDataCache.getInstance();
        cache.reload(cache.getDormId(), cache.getCurrentUserId());
        cache.notifyListeners();
    }

    @FXML
    private void markAsDone(ActionEvent event) {}

    @FXML private void signalAct1(ActionEvent event) { sendQuickSignal("Too Loud"); }
    @FXML private void signalAct2(ActionEvent event) { sendQuickSignal("Kitchen Messy"); }
    @FXML private void signalAct3(ActionEvent event) { sendQuickSignal("Trash Full"); }
    @FXML private void signalAct4(ActionEvent event) { sendQuickSignal("Dishes Piled Up"); }

    private void sendQuickSignal(String complaint) {
        if (currentUser == null || dormId == -1) {
            Popup.show("Error", "You must be in a dorm to send signals.");
            return;
        }
        boolean sent = signalService.sendSignal(currentUser.getUser_id(), 0, dormId, complaint, 1);
        if (sent) {
            Popup.show("Signal Sent", "Nudge sent to all dormmates: " + complaint);
            // Refresh cache
            doboard.common.cache.DormDataCache cache = doboard.common.cache.DormDataCache.getInstance();
            cache.reload(cache.getDormId(), cache.getCurrentUserId());
            cache.notifyListeners();
        } else {
            Popup.show("Error", "Failed to send signal.");
        }
    }

    public void addNotification(String msg, String time, boolean isNudge) {
        Node notif = ComponentFactory.createNotification(msg, time);
        if (notif != null) {
            if (isNudge) {
                nudgeNotifContainer.getChildren().add(notif);
            } else {
                systemNotifContainer.getChildren().add(notif);
            }
        }
    }

    public void addExpenseAlert(String message) {
        Node alertNode = DashboardComponentFactory.createExpenseAlert(message);
        if (alertNode != null) expensesAlertContainer.getChildren().add(alertNode);
    }
}