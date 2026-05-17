package doboard.dashboard;

import doboard.auth.User;
import doboard.common.session.SessionHandler;
import doboard.common.util.*;
import doboard.dorm.DormMember;
import doboard.dorm.DormService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
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
    @FXML private HBox navBarContainer;
    @FXML private Label currentScreenTitleLabel;
    @FXML private Button navDashboard;
    @FXML private Button navChores;
    @FXML private Button navExpenses;
    @FXML private Button navSignals;
    @FXML private VBox contentArea;
    @FXML private HBox topNavBar;
    @FXML private Label windowTitleLabel;

    private final CustomTitleBar titleBar = new CustomTitleBar();
    private final DormService dormService = new DormService();

    private boolean isAdmin = false;

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
            int dormId = dormService.getUserDormId(currentUser.getUser_id());

            DormMember.Role role = dormService.getUserRoleInDorm(currentUser.getUser_id(), dormId);
            if(role == DormMember.Role.ADMIN) isAdmin = true;
        } else {
            usernameVal.setText("Guest");
        }

        loadLayout();
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
    @FXML public void goDashboard() {if(!isAdmin) loadTab("/doboard/dashboard/content-view.fxml", "DASHBOARD", navDashboard);}
    @FXML public void goChores() {if(!isAdmin)loadTab("/doboard/chores/chore-view.fxml", "CHORES", navChores);}
    @FXML public void goExpenses() {if(!isAdmin)loadTab("/doboard/expenses/expenses-view.fxml", "EXPENSES", navExpenses);}
    @FXML public void goSignals() {if(!isAdmin)loadTab("/doboard/signals/signals-view.fxml", "SIGNALS", navSignals);}

    @FXML
    private void handleLogout(ActionEvent event) {
        SessionHandler.endSession();
        Stage stage = StageUtil.getStage(event);
        SceneLoader.loadScene(stage, getClass(), "/doboard/auth/login-view.fxml", "Login");
    }

    @FXML
    private void handleAddSpaces(ActionEvent event){
        // TODO: ? Ang pulos aning add spaces ky add ranag dorm kung mo balhin kag living space
        // kinda like code chum when you join a class, the code is given by the landlord
    }

    // ---Helper Functions---
    private void addSpace(String name){
        Node space = DashboardComponentFactory.createSpaceItem(name);
        if(space != null) spaceContainer.getChildren().add(space);
    }

    private void loadLayout(){
        if(isAdmin){
            navBarContainer.setVisible(false);
            navBarContainer.setManaged(false);
            loadTab("/doboard/dashboard/admin-content-view.fxml", "OVERVIEW", null);
        }else{
            navBarContainer.setVisible(true);
            navBarContainer.setManaged(true);
            loadTab("/doboard/dashboard/content-view.fxml", "DASHBOARD", navDashboard);
        }
    }

    private void loadTab(String fxmlPath, String screenTitle, Button activeTargetButton) {
        NavigationManager.loadView(getClass(), fxmlPath);
        currentScreenTitleLabel.setText(screenTitle);
        NavigationManager.setTitle(screenTitle.toLowerCase());

        resetNavButtonStyles();

        if(activeTargetButton != null){
            activeTargetButton.getStyleClass().removeAll("nav-tab", "nav-tab-active");
            activeTargetButton.getStyleClass().add("nav-tab-active");
        }
    }

    private void resetNavButtonStyles() {
        Button[] tabs = {navDashboard, navChores, navExpenses, navSignals};
        for (Button btn : tabs) {
            btn.getStyleClass().removeAll("nav-tab", "nav-tab-active");
            btn.getStyleClass().add("nav-tab");
        }
    }
}