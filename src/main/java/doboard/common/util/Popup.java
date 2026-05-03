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