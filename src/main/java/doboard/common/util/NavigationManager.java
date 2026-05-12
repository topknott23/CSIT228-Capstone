package doboard.common.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class NavigationManager {
    public static VBox contentArea;
    public static void setContentArea(VBox area){
        contentArea = area;
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
