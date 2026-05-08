package doboard.signals;

import doboard.auth.User;
import doboard.common.connection.SQLConnector;
import doboard.common.session.SessionHandler;
import doboard.common.util.NavigationManager;
import doboard.common.util.Popup;
import doboard.dorm.DormMember;
import doboard.dorm.DormMemberDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SignalsController {
    @FXML private VBox contentArea;
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

    private final DormMemberDAO dormMemberDAO = new DormMemberDAO();
    private final Map<String, Integer> tenantMap = new HashMap<>();

    @FXML
    private void initialize(){
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

        loadTenants();
    }

    private void loadTenants() {
        User currentUser = SessionHandler.loadSession();
        if(currentUser == null) return;

        int dormId = dormMemberDAO.getDormIdByUserId(currentUser.getUser_id());
        if(dormId == -1) return;

        List<DormMember> members = dormMemberDAO.getMembersByDorm(dormId);
        String query = "SELECT username FROM users WHERE user_id = ?";

        try (Connection c = SQLConnector.getConnection();
             PreparedStatement s = c.prepareStatement(query)) {

            for(DormMember member : members) {

                if(member.getUser_id() == currentUser.getUser_id()) continue;

                s.setInt(1, member.getUser_id());
                ResultSet r = s.executeQuery();
                if(r.next()) {
                    String username = r.getString("username");
                    tenantComboBox.getItems().add(username);
                    tenantMap.put(username, member.getUser_id());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
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

        Popup.show("Signal Sent", "Sent " + nudges + " nudge(s) to " + tenant + " for: " + complaint);
        tenantComboBox.getSelectionModel().clearSelection();
        complaintComboBox.getSelectionModel().clearSelection();
        nudgeCount1.setSelected(true);
    }

    @FXML private void goDashboard() { NavigationManager.loadView(getClass(), "/doboard/dashboard/content-view.fxml"); }
    @FXML private void goChores() { NavigationManager.loadView(getClass(), "/doboard/chores/chore-view.fxml"); }
    @FXML private void goExpenses() { NavigationManager.loadView(getClass(), "/doboard/expenses/expenses-view.fxml"); }
}