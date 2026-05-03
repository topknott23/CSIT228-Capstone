package doboard.billing;

public class BillSplit {
    private int split_id;
    private int bill_id;
    private int user_id;
    private double amount;
    private boolean isPaid;

    public BillSplit(int split_id, int bill_id, int user_id, double amount, boolean isPaid) {
        this.split_id = split_id;
        this.bill_id = bill_id;
        this.user_id = user_id;
        this.amount = amount;
        this.isPaid = isPaid;
    }

    //GETTERS
    public int getSplit_id() {
        return split_id;
    }

    public int getBill_id() {
        return bill_id;
    }

    public int getUser_id() {
        return user_id;
    }

    public double getAmount() {
        return amount;
    }

    public boolean isPaid() {
        return isPaid;
    }


    //SETTERS
    public void setSplit_id(int split_id) {
        this.split_id = split_id;
    }

    public void setBill_id(int bill_id) {
        this.bill_id = bill_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setPaid(boolean paid) {
        isPaid = paid;
    }
}
