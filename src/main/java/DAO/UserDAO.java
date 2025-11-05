package main.java.DAO;

public class UserDAO {

    // === SQL QUERIES ===
    private static final String SELECT_USER_BY_EMAIL = "SELECT user_ID, email, password_hash, role, join_date FROM users WHERE email = ?";
    private static final String INSERT_USER = "INSERT INTO users (email, password_hash, role, join_date) VALUES (?, ?, ?, ?)";
    //add ka lng kyle here

    //TODO: RUBIA
    //Handles user lookups (login) and base user creation
}
