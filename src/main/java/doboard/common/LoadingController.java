package doboard.common;

import doboard.auth.User;
import doboard.common.cache.DataPreloader;
import doboard.common.cache.DataSyncService;
import doboard.common.session.SessionHandler;
import doboard.common.util.NavigationManager;
import doboard.common.util.Popup;
import doboard.common.util.SceneLoader;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;

public class LoadingController {

    @FXML private ProgressBar progressBar;
    @FXML private Label statusLabel;

    private User userToLoad;
    private Stage currentStage;

    /**
     * Called by NavigationManager to pass in the context before we start loading.
     */
    public void setContext(Stage stage, User user) {
        this.currentStage = stage;
        this.userToLoad = user;
        startPreload();
    }

    private void startPreload() {
        if (userToLoad == null) return;

        DataPreloader preloader = new DataPreloader(userToLoad);

        // Bind UI to the background task
        progressBar.progressProperty().bind(preloader.progressProperty());
        statusLabel.textProperty().bind(preloader.messageProperty());

        preloader.setOnSucceeded(event -> {
            // Unbind to prevent memory leaks
            progressBar.progressProperty().unbind();
            statusLabel.textProperty().unbind();
            
            // Start the background polling service for live updates
            DataSyncService.getInstance().start();

            // Load the dashboard!
            SceneLoader.loadScene(currentStage, NavigationManager.class, "/doboard/dashboard/dashboard-view.fxml", "DoBoard - Dashboard");
        });

        preloader.setOnFailed(event -> {
            progressBar.progressProperty().unbind();
            statusLabel.textProperty().unbind();
            
            Throwable ex = preloader.getException();
            if (ex != null) ex.printStackTrace();
            
            Popup.show("Loading Error", "Failed to load workspace data. Returning to login.");
            SessionHandler.endSession();
            SceneLoader.loadScene(currentStage, NavigationManager.class, "/doboard/auth/login-view.fxml", "Login");
        });

        // Run the task on a background thread
        Thread loadThread = new Thread(preloader, "DataPreloader-Thread");
        loadThread.setDaemon(true);
        loadThread.start();
    }
}
