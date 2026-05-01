package doboard.core.features.chores;

public class ChoreAssignment {
    private int chore_id;
    private int user_id;

    public ChoreAssignment(int chore_id, int user_id) {
        this.chore_id = chore_id;
        this.user_id = user_id;
    }


    //GETTERS
    public int getChore_id() {
        return chore_id;
    }

    public int getUser_id() {
        return user_id;
    }


    //SETTERS
    public void setChore_id(int chore_id) {
        this.chore_id = chore_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }
}
