package doboard.dashboard;
import doboard.auth.User;
import doboard.common.session.SessionHandler;
import doboard.dorm.Dorm;
import doboard.dorm.DormService;
import doboard.expenses.ExpenseService;
import doboard.signals.SignalService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class AdminContentController {

    // --- LABELS FOR THE DORM INFO ---
    @FXML private Label dormNameLabel;
    @FXML private Label joinCodeLabel;

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
        User currentUser = SessionHandler.loadSession();

        if (currentUser != null) {
            if (currentUser.getUsername().equalsIgnoreCase("admin")) {
                // --- MASTER LANDLORD VIEW ---
                if (dormNameLabel != null) dormNameLabel.setText("MASTER PORTAL");
                if (joinCodeLabel != null) joinCodeLabel.setText("GLOBAL");
            } else {
                // --- SINGLE-DORM ADMIN VIEW (Legacy) ---
                dormId = dormService.getUserDormId(currentUser.getUser_id());
                Dorm dorm = dormService.getDormById(dormId);
                if (dorm != null) {
                    if (dormNameLabel != null) dormNameLabel.setText(dorm.getDorm_name().toUpperCase());
                    if (joinCodeLabel != null) joinCodeLabel.setText(dorm.getJoin_code());
                }
            }
        }

        setupTableColumns();
        loadLandlordFeeds();
    }

    private void setupTableColumns() {
        // Reactive maintenance table data-binding
        if (maintenanceTableView != null) {
            maintenanceTableView.setItems(ticketList);
        }
    }

    private void loadLandlordFeeds() {
        if (dormId == -1 && (SessionHandler.loadSession() == null || !SessionHandler.loadSession().getUsername().equalsIgnoreCase("admin"))) {
            return;
        }

        if (systemNotifContainer != null) systemNotifContainer.getChildren().clear();
        if (signalsNotifContainer != null) signalsNotifContainer.getChildren().clear();
        ticketList.clear();

        // TODO: Component-factory notification pipeline
    }

    @FXML
    private void handleCreateAdminBill() {
        // TODO: Implement bill logic / process bill to tenants
    }

}