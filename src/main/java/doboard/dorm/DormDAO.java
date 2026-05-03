package doboard.dorm;

import doboard.common.connection.SQLConnector;
import java.sql.*;

public class DormDAO {

    public boolean insert(Dorm dorm) {
        String query = "INSERT INTO dorms(dorm_name, join_code, created_at) VALUES(?, ?, ?)";
        try (Connection c = SQLConnector.getConnection();
             PreparedStatement s = c.prepareStatement(query)) {
            s.setString(1, dorm.getDorm_name());
            s.setString(2, dorm.getJoin_code());
            s.setTimestamp(3, Timestamp.from(dorm.getCreated_at()));
            return s.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Dorm findByJoinCode(String joinCode) {
        String query = "SELECT * FROM dorms WHERE join_code = ?";
        try (Connection c = SQLConnector.getConnection();
             PreparedStatement s = c.prepareStatement(query)) {
            s.setString(1, joinCode);
            ResultSet r = s.executeQuery();
            if (r.next()) {
                return new Dorm(
                        r.getInt("dorm_id"),
                        r.getString("dorm_name"),
                        r.getString("join_code"),
                        r.getTimestamp("created_at").toInstant()
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean update(Dorm dorm) {
        String query = "UPDATE dorms SET dorm_name = ?, join_code = ? WHERE dorm_id = ?";
        try (Connection c = SQLConnector.getConnection();
             PreparedStatement s = c.prepareStatement(query)) {
            s.setString(1, dorm.getDorm_name());
            s.setString(2, dorm.getJoin_code());
            s.setInt(3, dorm.getDorm_id());
            return s.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}

