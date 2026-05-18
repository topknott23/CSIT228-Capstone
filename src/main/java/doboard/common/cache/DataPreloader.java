package doboard.common.cache;

import doboard.auth.User;
import doboard.dorm.DormDAO;
import javafx.concurrent.Task;

/**
 * Background Task that preloads ALL dorm data into DormDataCache after login.
 * Runs on a separate thread so the UI stays responsive during loading.
 * Reports progress updates for the loading screen's progress bar and status
 * label.
 */
public class DataPreloader extends Task<Void> {

    private final User user;
    private final DormDAO dormDAO = new DormDAO();

    public DataPreloader(User user) {
        this.user = user;
    }

    @Override
    protected Void call() throws Exception {
        updateMessage("Resolving workspace...");
        updateProgress(1, 5);

        // 1. Resolve the user's dorm
        int dormId = dormDAO.getDormIdByUserId(user.getUser_id());

        if (dormId == -1) {
            updateMessage("No dorm found. Skipping preload.");
            updateProgress(5, 5);
            return null;
        }

        updateMessage("Loading dorm data...");
        updateProgress(2, 5);

        // 2. Reload all data into the singleton cache
        // This single call fetches: dorm info, members, chores, bills, splits, signals,
        // leaderboard
        DormDataCache cache = DormDataCache.getInstance();

        updateMessage("Fetching chores and bills...");
        updateProgress(3, 5);

        DormDataCache.CacheReloadResult result = cache.reload(dormId, user.getUser_id());

        updateMessage("Syncing notifications...");
        updateProgress(4, 5);

        // Small artificial delay so the user sees the loading screen
        // (otherwise it flashes too fast on localhost)
        Thread.sleep(600);

        updateMessage("Ready!");
        updateProgress(5, 5);

        System.out.println("DataPreloader: Cache loaded for dorm " + dormId
                + " (" + cache.getChores().size() + " chores, "
                + cache.getBills().size() + " bills, "
                + cache.getSignals().size() + " signals)");

        return null;
    }
}
