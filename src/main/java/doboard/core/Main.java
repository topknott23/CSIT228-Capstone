package doboard.core;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/doboard/view/login-view.fxml"));
        stage.setTitle("TEST");
        stage.setScene(new Scene(loader.load(), 800, 600));
        stage.show();
    }
}
//dapat on ang xampp og ni insert ka atong jdbc sql sa project structure