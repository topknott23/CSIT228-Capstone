package doboard.expenses;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import java.io.IOException;

public class ExpenseComponentFactory {

    public static Node createDueBill(String title, double amount) {
        try {
            FXMLLoader loader = new FXMLLoader(ExpenseComponentFactory.class.getResource("/doboard/expenses/duebill-item.fxml"));
            Node node = loader.load();
            ((Label) node.lookup("#billLabel")).setText(title);
            ((Label) node.lookup("#amountLabel")).setText(String.format("₱%.2f", amount));
            return node;
        } catch (IOException e) {
            return null;
        }
    }

    public static Node createProcessedBill(String date) {
        try {
            FXMLLoader loader = new FXMLLoader(ExpenseComponentFactory.class.getResource("/doboard/expenses/processedbill-item.fxml"));
            Node node = loader.load();
            Label dateLbl = (Label) node.lookup("#dateLabel");
            if (dateLbl != null) dateLbl.setText(date);
            return node;
        } catch (IOException e) {
            return null;
        }
    }

    public static Node createTransactionItem(String title, String date, double amount) {
        try {
            FXMLLoader loader = new FXMLLoader(ExpenseComponentFactory.class.getResource("/doboard/expenses/transaction-item.fxml"));
            Node node = loader.load();
            Label titleLbl = (Label) node.lookup("#transactionTitle");
            Label dateLbl = (Label) node.lookup("#transactionDate");
            Label amountLbl = (Label) node.lookup("#transactionAmount");
            if (titleLbl != null) titleLbl.setText("Paid: " + title);
            if (dateLbl != null) dateLbl.setText("Completed on " + date);
            if (amountLbl != null) {
                amountLbl.setText(String.format("-₱%,.2f", amount));
            }
            return node;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}