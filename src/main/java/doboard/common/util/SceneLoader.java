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
                Scene scene = new Scene(root);
                stage.setScene(scene);
            }
            if (!fxmlPath.contains("loading-view.fxml")) {
                stage.setWidth(1280);
                stage.setHeight(800);
                stage.centerOnScreen();
            }
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            System.out.println("Could not load FXML: " + fxmlPath);
            e.printStackTrace();
        }
    }

    public static <T> T loadSceneAndGetController(Stage stage, Class<?> context, String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(context.getResource(fxmlPath)));
            Parent root = loader.load();
            Scene currentScene = stage.getScene();
            if (currentScene != null) {
                currentScene.setRoot(root);
            } else {
                Scene scene = new Scene(root);
                stage.setScene(scene);
            }
            stage.setTitle(title);
            stage.show();
            return loader.getController();
        } catch (IOException e) {
            System.out.println("Could not load FXML and get controller: " + fxmlPath);
            e.printStackTrace();
            return null;
        }
    }
}