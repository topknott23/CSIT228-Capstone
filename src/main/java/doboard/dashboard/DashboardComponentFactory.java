package doboard.dashboard;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DashboardComponentFactory {


    public static Node createChoreItem(String title, LocalDate dueDate, Runnable onDone) {
        // 1. Title Label
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        // 2. Date Label (Grey, smaller)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd");
        Label dateLabel = new Label("Due: " + dueDate.format(formatter));
        dateLabel.setStyle("-fx-text-fill: grey; -fx-font-size: 11px;");

        // 3. Stack them vertically
        VBox textContainer = new VBox(2); // 2px spacing between title and date
        textContainer.getChildren().addAll(titleLabel, dateLabel);

        // 4. Set up the main container (Text on left, Button on right)
        HBox mainContainer = new HBox(10);
        HBox.setHgrow(textContainer, Priority.ALWAYS); // Push button to the right

        // 5. Done Button
        Button doneBtn = new Button("Done");
        doneBtn.setId("doneBtn"); // Keep your ID for the lookup
        doneBtn.getStyleClass().add("btn-primary");
        doneBtn.setOnAction(e -> onDone.run());

        mainContainer.getChildren().addAll(textContainer, doneBtn);

        // Add any existing padding/background styles to mainContainer here
        mainContainer.setStyle("-fx-padding: 10; -fx-background-color: white; -fx-background-radius: 5;");

        return mainContainer;
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
}