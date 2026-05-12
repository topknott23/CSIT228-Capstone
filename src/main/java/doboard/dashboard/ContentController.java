package doboard.dashboard;

import doboard.auth.User;
import doboard.common.session.SessionHandler;
import doboard.common.util.ComponentFactory;
import doboard.common.util.NavigationManager;
import doboard.chores.Chore;
import doboard.chores.ChoreDAO;
import doboard.chores.ChoreAssignmentDAO;
import doboard.expenses.Bill;
import doboard.expenses.BillDAO;
import doboard.expenses.BillSplit;
import doboard.expenses.BillSplitDAO;
import doboard.dorm.DormMemberDAO;
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
    @FXML private Label balanceValue;
    @FXML private VBox expensesAlertContainer;
    @FXML private Button signalBtnShrtct1;
    @FXML private Button signalBtnShrtct2;
    @FXML private Button signalBtnShrtct3;
    @FXML private Button signalBtnShrtct4;

    private final DormMemberDAO dormMemberDAO = new DormMemberDAO();
    private final ChoreDAO choreDAO = new ChoreDAO();
    private final ChoreAssignmentDAO choreAssignmentDAO = new ChoreAssignmentDAO();
    private final BillDAO billDAO = new BillDAO();
    private final BillSplitDAO billSplitDAO = new BillSplitDAO();
    private Timeline refreshTimeline;

    @FXML
    public void initialize() {
        refreshDashboard();
        setupHourlyRefresh();
    }

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

        User currentUser = SessionHandler.loadSession();
        if (currentUser == null) return;

        int dormId = dormMemberDAO.getDormIdByUserId(currentUser.getUser_id());
        if (dormId == -1) return;

        loadChores(dormId);
        loadBillsAndBalance(dormId, currentUser.getUser_id());
    }

    private void loadChores(int dormId) {
        List<Chore> allDormChores = choreDAO.findAllByDormId(dormId);
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

        if (dueCount > 0) {
            addNotification("You have " + dueCount + " pending chore(s) due!", "System", false);
        }
    }

    private void loadBillsAndBalance(int dormId, int userId) {
        double totalBalance = 0.0;
        List<Bill> dormBills = billDAO.findByDormId(dormId);

        for (Bill bill : dormBills) {
            List<BillSplit> splits = billSplitDAO.findByBillId(bill.getBill_id());
            for (BillSplit split : splits) {
                if (split.getUser_id() == userId && !split.isPaid()) {
                    totalBalance += split.getAmount();
                    addExpenseAlert("Due: " + bill.getTitle() + " - ₱" + String.format("%.2f", split.getAmount()));
                }
            }
        }
        balanceValue.setText(String.format("%.2f", totalBalance));

        if (totalBalance > 0) {
            addNotification("You have pending bills to pay.", "System", false);
        }
    }

    public void addChore(VBox container, Chore chore, boolean isDue) {
        Node choreNode = ComponentFactory.createChoreItem(
                chore.getTitle(), () -> {
                    markChoreAsDone(chore);
                });
        if (choreNode != null) {
            if (!isDue) {
                Node doneBtn = choreNode.lookup("#doneBtn");
                if (doneBtn != null) doneBtn.setVisible(false);
            }
            container.getChildren().add(choreNode);
        }
    }

    private void markChoreAsDone(Chore chore) {
        choreDAO.updateStatus(chore.getChore_id(), Chore.Status.COMPLETE);
        refreshDashboard();
    }

    @FXML private void goChores() { NavigationManager.loadView(getClass(), "/doboard/chores/chore-view.fxml"); }
    @FXML private void goExpenses() { NavigationManager.loadView(getClass(), "/doboard/expenses/expenses-view.fxml"); }
    @FXML private void goSignals() { NavigationManager.loadView(getClass(), "/doboard/signals/signals-view.fxml"); }

    @FXML
    private void markAsDone(ActionEvent event) {}

    @FXML
    private void signalAct1(ActionEvent event) {}
    @FXML
    private void signalAct2(ActionEvent event) {}
    @FXML
    private void signalAct3(ActionEvent event) {}
    @FXML
    private void signalAct4(ActionEvent event) {}

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
        Node alertNode = ComponentFactory.createExpenseAlert(message);
        if (alertNode != null) expensesAlertContainer.getChildren().add(alertNode);
    }
}
