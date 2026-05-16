package doboard.chores;

import doboard.auth.User;
import doboard.auth.UserDAO;
import doboard.dorm.DormMemberDAO;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ChoreService {
    private final ChoreDAO choreDAO = new ChoreDAO();
    private final ChoreAssignmentDAO choreAssignmentDAO = new ChoreAssignmentDAO();
    private final DormMemberDAO dormMemberDAO = new DormMemberDAO();

    public record LeaderboardEntry(String username, int completionCount){}

    public List<Chore> getAllDormChores(int dormId) {
        return choreDAO.findAllByDormId(dormId);
    }

    public int getDormIdForUser(int userId){
        return dormMemberDAO.getDormIdByUserId(userId);
    }

    public List<User> getDormUsers(int dormId){
        List<Integer> userIds = dormMemberDAO.getMembersByDorm(dormId)
                .stream()
                .map(doboard.dorm.DormMember::getUser_id)
                .collect(Collectors.toList());
        return UserDAO.getUsersByIds(userIds);
    }

    public List<LeaderboardEntry> getSortedLeaderboard(int dormId){
        Map<Integer, Integer> completionCounts = choreDAO.getChoreCompletionCounts(dormId);
        List<User> dormUsers = UserDAO.getUsersByIds(List.copyOf(completionCounts.keySet()));

        return dormUsers.stream()
                .sorted((u1, u2) -> completionCounts.get(u2.getUser_id()) - completionCounts.get(u1.getUser_id()))
                .map(user -> new LeaderboardEntry(
                        user.getUsername(),
                        completionCounts.get(user.getUser_id())
                ))
                .collect(Collectors.toList());
    }

    public void completeAndRotateChore(Chore chore) {
        choreDAO.updateStatus(chore.getChore_id(), Chore.Status.COMPLETE);

        if (chore.getFrequency() != doboard.common.enums.Frequency.ONCE) {
            LocalDate nextDueDate = chore.getDue_date();

            switch (chore.getFrequency()) {
                case DAILY -> nextDueDate = nextDueDate.plusDays(1);
                case WEEKLY -> nextDueDate = nextDueDate.plusWeeks(1);
                case MONTHLY -> nextDueDate = nextDueDate.plusMonths(1);
            }

            while (!nextDueDate.isAfter(LocalDate.now())) {
                switch (chore.getFrequency()) {
                    case DAILY -> nextDueDate = nextDueDate.plusDays(1);
                    case WEEKLY -> nextDueDate = nextDueDate.plusWeeks(1);
                    case MONTHLY -> nextDueDate = nextDueDate.plusMonths(1);
                }
            }

            Chore nextChore = new Chore(0, chore.getDorm_id(), chore.getTitle(), chore.getDescription(), chore.getFrequency(), nextDueDate, Chore.Status.PENDING);
            int newChoreId = choreDAO.insertAndReturnId(nextChore);

            if (newChoreId != -1) {
                List<Integer> assignedUserIds = choreAssignmentDAO.getUserIdsByChore(chore.getChore_id());
                for (int userId : assignedUserIds) {
                    choreAssignmentDAO.assign(new ChoreAssignment(newChoreId, userId));
                }
            }
        }
    }
}