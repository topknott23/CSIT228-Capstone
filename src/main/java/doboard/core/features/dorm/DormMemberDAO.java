package doboard.core.features.dorm;

import doboard.core.common.connection.SQLConnector;
import doboard.core.features.auth.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static doboard.core.features.dorm.DormDAO.findDorm;

public class DormMemberDAO {
    //The user passed should be an already authenticated user i.e. from login or from a previous session
    public static boolean joinDorm(String join_code, User user) {
        Dorm retrievedDorm = findDorm(join_code);
        if(retrievedDorm == null) {
            System.out.println("Invalid dorm code!");
            return false;
        }
        //Every user who joins a dorm is automatically set as a resident
        DormMember dormMember = new DormMember(retrievedDorm.getDorm_id(), user.getUser_id(), DormMember.Role.RESIDENT);
        if(dormMemberAlreadyExists(dormMember)) {
            System.out.println(user.getFull_name() + " already exists in a different dorm!");
            return false;
        }
        addDormMember(dormMember);
        System.out.println(user.getFull_name() + " joined dorm: " + retrievedDorm.getDorm_name());
        return true;
    }


    //This method should not be accessed anywhere else lol
    private static boolean addDormMember(DormMember dormMember) {
        String query  = "INSERT INTO dorm_members (dorm_id, user_id, role) VALUES (?, ?, ?) ";
        try(Connection c = SQLConnector.getConnection();
            PreparedStatement s = c.prepareStatement(query)) {

            s.setInt(1, dormMember.getDorm_id());
            s.setInt(2, dormMember.getUser_id());
            s.setString(3, dormMember.getRole().toString());

            int affects = s.executeUpdate();
            if(affects > 0) {
                System.out.println("Added dorm member: " + dormMember.getUser_id());
                return true;
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    //This method should not be accessed anywhere else lol
    private static boolean dormMemberAlreadyExists(DormMember dormMember) {
        String query = "SELECT * FROM dorm_member WHERE dorm_id = ? AND user_id = ?";
        try(Connection c = SQLConnector.getConnection();
            PreparedStatement s = c.prepareStatement(query)) {

            s.setInt(1, dormMember.getDorm_id());
            s.setInt(2, dormMember.getUser_id());
            ResultSet r = s.executeQuery();
            if(r.next()) {
                DormMember retrievedDorm = new DormMember(
                        r.getInt("dorm_id"),
                        r.getInt("user_id"),

                        //Dummy value:
                        DormMember.Role.RESIDENT
                );
                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
