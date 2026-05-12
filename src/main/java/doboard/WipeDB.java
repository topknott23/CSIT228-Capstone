package doboard;

import doboard.common.session.SessionHandler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

/**
 * Utility to wipe the database by dropping the dorm_app schema
 */
public class WipeDB {
    public static void main(String[] args) {
        String baseUrl = "jdbc:mysql://localhost:3306/?allowMultiQueries=true";
        String user = "root";
        String pass = "";

        try (Connection conn = DriverManager.getConnection(baseUrl, user, pass);
             Statement stmt = conn.createStatement()) {

            System.out.println("Dropping database dorm_app...");
            stmt.executeUpdate("DROP DATABASE IF EXISTS dorm_app;");
            System.out.println("Database wiped successfully! It will reinitialize on next app startup.");

            SessionHandler.endSession();
        } catch (Exception e) {
            System.err.println("Error wiping database: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
