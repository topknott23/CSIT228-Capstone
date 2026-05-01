package doboard.core;

import doboard.core.common.connection.SQLConnector;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/doboard/view/login-view.fxml"));
        stage.setTitle("DoBoard");
        stage.setAlwaysOnTop(true);
        stage.setScene(new Scene(loader.load(), 800, 600));
        stage.show();

        Connection c = SQLConnector.getConnection();
        if(c == null){
            System.out.println("Connection is null");
        } else {
            System.out.println("Connection Successful");
        }
    }
}
