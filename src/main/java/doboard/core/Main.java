package doboard.core;

import com.mysql.cj.Session;
import doboard.core.common.connection.SQLConnector;
import doboard.core.common.session.SessionHandler;
import doboard.core.common.ui.Popup;
import doboard.core.features.auth.User;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        //Attempt to restore previous session
        User savedUser = SessionHandler.loadSession();
        if(savedUser != null) {
            Popup.show("Welcome Back", "Session restored: welcome back " + savedUser.getUsername());
            //TODO: Ierase ni inig naa na ang dashboard
            /*
            SceneLoader.loadScene(stage, "/com/doboard/view/main-menu.fxml", "DoBoard - Dashboard");
            */
            showLogInScreen(stage);
        } else {
            showLogInScreen(stage);
        }
        //Test connection
        Connection c = SQLConnector.getConnection();
        if(c == null){
            System.out.println("Connection is null");
        } else {
            System.out.println("Connection Successful");
        }
    }

    public void showLogInScreen(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/doboard/view/login-view.fxml"));
        stage.setTitle("DoBoard");
        stage.setAlwaysOnTop(true);
        stage.setScene(new Scene(loader.load(), 800, 600));
        stage.show();
    }
}
