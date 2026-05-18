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
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import doboard.common.util.NavigationManager;
import doboard.common.util.Popup;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class AdminContentController {

    // --- MAIN LAYOUT ---
    @FXML private VBox contentArea;

    // --- LABELS FOR THE DORM INFO ---
    @FXML private Label dormNameLabel;
    @FXML private Label joinCodeLabel;

    // --- CONTAINERS ---
    @FXML private VBox systemNotifContainer;
    @FXML private VBox signalsNotifContainer;

    // --- MAINTENANCE TABLE ---
    @FXML private TableView<MaintenanceTicket> maintenanceTableView;
    @FXML private TableColumn<MaintenanceTicket, Integer> idColumn;
    @FXML private TableColumn<MaintenanceTicket, String> unitColumn;
    @FXML private TableColumn<MaintenanceTicket, String> issueColumn;
    @FXML private TableColumn<MaintenanceTicket, Void> actionColumn;

    // --- ADMIN BILLING ---
    @FXML private TextField rentAmountField;
    @FXML private TextField billPurposeField;
    @FXML private Button selectRecipientBtn;

    private final DormService dormService = new DormService();
    private final ExpenseService expenseService = new ExpenseService();
    private final SignalService signalService = new SignalService();

    private int dormId = -1;
    private final ObservableList<MaintenanceTicket> ticketList = FXCollections.observableArrayList();
    private Dorm selectedDorm = null;

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
    private void handleSelectRecipient() {
        showDialog("/doboard/dashboard/select-dorm-dialog.fxml", controller -> {
            if (controller instanceof SelectDormDialogController) {
                ((SelectDormDialogController) controller).setOnDormSelected(dorm -> {
                    this.selectedDorm = dorm;
                    selectRecipientBtn.setText(dorm.getDorm_name() + " (" + dorm.getJoin_code() + ")");
                });
            }
        });
    }

    @FXML
    private void handleCreateAdminBill() {
        if (rentAmountField.getText().isEmpty() || billPurposeField.getText().isEmpty() || selectedDorm == null) {
            Popup.show("Error", "Please fill in the amount, bill purpose, and select a recipient dorm.");
            return;
        }

        try {
            double amount = Double.parseDouble(rentAmountField.getText());
            if (amount <= 0 || amount > 100000) {
                Popup.show("Error", "Amount must be greater than zero and realistic.");
                return;
            }

            // Pull from the new TextField
            String purpose = billPurposeField.getText();
            boolean success = expenseService.processBillSplit(selectedDorm.getDorm_id(), purpose, amount);

            if (success) {
                Popup.show("Success", "Bill dispatched to " + selectedDorm.getDorm_name() + " tenants.");
                rentAmountField.clear();
                billPurposeField.clear(); // Clear the text field
                selectedDorm = null;
                selectRecipientBtn.setText("Select Recipient...");
            } else {
                Popup.show("Error", "Failed to dispatch bill. Are there tenants in this workspace?");
            }
        } catch (NumberFormatException e) {
            Popup.show("Error", "Invalid amount entered. Please use numbers only.");
        }
    }

    @FXML
    private void handleCreateWorkspace(ActionEvent event) {
        // Spawn the dialog and attach a success callback
        NavigationManager.showDormSetupDialog(((Node) event.getSource()).getScene().getWindow(), () -> {
            Popup.show("Dorm Created", "New dorm registered successfully! Check the Join Code.");
            // You can call loadLandlordFeeds() or refresh logic here in the future
        });
    }

    // --- UTILITY METHODS ---

    private void showDialog(String fxmlPath, java.util.function.Consumer<Object> controllerConfigurer) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(contentArea.getScene().getWindow());
            dialogStage.initStyle(StageStyle.TRANSPARENT);

            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            try { scene.getStylesheets().add(getClass().getResource("/styles/styles.css").toExternalForm()); } catch (Exception ignored) {}

            dialogStage.setScene(scene);

            if (controllerConfigurer != null) {
                controllerConfigurer.accept(loader.getController());
            }

            dialogStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}