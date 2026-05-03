package doboard.auth;

import doboard.common.connection.SQLConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {

    public static User Login(String username, String password) {
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";
        try(Connection c = SQLConnector.getConnection();
            PreparedStatement s = c.prepareStatement(query)) {
            s.setString(1, username);
            s.setString(2, password);
            ResultSet r = s.executeQuery();

            if(r.next()){
                User user = new User(
                        r.getInt("user_id"),
                        r.getString("username"),
                        r.getString("email"),
                        r.getString("full_name"),
                        r.getString("password")
                );

                System.out.println("User logged in: " + user.getFull_name());
                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static boolean Register(User user) {
        if(findUser(user)) {
            System.out.println("User already exists! Pick a different username.");
            return false;
        }

        String query = "INSERT INTO users(username, email, password, created_at, full_name) VALUES(?, ?, ?, ?, ?)";
        try(Connection c = SQLConnector.getConnection();
            PreparedStatement s = c.prepareStatement(query);
        ) {
            s.setString(1, user.getUsername());
            s.setString(2, user.getEmail());
            s.setString(3, user.getPassword());
            s.setTimestamp(4, java.sql.Timestamp.from(user.getCreated_at()));
            s.setString(5, user.getFull_name());

            int affectedRows = s.executeUpdate();
            if(affectedRows == 0) {
                System.out.printf("Register failed! Some other error is occurring\n");
            } else {
                System.out.printf("Register successful! Registered: " + user.getFull_name() + "\n");
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    public static boolean findUser(User user) {
        String sql = "SELECT username FROM users WHERE username = ?";

        try (Connection conn = SQLConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getUsername());

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            System.err.println("Error while searching for user: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}