package doboard.core.features.billing.DAO.Bill;

import doboard.core.features.billing.Bill;
import doboard.core.common.connection.SQLConnector;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BillDAO {

    public boolean insert(Bill bill) {
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

    public List<Bill> findByDormId(int dormId) {
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

    public boolean delete(int billId) {
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
}
