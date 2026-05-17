package doboard.common.util;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.util.Duration;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class Popup {

    private static Window getActiveWindow() {
        return Window.getWindows().stream()
                .filter(Window::isShowing)
                .findFirst()
                .orElse(null);
    }

    public static void show(String title, String message) {
        Platform.runLater(() -> {
            Window activeWindow = getActiveWindow();
            if (activeWindow == null) return;

            Stage toastStage = new Stage();
            toastStage.initOwner(activeWindow);
            toastStage.initStyle(StageStyle.TRANSPARENT);
            toastStage.initModality(Modality.NONE);

            HBox root = new HBox(15);
            root.setAlignment(Pos.CENTER_LEFT);
            root.getStyleClass().addAll("bg-white", "rounded-md", "shadow-md", "br-blue");
            root.setStyle("-fx-border-width: 2px; -fx-padding: 15px; -fx-pref-width: 320px;");

            try { root.getStylesheets().add(Popup.class.getResource("/styles/styles.css").toExternalForm()); } catch (Exception ignored) {}

            VBox textContainer = new VBox(5);
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

            PauseTransition delay = new PauseTransition(Duration.seconds(5));
            delay.setOnFinished(e -> toastStage.close());

            toastStage.show();
            toastStage.setX(activeWindow.getX() + activeWindow.getWidth() - toastStage.getWidth() - 30);
            toastStage.setY(activeWindow.getY() + activeWindow.getHeight() - toastStage.getHeight() - 30);
            delay.play();
        });
    }

    // --- GLOBAL CONFIRMATION DIALOG (Yes/No) ---
    public static boolean showConfirmation(String title, String message, String confirmText) {
        AtomicBoolean result = new AtomicBoolean(false);

        Stage stage = new Stage();
        stage.initOwner(getActiveWindow());
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.TRANSPARENT);

        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));
        root.getStyleClass().addAll("bg-white", "rounded-xl", "shadow-md");
        root.setStyle("-fx-border-color: #cacaca; -fx-border-radius: 15px; -fx-min-width: 350px;");

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().addAll("text-lg", "font-bold", "text-brand-blue");

        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setStyle("-fx-text-alignment: center; -fx-text-fill: #555;");

        HBox buttons = new HBox(15);
        buttons.setAlignment(Pos.CENTER);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().addAll("btn-secondary", "font-bold");
        cancelBtn.setOnAction(e -> stage.close());

        Button confirmBtn = new Button(confirmText);
        confirmBtn.getStyleClass().add("btn-primary");
        confirmBtn.setStyle("-fx-padding: 8 20 8 20; -fx-background-color: #e74c3c; -fx-text-fill: white;");
        confirmBtn.setOnAction(e -> {
            result.set(true);
            stage.close();
        });

        buttons.getChildren().addAll(cancelBtn, confirmBtn);
        root.getChildren().addAll(titleLabel, messageLabel, buttons);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        try { scene.getStylesheets().add(Popup.class.getResource("/styles/styles.css").toExternalForm()); } catch (Exception ignored) {}

        stage.setScene(scene);
        stage.centerOnScreen();
        stage.showAndWait();

        return result.get();
    }

    // --- GLOBAL INPUT DIALOG (Text Input) ---
    public static String showInput(String title, String message, String promptText) {
        AtomicReference<String> result = new AtomicReference<>(null);

        Stage stage = new Stage();
        stage.initOwner(getActiveWindow());
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.TRANSPARENT);

        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));
        root.getStyleClass().addAll("bg-white", "rounded-xl", "shadow-md");
        root.setStyle("-fx-border-color: #cacaca; -fx-border-radius: 15px; -fx-min-width: 350px;");

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().addAll("text-lg", "font-bold", "text-brand-blue");

        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setStyle("-fx-text-alignment: center; -fx-text-fill: #555;");

        TextField inputField = new TextField();
        inputField.setPromptText(promptText);
        inputField.getStyleClass().add("input-pill");

        HBox buttons = new HBox(15);
        buttons.setAlignment(Pos.CENTER);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().addAll("btn-secondary", "font-bold");
        cancelBtn.setOnAction(e -> stage.close());

        Button submitBtn = new Button("Submit");
        submitBtn.getStyleClass().add("btn-primary");
        submitBtn.setStyle("-fx-padding: 8 20 8 20;");
        submitBtn.setOnAction(e -> {
            result.set(inputField.getText());
            stage.close();
        });

        buttons.getChildren().addAll(cancelBtn, submitBtn);
        root.getChildren().addAll(titleLabel, messageLabel, inputField, buttons);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        try { scene.getStylesheets().add(Popup.class.getResource("/styles/styles.css").toExternalForm()); } catch (Exception ignored) {}

        stage.setScene(scene);
        stage.centerOnScreen();
        stage.showAndWait();

        return result.get();
    }
}