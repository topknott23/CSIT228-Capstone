package doboard.core.common.session;

import doboard.core.features.auth.User;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class SessionHandler {
    public static final String FILENAME = "user_sessior.ser";

    public static void saveSession(User user) {
        try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILENAME))) {
            oos.writeObject(user);
            System.out.println("Saved session of user: " + user.getUsername());
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}