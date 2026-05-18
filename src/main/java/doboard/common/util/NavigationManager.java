package doboard.common.util;

import doboard.auth.User;
import doboard.dashboard.DashboardController;
import doboard.dorm.DormDAO;
import doboard.dorm.DormSetupController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import java.io.IOException;

public class NavigationManager {
    public static VBox contentArea;
    public static Label windowTitleLabel;
    private static DashboardController dashboardController;

    private static final DormDAO dormDAO = new DormDAO();

    public static void setContentArea(VBox area) { contentArea = area; }
    public static void setWindowTitleLabel(Label label) { windowTitleLabel = label; }
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

    // --- NEW: THE GLOBAL DORM SETUP DIALOG SPAWNER ---
    public static void showDormSetupDialog(Window owner, Runnable onSuccess) {
        try {
            FXMLLoader loader = new FXMLLoader(NavigationManager.class.getResource("/doboard/dorm/dorm-setup-dialog.fxml"));
            Parent root = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(owner);

            // Apply the transparent, unclipped styling!
            dialogStage.initStyle(StageStyle.TRANSPARENT);
            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            try { scene.getStylesheets().add(NavigationManager.class.getResource("/styles/styles.css").toExternalForm()); } catch (Exception ignored) {}
            dialogStage.setScene(scene);

            DormSetupController controller = loader.getController();
            controller.setOnSuccess(onSuccess);

            dialogStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // --- UPDATED ROUTING LOGIC ---
// --- UPDATED ROUTING LOGIC ---
    public static void handlePostLoginRouting(Stage stage, User user){
        // 1. GLOBAL ADMIN BYPASS
        if (user.getUsername().equalsIgnoreCase("admin")) {
            SceneLoader.loadScene(stage, NavigationManager.class, "/doboard/dashboard/dashboard-view.fxml", "DoBoard - Master Portal");
            return;
        }

        // 2. NORMAL TENANT ROUTING
        int dormId = dormDAO.getDormIdByUserId(user.getUser_id());

        if (dormId != -1) {
            // LOAD CACHE FIRST
            doboard.common.LoadingController loadingController = SceneLoader.loadSceneAndGetController(
                    stage,
                    NavigationManager.class,
                    "/doboard/common/loading-view.fxml",
                    "DoBoard - Loading..."
            );
            if (loadingController != null) {
                loadingController.setContext(stage, user);
            }
        } else {
            showDormSetupDialog(stage, () -> {
                doboard.common.LoadingController loadingController = SceneLoader.loadSceneAndGetController(
                        stage,
                        NavigationManager.class,
                        "/doboard/common/loading-view.fxml",
                        "DoBoard - Loading..."
                );
                if (loadingController != null) {
                    loadingController.setContext(stage, user);
                }
            });
        }
    }
}