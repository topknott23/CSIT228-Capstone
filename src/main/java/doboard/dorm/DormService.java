package doboard.dorm;

import doboard.auth.User;

/**
 * Service class for high-level dorm operations.
 * Provides a clean API for dorm creation, joining, and validation.
 */
public class DormService {

    private final DormDAO dormDAO = new DormDAO();
    private final DormMemberDAO dormMemberDAO = new DormMemberDAO();

    /**
     * Creates a new dorm with the specified name and adds the creator as the owner.
     * The dorm is assigned a unique join code for other users to join.
     *
     * @param dormName The name of the dorm (e.g., "Maple Hall")
     * @param creatorUser The user creating the dorm (will become OWNER)
     * @return A DormOperationResult indicating success/failure with details
     */
    public DormOperationResult createDorm(String dormName, User creatorUser) {
        // Validate input
        if (dormName == null || dormName.trim().isEmpty()) {
            return DormOperationResult.failure("Dorm name cannot be empty");
        }

        if (creatorUser == null) {
            return DormOperationResult.failure("No user session found");
        }

        // Attempt to create dorm
        Dorm createdDorm = dormMemberDAO.createDormAsOwner(dormName, creatorUser);
        
        if (createdDorm == null) {
            return DormOperationResult.failure("Failed to create dorm. Check if you're already in a dorm.");
        }

        return DormOperationResult.success(
                "Dorm '" + dormName + "' created successfully",
                createdDorm
        );
    }

    /**
     * Joins an existing dorm using a join code.
     * The user becomes a RESIDENT of the dorm.
     *
     * @param joinCode The join code provided by the dorm owner/members
     * @param user The user joining the dorm
     * @return A DormOperationResult indicating success/failure with details
     */
    public DormOperationResult joinDormWithCode(String joinCode, User user) {
        // Validate input
        if (joinCode == null || joinCode.trim().isEmpty()) {
            return DormOperationResult.failure("Join code cannot be empty");
        }

        if (user == null) {
            return DormOperationResult.failure("No user session found");
        }

        // Validate code format
        if (!dormDAO.isValidJoinCode(joinCode)) {
            return DormOperationResult.failure("Invalid join code format");
        }

        // Get dorm info
        Dorm dorm = dormDAO.findByJoinCode(joinCode);
        if (dorm == null) {
            return DormOperationResult.failure("Join code not found. Please check the code and try again.");
        }

        // Attempt to join
        if (dormMemberDAO.joinDorm(joinCode, user)) {
            return DormOperationResult.success(
                    "Successfully joined dorm '" + dorm.getDorm_name() + "'",
                    dorm
            );
        } else {
            return DormOperationResult.failure("Failed to join dorm. You may already be in a dorm.");
        }
    }

    /**
     * Checks if a user is currently in a dorm.
     *
     * @param userId The user ID to check
     * @return true if the user is in a dorm, false otherwise
     */
    public boolean isUserInDorm(int userId) {
        return dormMemberDAO.isUserInDorm(userId);
    }

    /**
     * Gets the dorm ID for a user.
     *
     * @param userId The user ID
     * @return The dorm ID, or -1 if user is not in any dorm
     */
    public int getUserDormId(int userId) {
        return dormMemberDAO.getDormIdByUserId(userId);
    }

    /**
     * Gets dorm details by ID.
     *
     * @param dormId The dorm ID
     * @return The Dorm object, or null if not found
     */
    public Dorm getDormById(int dormId) {
        return dormDAO.findById(dormId);
    }

    /**
     * Gets the join code for a specific dorm (useful for showing to dorm members).
     * Note: In a real app, you might want to restrict this to dorm owners/admins.
     *
     * @param dormId The dorm ID
     * @return The join code, or empty string if dorm not found
     */
    public String getDormJoinCode(int dormId) {
        Dorm dorm = dormDAO.findById(dormId);
        return dorm != null ? dorm.getJoin_code() : "";
    }

    /**
     * Wrapper class for dorm operation results.
     * Provides a clean way to return success/failure with optional dorm data.
     */
    public static class DormOperationResult {
        private final boolean success;
        private final String message;
        private final Dorm dorm;

        private DormOperationResult(boolean success, String message, Dorm dorm) {
            this.success = success;
            this.message = message;
            this.dorm = dorm;
        }

        public static DormOperationResult success(String message, Dorm dorm) {
            return new DormOperationResult(true, message, dorm);
        }

        public static DormOperationResult failure(String message) {
            return new DormOperationResult(false, message, null);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public Dorm getDorm() {
            return dorm;
        }

        @Override
        public String toString() {
            return (success ? "✓ " : "✗ ") + message +
                    (dorm != null ? " [Dorm ID: " + dorm.getDorm_id() + ", Code: " + dorm.getJoin_code() + "]" : "");
        }
    }
}
