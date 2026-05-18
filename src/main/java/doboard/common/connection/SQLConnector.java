package doboard.common.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import doboard.common.util.Popup;

public class SQLConnector {
//    public static final String URL = "jdbc:mysql://192.168.0.99:3306/dorm_app";
//    public static final String USER = "user";
//    public static final String PASS = "123";
    public static final String URL = "jdbc:mysql://localhost:3306/dorm_app?serverTimezone=Asia/Manila";
    public static final String USER = "root";
    public static final String PASS = "";
    public static Connection getConnection() {
        Connection conn = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(URL, USER, PASS);
            System.out.println("Connected to database successfully");
        } catch (SQLException e) {
            // NO DB CASE
            if (e.getErrorCode() == 1049) {
                Popup.show("Database Missing",
                        "Database 'dorm_app' not found. Initializing a new database, please wait...");
                if (InitDB.setupDatabase()) {
                    try {
                        conn = DriverManager.getConnection(URL, USER, PASS);
                        Popup.show("Setup Complete", "Database initialized and connected successfully!");
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
            }else{
                e.printStackTrace();
            }
        } catch (ClassNotFoundException e){
            e.printStackTrace();
        }
        return conn;
    }
}