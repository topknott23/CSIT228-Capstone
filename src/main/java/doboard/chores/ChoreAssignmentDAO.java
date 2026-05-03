package doboard.chores;

import doboard.common.connection.SQLConnector;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ChoreAssignmentDAO {

    public boolean assign(ChoreAssignment assignment) {
        String query = "INSERT INTO chore_assignments(chore_id, user_id) VALUES(?, ?)";
        try (Connection c = SQLConnector.getConnection();
             PreparedStatement s = c.prepareStatement(query)) {
            s.setInt(1, assignment.getChore_id());
            s.setInt(2, assignment.getUser_id());
            return s.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Integer> getUserIdsByChore(int choreId) {
        List<Integer> userIds = new ArrayList<>();
        String query = "SELECT user_id FROM chore_assignments WHERE chore_id = ?";
        try (Connection c = SQLConnector.getConnection();
             PreparedStatement s = c.prepareStatement(query)) {
            s.setInt(1, choreId);
            ResultSet r = s.executeQuery();
            while (r.next()) {
                userIds.add(r.getInt("user_id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userIds;
    }

    public boolean unassignAll(int choreId) {
        String query = "DELETE FROM chore_assignments WHERE chore_id = ?";
        try (Connection c = SQLConnector.getConnection();
             PreparedStatement s = c.prepareStatement(query)) {
            s.setInt(1, choreId);
            return s.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
