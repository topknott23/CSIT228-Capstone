package doboard.core.features.dorm;

public class DormMember {
    private int dorm_id;
    private int user_id;
    private Role role;

    public enum Role  {
        ADMIN,
        RESIDENT
    }

    public DormMember(int dorm_id, int user_id, Role role) {
        this.dorm_id = dorm_id;
        this.user_id = user_id;
        this.role = role;
    }


    //GETTERS
    public int getDorm_id() {
        return dorm_id;
    }

    public int getUser_id() {
        return user_id;
    }

    public Role getRole() {
        return role;
    }


    //SETTERS
    public void setDorm_id(int dorm_id) {
        this.dorm_id = dorm_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}

