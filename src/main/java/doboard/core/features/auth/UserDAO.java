package doboard.core.features.auth;

import doboard.core.common.connection.SQLConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {

    public User findByUsernameAndPassword(String email, String password) {
        String query = "SELECT * FROM users WHERE email = ? AND password = ?";
        try (Connection c = SQLConnector.getConnection();
                PreparedStatement s = c.prepareStatement(query)) {

            // bugo heron
            s.setString(1, email);
            s.setString(2, password);
            ResultSet r = s.executeQuery();

            if (r.next()) {
                User user = new User(
                        r.getInt("user_id"),
                        r.getString("username"),
                        r.getString("email"),
                        r.getString("password"));
                user.setFull_name(r.getString("full_name"));
                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("debug nganong dili mani mogana kayawa gwapaha aning sarah");
        return null;
    }

    public boolean insert(User user) {
        String query = "INSERT INTO users(username, email, password, created_at, full_name) VALUES(?, ?, ?, ?, ?)";
        try (Connection c = SQLConnector.getConnection();
                PreparedStatement s = c.prepareStatement(query)) {

            s.setString(1, user.getUsername());
            s.setString(2, user.getEmail());
            s.setString(3, user.getPassword());
            s.setTimestamp(4, java.sql.Timestamp.from(user.getCreated_at()));
            s.setString(5, user.getFull_name());

            return s.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
