package doboard.chores;

import doboard.common.enums.Frequency;
import doboard.common.connection.SQLConnector;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChoreDAO {

    // ---CORE OPERATIONS---
    public boolean insert(Chore chore) {
        String query = "INSERT INTO chores(dorm_id, title, description, frequency, due_date, status) VALUES(?, ?, ?, ?, ?, ?)";
        try (Connection c = SQLConnector.getConnection();
             PreparedStatement s = c.prepareStatement(query)) {
            s.setInt(1, chore.getDorm_id());
            s.setString(2, chore.getTitle());
            s.setString(3, chore.getDescription());
            s.setString(4, chore.getFrequency().name());
            s.setDate(5, Date.valueOf(chore.getDue_date()));
            s.setString(6, chore.getStatus().name());
            return s.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public int insertAndReturnId(Chore chore) {
        String query = "INSERT INTO chores(dorm_id, title, description, frequency, due_date, status) VALUES(?, ?, ?, ?, ?, ?)";
        try (Connection c = SQLConnector.getConnection();
             PreparedStatement s = c.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            s.setInt(1, chore.getDorm_id());
            s.setString(2, chore.getTitle());
            s.setString(3, chore.getDescription());
            s.setString(4, chore.getFrequency().name());
            s.setDate(5, Date.valueOf(chore.getDue_date()));
            s.setString(6, chore.getStatus().name());
            
            int affectedRows = s.executeUpdate();
            if (affectedRows == 0) {
                return -1;
            }

            try (ResultSet generatedKeys = s.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public Chore findById(int choreId) {
        String query = "SELECT * FROM chores WHERE chore_id = ?";
        try (Connection c = SQLConnector.getConnection();
             PreparedStatement s = c.prepareStatement(query)) {
            s.setInt(1, choreId);
            ResultSet r = s.executeQuery();
            if (r.next()) {
                return new Chore(
                        r.getInt("chore_id"),
                        r.getInt("dorm_id"),
                        r.getString("title"),
                        r.getString("description"),
                        Frequency.valueOf(r.getString("frequency")),
                        r.getDate("due_date").toLocalDate(),
                        Chore.Status.valueOf(r.getString("status"))
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Chore> findAllByDormId(int dormId) {
        List<Chore> chores = new ArrayList<>();
        String query = "SELECT * FROM chores WHERE dorm_id = ?";
        try (Connection c = SQLConnector.getConnection();
             PreparedStatement s = c.prepareStatement(query)) {
            s.setInt(1, dormId);
            ResultSet r = s.executeQuery();
            while (r.next()) {
                chores.add(new Chore(
                        r.getInt("chore_id"),
                        r.getInt("dorm_id"),
                        r.getString("title"),
                        r.getString("description"),
                        Frequency.valueOf(r.getString("frequency").toUpperCase()),
                        r.getDate("due_date").toLocalDate(),
                        Chore.Status.valueOf(r.getString("status"))
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return chores;
    }

    public List<Chore> findByStatus(int dormId, Chore.Status status) {
        List<Chore> chores = new ArrayList<>();
        String query = "SELECT * FROM chores WHERE dorm_id = ? AND status = ?";
        try (Connection c = SQLConnector.getConnection();
             PreparedStatement s = c.prepareStatement(query)) {
            s.setInt(1, dormId);
            s.setString(2, status.name());
            ResultSet r = s.executeQuery();
            while (r.next()) {
                chores.add(new Chore(
                        r.getInt("chore_id"),
                        r.getInt("dorm_id"),
                        r.getString("title"),
                        r.getString("description"),
                        Frequency.valueOf(r.getString("frequency")),
                        r.getDate("due_date").toLocalDate(),
                        Chore.Status.valueOf(r.getString("status"))
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return chores;
    }

    public boolean update(Chore chore) {
        String query = "UPDATE chores SET title = ?, description = ?, frequency = ?, due_date = ?, status = ? WHERE chore_id = ?";
        try (Connection c = SQLConnector.getConnection();
             PreparedStatement s = c.prepareStatement(query)) {
            s.setString(1, chore.getTitle());
            s.setString(2, chore.getDescription());
            s.setString(3, chore.getFrequency().name());
            s.setDate(4, Date.valueOf(chore.getDue_date()));
            s.setString(5, chore.getStatus().name());
            s.setInt(6, chore.getChore_id());
            return s.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateStatus(int choreId, Chore.Status status) {
        String query = "UPDATE chores SET status = ? WHERE chore_id = ?";
        try (Connection c = SQLConnector.getConnection();
             PreparedStatement s = c.prepareStatement(query)) {
            s.setString(1, status.name());
            s.setInt(2, choreId);
            return s.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(int choreId) {
        String query = "DELETE FROM chores WHERE chore_id = ?";
        try (Connection c = SQLConnector.getConnection();
             PreparedStatement s = c.prepareStatement(query)) {
            s.setInt(1, choreId);
            return s.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Map<Integer, Integer> getChoreCompletionCounts(int dormId) {
        Map<Integer, Integer> counts = new HashMap<>();
        String query = "SELECT u.user_id, COUNT(*) as completed_count " +
                      "FROM users u " +
                      "JOIN chore_assignments ca ON u.user_id = ca.user_id " +
                      "JOIN chores c ON ca.chore_id = c.chore_id " +
                      "WHERE c.dorm_id = ? AND c.status = 'COMPLETE' " +
                      "GROUP BY u.user_id " +
                      "ORDER BY completed_count DESC";
        try (Connection c = SQLConnector.getConnection();
             PreparedStatement s = c.prepareStatement(query)) {
            s.setInt(1, dormId);
            ResultSet r = s.executeQuery();
            while (r.next()) {
                counts.put(r.getInt("user_id"), r.getInt("completed_count"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return counts;
    }

    // ---ASSIGNMENT OPERATIONS---
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

    public boolean unassign(int choreId, int userId) {
        String query = "DELETE FROM chore_assignments WHERE chore_id = ? AND user_id = ?";
        try (Connection c = SQLConnector.getConnection();
             PreparedStatement s = c.prepareStatement(query)) {
            s.setInt(1, choreId);
            s.setInt(2, userId);
            return s.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
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

    public List<Integer> getChoresByUser(int userId) {
        List<Integer> choreIds = new ArrayList<>();
        String query = "SELECT chore_id FROM chore_assignments WHERE user_id = ?";
        try (Connection c = SQLConnector.getConnection();
             PreparedStatement s = c.prepareStatement(query)) {
            s.setInt(1, userId);
            ResultSet r = s.executeQuery();
            while (r.next()) {
                choreIds.add(r.getInt("chore_id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return choreIds;
    }

    public int countCompletedChoresByUser(int userId, int dormId) {
        String query = "SELECT COUNT(*) as count FROM chore_assignments ca " +
                "JOIN chores c ON ca.chore_id = c.chore_id " +
                "WHERE ca.user_id = ? AND c.dorm_id = ? AND c.status = 'COMPLETE'";
        try (Connection c = SQLConnector.getConnection();
             PreparedStatement s = c.prepareStatement(query)) {
            s.setInt(1, userId);
            s.setInt(2, dormId);
            ResultSet r = s.executeQuery();
            if (r.next()) {
                return r.getInt("count");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
