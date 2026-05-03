package doboard.common.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Objects;

public class SceneLoader {

    public static void loadScene(Stage stage, Class<?> context, String fxmlPath, String title) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(context.getResource(fxmlPath)));
            Scene currentScene = stage.getScene();
            if (currentScene != null) {
                // If a scene already exists, igo ra iswap the root to preserve window size/fullscreen state
                currentScene.setRoot(root);
            } else {
                // First time setup (like initial app launch)
                Scene scene = new Scene(root, 800, 600);
                stage.setScene(scene);
            }
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            System.out.println("Could not load FXML: " + fxmlPath);
            e.printStackTrace();
        }
    }
}