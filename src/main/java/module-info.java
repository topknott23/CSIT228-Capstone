module doboard.core {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires mysql.connector.j;
    requires doboard.core;

    // This allows JavaFX to find FXML files
    opens doboard.core to javafx.fxml;
    exports doboard.core;
    opens doboard.core.features.auth to javafx.fxml;
}