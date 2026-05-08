package doboard.dashboard;

import doboard.common.util.ComponentFactory;
import doboard.common.util.NavigationManager;
import doboard.common.util.CustomTitleBar; // <-- Import the new utility
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class DashboardController {
    @FXML private StackPane rootPane;
    @FXML private VBox spaceContainer;
    @FXML private ImageView profileImage;
    @FXML private Label usernameVal;
    @FXML private VBox contentArea;
    @FXML private HBox topNavBar;

    // Instantiate the window manager
    private final CustomTitleBar titleBar = new CustomTitleBar();

    @FXML
    public void initialize(){
        // for content area
        NavigationManager.setContentArea(contentArea);
        NavigationManager.loadView(getClass(), "/doboard/dashboard/content-view.fxml");

        // Setup window dragging and resizing functionality
        titleBar.makeDraggable(topNavBar);
    }

    // --- WINDOW CONTROLS (Delegated to utility class) ---
    @FXML
    private void minimizeWindow(ActionEvent event) {
        titleBar.minimize(event);
    }

    @FXML
    private void maximizeWindow(ActionEvent event) {
        titleBar.maximize(event);
    }

    @FXML
    private void closeWindow(ActionEvent event) {
        titleBar.close(event);
    }

    // --- DASHBOARD ACTIONS ---
    @FXML
    private void handleAddSpaces(ActionEvent event){
        // TODO
    }

    @FXML
    private void handleProfileSettings(ActionEvent event){ }
    @FXML
    private void handleNotificationSettings(ActionEvent event){ }
    @FXML
    private void handleAutomationSettings(ActionEvent event){ }
    @FXML
    private void handlePrivacySettings(ActionEvent event){ }

    @FXML
    private void handleLogout(ActionEvent event){
        // TODO: Logout logic
    }

    public void addSpace(String name){
        Node space = ComponentFactory.createSpaceItem(name);
        if(space != null) spaceContainer.getChildren().add(space);
    }
}