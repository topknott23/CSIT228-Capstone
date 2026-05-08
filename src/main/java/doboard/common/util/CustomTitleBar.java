package doboard.common.util;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class CustomTitleBar {
    private double xOffset = 0;
    private double yOffset = 0;
    private boolean isMaximized = false;
    private double savedX, savedY, savedWidth, savedHeight;

    /**
     * Attaches dragging and double-click to maximize behavior to the provided HBox.
     */
    public void makeDraggable(HBox topNavBar) {
        if (topNavBar == null) return;

        topNavBar.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        topNavBar.setOnMouseDragged(event -> {
            Stage stage = (Stage) topNavBar.getScene().getWindow();
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });

        topNavBar.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                toggleMaximize((Stage) topNavBar.getScene().getWindow());
            }
        });
    }

    public void minimize(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setIconified(true);
    }

    public void maximize(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        toggleMaximize(stage);
    }

    public void close(ActionEvent event) {
        Platform.exit();
        System.exit(0);
    }

    private void toggleMaximize(Stage stage) {
        if (isMaximized) {
            // Restore to original size
            stage.setX(savedX);
            stage.setY(savedY);
            stage.setWidth(savedWidth);
            stage.setHeight(savedHeight);
            isMaximized = false;
        } else {
            // Save current size
            savedX = stage.getX();
            savedY = stage.getY();
            savedWidth = stage.getWidth();
            savedHeight = stage.getHeight();

            // Get the screen the window is currently on
            Screen screen = Screen.getScreensForRectangle(stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight()).get(0);
            Rectangle2D visualBounds = screen.getVisualBounds(); // Gets bounds without taskbar

            // Snap to screen bounds
            stage.setX(visualBounds.getMinX());
            stage.setY(visualBounds.getMinY());
            stage.setWidth(visualBounds.getWidth());
            stage.setHeight(visualBounds.getHeight());

            isMaximized = true;
        }
    }
}