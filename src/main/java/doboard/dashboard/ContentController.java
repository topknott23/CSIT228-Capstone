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
            dormId = dormService.getUserDormId(currentUser.getUser_id());

            // --- NEW: FETCH AND INJECT DORM INFO ---
            Dorm dorm = dormService.getDormById(dormId);
            if (dorm != null) {
                if (dormNameLabel != null) dormNameLabel.setText(dorm.getDorm_name());
                if (joinCodeLabel != null) joinCodeLabel.setText(dorm.getJoin_code());
            }
        }

        refreshDashboard();
        setupHourlyRefresh();
    }

    @FXML private void goSignals(){NavigationManager.switchToTab("SIGNALS");}
    @FXML private void goExpenses(){NavigationManager.switchToTab("EXPENSES");}
    @FXML private void goChores(){NavigationManager.switchToTab("CHORES");}

    private void setupHourlyRefresh() {
        refreshTimeline = new Timeline(new KeyFrame(Duration.hours(1), event -> {
            refreshDashboard();
        }));
        refreshTimeline.setCycleCount(Timeline.INDEFINITE);
        refreshTimeline.play();
    }

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
        List<Chore> allDormChores = choreService.getAllDormChores(dormId);
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
        ExpenseService.UserBalanceSummary summary = expenseService.getUserBalanceDetails(dormId, userId);

        for (String alert : summary.alerts()) {
            addExpenseAlert(alert);
        }

        balanceValue.setText(String.format("%.2f", summary.totalBalance()));
        if (kpiBalanceVal != null) {
            kpiBalanceVal.setText(String.format("₱%.2f", summary.totalBalance()));
        }

        if (summary.totalBalance() > 0) {
            addNotification("You have pending bills to pay.", "System", false);
        }
    }

    private void loadNotifications() {
        List<SignalDAO.Signal> signals = signalService.getRecentSignals(currentUser.getUser_id(), dormId);

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
        refreshDashboard();
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