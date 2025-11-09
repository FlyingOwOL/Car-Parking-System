package Model.Entity;

import java.time.LocalDate;

/**
 * Maps to the 'users' table in the database.
 * Represents a system user account with authentication credentials and role assignment.
 */
public class User {
    private int user_ID;
    private String email;
    private String password_hash;
    private UserRole role;
    private LocalDate join_date;

    // === CONSTRUCTORS ===

    /**
     * Default constructor
     */
    public User() {}

    /**
     * Constructor for existing users retrieved from the database
     * @param user_ID The user's unique identifier
     * @param email The user's email address
     * @param password_hash The user's hashed password for authentication
     * @param role The user's role (Customer or Admin)
     * @param join_date The date when the user registered
     */
    public User(int user_ID, String email, String password_hash, UserRole role, LocalDate join_date) {
        this.user_ID = user_ID;
        this.email = email;
        this.password_hash = password_hash;
        this.role = role;
        this.join_date = join_date;
    }

    /**
     * Constructor for new users
     * @param email The user's email address
     * @param password_hash The user's hashed password for authentication
     * @param role The user's role (Customer or Admin)
     * @param join_date The date when the user registered
     */
    public User(String email, String password_hash, UserRole role, LocalDate join_date) {
        this.email = email;
        this.password_hash = password_hash;
        this.role = role;
        this.join_date = join_date;
    }

    // === GETTERS and SETTERS ===

    public  int getUser_ID() {return user_ID;}
    public void setUser_ID(int user_ID) {this.user_ID = user_ID;}

    public String getEmail() {return email;}
    public void setEmail(String email) {this.email = email;}

    public String getPassword_hash() {return password_hash;}
    public void setPassword_hash(String password_hash) {this.password_hash = password_hash;}

    public UserRole getRole() {return role;}
    public void setRole(UserRole role) {this.role = role;}

    public LocalDate getJoin_date() {return join_date;}
    public void setJoin_date(LocalDate join_date) {this.join_date = join_date;}

    @Override
    public String toString() {
        return "User{" +
                "User ID: " + user_ID +
                ", Email: " + email +
                ", Role: " + role +
                ", Date joined: " + join_date + '}';
    }
}