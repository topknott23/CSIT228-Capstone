package doboard.expenses;

import doboard.common.util.ComponentFactory;
import doboard.common.util.NavigationManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class ExpensesController {
    @FXML private VBox contentArea;
    @FXML private VBox dueBillContainer;
    @FXML private TextField billAmountTextField;
    @FXML private ComboBox purposeComboBox;
    @FXML private VBox processedBillContainer;
    @FXML private VBox transactionContainer;

    @FXML
    public void initialize() {
        // TODO: init  the values for the comboBox
    }

    public void addBillRow(String title, double amt) {
        Node row = ComponentFactory.createDueBill(title, amt);
        if (row != null) dueBillContainer.getChildren().add(row);
    }

    public void addProcessedBill(String date){
        Node row = ComponentFactory.createProcessedBill(date);
        if(row != null) processedBillContainer.getChildren().add(row);
    }

    public void addTransaction(String title, String date, double amount){
        Node row = ComponentFactory.createTransactionItem(title, date, amount);
        if(row != null) transactionContainer.getChildren().add(row);
    }

    @FXML
    private void splitBill(ActionEvent event){
        // TODO: split
    }

    @FXML
    private void markAsPaid(ActionEvent event){
        // TODO: logic for marking in db
    }

    @FXML
    private void undoBill(ActionEvent event){
        // TODO: undo processed bill
    }

    @FXML private void goDashboard() { NavigationManager.loadView(getClass(), "/doboard/dashboard/content-view.fxml"); }
    @FXML private void goChores() { NavigationManager.loadView(getClass(), "/doboard/chores/chore-view.fxml"); }
    @FXML private void goSignals() { NavigationManager.loadView(getClass(), "/doboard/signals/signals-view.fxml"); }
}