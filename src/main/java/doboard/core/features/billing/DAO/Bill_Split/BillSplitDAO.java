package doboard.core.features.billing.DAO.Bill_Split;

import doboard.core.features.billing.BillSplit;
import doboard.core.common.connection.SQLConnector;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BillSplitDAO {

    public boolean insert(BillSplit split) {
        String query = "INSERT INTO bill_splits(bill_id, user_id, amount, is_paid) VALUES(?, ?, ?, ?)";
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
                        r.getDouble("amount"),
                        r.getBoolean("is_paid")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return splits;
    }

    public boolean updateStatus(int splitId, boolean isPaid) {
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
