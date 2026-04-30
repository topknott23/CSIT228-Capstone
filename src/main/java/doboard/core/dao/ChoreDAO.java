package doboard.core.dao;

import doboard.core.models.Chore;
import doboard.core.models.Frequency;
import doboard.core.util.Database;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ChoreDAO {

    public boolean createChore(int dormId, String title, String description, String frequency, LocalDate dueDate, int assignedUserId) {
        String insertChoreSql = "INSERT INTO chores (dorm_id, title, description, frequency, due_date) VALUES (?, ?, ?, ?, ?)";
        String insertAssignmentSql = "INSERT INTO chore_assignments (chore_id, user_id) VALUES (?, ?)";

        try {
            Connection conn = Database.getInstance().getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement choreStmt = conn.prepareStatement(insertChoreSql, Statement.RETURN_GENERATED_KEYS)) {
                choreStmt.setInt(1, dormId);
                choreStmt.setString(2, title);
                choreStmt.setString(3, description);
                choreStmt.setString(4, frequency.toLowerCase());
                choreStmt.setDate(5, Date.valueOf(dueDate));

                int affectedRows = choreStmt.executeUpdate();
                if (affectedRows == 0) throw new SQLException("Creating chore failed.");

                try (ResultSet generatedKeys = choreStmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int choreId = generatedKeys.getInt(1);

                        // Assign chore
                        try (PreparedStatement assignStmt = conn.prepareStatement(insertAssignmentSql)) {
                            assignStmt.setInt(1, choreId);
                            assignStmt.setInt(2, assignedUserId);
                            assignStmt.executeUpdate();
                        }
                        
                        conn.commit();
                        return true;
                    }
                }
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<doboard.core.models.ChoreDisplay> getChoresForUser(int dormId, int userId, boolean isAdmin) {
        List<doboard.core.models.ChoreDisplay> chores = new ArrayList<>();
        String sql;
        if (isAdmin) {
            sql = "SELECT c.*, u.username FROM chores c " +
                  "JOIN chore_assignments ca ON c.chore_id = ca.chore_id " +
                  "JOIN users u ON ca.user_id = u.user_id " +
                  "WHERE c.dorm_id = ?";
        } else {
            sql = "SELECT c.*, u.username FROM chores c " +
                  "JOIN chore_assignments ca ON c.chore_id = ca.chore_id " +
                  "JOIN users u ON ca.user_id = u.user_id " +
                  "WHERE c.dorm_id = ? AND ca.user_id = ?";
        }

        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, dormId);
            if (!isAdmin) {
                pstmt.setInt(2, userId);
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    chores.add(new doboard.core.models.ChoreDisplay(
                        rs.getInt("chore_id"),
                        rs.getString("title"),
                        rs.getString("username"),
                        rs.getDate("due_date").toLocalDate(),
                        rs.getString("status").toUpperCase()
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return chores;
    }

    public boolean markChoreAsDone(int choreId) {
        String sql = "UPDATE chores SET status = 'completed' WHERE chore_id = ?";
        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, choreId);
            if (pstmt.executeUpdate() > 0) {
                handleChoreRotation(choreId);
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void handleChoreRotation(int completedChoreId) {
        String fetchSql = "SELECT c.dorm_id, c.title, c.description, c.frequency, c.due_date, ca.user_id " +
                          "FROM chores c JOIN chore_assignments ca ON c.chore_id = ca.chore_id " +
                          "WHERE c.chore_id = ?";
        
        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(fetchSql)) {
            
            pstmt.setInt(1, completedChoreId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String freq = rs.getString("frequency");
                    if (freq == null || "once".equalsIgnoreCase(freq)) return;

                    int dormId = rs.getInt("dorm_id");
                    String title = rs.getString("title");
                    String desc = rs.getString("description");
                    LocalDate oldDate = rs.getDate("due_date").toLocalDate();
                    int currentUserId = rs.getInt("user_id");

                    LocalDate newDate = oldDate;
                    if ("daily".equalsIgnoreCase(freq)) newDate = oldDate.plusDays(1);
                    else if ("weekly".equalsIgnoreCase(freq)) newDate = oldDate.plusWeeks(1);
                    else if ("monthly".equalsIgnoreCase(freq)) newDate = oldDate.plusMonths(1);

                    // Find next user
                    DormDAO dormDAO = new DormDAO();
                    List<doboard.core.models.User> members = dormDAO.getDormMembers(dormId);
                    if (members.isEmpty()) return;

                    int nextUserId = members.get(0).getUser_id();
                    for (int i = 0; i < members.size(); i++) {
                        if (members.get(i).getUser_id() == currentUserId) {
                            nextUserId = members.get((i + 1) % members.size()).getUser_id();
                            break;
                        }
                    }

                    // Create the new rotated chore
                    createChore(dormId, title, desc, freq, newDate, nextUserId);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void cleanupOldCompletedChores() {
        String sql = "DELETE FROM chores WHERE status = 'completed' AND due_date < CURDATE()";
        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
