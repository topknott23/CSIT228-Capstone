package doboard.expenses;

import doboard.dorm.DormMember;
import doboard.dorm.DormMemberDAO;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ExpenseService {
    private final BillDAO billDAO = new BillDAO();
    private final BillSplitDAO billSplitDAO = new BillSplitDAO();
    private final DormMemberDAO dormMemberDAO = new DormMemberDAO();

    public record UserBalanceSummary(double totalBalance, List<String> alerts) {}

    public int getDormIdForUser(int userId) {
        return dormMemberDAO.getDormIdByUserId(userId);
    }
    public List<Bill> getDormBills(int dormId) {
        return billDAO.findByDormId(dormId);
    }
    public List<BillSplit> getSplitsForBill(int billId) {
        return billSplitDAO.findByBillId(billId);
    }
    public boolean updateSplitStatus(int splitId, boolean isPaid) {
        return billSplitDAO.updateStatus(splitId, isPaid);
    }

    public boolean processBillSplit(int dormId, String purpose, double amount) {
        Bill newBill = new Bill(0, dormId, purpose, amount, LocalDate.now().plusDays(7));
        int insertedBillId = billDAO.insertAndGetId(newBill);

        if (insertedBillId == -1) return false;

        List<DormMember> members = dormMemberDAO.getMembersByDorm(dormId);
        if (!members.isEmpty()) {
            double splitAmount = amount / members.size();
            for (DormMember member : members) {
                BillSplit split = new BillSplit(0, insertedBillId, member.getUser_id(), splitAmount, false);
                billSplitDAO.insert(split);
            }
        }
        return true;
    }

    public UserBalanceSummary getUserBalanceDetails(int dormId, int userId) {
        double totalBalance = 0.0;
        List<String> alerts = new ArrayList<>();
        List<Bill> dormBills = billDAO.findByDormId(dormId);

        for (Bill bill : dormBills) {
            List<BillSplit> splits = billSplitDAO.findByBillId(bill.getBill_id());
            for (BillSplit split : splits) {
                if (split.getUser_id() == userId && !split.isPaid()) {
                    totalBalance += split.getAmount();
                    alerts.add("Due: " + bill.getTitle() + " - ₱" + String.format("%.2f", split.getAmount()));
                }
            }
        }
        return new UserBalanceSummary(totalBalance, alerts);
    }
}