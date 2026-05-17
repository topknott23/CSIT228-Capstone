package doboard.signals;

import doboard.auth.User;
import doboard.common.session.SessionHandler;
import doboard.common.util.ComponentFactory;
import doboard.common.util.NavigationManager;
import doboard.common.util.Popup;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SignalsController {
    @FXML private VBox contentArea;
    @FXML private VBox signalLogContainer; // Added for the new left column log
    @FXML private ComboBox<String> reasonComboBox;
    @FXML private RadioButton dndHour2;
    @FXML private RadioButton dndHour3;
    @FXML private RadioButton dndHour4;
    @FXML private RadioButton dndHour8;
    @FXML private ComboBox<String> complaintComboBox;
    @FXML private ComboBox<String> tenantComboBox;
    @FXML private RadioButton nudgeCount1;
    @FXML private RadioButton nudgeCount2;
    @FXML private RadioButton nudgeCount5;
    @FXML private RadioButton nudgeCount10;

    private final SignalService signalService = new SignalService();
    private Map<String, Integer> tenantMap = new HashMap<>();

    private User currentUser;
    private int dormId = -1;

    @FXML
    private void initialize(){
        NavigationManager.setTitle("Signals"); // ye
        reasonComboBox.getItems().addAll("Studying", "Sleeping", "In a Meeting", "Sick", "Other");
        complaintComboBox.getItems().addAll("Too Loud", "Kitchen Messy", "Trash Full", "Dishes Piled Up", "Bathroom Dirty");

        ToggleGroup dndGroup = new ToggleGroup();
        dndHour2.setToggleGroup(dndGroup);
        dndHour3.setToggleGroup(dndGroup);
        dndHour4.setToggleGroup(dndGroup);
        dndHour8.setToggleGroup(dndGroup);
        dndHour2.setSelected(true);

        ToggleGroup nudgeGroup = new ToggleGroup();
        nudgeCount1.setToggleGroup(nudgeGroup);
        nudgeCount2.setToggleGroup(nudgeGroup);
        nudgeCount5.setToggleGroup(nudgeGroup);
        nudgeCount10.setToggleGroup(nudgeGroup);
        nudgeCount1.setSelected(true);

        currentUser = SessionHandler.loadSession();
        if (currentUser != null) {
            dormId = signalService.getDormIdForUser(currentUser.getUser_id());
        }

        loadTenants();
        loadSignalLogs(); // Initializing the logs on startup
    }

    private void loadTenants() {
        if(currentUser == null || dormId == -1) return;

        tenantMap = signalService.getDormmatesMap(currentUser.getUser_id(), dormId);
        tenantComboBox.getItems().addAll(tenantMap.keySet());
    }

    // New method to fetch and populate the left column history log
    private void loadSignalLogs() {
        if (signalLogContainer == null) return;
        signalLogContainer.getChildren().clear();

        if (currentUser == null || dormId == -1) return;

        List<SignalDAO.Signal> signals = signalService.getRecentSignals(currentUser.getUser_id(), dormId);

        if (signals.isEmpty()) {
            // Set up a nice cartoony/blue empty state
            Label emptyLabel = new Label("All quiet! No signals right now.");
            emptyLabel.setStyle("-fx-text-fill: #406AAF; -fx-font-weight: bold; -fx-padding: 20;");
            signalLogContainer.getChildren().add(emptyLabel);
            return;
        }

        for (SignalDAO.Signal signal : signals) {
            // Leveraging your existing ComponentFactory for the Notif-Item
            Node logItem = ComponentFactory.createNotification(
                    signal.senderName() + ": " + signal.complaint(),
                    signal.sentAt()
            );
            if(logItem != null) {
                signalLogContainer.getChildren().add(logItem);
            }
        }
    }

    @FXML
    private void disableNotifs(ActionEvent event){
        String reason = reasonComboBox.getValue();
        if(reason == null) {
            Popup.show("Error", "Please select a reason for disabling notifications.");
            return;
        }

        int hours = 2;
        if(dndHour3.isSelected()) hours = 3;
        else if(dndHour4.isSelected()) hours = 4;
        else if(dndHour8.isSelected()) hours = 8;

        // Persist the DND status to database
        if (currentUser != null && dormId != -1) {
            signalService.saveDndStatus(currentUser.getUser_id(), dormId, reason, hours);
            loadSignalLogs(); // Refresh the log to show the DND system message
        }

        Popup.show("Do Not Disturb", "Notifications disabled for " + hours + " hours. Reason: " + reason);
        reasonComboBox.getSelectionModel().clearSelection();
        dndHour2.setSelected(true);
    }

    @FXML
    private void sendNudge(ActionEvent event){
        String tenant = tenantComboBox.getValue();
        String complaint = complaintComboBox.getValue();

        if(tenant == null || complaint == null) {
            Popup.show("Error", "Please select both a tenant and a complaint.");
            return;
        }

        int nudges = 1;
        if(nudgeCount2.isSelected()) nudges = 2;
        else if(nudgeCount5.isSelected()) nudges = 5;
        else if(nudgeCount10.isSelected()) nudges = 10;

        // Persist signal to database
        if (currentUser != null && dormId != -1) {
            int receiverId = tenantMap.getOrDefault(tenant, 0);
            boolean sent = signalService.sendSignal(currentUser.getUser_id(), receiverId, dormId, complaint, nudges);

            if (sent) {
                Popup.show("Signal Sent", "Sent " + nudges + " nudge(s) to " + tenant + " for: " + complaint);
                loadSignalLogs(); // Instantly refresh the UI so the user sees what they just sent
            } else {
                Popup.show("Error", "Failed to send signal. Please try again.");
            }
        } else {
            Popup.show("Error", "You must be logged in and in a dorm to send signals.");
        }

        tenantComboBox.getSelectionModel().clearSelection();
        complaintComboBox.getSelectionModel().clearSelection();
        nudgeCount1.setSelected(true);
    }

    @FXML private void goDashboard() { NavigationManager.loadView(getClass(), "/doboard/dashboard/content-view.fxml"); }
    @FXML private void goChores() { NavigationManager.loadView(getClass(), "/doboard/chores/chore-view.fxml"); }
    @FXML private void goExpenses() { NavigationManager.loadView(getClass(), "/doboard/expenses/expenses-view.fxml"); }
}