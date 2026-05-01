package doboard.core.features.auth;

import doboard.core.common.ui.SceneLoader;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button signInButton;
    @FXML private StackPane rootPane;
    @FXML private ImageView backgroundView;

    @FXML
    private void initialize(){
        backgroundView.fitHeightProperty().bind(rootPane.heightProperty());
        backgroundView.fitWidthProperty().bind(rootPane.widthProperty());
    }

    @FXML
    private void goRegister(ActionEvent event){
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        SceneLoader.loadScene(stage, "/com/doboard/view/register-view.fxml", "TEST");
    }

    @FXML
    private void handleSignIn(ActionEvent event) {
        // TODO: Implement logic
    }


}
