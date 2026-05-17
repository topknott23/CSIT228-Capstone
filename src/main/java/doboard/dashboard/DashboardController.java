package doboard.dashboard;

import doboard.auth.User;
import doboard.common.session.SessionHandler;
import doboard.common.util.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
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
    @FXML private Label currentScreenTitleLabel;
    @FXML private Button navDashboard;
    @FXML private Button navChores;
    @FXML private Button navExpenses;
    @FXML private Button navSignals;
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
        NavigationManager.setDashboardController(this);
        titleBar.makeDraggable(topNavBar);


        User currentUser = SessionHandler.loadSession();
        if (currentUser != null) {
            usernameVal.setText(currentUser.getUsername());
        } else {
            usernameVal.setText("Guest");
        }

        loadTab("/doboard/dashboard/content-view.fxml", "DASHBOARD", navDashboard);
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
    @FXML public void goDashboard() { loadTab("/doboard/dashboard/content-view.fxml", "DASHBOARD", navDashboard); }
    @FXML public void goChores() { loadTab("/doboard/chores/chore-view.fxml", "CHORES", navChores); }
    @FXML public void goExpenses() { loadTab("/doboard/expenses/expenses-view.fxml", "EXPENSES", navExpenses); }
    @FXML public void goSignals() { loadTab("/doboard/signals/signals-view.fxml", "SIGNALS", navSignals); }

    @FXML
    private void handleLogout(ActionEvent event) {
        SessionHandler.endSession();
        Stage stage = StageUtil.getStage(event);
        SceneLoader.loadScene(stage, getClass(), "/doboard/auth/login-view.fxml", "Login");
    }

    // ---Helper Functions---
    private void addSpace(String name){
        Node space = DashboardComponentFactory.createSpaceItem(name);
        if(space != null) spaceContainer.getChildren().add(space);
    }

    private void loadTab(String fxmlPath, String screenTitle, Button activeTargetButton) {
        NavigationManager.loadView(getClass(), fxmlPath);

        // Synchronize title indicators across layout views
        currentScreenTitleLabel.setText(screenTitle);
        NavigationManager.setTitle(screenTitle.toLowerCase());

        resetNavButtonStyles();
        activeTargetButton.getStyleClass().removeAll("nav-tab", "nav-tab-active");
        activeTargetButton.getStyleClass().add("nav-tab-active");
    }

    private void resetNavButtonStyles() {
        Button[] tabs = {navDashboard, navChores, navExpenses, navSignals};
        for (Button btn : tabs) {
            btn.getStyleClass().removeAll("nav-tab", "nav-tab-active");
            btn.getStyleClass().add("nav-tab");
        }
    }
}