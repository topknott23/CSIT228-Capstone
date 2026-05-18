package doboard.common.cache;

import doboard.chores.Chore;
import doboard.expenses.Bill;
import doboard.expenses.BillSplit;
import doboard.signals.SignalDAO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * MVP Notification Differ
 *
 * Compares two snapshots of dorm data (old vs new) and generates
 * human-readable notification messages for anything that changed.
 *
 * VERTICAL SLICE — This class has NO dependencies on JavaFX or UI.
 * It is purely data-in, strings-out. Your groupmates can extend this
 * by simply adding a new diff method for any new entity type.
 *
 * HOW TO EXTEND:
 * 1. Add a new diff method below (e.g., diffMaintenanceRequests(...))
 * 2. Call it from computeDiffs() at the bottom
 * 3. That's it. The DataSyncService will automatically pick up and show the new notifications.
 */
public class DormNotificationDiffer {

    // --- DATA HOLDER ---
    // A clean snapshot of the cache at a point in time, used for comparison.
    public static class CacheSnapshot {
        public final List<SignalDAO.Signal> signals;
        public final List<Chore> chores;
        public final List<Bill> bills;
        public final Map<Integer, List<BillSplit>> billSplitsMap;
        public final int currentUserId;

        public CacheSnapshot(List<SignalDAO.Signal> signals,
                             List<Chore> chores,
                             List<Bill> bills,
                             Map<Integer, List<BillSplit>> billSplitsMap,
                             int currentUserId) {
            this.signals = signals;
            this.chores = chores;
            this.bills = bills;
            this.billSplitsMap = billSplitsMap;
            this.currentUserId = currentUserId;
        }
    }

    // --- MAIN ENTRY POINT ---

    /**
     * Compares oldSnapshot to newSnapshot and returns a list of notification strings.
     * Returns an empty list if nothing changed or if this is the first load.
     *
     * @param old The snapshot of data BEFORE the reload
     * @param fresh The snapshot of data AFTER the reload
     * @return A list of human-readable notification strings, one per event
     */
    public static List<String> computeDiffs(CacheSnapshot old, CacheSnapshot fresh) {
        List<String> notifications = new ArrayList<>();

        // Only run diffs if we have a valid old state to compare against.
        // Prevents a flood of notifications on the very first load.
        if (old == null) return notifications;

        // --- DIFF EACH ENTITY TYPE ---
        // Your groupmates can plug in new diff methods here.

        notifications.addAll(diffSignals(old.signals, fresh.signals));
        notifications.addAll(diffChores(old.chores, fresh.chores));
        notifications.addAll(diffBillSplits(old.billSplitsMap, fresh.billSplitsMap, fresh.bills, fresh.currentUserId));

        return notifications;
    }

    // =========================================================================
    // SIGNALS DIFF
    // Detects: New nudges received by the current user
    // =========================================================================
    private static List<String> diffSignals(List<SignalDAO.Signal> oldSignals, List<SignalDAO.Signal> freshSignals) {
        List<String> notifications = new ArrayList<>();

        Set<Integer> oldIds = oldSignals.stream()
                .map(SignalDAO.Signal::id)
                .collect(Collectors.toSet());

        for (SignalDAO.Signal signal : freshSignals) {
            if (!oldIds.contains(signal.id())) {
                // This is a brand new signal
                notifications.add("🔔 " + signal.senderName() + ": " + signal.complaint());
            }
        }

        return notifications;
    }

    // =========================================================================
    // CHORES DIFF
    // Detects: A chore was completed by someone, or a new chore was added
    // =========================================================================
    private static List<String> diffChores(List<Chore> oldChores, List<Chore> freshChores) {
        List<String> notifications = new ArrayList<>();

        Map<Integer, Chore> oldMap = oldChores.stream()
                .collect(Collectors.toMap(Chore::getChore_id, c -> c));

        for (Chore freshChore : freshChores) {
            Chore oldChore = oldMap.get(freshChore.getChore_id());

            if (oldChore == null) {
                // Brand new chore was added
                notifications.add("🧹 New chore added: " + freshChore.getTitle());
            } else if (oldChore.getStatus() != Chore.Status.COMPLETE
                    && freshChore.getStatus() == Chore.Status.COMPLETE) {
                // A chore was just marked as complete
                // NOTE: Chore model has no user_id field — extend this when assignee tracking is added
                notifications.add("✅ A chore was completed: " + freshChore.getTitle());
            }
        }

        return notifications;
    }

    // =========================================================================
    // BILL SPLITS DIFF
    // Detects: A new bill was split that includes the current user,
    //          or someone paid their split
    // =========================================================================
    private static List<String> diffBillSplits(Map<Integer, List<BillSplit>> oldSplitsMap,
                                               Map<Integer, List<BillSplit>> freshSplitsMap,
                                               List<Bill> freshBills,
                                               int currentUserId) {
        List<String> notifications = new ArrayList<>();

        // Build a bill title lookup
        Map<Integer, String> billTitles = freshBills.stream()
                .collect(Collectors.toMap(Bill::getBill_id, Bill::getTitle));

        for (Map.Entry<Integer, List<BillSplit>> entry : freshSplitsMap.entrySet()) {
            int billId = entry.getKey();
            List<BillSplit> freshSplits = entry.getValue();
            List<BillSplit> oldSplits = oldSplitsMap.getOrDefault(billId, new ArrayList<>());

            Map<Integer, BillSplit> oldSplitById = oldSplits.stream()
                    .collect(Collectors.toMap(BillSplit::getSplit_id, s -> s));

            String billTitle = billTitles.getOrDefault(billId, "a bill");

            for (BillSplit freshSplit : freshSplits) {
                BillSplit oldSplit = oldSplitById.get(freshSplit.getSplit_id());

                if (oldSplit == null && freshSplit.getUser_id() == currentUserId) {
                    // New split assigned to current user
                    notifications.add("💸 New bill split: " + billTitle
                            + " – ₱" + String.format("%.2f", freshSplit.getAmount()));
                } else if (oldSplit != null
                        && !oldSplit.isPaid()
                        && freshSplit.isPaid()
                        && freshSplit.getUser_id() != currentUserId) {
                    // Someone else paid their split (informational, not for your own payment)
                    notifications.add("💰 A dormmate paid their split for: " + billTitle);
                }
            }
        }

        return notifications;
    }
}
