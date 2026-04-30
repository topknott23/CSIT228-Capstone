module doboard.core {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    // This allows JavaFX to find FXML files
    opens doboard.core.controllers to javafx.fxml;
    exports doboard.core;
}