package doboard.core.features.billing;

public class BillSplit {
    private int split_id;
    private int bill_id;
    private int user_id;

    public BillSplit(int split_id, int bill_id, int user_id) {
        this.split_id = split_id;
        this.bill_id = bill_id;
        this.user_id = user_id;
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
}
