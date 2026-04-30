package doboard.core.dao;

import doboard.core.models.Bill;
import doboard.core.models.User;
import doboard.core.util.Database;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BillDAO {

    public boolean createBillAndSplit(int dormId, String title, double totalAmount, LocalDate dueDate) {
        String insertBillSql = "INSERT INTO bills (dorm_id, title, total_amount, due_date) VALUES (?, ?, ?, ?)";
        String insertSplitSql = "INSERT INTO bill_splits (bill_id, user_id, amount_owed, is_paid) VALUES (?, ?, ?, false)";

        DormDAO dormDAO = new DormDAO();
        List<User> members = dormDAO.getDormMembers(dormId);

        if (members.isEmpty()) return false;

        double splitAmount = totalAmount / members.size();

        try {
            Connection conn = Database.getInstance().getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement billStmt = conn.prepareStatement(insertBillSql, Statement.RETURN_GENERATED_KEYS)) {
                billStmt.setInt(1, dormId);
                billStmt.setString(2, title);
                billStmt.setDouble(3, totalAmount);
                billStmt.setDate(4, Date.valueOf(dueDate));

                int affectedRows = billStmt.executeUpdate();
                if (affectedRows == 0) throw new SQLException("Creating bill failed.");

                try (ResultSet generatedKeys = billStmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int billId = generatedKeys.getInt(1);

                        // Split among members
                        try (PreparedStatement splitStmt = conn.prepareStatement(insertSplitSql)) {
                            for (User member : members) {
                                splitStmt.setInt(1, billId);
                                splitStmt.setInt(2, member.getUser_id());
                                splitStmt.setDouble(3, splitAmount);
                                splitStmt.addBatch();
                            }
                            splitStmt.executeBatch();
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

    public List<Bill> getBillsForDorm(int dormId) {
        List<Bill> bills = new ArrayList<>();
        String sql = "SELECT * FROM bills WHERE dorm_id = ?";

        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, dormId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    bills.add(new Bill(
                        rs.getInt("bill_id"),
                        rs.getInt("dorm_id"),
                        rs.getString("title"),
                        String.valueOf(rs.getDouble("total_amount")),
                        rs.getDate("due_date").toLocalDate()
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bills;
    }

    public List<String> getUserBillSummaries(int dormId, int userId) {
        List<String> summaries = new ArrayList<>();
        String sql = "SELECT b.title, b.total_amount, b.due_date, s.amount_owed, s.is_paid " +
                     "FROM bills b JOIN bill_splits s ON b.bill_id = s.bill_id " +
                     "WHERE b.dorm_id = ? AND s.user_id = ?";

        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, dormId);
            pstmt.setInt(2, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String title = rs.getString("title");
                    double total = rs.getDouble("total_amount");
                    double owed = rs.getDouble("amount_owed");
                    LocalDate due = rs.getDate("due_date").toLocalDate();
                    boolean isPaid = rs.getBoolean("is_paid");
                    
                    String status = isPaid ? "[PAID]" : "[UNPAID]";
                    summaries.add(String.format("%s - Total: $%.2f | You Owe: $%.2f (Due: %s) %s", 
                                  title, total, owed, due.toString(), status));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return summaries;
    }
}
