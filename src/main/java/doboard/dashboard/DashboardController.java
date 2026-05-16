package doboard.dashboard;

import doboard.auth.User;
import doboard.common.session.SessionHandler;
import doboard.common.util.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class DashboardController {
    @FXML private StackPane rootPane;
    @FXML private VBox spaceContainer;
    @FXML private ImageView profileImage;
    @FXML private Label usernameVal;
    @FXML private VBox contentArea;
    @FXML private HBox topNavBar;
    @FXML private Label windowTitleLabel;
    // Instantiate the window manager
    private final CustomTitleBar titleBar = new CustomTitleBar();

    @FXML
    public void initialize(){
        NavigationManager.setWindowTitleLabel(windowTitleLabel);
        // ^^ CUSTOM TITLE for every screen
        NavigationManager.setContentArea(contentArea);
        NavigationManager.loadView(getClass(), "/doboard/dashboard/content-view.fxml");
        titleBar.makeDraggable(topNavBar);


        User currentUser = SessionHandler.loadSession();
        if (currentUser != null) {
            usernameVal.setText(currentUser.getUsername());
        } else {
            usernameVal.setText("Guest"); // Fallback just in case the session didn't load properly
        }
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
    private void handleProfileSettings(ActionEvent event){ }
    @FXML
    private void handleNotificationSettings(ActionEvent event){ }
    @FXML
    private void handleAutomationSettings(ActionEvent event){ }
    @FXML
    private void handlePrivacySettings(ActionEvent event){ }

    @FXML
    private void handleLogout(ActionEvent event) {
        SessionHandler.endSession();
        Stage stage = StageUtil.getStage(event);
        SceneLoader.loadScene(stage, getClass(), "/doboard/auth/login-view.fxml", "Login");
    }

    public void addSpace(String name){
        Node space = ComponentFactory.createSpaceItem(name);
        if(space != null) spaceContainer.getChildren().add(space);
    }
}