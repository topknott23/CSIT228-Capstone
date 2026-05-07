package doboard.signals;

import doboard.common.util.NavigationManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.layout.VBox;

public class SignalsController {
    @FXML private VBox contentArea;
    @FXML private ComboBox reasonComboBox;
    @FXML private RadioButton dndHour2;
    @FXML private RadioButton dndHour3;
    @FXML private RadioButton dndHour4;
    @FXML private RadioButton dndHour8;
    @FXML private ComboBox complaintComboBox;
    @FXML private ComboBox tenantComboBox;
    @FXML private RadioButton nudgeCount1;
    @FXML private RadioButton nudgeCount2;
    @FXML private RadioButton nudgeCount5;
    @FXML private RadioButton nudgeCount10;

    @FXML private void goDashboard() { NavigationManager.loadView(getClass(), "/doboard/dashboard/content-view.fxml"); }
    @FXML private void goChores() { NavigationManager.loadView(getClass(), "/doboard/chores/chore-view.fxml"); }
    @FXML private void goExpenses() { NavigationManager.loadView(getClass(), "/doboard/expenses/expenses-view.fxml"); }

    @FXML
    private void initialize(){
        // TODO: Initialize comboBoxes
    }

    @FXML
    private void disableNotifs(ActionEvent event){
        // TODO: notifs feature
    }

    @FXML
    private void sendNudge(ActionEvent event){
        // TODO: nudge feature
    }
}
