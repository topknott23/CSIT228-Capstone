package doboard.core.features.dorm;

import doboard.core.common.connection.SQLConnector;
import doboard.core.features.auth.User;

import java.lang.annotation.Retention;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DormDAO {


    public static boolean createDorm(Dorm dorm) {
        String query = "INSERT INTO dorms (dorm_name, join_code, created_at) VALUES (?, ?, ?)";
        try(Connection c = SQLConnector.getConnection();
            PreparedStatement s = c.prepareStatement(query)) {

            s.setString(1, dorm.getDorm_name());
            s.setString(2, dorm.getJoin_code());
            s.setTimestamp(3, java.sql.Timestamp.from(dorm.getCreated_at()));

            int rows = s.executeUpdate();
            if(rows > 0) {
                System.out.println("Dorm created successfully");
                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.out.println("Dorm was not created");
            return false;
    }

    public static Dorm findDorm(String join_code) {
        String query = "SELECT * FROM dorms WHERE join_code = ?";

        try(Connection c = SQLConnector.getConnection();
        PreparedStatement s = c.prepareStatement(query)) {

            s.setString(1, join_code);
            ResultSet r = s.executeQuery();
            if(r.next()) {
                Dorm dorm = new Dorm(
                        r.getInt("dorm_id"),
                        r.getString("dorm_name"),
                        r.getString("dorm_code"),
                        r.getTimestamp("created_at").toInstant()
                );

                System.out.println("Found dorm with name " +  dorm.getDorm_name());
                return dorm;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

}