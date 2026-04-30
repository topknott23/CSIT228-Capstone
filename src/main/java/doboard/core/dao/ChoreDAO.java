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

    public List<String> getChoreSummariesForUser(int dormId, int userId) {
        List<String> summaries = new ArrayList<>();
        String sql = "SELECT c.* FROM chores c " +
                     "JOIN chore_assignments ca ON c.chore_id = ca.chore_id " +
                     "WHERE c.dorm_id = ? AND ca.user_id = ?";

        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, dormId);
            pstmt.setInt(2, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String title = rs.getString("title");
                    LocalDate due = rs.getDate("due_date").toLocalDate();
                    String status = rs.getString("status").toUpperCase();
                    
                    summaries.add(String.format("%s - Due: %s [%s]", title, due.toString(), status));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return summaries;
    }
}
