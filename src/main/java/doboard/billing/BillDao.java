package doboard.billing;

import doboard.common.connection.SQLConnector;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BillDao {

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

    public Bill findById(int billId) {
        String query = "SELECT * FROM bills WHERE bill_id = ?";
        try (Connection c = SQLConnector.getConnection();
             PreparedStatement s = c.prepareStatement(query)) {
            s.setInt(1, billId);
            ResultSet r = s.executeQuery();
            if (r.next()) {
                Bill bill = new Bill(
                        r.getInt("bill_id"),
                        r.getInt("dorm_id"),
                        r.getString("title"),
                        r.getDouble("total_amount"),
                        r.getDate("due_date").toLocalDate()
                );
                bill.setCreated_at(r.getTimestamp("created_at").toInstant());
                return bill;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Bill> findAllByDormId(int dormId) {
        List<Bill> bills = new ArrayList<>();
        String query = "SELECT * FROM bills WHERE dorm_id = ?";
        try (Connection c = SQLConnector.getConnection();
             PreparedStatement s = c.prepareStatement(query)) {
            s.setInt(1, dormId);
            ResultSet r = s.executeQuery();
            while (r.next()) {
                Bill bill = new Bill(
                        r.getInt("bill_id"),
                        r.getInt("dorm_id"),
                        r.getString("title"),
                        r.getDouble("total_amount"),
                        r.getDate("due_date").toLocalDate()
                );
                bill.setCreated_at(r.getTimestamp("created_at").toInstant());
                bills.add(bill);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bills;
    }

    public boolean update(Bill bill) {
        String query = "UPDATE bills SET dorm_id = ?, title = ?, total_amount = ?, due_date = ? WHERE bill_id = ?";
        try (Connection c = SQLConnector.getConnection();
             PreparedStatement s = c.prepareStatement(query)) {
            s.setInt(1, bill.getBill_dorm_id());
            s.setString(2, bill.getTitle());
            s.setDouble(3, bill.getTotal_amount());
            s.setDate(4, Date.valueOf(bill.getBill_due_date()));
            s.setInt(5, bill.getBill_id());
            return s.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
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
