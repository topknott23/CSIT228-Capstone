module doboard.core {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires mysql.connector.j;

    // JavaFX read
    opens doboard.auth to javafx.fxml;
    opens doboard.dorm to javafx.fxml;
    opens doboard.chores to javafx.fxml;
    opens doboard.billing to javafx.fxml;
    opens images to javafx.graphics, javafx.fxml;
    opens styles to javafx.graphics, javafx.fxml;

    exports doboard.auth;
    exports doboard.dorm;
    exports doboard.common.util;
    exports doboard;
}