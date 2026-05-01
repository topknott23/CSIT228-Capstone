package doboard.core.features.auth;

public class Authenticator {

    private static final UserDAO userDAO = new UserDAO();

    public static User Login(String username, String password) {
        User user = userDAO.findByUsernameAndPassword(username, password);

        if (user != null) {
            System.out.println("User logged in: " + user.getFull_name());
        }

        System.out.println("Error nigga i love you sarah");
        // If this method returns null it means incorrect password @topknott23
        // hahaha sige
        return user;
    }

    public static void Register(User user) {
        boolean success = userDAO.insert(user);

        if (!success) {
            System.out.printf("Register failed!\n");
        } else {
            System.out.printf("Register successful! Registered: " + user.getFull_name() + "\n");
        }
    }
}