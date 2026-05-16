package doboard.common.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class NavigationManager {
    public static VBox contentArea;
    public static Label windowTitleLabel; // <-- Added reference for the title label

    public static void setContentArea(VBox area){
        contentArea = area;
    }

    public static void setWindowTitleLabel(Label label) {
        windowTitleLabel = label;
    }

    public static void setTitle(String currentScreen) {
        if (windowTitleLabel != null) {
            windowTitleLabel.setText("MAINTENANT - " + currentScreen.toUpperCase());
        }
    }

    public static <T> T loadView(Class<?> context, String file){
        if(contentArea == null) return null;
        try{
            FXMLLoader loader = new FXMLLoader(context.getResource(file));
            Parent view = loader.load();
            contentArea.getChildren().setAll(view);
            return loader.getController();
        } catch(IOException e){
            e.printStackTrace();
            return null;
        }
    }
}