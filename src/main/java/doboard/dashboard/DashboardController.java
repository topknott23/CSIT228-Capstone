package doboard.dashboard;

import doboard.common.util.ComponentFactory;
import doboard.common.util.NavigationManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

public class DashboardController {
    @FXML private StackPane rootPane;
    @FXML private VBox spaceContainer;
    @FXML private ImageView profileImage;
    @FXML private Label usernameVal;
    @FXML private VBox contentArea;

    @FXML
    public void initialize(){
        // modify rani para dynamic profile
//        Circle clip = new Circle(22.5, 22.5, 22.5);
//        profileImage.setClip(clip);
        // for content area
        NavigationManager.setContentArea(contentArea);
        NavigationManager.loadView(getClass(), "/doboard/dashboard/content-view.fxml");
    }

    @FXML
    private void handleAddSpaces(ActionEvent event){
        // TODO: some pop up. Kamo lay buhat ani. Basin designan ra nako.
    }

    @FXML
    private void handleProfileSettings(ActionEvent event){
        // Contemplation...
    }
    @FXML
    private void handleNotificationSettings(ActionEvent event){
        // Contemplation...
    }
    @FXML
    private void handleAutomationSettings(ActionEvent event){
        // Contemplation...
    }
    @FXML
    private void handlePrivacySettings(ActionEvent event){
        // Contemplation...
    }

    @FXML
    private void handleLogout(ActionEvent event){
        // TODO: Logout logic
    }

    // Helpers
    public void addSpace(String name){
        Node space = ComponentFactory.createSpaceItem(name);
        if(space != null) spaceContainer.getChildren().add(space);
    }
}
