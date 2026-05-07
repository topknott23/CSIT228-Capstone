package doboard.common.util;

import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.stage.Stage;

public class StageUtil {
    public static Stage getStage(ActionEvent event){
        return (Stage) ((Node) event.getSource()).getScene().getWindow();
    }
}
