package Utilities;

import Model.Entity.User;
import Model.Entity.UserRole;

/**
 * SessionManager is a static utility class responsible for holding the state
 * of the currently logged-in user (the session).
 * * This ensures that the user's ID and role are available globally
 * for authorization checks in the Controller and Service layers.
 */
public class SessionManager {

    private static User currentUser;

    /**
     * Initializes the session after successful authentication.
     * @param user The authenticated User object retrieved from UserService.login().
     */
    public static void login(User user) {
        if (user != null) {
            currentUser = user;
            System.out.println("SessionManager: User logged in. ID: " + user.getUser_ID() + ", Role: " + user.getRole());
        }
    }

    /**
     * Terminates the session (logs the user out).
     */
    public static void logout() {
        if (currentUser != null) {
            System.out.println("SessionManager: User logged out. ID: " + currentUser.getUser_ID());
        }
    }

    /**
     * Checks if a user is currently logged into the system.
     * @return true if currentUser is not null, false otherwise.
     */
    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * Retrieves the currently logged-in User object.
     * Controllers should check isLoggedIn() before calling this.
     * @return The User object, or null if no session exists.
     */
    public static User getCurrentUser() {
        return currentUser;
    }

    /**
     * Retrieves the role of the currently logged-in user.
     * @return The UserRole enum (ADMIN or CUSTOMER), or null if not logged in.
     */
    public static UserRole getCurrentUserRole() {
        if (currentUser != null) {
            return currentUser.getRole();
        }
        return null;
    }

    /**
     * Quick check to see if the current user is an Admin.
     * @return true if the current user has the ADMIN role.
     */
    public static boolean isAdmin() {
        return currentUser != null && currentUser.getRole() == UserRole.ADMIN;
    }
}
