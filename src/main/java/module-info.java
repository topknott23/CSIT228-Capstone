module doboard.core {
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.fxml;
    requires java.sql;
    requires mysql.connector.j;
//    requires doboard.core;

    // JavaFX read
    opens doboard.auth to javafx.fxml;
    opens doboard.dashboard to javafx.fxml;
    opens doboard.chores to javafx.fxml;
    opens doboard.expenses to javafx.fxml;
    opens doboard.signals to javafx.fxml;
    opens doboard.common to javafx.fxml;
    opens doboard.common.util to javafx.fxml;
    opens doboard.dorm to javafx.fxml;
    // Graphics/CSS read
    opens images to javafx.graphics, javafx.fxml;
    opens styles to javafx.graphics, javafx.fxml;

    exports doboard.auth;
    exports doboard.dashboard;
    exports doboard.chores;
    exports doboard.expenses;
    exports doboard.signals;
    exports doboard.common.util;
    exports doboard;
}