package main.java.Service;

import main.java.DAO.UserDAO;
import main.java.Model.Entity.User;
import main.java.Model.Entity.UserRole;

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

    private boolean verifyPassword(String password, String storedPassword) {
        return storedPassword.equals(password);
    }

    private boolean isAdmin(User user) {
        return user != null && user.getRole() == UserRole.ADMIN;
    }

}
