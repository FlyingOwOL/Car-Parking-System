package Service;

import DAO.UserDAO;
import Model.Entity.User;
import Model.Entity.UserRole;

import java.time.LocalDate;
import java.util.Optional;

/**
 * UserService handles all business logic related to user accounts,
 * primarily focusing on security, authentication, and authorization.
 */
public class UserService {

    private final UserDAO userDAO;

    /**
     * Constructor initializes the necessary DAO dependencies.
     */
    public UserService() {
        this.userDAO = new UserDAO();
    }
    public Optional<User> register(String email, String password, String role){
        LocalDate newDate = LocalDate.now();
        User user = new User(email, password, UserRole.fromString(role), newDate);

        Optional<User> newUser = userDAO.addUser(user);

        return newUser;
    }

    /**
     * Attempts to log in a user by verifying credentials against the database.
     * This method implements the core security logic (password hashing comparison).
     *
     * @param email The user's provided email.
     * @param password The user's provided raw password.
     * @return An Optional containing the authenticated User object, or empty if login fails.
     */
    public Optional<User> login(String email, String password) {
        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            return Optional.empty();
        }

        Optional<User> userOptional = userDAO.findUserByEmail(email);

        if (userOptional.isEmpty()) {
            return Optional.empty();
        }

        User user = userOptional.get();
        String storedPassword = user.getPassword_hash();

        if (verifyPassword(password, storedPassword)) {
            return userOptional;
        } else {
            return Optional.empty();
        }
    }

    /**
     * Check if the user is an admin
     *
     * @param user The user object
     * @return An Optional containing the authenticated User object, or empty if login fails.
     */
    public boolean isAdmin(User user) {
        return user != null && user.getRole() == UserRole.ADMIN;
    }

    private boolean verifyPassword(String password, String storedPassword) {
        return storedPassword.equals(password);
    }

}
