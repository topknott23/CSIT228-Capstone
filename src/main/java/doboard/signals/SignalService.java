package doboard.signals;

import doboard.auth.User;
import doboard.auth.UserDAO;
import doboard.dorm.DormDAO;
import doboard.dorm.DormMember;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SignalService {
    private final DormDAO dormDAO = new DormDAO();
    private final SignalDAO signalDAO = new SignalDAO();

    public int getDormIdForUser(int userId) {
        return dormDAO.getDormIdByUserId(userId);
    }

    /**
     * Aggregates other dorm members and resolves their user profiles across package boundaries
     */
    public Map<String, Integer> getDormmatesMap(int currentUserId, int dormId) {
        Map<String, Integer> tenantMap = new HashMap<>();
        List<DormMember> members = dormDAO.getMembersByDorm(dormId);

        List<Integer> userIds = members.stream()
                .map(DormMember::getUser_id)
                .filter(id -> id != currentUserId)
                .toList();

        List<User> users = UserDAO.getUsersByIds(userIds);
        for (User u : users) {
            tenantMap.put(u.getUsername(), u.getUser_id());
        }
        return tenantMap;
    }

    /**
     * Fetches all notifications/signals broadcasted within a specific dorm room.
     */
    public List<SignalDAO.Signal> getAllSignalsForDorm(int dormId) {
        // Delegates directly to the administrative DAO lookup we defined earlier
        List<SignalDAO.Signal> signals = new java.util.ArrayList<>();
        String query = """
        SELECT n.*, u.username AS sender_name 
        FROM notifications n 
        JOIN users u ON n.sender_id = u.user_id 
        WHERE n.dorm_id = ? 
        ORDER BY n.sent_at DESC LIMIT 40
        """;
        try (java.sql.Connection c = doboard.common.connection.SQLConnector.getConnection();
             java.sql.PreparedStatement s = c.prepareStatement(query)) {
            s.setInt(1, dormId);
            java.sql.ResultSet r = s.executeQuery();
            while (r.next()) {
                signals.add(new SignalDAO.Signal(
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
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
        return signals;
    }

    /**
     * Deletes/resolves an active room signal from the system.
     */
    public void resolveSignal(int notificationId) {
        String sql = "DELETE FROM notifications WHERE notification_id = ?";
        try (java.sql.Connection c = doboard.common.connection.SQLConnector.getConnection();
             java.sql.PreparedStatement s = c.prepareStatement(sql)) {
            s.setInt(1, notificationId);
            s.executeUpdate();
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveDndStatus(int userId, int dormId, String reason, int hours) {
        signalDAO.saveDndStatus(userId, dormId, reason, hours);
    }

    public boolean sendSignal(int senderId, int receiverId, int dormId, String complaint, int nudges) {
        return signalDAO.insertSignal(senderId, receiverId, dormId, complaint, nudges);
    }

    public List<SignalDAO.Signal> getRecentSignals(int userId, int dormId){
        return signalDAO.getSignalsForUser(userId, dormId);
    }
}