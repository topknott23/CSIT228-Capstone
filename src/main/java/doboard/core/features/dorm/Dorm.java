package doboard.core.features.dorm;
import java.time.Instant;


public class Dorm {
    private final int dorm_id;
    private String dorm_name;
    private String join_code;
    private final Instant created_at;

    public Dorm(int dorm_id, String dorm_name, String join_code) {
        this.dorm_id = dorm_id;
        this.dorm_name = dorm_name;
        this.join_code = join_code;
        this.created_at = Instant.now();
    }


    //GETTERS
    public int getDorm_id() {
        return dorm_id;
    }

    public String getDorm_name() {
        return dorm_name;
    }

    public String getJoin_code() {
        return join_code;
    }

    public Instant getCreated_at() {
        return created_at;
    }


    //SETTERS
    public void setDorm_name(String dorm_name) {
        this.dorm_name = dorm_name;
    }

    public void setJoin_code(String join_code) {
        this.join_code = join_code;
    }
}
