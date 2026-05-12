package doboard.signals;

import doboard.common.connection.SQLConnector;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for signal/nudge notifications.
 * Handles inserting signals and retrieving them for display.
 */
public class SignalDAO {


    public boolean insertSignal(int senderId, int receiverId, int dormId, String complaint, int nudgeCount) {
        String query = "INSERT INTO notifications(sender_id, receiver_id, dorm_id, message, nudge_count) VALUES(?, ?, ?, ?, ?)";
        try (Connection c = SQLConnector.getConnection();
             PreparedStatement s = c.prepareStatement(query)) {
            s.setInt(1, senderId);
            s.setInt(2, receiverId);
            s.setInt(3, dormId);
            s.setString(4, complaint);
            s.setInt(5, nudgeCount);
            return s.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Gets all signals/nudges relevant to a user (sent to them or broadcast in their dorm).
     * Returns the most recent 20 signals.

     */
    public List<Signal> getSignalsForUser(int userId, int dormId) {
        List<Signal> signals = new ArrayList<>();
        String query = """
            SELECT n.*, u.username AS sender_name 
            FROM notifications n 
            JOIN users u ON n.sender_id = u.user_id 
            WHERE n.dorm_id = ? AND (n.receiver_id = ? OR n.receiver_id = 0) AND n.sender_id != ?
            ORDER BY n.sent_at DESC 
            LIMIT 20
            """;
        try (Connection c = SQLConnector.getConnection();
             PreparedStatement s = c.prepareStatement(query)) {
            s.setInt(1, dormId);
            s.setInt(2, userId);
            s.setInt(3, userId); // Don't show your own signals to yourself
            ResultSet r = s.executeQuery();
            while (r.next()) {
                signals.add(new Signal(
                        r.getInt("notification_id"),
                        r.getInt("sender_id"),
                        r.getString("sender_name"),
                        r.getInt("receiver_id"),
                        r.getInt("dorm_id"),
                        r.getString("message"),
                        r.getInt("nudge_count"),
                        r.getTimestamp("sent_at").toString()
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return signals;
    }

    /**
     * Saves a Do Not Disturb entry for a user.

     */
    public boolean saveDndStatus(int userId, int dormId, String reason, int hours) {
        // Insert a system notification about DND status
        String query = "INSERT INTO notifications(sender_id, receiver_id, dorm_id, message, nudge_count) VALUES(?, 0, ?, ?, 0)";
        try (Connection c = SQLConnector.getConnection();
             PreparedStatement s = c.prepareStatement(query)) {
            s.setInt(1, userId);
            s.setInt(2, dormId);
            s.setString(3, "🔇 DND for " + hours + "hrs — " + reason);
            return s.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Immutable record representing a signal/notification.
     */
    public record Signal(int id, int senderId, String senderName, int receiverId, int dormId,
                          String complaint, int nudgeCount, String sentAt) {
    }
}
