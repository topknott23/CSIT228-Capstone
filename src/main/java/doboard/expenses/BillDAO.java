package doboard.expenses;

import doboard.common.connection.SQLConnector;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BillDAO {

    // ---CORE OPERATIONS--
    public boolean insertBill(Bill bill) {
        String query = "INSERT INTO bills(dorm_id, title, total_amount, due_date, created_at) VALUES(?, ?, ?, ?, ?)";
        try (Connection c = SQLConnector.getConnection();
             PreparedStatement s = c.prepareStatement(query)) {
            s.setInt(1, bill.getBill_dorm_id());
            s.setString(2, bill.getTitle());
            s.setDouble(3, bill.getTotal_amount());
            s.setDate(4, Date.valueOf(bill.getBill_due_date()));
            s.setTimestamp(5, Timestamp.from(bill.getCreated_at()));
            return s.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public int insertAndGetBillId(Bill bill) {
        String query = "INSERT INTO bills(dorm_id, title, total_amount, due_date, created_at) VALUES(?, ?, ?, ?, ?)";
        try (Connection c = SQLConnector.getConnection();
             PreparedStatement s = c.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            s.setInt(1, bill.getBill_dorm_id());
            s.setString(2, bill.getTitle());
            s.setDouble(3, bill.getTotal_amount());
            s.setDate(4, Date.valueOf(bill.getBill_due_date()));
            s.setTimestamp(5, Timestamp.from(bill.getCreated_at()));

            int affectedRows = s.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = s.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public List<Bill> findBillByDormId(int dormId) {
        List<Bill> bills = new ArrayList<>();
        String query = "SELECT * FROM bills WHERE dorm_id = ?";
        try (Connection c = SQLConnector.getConnection();
             PreparedStatement s = c.prepareStatement(query)) {
            s.setInt(1, dormId);
            ResultSet r = s.executeQuery();
            while (r.next()) {
                bills.add(new Bill(
                        r.getInt("bill_id"),
                        r.getInt("dorm_id"),
                        r.getString("title"),
                        r.getDouble("total_amount"),
                        r.getDate("due_date").toLocalDate()
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bills;
    }

    public boolean deleteBill(int billId) {
        String query = "DELETE FROM bills WHERE bill_id = ?";
        try (Connection c = SQLConnector.getConnection();
             PreparedStatement s = c.prepareStatement(query)) {
            s.setInt(1, billId);
            return s.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ---SPLIT OPERATIONS---
    public boolean insertBillSplit(BillSplit split) {
        String query = "INSERT INTO bill_splits(bill_id, user_id, amount_owed, is_paid) VALUES(?, ?, ?, ?)";
        try (Connection c = SQLConnector.getConnection();
             PreparedStatement s = c.prepareStatement(query)) {
            s.setInt(1, split.getBill_id());
            s.setInt(2, split.getUser_id());
            s.setDouble(3, split.getAmount());
            s.setBoolean(4, split.isPaid());
            return s.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<BillSplit> findSplitsByBillId(int billId) {
        List<BillSplit> splits = new ArrayList<>();
        String query = "SELECT * FROM bill_splits WHERE bill_id = ?";
        try (Connection c = SQLConnector.getConnection();
             PreparedStatement s = c.prepareStatement(query)) {
            s.setInt(1, billId);
            ResultSet r = s.executeQuery();
            while (r.next()) {
                splits.add(new BillSplit(
                        r.getInt("split_id"),
                        r.getInt("bill_id"),
                        r.getInt("user_id"),
                        r.getDouble("amount_owed"),
                        r.getBoolean("is_paid")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return splits;
    }

    public boolean updateSplitStatus(int splitId, boolean isPaid) {
        String query = "UPDATE bill_splits SET is_paid = ? WHERE split_id = ?";
        try (Connection c = SQLConnector.getConnection();
             PreparedStatement s = c.prepareStatement(query)) {
            s.setBoolean(1, isPaid);
            s.setInt(2, splitId);
            return s.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}