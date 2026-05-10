package doboard;

import doboard.common.connection.SQLConnector;
import doboard.common.session.SessionHandler;
import doboard.common.util.Popup;
import doboard.auth.User;
import doboard.common.util.SceneLoader;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle; // <-- 1. ADDED THIS IMPORT
import java.sql.Connection;

public class Main extends Application {
    @Override
    public void start(Stage stage) {
        // Removed windows frame, para custom atoa
        stage.initStyle(StageStyle.UNDECORATED);
        stage.getIcons().add(new Image(Main.class.getResourceAsStream("/images/logo.png")));
        // Windows icon rani ^^


        //Attempt to restore previous session
        User savedUser = SessionHandler.loadSession();
        if(savedUser != null) {
            Popup.show("Welcome Back", "Session restored: welcome back " + savedUser.getUsername());
            SceneLoader.loadScene(stage, Main.class, "/doboard/dashboard/dashboard-view.fxml", "Maintenant - Dashboard");
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

    public void showLogInScreen(Stage stage) {
        // change per needed, ako rana g dashboard for testing purposes
        SceneLoader.loadScene(stage, Main.class, "/doboard/auth/login-view.fxml", "Maintenant");
        stage.setAlwaysOnTop(false);
    }

    public static void main(String[] args) {
        launch(args);
    }
}