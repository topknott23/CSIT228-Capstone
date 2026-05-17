package doboard;

import doboard.common.util.SceneLoader;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Main extends Application {
    @Override
    public void start(Stage stage) {
        // Removed windows frame, para custom atoa
        //Commented out because some screens dont have an exit button
        // Uncommented, ill apply the buttons na
        stage.initStyle(StageStyle.UNDECORATED);

        stage.getIcons().add(new Image(Main.class.getResourceAsStream("/images/logo.png")));
        // Windows icon rani ^^

        SceneLoader.loadScene(stage, Main.class, "/doboard/dashboard/dashboard-view.fxml", "Maintentant");
        //For Testing ^

        //Attempt to restore previous session
//        User savedUser = SessionHandler.loadSession();
//        if(savedUser != null) {
//            Popup.show("Welcome Back", "Session restored: welcome back " + savedUser.getUsername());
//            System.out.println("USER ID: " + savedUser.getUser_id());
//
//            if(DormMemberDAO.isUserInDorm(savedUser.getUser_id()))
//                SceneLoader.loadScene(stage, Main.class,    "/doboard/dashboard/dashboard-view.fxml", "Maintenant - Dashboard");
//            else {
//                Popup.show("Enter a dorm", "You are not in any dorm. Please enter or create a dorm to continue");
//                SceneLoader.loadScene(stage, Main.class, "/doboard/dorm/dorm-view.fxml", "Maintenant - Dashboard");
//            }
//
//        } else {
//            showLogInScreen(stage);
//        }
//        //Test connection
//        Connection c = SQLConnector.getConnection();
//        if(c == null) {
//            System.out.println("Connection is null");
//        } else {
//            System.out.println("Connection Successful");
//        }
    }

    public void showLogInScreen(Stage stage) {
        SceneLoader.loadScene(stage, Main.class, "/doboard/auth/login-view.fxml", "Maintenant");
    }

    public static void main(String[] args) {
        launch(args);
    }
}