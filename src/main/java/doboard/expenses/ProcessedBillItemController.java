package doboard.expenses;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ProcessedBillItemController {

    @FXML private Label dateLabel;

    @FXML
    private void undoBill(ActionEvent event) {
        // Since clicking undo opens a global dialog to choose which bill to revert,
        // we can simply initialize a temporary main controller instance to handle the dialog wizard logic safely.
        ExpensesController tempController = new ExpensesController();
        tempController.undoBill(event);
    }
}