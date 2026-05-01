package doboard.core.features.auth;

import java.io.Serializable;
import java.time.Instant;

public class User implements Serializable {
    private final int user_id;
    private String username;
    private String email;
    private String full_name;
    private transient String password;
    private final Instant created_at; //Timestamp

    public User(int user_id, String username, String email, String password) {
        this.user_id = user_id;
        this.username = username;
        this.email = email;
        this.password = password;
        created_at = Instant.now();
    }

    //This constructor should be only called when logging in an existing user, see Authenticator.Login
    public User() {
        this.user_id = -1;
        this.username = null;
        this.email = null;
        this.password = null;
        this.created_at = null;
    }


    //GETTERS
    public int getUser_id() {
        return user_id;
    }

    public long getCreated_at() {
        return created_at.getEpochSecond();
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getFull_name() {
        return full_name;
    }


    //SETTERS
    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setFull_name(String full_name) {
        this.full_name = full_name;
    }
}