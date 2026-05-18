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
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AdminContentController {

    @FXML private VBox contentArea;
    @FXML private Label dormNameLabel;
    @FXML private Label joinCodeLabel;
    @FXML private VBox systemNotifContainer;
    @FXML private VBox signalsNotifContainer;

    @FXML private TableView<MaintenanceTicket> maintenanceTableView;
    @FXML private TableColumn<MaintenanceTicket, Integer> idColumn;
    @FXML private TableColumn<MaintenanceTicket, String> unitColumn;
    @FXML private TableColumn<MaintenanceTicket, String> issueColumn;
    @FXML private TableColumn<MaintenanceTicket, Void> actionColumn; // Custom interactive button column

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
                if (dormNameLabel != null) dormNameLabel.setText("MASTER PORTAL");
                if (joinCodeLabel != null) joinCodeLabel.setText("GLOBAL");
            } else {
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
        if (maintenanceTableView != null) {
            idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            unitColumn.setCellValueFactory(new PropertyValueFactory<>("unit"));
            issueColumn.setCellValueFactory(new PropertyValueFactory<>("issue"));

            // INTEGRATED: Configures the Action column button mechanics
            setupActionColumn();

            maintenanceTableView.setItems(ticketList);
        }
    }

    // INTEGRATED: Pulls data records from maintenance_requests instead of old signals loop
    private void loadLandlordFeeds() {
        if (dormId == -1 && (SessionHandler.loadSession() == null || !SessionHandler.loadSession().getUsername().equalsIgnoreCase("admin"))) {
            return;
        }

        if (systemNotifContainer != null) systemNotifContainer.getChildren().clear();
        if (signalsNotifContainer != null) signalsNotifContainer.getChildren().clear();
        ticketList.clear();

        String query = "SELECT mr.request_id, d.dorm_name, u.username, mr.issue_description " +
                "FROM maintenance_requests mr " +
                "JOIN dorms d ON mr.dorm_id = d.dorm_id " +
                "JOIN users u ON mr.user_id = u.user_id " +
                "WHERE mr.status = 'PENDING' " +
                (dormId != -1 ? "AND mr.dorm_id = ? " : "") +
                "ORDER BY mr.created_at DESC";

        try (Connection conn = doboard.common.connection.SQLConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            if (dormId != -1) {
                stmt.setInt(1, dormId);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("request_id");
                    String dormName = rs.getString("dorm_name");
                    String username = rs.getString("username");
                    String issue = rs.getString("issue_description");

                    String locationContext = dormName + " (" + username + ")";

                    ticketList.add(new MaintenanceTicket(
                            id,
                            locationContext,
                            issue
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to synchronize active maintenance request views for admin panel.");
            e.printStackTrace();
        }
    }

    // INTEGRATED: Populates the Action column with functional "Resolve" buttons
    private void setupActionColumn() {
        Callback<TableColumn<MaintenanceTicket, Void>, TableCell<MaintenanceTicket, Void>> cellFactory =
                new Callback<>() {
                    @Override
                    public TableCell<MaintenanceTicket, Void> call(final TableColumn<MaintenanceTicket, Void> param) {
                        return new TableCell<>() {
                            private final Button btn = new Button("Resolve");

                            {
                                btn.getStyleClass().add("btn-small");
                                btn.setStyle("-fx-background-color: #406AAF; -fx-text-fill: white; -fx-cursor: hand; -fx-font-size: 11px;");

                                btn.setOnAction(event -> {
                                    MaintenanceTicket selectedTicket = getTableView().getItems().get(getIndex());
                                    handleResolveTicket(selectedTicket.getId());
                                });
                            }

                            @Override
                            public void updateItem(Void item, boolean empty) {
                                super.updateItem(item, empty);
                                if (empty) {
                                    setGraphic(null);
                                } else {
                                    setGraphic(btn);
                                }
                            }
                        };
                    }
                };

        actionColumn.setCellFactory(cellFactory);
    }

    // INTEGRATED: Processes resolution updates directly against the SQL context
    private void handleResolveTicket(int ticketId) {
        String updateSQL = "UPDATE maintenance_requests SET status = 'RESOLVED' WHERE request_id = ?";

        try (Connection conn = doboard.common.connection.SQLConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(updateSQL)) {

            stmt.setInt(1, ticketId);
            int rowsUpdated = stmt.executeUpdate();

            if (rowsUpdated > 0) {
                // Re-fetch the live collections stream to remove the item dynamically
                loadLandlordFeeds();
            }
        } catch (SQLException e) {
            System.err.println("Database context failed to update maintenance ticket resolution.");
            e.printStackTrace();
            Popup.show("SQL Error", "Could not complete request processing over live server transaction.");
        }
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

            String purpose = billPurposeField.getText();
            boolean success = expenseService.processBillSplit(selectedDorm.getDorm_id(), purpose, amount);

            if (success) {
                Popup.show("Success", "Bill dispatched to " + selectedDorm.getDorm_name() + " tenants.");
                rentAmountField.clear();
                billPurposeField.clear();
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
        NavigationManager.showDormSetupDialog(((Node) event.getSource()).getScene().getWindow(), () -> {
            Popup.show("Dorm Created", "New dorm registered successfully! Check the Join Code.");
        });
    }

    @FXML
    private void handleManageDorms(ActionEvent event) {
        showDialog("/doboard/dashboard/admin-managed-dorms-view.fxml", null);
    }

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