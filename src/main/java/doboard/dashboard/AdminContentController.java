package doboard.dashboard;

import doboard.dorm.DormService;
import doboard.expenses.ExpenseService;
import doboard.signals.SignalService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class AdminContentController {
    @FXML private VBox systemNotifContainer;
    @FXML private VBox signalsNotifContainer;
    @FXML private TableView<MaintenanceTicket> maintenanceTableView;
    @FXML private TableColumn<MaintenanceTicket, Integer> idColumn;
    @FXML private TableColumn<MaintenanceTicket, String> unitColumn;
    @FXML private TableColumn<MaintenanceTicket, String> issueColumn;
    @FXML private TableColumn<MaintenanceTicket, Void> actionColumn;
    @FXML private TextField rentAmountField;
    @FXML private ComboBox<String> billTypeComboBox;
    @FXML private ComboBox<String> recipientComboBox;

    private final DormService dormService = new DormService();
    private final ExpenseService expenseService = new ExpenseService();
    private final SignalService signalService = new SignalService();

    private int dormId = -1;
    private final ObservableList<MaintenanceTicket> ticketList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // TODO: Implement the combo box with issues like leaky faucet, pipe, and other dorm related problems
        // or general initialization of things

        setupTableColumns();
        loadLandlordFeeds();
    }

    private void setupTableColumns() {
        // TODO: Reactive maintenance table data-binding
        maintenanceTableView.setItems(ticketList);
    }

    private void loadLandlordFeeds() {
        if (dormId == -1) return;

        systemNotifContainer.getChildren().clear();
        signalsNotifContainer.getChildren().clear();
        ticketList.clear();

        // TODO: Component-factory notification pipeline
    }

    @FXML
    private void handleCreateAdminBill(){
        // TODO: Implement bill logic / process bill to tenants
    }

    // TODO: Pwede ramo mag add og para remove sa tenant ana nga dorm. If di mo mobuhat ky e mention lng si Renz
    // kinda like codechum classes, wherein ma remove mo sa teacher from that class ky mo balhin nag lain
}
