package doboard.core.features.dorm.DAO.Dorm_Member;

import doboard.core.features.dorm.DormMember;
import doboard.core.common.connection.SQLConnector;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DormMemberDAO {

    public boolean addMember(DormMember member) {
        String query = "INSERT INTO dorm_members(dorm_id, user_id, role) VALUES(?, ?, ?)";
        try (Connection c = SQLConnector.getConnection();
             PreparedStatement s = c.prepareStatement(query)) {
            s.setInt(1, member.getDorm_id());
            s.setInt(2, member.getUser_id());
            s.setString(3, member.getRole().name());
            return s.executeUpdate() > 0;
        } catch (SQLException e) {
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

    public boolean removeMember(int dormId, int userId) {
        String query = "DELETE FROM dorm_members WHERE dorm_id = ? AND user_id = ?";
        try (Connection c = SQLConnector.getConnection();
             PreparedStatement s = c.prepareStatement(query)) {
            s.setInt(1, dormId);
            s.setInt(2, userId);
            return s.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
