module doboard.core {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    // This allows JavaFX to find FXML files
    opens doboard.core.controllers to javafx.fxml;

    // This allows other parts of the app to use models (classes)
    exports doboard.core.models;
    exports doboard.core.controllers;
}