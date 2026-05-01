package doboard.core.common.session;

import doboard.core.features.auth.User;

import java.io.*;

public class SessionHandler {
    public static final String FILENAME = "user_session.ser";

    public static void saveSession(User user) {
        try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILENAME))) {
            oos.writeObject(user);
            System.out.println("Saved session of user: " + user.getUsername());
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public static void endSession() {
        File file = new File(FILENAME);
        if (file.exists()) {
            if (file.delete()) {
                System.out.println("Session ended: Deleted file");
            } else {
                System.err.println("Could not delete session file.");
            }
        }
    }

    public static User loadSession() {
        File file = new File(FILENAME);
        if (!file.exists()) {
            return null;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (User) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            file.delete();
            return null;
        }
    }
}