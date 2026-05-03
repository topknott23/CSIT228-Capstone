package doboard.dorm;

import doboard.auth.User;
import doboard.common.connection.SQLConnector;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DormMemberDAO {

    public boolean joinDorm(String joinCode, User user){
        DormDAO dormDAO = new DormDAO();
        Dorm retrieveDorm = dormDAO.findByJoinCode(joinCode);

        if(retrieveDorm == null) return false;

        DormMember member = new DormMember(
                retrieveDorm.getDorm_id(),
                user.getUser_id(),
                DormMember.Role.RESIDENT
        );

        return addMember(member);
    }

    public boolean addMember(DormMember member){
        String query = "INSERT INTO dorm_members(dorm_id, user_id, role) VALUES(?, ?, ?)";
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
                        DormMember.Role.valueOf(r.getString("role"))
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return members;
    }
}
