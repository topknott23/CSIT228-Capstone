package doboard.dashboard;

import doboard.auth.User;
import doboard.auth.UserDAO;
import doboard.common.session.SessionHandler;
import doboard.dorm.Dorm;
import doboard.dorm.DormDAO;
import doboard.expenses.Bill;
import doboard.expenses.BillDAO;
import doboard.expenses.BillSplit;
import doboard.signals.SignalDAO;
import doboard.common.util.Popup;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AdminBillHistoryController {

    @FXML private ComboBox<DormWrapper> dormFilterComboBox;
    @FXML private VBox billHistoryListContainer;

    private final DormDAO dormDAO = new DormDAO();
    private final BillDAO billDAO = new BillDAO();
    private final SignalDAO signalDAO = new SignalDAO();

    @FXML
    public void initialize() {
        populateDormFilter();
        loadBillHistory();
    }

    /**
     * Populates the Dorm Filter ComboBox with a list of managed dorm workspaces,
     * including an option to view bills for all workspaces.
     */
    private void populateDormFilter() {
        List<DormWrapper> items = new ArrayList<>();
        // Option to filter by all workspaces
        items.add(new DormWrapper(null));

        List<Dorm> dorms = dormDAO.findAllDorms();
        for (Dorm d : dorms) {
            items.add(new DormWrapper(d));
        }

        dormFilterComboBox.getItems().setAll(items);
        dormFilterComboBox.setValue(items.get(0));

        // Listen for filter workspace changes
        dormFilterComboBox.setOnAction(event -> loadBillHistory());
    }

    /**
     * Queries and displays billing records based on the selected workspace filter.
     */
    private void loadBillHistory() {
        billHistoryListContainer.getChildren().clear();

        DormWrapper selected = dormFilterComboBox.getValue();
        List<Bill> bills;

        if (selected == null || selected.getDorm() == null) {
            // Fetch all bills for all dorms
            bills = new ArrayList<>();
            List<Dorm> allDorms = dormDAO.findAllDorms();
            for (Dorm d : allDorms) {
                bills.addAll(billDAO.findBillByDormId(d.getDorm_id()));
            }
        } else {
            // Fetch bills for selected dorm only
            bills = billDAO.findBillByDormId(selected.getDorm().getDorm_id());
        }

        if (bills.isEmpty()) {
            Label noBillsLabel = new Label("No billing history found.");
            noBillsLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 14px; -fx-padding: 20px;");
            billHistoryListContainer.getChildren().add(noBillsLabel);
            return;
        }

        // Populate list
        for (Bill bill : bills) {
            VBox billCard = createBillCard(bill);
            billHistoryListContainer.getChildren().add(billCard);
        }
    }

    /**
     * Builds a detailed billing card with total calculations, progress display,
     * and expandable split detail records.
     */
    private VBox createBillCard(Bill bill) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: rgba(255, 255, 255, 0.75); -fx-background-radius: 12px; -fx-padding: 15px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 2);");

        // Resolve Dorm Name
        Dorm dorm = dormDAO.findById(bill.getBill_dorm_id());
        String dormContext = dorm != null ? dorm.getDorm_name() : "Unknown Workspace";

        // Query splits
        List<BillSplit> splits = billDAO.findSplitsByBillId(bill.getBill_id());
        double totalCollected = 0.0;
        int paidCount = 0;
        for (BillSplit s : splits) {
            if (s.isPaid()) {
                totalCollected += s.getAmount();
                paidCount++;
            }
        }
        int totalCount = splits.size();
        double progress = totalCount > 0 ? (double) paidCount / totalCount : 0.0;

        // Card Header Row
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(2);
        Label titleLabel = new Label(bill.getTitle());
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #31548F;");
        Label contextLabel = new Label("Workspace: " + dormContext + " | Due Date: " + bill.getBill_due_date());
        contextLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 11px;");
        titleBox.getChildren().addAll(titleLabel, contextLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox amountBox = new VBox(2);
        amountBox.setAlignment(Pos.CENTER_RIGHT);
        Label amountLabel = new Label("₱" + String.format("%.2f", bill.getTotal_amount()));
        amountLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #31548F;");
        Label collectionProgressLabel = new Label(paidCount + "/" + totalCount + " Paid");
        collectionProgressLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 11px;");
        amountBox.getChildren().addAll(amountLabel, collectionProgressLabel);

        header.getChildren().addAll(titleBox, spacer, amountBox);

        // Progress Bar
        ProgressBar progressBar = new ProgressBar(progress);
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.setStyle("-fx-accent: #406AAF; -fx-control-inner-background: rgba(0,0,0,0.05);");

        // Action controls row
        HBox controls = new HBox(10);
        controls.setAlignment(Pos.CENTER_RIGHT);

        Button toggleDetailsBtn = new Button("View Breakdown");
        toggleDetailsBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #31548F; -fx-font-weight: bold; -fx-cursor: hand;");

        Button deleteBillBtn = new Button("Delete Bill");
        deleteBillBtn.setStyle("-fx-background-color: #EF4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6px; -fx-padding: 5 12; -fx-cursor: hand;");
        deleteBillBtn.setOnAction(e -> {
            boolean confirm = Popup.showConfirmation("Delete Bill", "Are you sure you want to delete this bill and all its resident splits?", "Delete");
            if (confirm) {
                // Delete splits first, then bill (respect FK constraint cleanup)
                for (BillSplit s : splits) {
                    billDAO.updateSplitStatus(s.getSplit_id(), false); // Clean status representation
                }
                if (billDAO.deleteBill(bill.getBill_id())) {
                    Popup.show("Success", "Bill deleted successfully.");
                    loadBillHistory();
                } else {
                    Popup.show("Error", "Could not delete the bill.");
                }
            }
        });

        controls.getChildren().addAll(deleteBillBtn, toggleDetailsBtn);

        // Expanded details container
        VBox detailsContainer = new VBox(8);
        detailsContainer.setManaged(false);
        detailsContainer.setVisible(false);
        detailsContainer.setStyle("-fx-padding: 10 0 0 0; -fx-border-color: rgba(0,0,0,0.05) transparent transparent transparent;");

        // Wires details toggle action
        toggleDetailsBtn.setOnAction(e -> {
            boolean isVisible = detailsContainer.isVisible();
            detailsContainer.setVisible(!isVisible);
            detailsContainer.setManaged(!isVisible);
            toggleDetailsBtn.setText(!isVisible ? "Hide Breakdown" : "View Breakdown");
        });

        // Add resident splits to details panel
        List<Integer> userIds = splits.stream().map(BillSplit::getUser_id).collect(Collectors.toList());
        List<User> users = UserDAO.getUsersByIds(userIds);
        Map<Integer, User> userMap = users.stream().collect(Collectors.toMap(User::getUser_id, u -> u));

        for (BillSplit split : splits) {
            User user = userMap.get(split.getUser_id());
            String fullName = user != null ? user.getFull_name() : "Unknown User";

            HBox splitRow = new HBox(10);
            splitRow.setAlignment(Pos.CENTER_LEFT);
            splitRow.setStyle("-fx-padding: 8; -fx-background-color: rgba(0,0,0,0.02); -fx-background-radius: 6px;");

            VBox tenantInfo = new VBox(2);
            Label tenantName = new Label(fullName);
            tenantName.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #333333;");
            Label splitOwed = new Label("Owes: ₱" + String.format("%.2f", split.getAmount()));
            splitOwed.setStyle("-fx-text-fill: #666666; -fx-font-size: 11px;");
            tenantInfo.getChildren().addAll(tenantName, splitOwed);

            Region splitSpacer = new Region();
            HBox.setHgrow(splitSpacer, Priority.ALWAYS);

            // Status label
            Label statusBadge = new Label(split.isPaid() ? "Paid" : "Pending");
            statusBadge.setStyle(split.isPaid()
                    ? "-fx-background-color: rgba(22, 170, 83, 0.12); -fx-text-fill: #16aa53; -fx-font-weight: bold; -fx-padding: 4 8; -fx-background-radius: 4px; -fx-font-size: 11px;"
                    : "-fx-background-color: rgba(234, 179, 8, 0.12); -fx-text-fill: #EAB308; -fx-font-weight: bold; -fx-padding: 4 8; -fx-background-radius: 4px; -fx-font-size: 11px;");

            // Nudge button
            Button nudgeBtn = new Button("Nudge");
            nudgeBtn.setStyle("-fx-background-color: #406AAF; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 4px; -fx-padding: 4 8; -fx-cursor: hand; -fx-font-size: 11px;");
            nudgeBtn.setVisible(!split.isPaid());
            nudgeBtn.setManaged(!split.isPaid());
            nudgeBtn.setOnAction(event -> {
                User landlord = SessionHandler.loadSession();
                int senderId = landlord != null ? landlord.getUser_id() : 9999;
                String complaint = "Please settle your pending split for: " + bill.getTitle();
                boolean nudgeSuccess = signalDAO.insertSignal(senderId, split.getUser_id(), bill.getBill_dorm_id(), complaint, 1);
                if (nudgeSuccess) {
                    Popup.show("Nudge Dispatched", "Sent a polite reminder to " + fullName);
                } else {
                    Popup.show("Error", "Could not send nudge notification.");
                }
            });

            // Toggle payment status override button
            Button toggleStatusBtn = new Button(split.isPaid() ? "Mark Unpaid" : "Mark Paid");
            toggleStatusBtn.setStyle("-fx-background-color: rgba(64, 106, 175, 0.1); -fx-text-fill: #31548F; -fx-font-weight: bold; -fx-background-radius: 4px; -fx-padding: 4 8; -fx-cursor: hand; -fx-font-size: 11px;");
            toggleStatusBtn.setOnAction(event -> {
                boolean targetPaidState = !split.isPaid();
                boolean updateSuccess = billDAO.updateSplitStatus(split.getSplit_id(), targetPaidState);
                if (updateSuccess) {
                    Popup.show("Success", "Updated split status for " + fullName);
                    loadBillHistory();
                } else {
                    Popup.show("Error", "Failed to update split payment status.");
                }
            });

            splitRow.getChildren().addAll(tenantInfo, splitSpacer, statusBadge, nudgeBtn, toggleStatusBtn);
            detailsContainer.getChildren().add(splitRow);
        }

        card.getChildren().addAll(header, progressBar, controls, detailsContainer);
        return card;
    }

    /**
     * Inner helper wrapper class to handle Dorm selection list entries cleanly
     * inside the JavaFX ComboBox.
     */
    private static class DormWrapper {
        private final Dorm dorm;

        public DormWrapper(Dorm dorm) {
            this.dorm = dorm;
        }

        public Dorm getDorm() {
            return dorm;
        }

        @Override
        public String toString() {
            if (dorm == null) {
                return "All Workspaces";
            }
            return dorm.getDorm_name() + " (" + dorm.getJoin_code() + ")";
        }
    }
}
