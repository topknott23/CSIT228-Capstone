package doboard.signals;

import doboard.auth.User;
import doboard.auth.UserDAO;
import doboard.dorm.DormDAO;
import doboard.dorm.DormMember;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SignalService {
    private final DormDAO dormDAO = new DormDAO();
    private final SignalDAO signalDAO = new SignalDAO();

    public int getDormIdForUser(int userId) {
        return dormDAO.getDormIdByUserId(userId);
    }

    /**
     * Aggregates other dorm members and resolves their user profiles across package boundaries
     */
    public Map<String, Integer> getDormmatesMap(int currentUserId, int dormId) {
        Map<String, Integer> tenantMap = new HashMap<>();
        List<DormMember> members = dormDAO.getMembersByDorm(dormId);

        List<Integer> userIds = members.stream()
                .map(DormMember::getUser_id)
                .filter(id -> id != currentUserId)
                .toList();

        List<User> users = UserDAO.getUsersByIds(userIds);
        for (User u : users) {
            tenantMap.put(u.getUsername(), u.getUser_id());
        }
        return tenantMap;
    }

    public void saveDndStatus(int userId, int dormId, String reason, int hours) {
        signalDAO.saveDndStatus(userId, dormId, reason, hours);
    }

    public boolean sendSignal(int senderId, int receiverId, int dormId, String complaint, int nudges) {
        return signalDAO.insertSignal(senderId, receiverId, dormId, complaint, nudges);
    }

    public List<SignalDAO.Signal> getRecentSignals(int userId, int dormId){
        return signalDAO.getSignalsForUser(userId, dormId);
    }
}