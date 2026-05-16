package doboard.common.util;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.util.Duration;

public class Popup {

    public static void show(String title, String message) {
        // Platform.runLater ensures the toast waits for any scene-swapping to finish first
        Platform.runLater(() -> {

            // Find the active window safely
            Window activeWindow = Window.getWindows().stream()
                    .filter(Window::isShowing)
                    .findFirst()
                    .orElse(null);

            if (activeWindow == null) return;

            // Use a transparent Stage instead of Popup for better stability during scene changes
            Stage toastStage = new Stage();
            toastStage.initOwner(activeWindow);
            toastStage.initStyle(StageStyle.TRANSPARENT);
            toastStage.initModality(Modality.NONE); // Non-blocking

            HBox root = new HBox(15);
            root.setAlignment(Pos.CENTER_LEFT);
            root.getStyleClass().addAll("bg-white", "rounded-md", "shadow-md", "br-blue");
            root.setStyle("-fx-border-width: 2px; -fx-padding: 15px; -fx-pref-width: 320px;");

            try {
                root.getStylesheets().add(Popup.class.getResource("/styles/styles.css").toExternalForm());
            } catch (Exception e) {
                System.err.println("Could not load styles.css for Toast");
            }

            VBox textContainer = new VBox(5);
            textContainer.setStyle("-fx-cursor: hand;");
            HBox.setHgrow(textContainer, Priority.ALWAYS);

            Label titleLbl = new Label(title.toUpperCase());
            titleLbl.getStyleClass().addAll("font-bold", "text-brand-blue");
            titleLbl.setStyle("-fx-font-size: 13px;");

            Label msgLbl = new Label(message);
            msgLbl.setWrapText(true);
            msgLbl.setStyle("-fx-text-fill: #666666; -fx-font-size: 11px;");

            textContainer.getChildren().addAll(titleLbl, msgLbl);

            Button closeBtn = new Button("X");
            closeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #999; -fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 14px;");
            closeBtn.setOnAction(e -> toastStage.close());

            root.getChildren().addAll(textContainer, closeBtn);

            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            toastStage.setScene(scene);

            // Click Interaction to open full modal
            textContainer.setOnMouseClicked(e -> {
                toastStage.close();
                showFullModal(title, message, activeWindow);
            });

            // Auto-close timer
            PauseTransition delay = new PauseTransition(Duration.seconds(5));
            delay.setOnFinished(e -> toastStage.close());

            // Show it to calculate exact width/height
            toastStage.show();

            // Position it exactly in the bottom right corner of the active window
            double x = activeWindow.getX() + activeWindow.getWidth() - toastStage.getWidth() - 30;
            double y = activeWindow.getY() + activeWindow.getHeight() - toastStage.getHeight() - 30;
            toastStage.setX(x);
            toastStage.setY(y);

            delay.play();
        });
    }

    // --- The Full Modal Fallback ---
    private static void showFullModal(String title, String message, Window owner) {
        Stage stage = new Stage();
        stage.initOwner(owner);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.TRANSPARENT);

        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(25, 30, 25, 30));
        root.getStyleClass().addAll("bg-white", "rounded-xl", "br-blue", "shadow-md");
        root.setStyle("-fx-border-width: 3px; -fx-min-width: 300px; -fx-max-width: 400px;");

        Label titleLabel = new Label(title.toUpperCase());
        titleLabel.getStyleClass().addAll("text-lg", "font-bold", "text-brand-blue");

        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setStyle("-fx-text-alignment: center; -fx-font-size: 13px; -fx-text-fill: #444444;");

        Button okButton = new Button("OK");
        okButton.getStyleClass().add("btn-primary");
        okButton.setPrefWidth(120);
        okButton.setStyle("-fx-padding: 8px;");
        okButton.setOnAction(e -> stage.close());

        root.getChildren().addAll(titleLabel, messageLabel, okButton);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        try {
            scene.getStylesheets().add(Popup.class.getResource("/styles/styles.css").toExternalForm());
        } catch (Exception e) {}

        stage.setScene(scene);
        stage.setAlwaysOnTop(true);
        stage.centerOnScreen();
        stage.showAndWait();
    }
}


// old popup
/*
package doboard.common.util;

import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class Popup {
    private static Popup instance;

    private Popup() {}

    public static Popup getInstance() {
        if(instance == null) {
            instance = new Popup();
        }
        return instance;
    }

    public static void show(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.setAlwaysOnTop(true);
        alert.showAndWait();
    }

}
 */
