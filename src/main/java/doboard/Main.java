package doboard;

import doboard.common.connection.SQLConnector;
import doboard.common.session.SessionHandler;
import doboard.common.util.Popup;
import doboard.auth.User;
import doboard.common.util.SceneLoader;
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
        SceneLoader.loadScene(stage, Main.class, "/doboard/auth/login-view.fxml", "DoBoard");
        stage.setAlwaysOnTop(true);
    }
}
