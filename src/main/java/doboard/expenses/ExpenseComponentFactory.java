package doboard.expenses;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Button;

import java.io.IOException;

public class ExpenseComponentFactory {

    // Updated to accept a Runnable action for the payment button
    public static Node createDueBill(String title, double amount, Runnable onPay) {
        try {
            FXMLLoader loader = new FXMLLoader(ExpenseComponentFactory.class.getResource("/doboard/expenses/duebill-item.fxml"));
            Node node = loader.load();

            Label titleLabel = (Label) node.lookup("#titleLabel");
            Label amountLabel = (Label) node.lookup("#amountLabel");

            if (titleLabel != null) titleLabel.setText(title);
            if (amountLabel != null) amountLabel.setText("₱" + String.format("%.2f", amount));

            // Locate the button by its fx:id and wire the callback
            Button payBtn = (Button) node.lookup("#payBtn");
            if (payBtn != null && onPay != null) {
                payBtn.setOnAction(event -> onPay.run());
            }

            return node;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Node createProcessedBill(String description) {
        try {
            FXMLLoader loader = new FXMLLoader(ExpenseComponentFactory.class.getResource("/doboard/expenses/processedbill-item.fxml"));
            Node node = loader.load();

            Label descLabel = (Label) node.lookup("#descLabel");
            if (descLabel != null) descLabel.setText(description);

            return node;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Node createTransactionItem(String title, String date, double amount, Runnable onUndoAction) {
        try {
            FXMLLoader loader = new FXMLLoader(ExpenseComponentFactory.class.getResource("/doboard/expenses/transaction-item.fxml"));
            Node node = loader.load();

            // Target component programmatic ID references
            Label titleLabel = (Label) node.lookup("#titleLabel");
            Label dateLabel = (Label) node.lookup("#dateLabel");
            Label amountLabel = (Label) node.lookup("#amountLabel");
            Button undoButton = (Button) node.lookup("#undoButton"); // Look up the button component

            if (titleLabel != null) titleLabel.setText(title);
            if (dateLabel != null) dateLabel.setText(date);
            if (amountLabel != null) amountLabel.setText("-₱" + String.format("%.2f", amount));

            // If the element and callback exist, attach the action listener dynamically
            if (undoButton != null && onUndoAction != null) {
                undoButton.setOnAction(event -> onUndoAction.run());
            }

            return node;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}