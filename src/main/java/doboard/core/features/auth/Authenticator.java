package doboard.core.features.auth;

import doboard.core.common.connection.SQLConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Authenticator {
    public static User Login(String username, String password){
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";
        try(Connection c = SQLConnector.getConnection();
            PreparedStatement s = c.prepareStatement(query);
        ) {
            s.setString(1, username);
            s.setString(2, password);
            ResultSet r = s.executeQuery();

            String full_name;
            if(r.next()){
                User user = new User(
                        r.getInt("user_id"),
                        r.getString("username"),
                        r.getString("email"),
                        r.getString("password")
                );
                user.setFull_name(r.getString("full_name"));

                full_name = r.getString("full_name");
                System.out.println("User logged in: " + full_name);
                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        //If this method returns null it means incorrect password @topknott23
        //hahaha sige
        return null;
    }

    public static void Register(User user) {
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
            if(affectedRows == 0){
                System.out.printf("Register failed!\n");
            } else {
                System.out.printf("Register successful! Registered: " + user.getFull_name() + "\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}