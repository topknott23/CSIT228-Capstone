package doboard.billing;

import doboard.common.connection.SQLConnector;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BillSplitDao {

    public boolean insert(BillSplit split) {
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

    public List<BillSplit> findByBillId(int billId) {
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

    public List<BillSplit> findByUserId(int userId) {
        List<BillSplit> splits = new ArrayList<>();
        String query = "SELECT * FROM bill_splits WHERE user_id = ?";
        try (Connection c = SQLConnector.getConnection();
             PreparedStatement s = c.prepareStatement(query)) {
            s.setInt(1, userId);
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

    public boolean markAsPaid(int splitId) {
        String query = "UPDATE bill_splits SET is_paid = 1, paid_date = ? WHERE split_id = ?";
        try (Connection c = SQLConnector.getConnection();
             PreparedStatement s = c.prepareStatement(query)) {
            s.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            s.setInt(2, splitId);
            return s.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean markAsUnpaid(int splitId) {
        String query = "UPDATE bill_splits SET is_paid = 0, paid_date = NULL WHERE split_id = ?";
        try (Connection c = SQLConnector.getConnection();
             PreparedStatement s = c.prepareStatement(query)) {
            s.setInt(1, splitId);
            return s.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteByBillId(int billId) {
        String query = "DELETE FROM bill_splits WHERE bill_id = ?";
        try (Connection c = SQLConnector.getConnection();
             PreparedStatement s = c.prepareStatement(query)) {
            s.setInt(1, billId);
            return s.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(int splitId) {
        String query = "DELETE FROM bill_splits WHERE split_id = ?";
        try (Connection c = SQLConnector.getConnection();
             PreparedStatement s = c.prepareStatement(query)) {
            s.setInt(1, splitId);
            return s.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
