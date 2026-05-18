package doboard.dashboard;

import doboard.common.connection.SQLConnector;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class MaintenanceDAO {

    /**
     * Inserts a new pending maintenance request into the database schema layer.
     * @param dormId The target workspace ID context
     * @param userId The tenant sender account identifier
     * @param issueDescription The raw string describing the asset damage
     * @return true if row insertion execution succeeds, false otherwise
     */
    public boolean createRequest(int dormId, int userId, String issueDescription) {
        String query = "INSERT INTO maintenance_requests (dorm_id, user_id, issue_description, status) VALUES (?, ?, ?, 'PENDING')";

        try (Connection conn = SQLConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, dormId);
            stmt.setInt(2, userId);
            stmt.setString(3, issueDescription);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("SQL execution failed inside MaintenanceDAO.createRequest:");
            e.printStackTrace();
            return false;
        }
    }
}