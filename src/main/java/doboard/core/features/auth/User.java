package doboard.core.features.auth;

import java.io.Serializable;
import java.time.Instant;

public class User implements Serializable {
    private final int user_id;
    private String username;
    private String email;
    private transient String password;
    private final Instant created_at; //Timestamp

    public User(int user_id, String username, String email, String password) {
        this.user_id = user_id;
        this.username = username;
        this.email = email;
        this.password = password;
        created_at = Instant.now();
    }


    //GETTERS
    public int getUser_id() {
        return user_id;
    }

    public Instant getCreated_at() {
        return created_at;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }


    //SETTERS
    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}