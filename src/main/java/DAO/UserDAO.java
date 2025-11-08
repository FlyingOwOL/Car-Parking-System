package main.java.DAO;

import main.java.Model.Entity.User;
import main.java.Model.Entity.UserRole;

import java.sql.*;
import java.time.LocalDate;
import java.util.Optional;

public class UserDAO {

    // === SQL QUERIES ===
    private static final String SELECT_USER_BY_EMAIL = "SELECT user_ID, email, password_hash, role, join_date FROM users WHERE email = ?";
    private static final String INSERT_USER = "INSERT INTO users (email, password_hash, role, join_date) VALUES (?, ?, ?, ?)";

    /**
     * Retrieves a User record from the database using their email address.
     * This is the first step in the login process.
     *
     * @param email The unique email address of the user.
     * @return An Optional containing the User object if found, otherwise empty.
     */
    public Optional<User> selectUserByEmail(String email) {
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
}
