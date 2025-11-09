package DAO;

import Model.Entity.User;
import Model.Entity.UserRole;

import java.sql.*;
import java.time.LocalDate;
import java.util.Optional;

/**
 * UserDAO is responsible for all data access operations related to the 'users' table.
 * This includes authentication lookup and inserting new base user records.
 */
public class UserDAO {

    // === SQL QUERIES ===
    private static final String SELECT_USER_BY_EMAIL = "SELECT * FROM users WHERE email = ?";
    private static final String INSERT_USER = "INSERT INTO users (email, password_hash, role, join_date) VALUES (?, ?, ?, ?)";

    /**
     * Retrieves a User record from the database using their email address.
     * This is the first step in the login process.
     *
     * @param email The unique email address of the user.
     * @return An Optional containing the User object if found, otherwise empty.
     */
    public Optional<User> findUserByEmail(String email) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBConnectionUtil.getConnection();
            ps = conn.prepareStatement(SELECT_USER_BY_EMAIL);
            ps.setString(1, email);

            rs = ps.executeQuery();

            if (rs.next()) {
                int userID = rs.getInt("user_ID");
                String dbEmail = rs.getString("email");
                String passwordHash = rs.getString("password_hash");
                UserRole role = UserRole.valueOf(rs.getString("role").toUpperCase());
                LocalDate joinDate = rs.getObject("join_date", LocalDate.class);

                User user = new User(userID, dbEmail, passwordHash, role, joinDate);
                return Optional.of(user);
            }
            return Optional.empty();
        } catch (SQLException e) {
            System.err.println("UserDAO Error in findUserByEmail: " + e.getMessage());
            return Optional.empty();
        } finally {
            DBConnectionUtil.closeConnection(conn, ps, rs);
        }
    }

    /**
     * Inserts a new User record into the database during registration.
     *
     * @param user The User object containing email, password hash, and role.
     * @return The generated user_ID if successful, otherwise -1.
     */
    public Optional<User> addUser(User user) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        int generatedID = -1;

        try {
            conn = DBConnectionUtil.getConnection();
            ps = conn.prepareStatement(INSERT_USER, Statement.RETURN_GENERATED_KEYS);

            ps.setString(1, user.getEmail());
            ps.setString(2, user.getPassword_hash());
            ps.setString(3, user.getRole().name());
            ps.setObject(4, user.getJoin_date());

            int affectedRows = ps.executeUpdate();

            if (affectedRows > 0) {
                rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    generatedID = rs.getInt(1);
                }
            }

            if (generatedID > 0) {
                User newUser = new User(
                        generatedID,
                        user.getEmail(),
                        user.getPassword_hash(),
                        user.getRole(),
                        user.getJoin_date()
                );
                return Optional.of(newUser);
            }

        } catch (SQLException e) {
            System.err.println("UserDAO Error in addUser: " + e.getMessage());
        } finally {
            DBConnectionUtil.closeConnection(conn, ps, rs);
        }
        return Optional.empty();
    }
}