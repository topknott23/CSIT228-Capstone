package doboard.chores;

import doboard.common.enums.Frequency;
import doboard.common.connection.SQLConnector;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ChoreDAO {

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
}
