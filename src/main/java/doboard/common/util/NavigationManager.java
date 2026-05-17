package doboard.common.util;

import doboard.auth.User;
import doboard.dashboard.DashboardController;
import doboard.dorm.DormDAO;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class NavigationManager {
    public static VBox contentArea;
    public static Label windowTitleLabel;
    private static DashboardController dashboardController;

    private static final DormDAO dormDAO = new DormDAO();

    public static void setContentArea(VBox area){
        contentArea = area;
    }

    public static void setWindowTitleLabel(Label label) {
        windowTitleLabel = label;
    }

    public static void setDashboardController(DashboardController controller){ dashboardController = controller; }

    public static void switchToTab(String tabName) {
        if (dashboardController != null) {
            switch (tabName.toUpperCase()) {
                case "DASHBOARD" -> dashboardController.goDashboard();
                case "CHORES" -> dashboardController.goChores();
                case "EXPENSES" -> dashboardController.goExpenses();
                case "SIGNALS" -> dashboardController.goSignals();
            }
        }
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

    public static void handlePostLoginRouting(Stage stage, User user){
        int dormId = dormDAO.getDormIdByUserId(user.getUser_id());

        if (dormId != -1) {
            // User is already in a dorm, send them straight to the main dashboard
            SceneLoader.loadScene(stage, NavigationManager.class, "/doboard/dashboard/dashboard-view.fxml", "DoBoard - Dashboard");
        } else {
            // User has no dorm affiliation, redirect them to join or create one
            SceneLoader.loadScene(stage, NavigationManager.class, "/doboard/dorm/dorm-view.fxml", "DoBoard - Join or Create Dorm");
        }
    }
}