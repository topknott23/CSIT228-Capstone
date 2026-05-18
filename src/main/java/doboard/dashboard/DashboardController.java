package doboard.dashboard;

import doboard.auth.User;
import doboard.common.TitleBarController;
import doboard.common.session.SessionHandler;
import doboard.common.util.*;
import doboard.dorm.Dorm;
import doboard.dorm.DormMember;
import doboard.dorm.DormService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class DashboardController {
    @FXML private StackPane rootPane;
    @FXML private Label usernameVal;
    @FXML private HBox navBarContainer;
    @FXML private Label currentScreenTitleLabel;

    // --- NEW LABELS FOR THE WORKSPACE BADGE ---
    @FXML private HBox workspaceBadgeContainer;
    @FXML private Label tenantDormNameLabel;
    @FXML private Label tenantJoinCodeLabel;

    @FXML private Button navDashboard;
    @FXML private Button navChores;
    @FXML private Button navExpenses;
    @FXML private Button navSignals;
    @FXML private VBox contentArea;

    @FXML private VBox tenantSidebar;
    @FXML private VBox adminSidebar;

    @FXML private TitleBarController embeddedTitleBarController;

    private final CustomTitleBar titleBar = new CustomTitleBar();
    private final DormService dormService = new DormService();
    private boolean isAdmin = false;
    private String originalJoinCode;

    @FXML
    public void initialize(){
        if(embeddedTitleBarController != null){
            NavigationManager.setWindowTitleLabel(embeddedTitleBarController.getWindowTitleLabel());
        }
        NavigationManager.setContentArea(contentArea);
        NavigationManager.setDashboardController(this);

        User currentUser = SessionHandler.loadSession();
        if (currentUser != null) {
            usernameVal.setText(currentUser.getUsername());

            // --- GLOBAL LANDLORD CHECK & DORM DATA FETCH ---
            if (currentUser.getUsername().equalsIgnoreCase("admin")) {
                isAdmin = true;
                // Hide the badge container completely for the Master Admin
                if (workspaceBadgeContainer != null) workspaceBadgeContainer.setVisible(false);
            } else {
                doboard.common.cache.DormDataCache cache = doboard.common.cache.DormDataCache.getInstance();
                Dorm dorm = cache.getDorm();
                
                if (dorm != null) {
                    if (tenantDormNameLabel != null) tenantDormNameLabel.setText(dorm.getDorm_name().toUpperCase());
                    if (tenantJoinCodeLabel != null) tenantJoinCodeLabel.setText(dorm.getJoin_code());

                    // Find current user's role from cached members
                    isAdmin = cache.getMembers().stream()
                        .filter(u -> u.getUser_id() == currentUser.getUser_id())
                        .findFirst()
                        .map(u -> dormService.getUserRoleInDorm(currentUser.getUser_id(), dorm.getDorm_id()) == DormMember.Role.ADMIN) // Role is not in User model, keep this or update User model. But dormService works for now. 
                        .orElse(false);
                }
                
                // Keep workspace badge updated if dorm name changes
                cache.addListener(() -> {
                    Dorm updatedDorm = cache.getDorm();
                    if (updatedDorm != null) {
                        if (tenantDormNameLabel != null) tenantDormNameLabel.setText(updatedDorm.getDorm_name().toUpperCase());
                    }
                });
            }
        } else {
            usernameVal.setText("Guest");
        }

        loadLayout();
    }

    @FXML private void handleProfileSettings(ActionEvent event) {if (!isAdmin) loadTab("/doboard/dashboard/profile-view.fxml", "PROFILE", null);}
    @FXML private void handleDormmatesList(ActionEvent event) {if (!isAdmin) loadTab("/doboard/dorm/dormmates-view.fxml", "DORMMATES", null);}
    @FXML private void handleArchivedBills(ActionEvent event) {if (!isAdmin) loadTab("/doboard/expenses/archived-bills-view.fxml", "ARCHIVED BILLS", null);}
    public void handleOpenDormChat(ActionEvent actionEvent) {if (!isAdmin) loadTab("/doboard/chatbox/chatbox-view.fxml", "DORM CHAT", null);}
    @FXML public void goDashboard() {if(!isAdmin) loadTab("/doboard/dashboard/content-view.fxml", "DASHBOARD", navDashboard);}
    @FXML public void goChores() {if(!isAdmin)loadTab("/doboard/chores/chore-view.fxml", "CHORES", navChores);}
    @FXML public void goExpenses() {if(!isAdmin)loadTab("/doboard/expenses/expenses-view.fxml", "EXPENSES", navExpenses);}
    @FXML public void goSignals() {if(!isAdmin)loadTab("/doboard/signals/signals-view.fxml", "SIGNALS", navSignals);}

    @FXML
    private void handleLogout(ActionEvent event) {
        doboard.common.cache.DataSyncService.getInstance().stop();
        doboard.common.cache.DormDataCache.getInstance().clear();
        SessionHandler.endSession();
        Stage stage = StageUtil.getStage(event);
        SceneLoader.loadScene(stage, getClass(), "/doboard/auth/login-view.fxml", "Login");
    }


    private void loadLayout() {
        if (isAdmin) {
            navBarContainer.setVisible(false);
            navBarContainer.setManaged(false);

            tenantSidebar.setVisible(false);
            tenantSidebar.setManaged(false);
            adminSidebar.setVisible(true);
            adminSidebar.setManaged(true);

            loadTab("/doboard/dashboard/admin-content-view.fxml", "OVERVIEW", null);
        } else {
            navBarContainer.setVisible(true);
            navBarContainer.setManaged(true);

            tenantSidebar.setVisible(true);
            tenantSidebar.setManaged(true);
            adminSidebar.setVisible(false);
            adminSidebar.setManaged(false);

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

    @FXML
    public void copyCode(MouseEvent mouseEvent) {
        if (originalJoinCode == null) return;

        // 1. Send original code to clipboard
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(originalJoinCode);
        clipboard.setContent(content);

        tenantJoinCodeLabel.setText("Copied!");
        tenantJoinCodeLabel.setStyle("-fx-text-fill: #16aa53; -fx-cursor: hand; -fx-font-weight: bold;"); // Turns green
    }

    @FXML
    public void renderCopyCodeBoxTooltip(MouseEvent mouseEvent) {
        var eventType = mouseEvent.getEventType();

        if (eventType == MouseEvent.MOUSE_ENTERED) {
            originalJoinCode = tenantJoinCodeLabel.getText();

            tenantJoinCodeLabel.setText("Copy Code?");

            tenantJoinCodeLabel.setStyle("-fx-text-fill: #73b7f3; -fx-cursor: hand; -fx-font-weight: bold;");
        }

        else if (eventType == MouseEvent.MOUSE_EXITED) {
            if (originalJoinCode != null) {
                tenantJoinCodeLabel.setText(originalJoinCode);
            }
            tenantJoinCodeLabel.setStyle("-fx-text-fill: #3F69AF; -fx-cursor: hand; -fx-font-weight: bold;");
        }
    }

    @FXML
    private void handleAdminDashboard(ActionEvent event) {
        loadTab("/doboard/dashboard/admin-content-view.fxml", "OVERVIEW", null);
    }

    @FXML
    private void handleManageDormsAdmin(ActionEvent event) {

        loadTab("/doboard/dashboard/admin-managed-dorms-view.fxml", "MANAGE DORMS", null);
    }

    @FXML
    private void handleBillHistoryAdmin(ActionEvent event) {

        Popup.show("Coming Soon", "Bill History module is currently being prepared.");
    }

}