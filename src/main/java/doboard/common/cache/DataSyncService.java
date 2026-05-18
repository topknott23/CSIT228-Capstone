package doboard.common.cache;

import doboard.common.util.Popup;
import javafx.application.Platform;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Background daemon that polls the database for changes made by other sessions.
 * Runs independently of the JavaFX Application Thread.
 */
public class DataSyncService {

    private static DataSyncService instance;
    private ScheduledExecutorService scheduler;
    private boolean isRunning = false;

    private DataSyncService() {
    }

    public static synchronized DataSyncService getInstance() {
        if (instance == null) {
            instance = new DataSyncService();
        }
        return instance;
    }

    /**
     * Starts the polling service. Should be called after successful login &
     * preload.
     * Polls every 10 seconds.
     */
    public synchronized void start() {
        if (isRunning)
            return;

        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "DataSync-Poller-Thread");
            t.setDaemon(true); // Don't block app shutdown
            return t;
        });

        // Run the polling task every 10 seconds
        scheduler.scheduleAtFixedRate(this::pollForUpdates, 10, 10, TimeUnit.SECONDS);
        isRunning = true;
        System.out.println("DataSyncService: Started background polling (10s interval)");
    }

    /**
     * Stops the polling service. Should be called on logout.
     */
    public synchronized void stop() {
        if (!isRunning || scheduler == null)
            return;

        scheduler.shutdownNow();
        isRunning = false;
        System.out.println("DataSyncService: Stopped background polling");
    }

    /**
     * The actual task that runs every 10 seconds on the background thread.
     */
    private void pollForUpdates() {
        DormDataCache cache = DormDataCache.getInstance();
        int dormId = cache.getDormId();
        int userId = cache.getCurrentUserId();

        if (dormId == -1 || userId == -1)
            return; // Not in a dorm or not logged in

        try {
            // Attempt to reload the cache. reload() returns true if the hash changed.
            boolean hasChanges = cache.reload(dormId, userId);

            if (hasChanges) {
                System.out.println("DataSyncService: Remote changes detected! Notifying UI listeners...");

                // Optional: Show a subtle toast notification that data synced
                Platform.runLater(() -> {
                    Popup.showSubtleToast("Sarah Workspace synced with latest changes");
                });

                // Tell the cache to fire all registered controller update callbacks
                cache.notifyListeners();
            }
        } catch (Exception e) {
            System.err.println("DataSyncService: Error during polling: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
