package doboard.dashboard;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import java.io.IOException;

public class DashboardComponentFactory {

    public static Node createChoreItem(String title, Runnable onDoneAction) {
        try {
            FXMLLoader loader = new FXMLLoader(DashboardComponentFactory.class.getResource("/doboard/dashboard/chore-item.fxml"));
            Node node = loader.load();

            Label titleLabel = (Label) node.lookup("#choreTitleLabel");
            Button doneBtn = (Button) node.lookup("#doneBtn");
            if (titleLabel != null) titleLabel.setText(title);
            if (doneBtn != null) {
                doneBtn.setOnAction(e -> onDoneAction.run());
            }
            return node;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Node createExpenseAlert(String message) {
        try {
            FXMLLoader loader = new FXMLLoader(DashboardComponentFactory.class.getResource("/doboard/dashboard/expense-item.fxml"));
            Node node = loader.load();

            Label label = (Label) node.lookup("#alertLabel");
            if (label != null) label.setText("• " + message);

            return node;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Node createSpaceItem(String name) {
        try {
            FXMLLoader loader = new FXMLLoader(DashboardComponentFactory.class.getResource("/doboard/dashboard/space-item.fxml"));
            Node node = loader.load();

            // FIXED: Cast to Button matching space-item.fxml root element to avoid ClassCastException
            Button spaceBtn = (Button) node.lookup("#spaceLabel");
            if (spaceBtn != null) spaceBtn.setText(name);

            return node;
        } catch (IOException e) {
            return null;
        }
    }
}