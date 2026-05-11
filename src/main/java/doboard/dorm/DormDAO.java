package doboard.dorm;

import doboard.common.connection.SQLConnector;
import doboard.common.util.JoinCodeGenerator;
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
        String query = "SELECT * FROM dorms WHERE UPPER(join_code) = ?";
        try (Connection c = SQLConnector.getConnection();
             PreparedStatement s = c.prepareStatement(query)) {
            s.setString(1, JoinCodeGenerator.normalize(joinCode));
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

    /**
     * Creates a new dorm with an auto-generated unique join code.
     *
     * @param dormName The name of the dorm
     * @return The created Dorm object with generated join code, or null if creation failed
     */
    public Dorm createDormWithCode(String dormName) {
        String joinCode = generateUniqueCode();
        if (joinCode == null) {
            System.err.println("Failed to generate unique join code");
            return null;
        }

        Dorm dorm = new Dorm(0, dormName, joinCode);
        if (insert(dorm)) {
            // Retrieve the auto-generated dorm_id
            return findByJoinCode(joinCode);
        }
        return null;
    }

    /**
     * Generates a unique join code that doesn't already exist in the database.
     * Attempts up to 10 times to find a unique code.
     *
     * @return A unique join code, or null if generation fails after max attempts
     */
    private String generateUniqueCode() {
        int maxAttempts = 10;
        for (int i = 0; i < maxAttempts; i++) {
            String code = JoinCodeGenerator.generateCode();
            if (!codeExists(code)) {
                return code;
            }
        }
        System.err.println("Could not generate unique join code after " + maxAttempts + " attempts");
        return null;
    }

    /**
     * Checks if a join code already exists in the database.
     *
     * @param joinCode The code to check
     * @return true if the code exists, false otherwise
     */
    public boolean codeExists(String joinCode) {
        return findByJoinCode(JoinCodeGenerator.normalize(joinCode)) != null;
    }

    /**
     * Finds a dorm by its ID.
     *
     * @param dormId The dorm ID
     * @return The Dorm object, or null if not found
     */
    public Dorm findById(int dormId) {
        String query = "SELECT * FROM dorms WHERE dorm_id = ?";
        try (Connection c = SQLConnector.getConnection();
             PreparedStatement s = c.prepareStatement(query)) {
            s.setInt(1, dormId);
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

    /**
     * Validates if a join code is valid and belongs to an existing dorm.
     * Also validates the format of the code.
     *
     * @param joinCode The code to validate
     * @return true if the code is valid and exists, false otherwise
     */
    public boolean isValidJoinCode(String joinCode) {
        if (!JoinCodeGenerator.isValidFormat(joinCode)) {
            return false;
        }
        return codeExists(joinCode);
    }
}



