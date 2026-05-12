package doboard.dorm;

import doboard.auth.User;
import doboard.common.connection.SQLConnector;
import doboard.common.util.JoinCodeGenerator;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DormMemberDAO {

    /**
     * Joins a user to a dorm using a join code.
     * Validates the code and checks if user is already in a dorm.
     *
     * @param joinCode The join code to use
     * @param user The user joining the dorm
     * @return true if successfully joined, false otherwise
     */
    public boolean joinDorm(String joinCode, User user) {
        // Check if user is already in a dorm
        if (isUserInDorm(user.getUser_id())) {
            System.err.println("User is already in a dorm");
            return false;
        }

        DormDAO dormDAO = new DormDAO();
        
        // Validate join code format and existence
        if (!dormDAO.isValidJoinCode(joinCode)) {
            System.err.println("Invalid join code: " + joinCode);
            return false;
        }

        Dorm retrieveDorm = dormDAO.findByJoinCode(joinCode);
        if (retrieveDorm == null) {
            System.err.println("Dorm not found for code: " + joinCode);
            return false;
        }

        DormMember member = new DormMember(
                retrieveDorm.getDorm_id(),
                user.getUser_id(),
                DormMember.Role.MEMBER
        );

        return addMember(member);
    }

    /**
     * Creates a new dorm and adds the creator as the owner.
     *
     * @param dormName The name of the dorm
     * @param creatorUser The user creating the dorm (will be the owner)
     * @return The created Dorm object, or null if creation failed
     */
    public Dorm createDormAsOwner(String dormName, User creatorUser) {
        // Check if user is already in a dorm
        if (isUserInDorm(creatorUser.getUser_id())) {
            System.err.println("User is already in a dorm");
            return null;
        }

        DormDAO dormDAO = new DormDAO();
        Dorm createdDorm = dormDAO.createDormWithCode(dormName);

        if (createdDorm == null) {
            System.err.println("Failed to create dorm");
            return null;
        }

        // Add creator as owner
        DormMember ownerMember = new DormMember(
                createdDorm.getDorm_id(),
                creatorUser.getUser_id(),
                DormMember.Role.ADMIN
        );

        if (addMember(ownerMember)) {
            return createdDorm;
        } else {
            System.err.println("Failed to add owner to dorm");
            // Note: In production, you might want to rollback the dorm creation here
            return null;
        }
    }

    public boolean addMember(DormMember member){
        String query = "INSERT INTO dorm_members(dorm_id, user_id, role) VALUES(?, ?, ?)";
        System.out.println("ADDING " + member.getUser_id());
        System.out.println("ADDING " + member.getDorm_id());
        System.out.println("ADDING " + member.getRole().name());
        try(Connection c = SQLConnector.getConnection();
             PreparedStatement ps = c.prepareStatement(query)){
            ps.setInt(1, member.getDorm_id());
            ps.setInt(2, member.getUser_id());
            ps.setString(3, member.getRole().name());
            return ps.executeUpdate() > 0;
        }catch(SQLException e){
            e.printStackTrace();
        }
        return false;
    }

    public List<DormMember> getMembersByDorm(int dormId) {
        List<DormMember> members = new ArrayList<>();
        String query = "SELECT * FROM dorm_members WHERE dorm_id = ?";
        try (Connection c = SQLConnector.getConnection();
             PreparedStatement s = c.prepareStatement(query)) {
            s.setInt(1, dormId);
            ResultSet r = s.executeQuery();
            while (r.next()) {
                members.add(new DormMember(
                        r.getInt("dorm_id"),
                        r.getInt("user_id"),
                        DormMember.Role.valueOf(r.getString("role").toUpperCase())
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return members;
    }
        //ASDASDASDASD
    public static int getDormIdByUserId(int userId) {
        String query = "SELECT dorm_id FROM dorm_members WHERE user_id = ?";
        try (Connection c = SQLConnector.getConnection();
             PreparedStatement s = c.prepareStatement(query)) {
            s.setInt(1, userId);
            ResultSet r = s.executeQuery();
            if (r.next()) {
                return r.getInt("dorm_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Checks if a user is already a member of any dorm.
     *
     * @param userId The user ID to check
     * @return true if the user is in a dorm, false otherwise
     */
    public static boolean isUserInDorm(int userId) {
        return getDormIdByUserId(userId) != -1;
    }
}
