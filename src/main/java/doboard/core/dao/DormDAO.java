package doboard.core.dao;

import doboard.core.models.Dorm;
import doboard.core.util.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DormDAO {

    public Dorm createDorm(String dormName, int adminUserId, String joinCode) {
        String insertDormSql = "INSERT INTO dorms (dorm_name, join_code) VALUES (?, ?)";
        String insertMemberSql = "INSERT INTO dorm_members (dorm_id, user_id, role) VALUES (?, ?, 'admin')";

        try {
            Connection conn = Database.getInstance().getConnection();
            conn.setAutoCommit(false); // Start transaction

            try (PreparedStatement dormStmt = conn.prepareStatement(insertDormSql, Statement.RETURN_GENERATED_KEYS)) {
                dormStmt.setString(1, dormName);
                dormStmt.setString(2, joinCode);
                
                int affectedRows = dormStmt.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Creating dorm failed, no rows affected.");
                }

                try (ResultSet generatedKeys = dormStmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int dormId = generatedKeys.getInt(1);
                        
                        // Now add the admin to dorm_members
                        try (PreparedStatement memberStmt = conn.prepareStatement(insertMemberSql)) {
                            memberStmt.setInt(1, dormId);
                            memberStmt.setInt(2, adminUserId);
                            memberStmt.executeUpdate();
                        }
                        
                        conn.commit(); // Commit transaction
                        return new Dorm(dormId, dormName, joinCode);
                    } else {
                        throw new SQLException("Creating dorm failed, no ID obtained.");
                    }
                }
            } catch (SQLException e) {
                conn.rollback(); // Rollback if any step fails
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Dorm joinDorm(String joinCode, int userId) {
        String findDormSql = "SELECT * FROM dorms WHERE join_code = ?";
        String insertMemberSql = "INSERT INTO dorm_members (dorm_id, user_id, role) VALUES (?, ?, 'member')";

        try (Connection conn = Database.getInstance().getConnection()) {
            
            // 1. Find Dorm by code
            Dorm foundDorm = null;
            try (PreparedStatement findStmt = conn.prepareStatement(findDormSql)) {
                findStmt.setString(1, joinCode);
                try (ResultSet rs = findStmt.executeQuery()) {
                    if (rs.next()) {
                        foundDorm = new Dorm(rs.getInt("dorm_id"), rs.getString("dorm_name"), rs.getString("join_code"));
                    }
                }
            }

            if (foundDorm != null) {
                // 2. Add user to dorm_members
                try (PreparedStatement insertStmt = conn.prepareStatement(insertMemberSql)) {
                    insertStmt.setInt(1, foundDorm.getDorm_id());
                    insertStmt.setInt(2, userId);
                    insertStmt.executeUpdate();
                    return foundDorm;
                } catch (SQLException e) {
                    System.err.println("User is likely already in this dorm or an error occurred.");
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Dorm getUserDorm(int userId) {
        String sql = "SELECT d.dorm_id, d.dorm_name, d.join_code FROM dorms d " +
                     "JOIN dorm_members m ON d.dorm_id = m.dorm_id " +
                     "WHERE m.user_id = ?";
        
        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Dorm(rs.getInt("dorm_id"), rs.getString("dorm_name"), rs.getString("join_code"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public java.util.List<doboard.core.models.User> getDormMembers(int dormId) {
        java.util.List<doboard.core.models.User> members = new java.util.ArrayList<>();
        String sql = "SELECT u.* FROM users u JOIN dorm_members m ON u.user_id = m.user_id WHERE m.dorm_id = ?";
        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, dormId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    members.add(new doboard.core.models.User(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getString("password")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return members;
    }

    public boolean isUserAdmin(int userId, int dormId) {
        String sql = "SELECT role FROM dorm_members WHERE user_id = ? AND dorm_id = ?";
        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, dormId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return "admin".equalsIgnoreCase(rs.getString("role"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
