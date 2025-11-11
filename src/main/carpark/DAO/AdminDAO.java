package DAO;

import Model.Entity.Admin;

import java.sql.*;
import java.util.Optional;

/**
 * AdminDAO provides the Data Access Object interface for the 'admins' table.
 * This class handles Admin profiles which are linked to user accounts.
 */
public class AdminDAO {

    // === SQL QUERIES ===
    private static final String INSERT_ADMIN = "INSERT INTO admins (user_ID, firstname, surname, contact_number, job_title, branch_ID) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String SELECT_BY_USER_ID = "SELECT * FROM admins WHERE userID = ?";
    private static final String UPDATE_ADMIN = "UPDATE admins SET firstname = ?, surname = ?, contact_number = ?, job_title = ?, branch_ID = ? WHERE admin_ID = ?";
    private static final String DELETE_ADMIN = "DELETE FROM admins WHERE admin_ID = ?";

    /**
     * Inserts a new Admin record linked to an existing user_ID.
     *
     * @param admin The Admin object to insert.
     * @return Optional containing the new Admin with generated admin_ID.
     */
    public Optional<Admin> addAdmin(Admin admin) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        int generatedID = -1;

        try {
            conn = DBConnectionUtil.getConnection();
            ps = conn.prepareStatement(INSERT_ADMIN, Statement.RETURN_GENERATED_KEYS);

            ps.setInt(1, admin.getUser_ID());
            ps.setString(2, admin.getFirstname());
            ps.setString(3, admin.getSurname());
            ps.setString(4, admin.getContact_number());
            ps.setString(5, admin.getJob_title());
            ps.setInt(6, admin.getBranch_ID());

            int affectedRows = ps.executeUpdate();

            if (affectedRows > 0) {
                rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    generatedID = rs.getInt(1);
                }
            }

            if (generatedID > 0) {
                Admin newAdmin = new Admin(
                        generatedID,
                        admin.getUser_ID(),
                        admin.getFirstname(),
                        admin.getSurname(),
                        admin.getContact_number(),
                        admin.getJob_title(),
                        admin.getBranch_ID()
                );

                return Optional.of(newAdmin);
            }
        } catch (SQLException e) {
            System.err.println("AdminDAO Error in addAdmin: " + e.getMessage());
        } finally {
            DBConnectionUtil.closeConnection(conn, ps, null);
        }
        return Optional.empty();
    }

    /**
     * Retrieves an Admin profile by its associated user_ID.
     * Used after login to load admin-specific information.
     *
     * @param user_ID The unique identifier of the user account.
     * @return Optional containing the Admin if found, empty Optional otherwise.
     */
    public Optional<Admin> findAdminByUserID(int user_ID) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBConnectionUtil.getConnection();
            ps = conn.prepareStatement(SELECT_BY_USER_ID);
            ps.setInt(1, user_ID);

            rs = ps.executeQuery();

            if (rs.next()) {
                Admin admin = extractAdminFromResultSet(rs);
                return Optional.of(admin);
            }
            return Optional.empty();
        } catch (SQLException e) {
            System.err.println("AdminDAO Error in findAdminByUser" + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        } finally {
            DBConnectionUtil.closeConnection(conn, ps, rs);
        }
    }

    /**
     * Updates an existing admin's profile information.
     *
     * @param admin Admin object with updated fields and existing admin_ID.
     * @return true if update is successful, false otherwise.
     */
    public boolean updateAdmin(Admin admin) {
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = DBConnectionUtil.getConnection();
            ps = conn.prepareStatement(UPDATE_ADMIN);

            ps.setString(1, admin.getFirstname());
            ps.setString(2, admin.getSurname());
            ps.setString(3, admin.getContact_number());
            ps.setString(4, admin.getJob_title());
            ps.setInt(5, admin.getBranch_ID());

            int affectedRows = ps.executeUpdate();

            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("AdminDAO Error in updateAdmin: " + e.getMessage());
            return false;
        } finally {
            DBConnectionUtil.closeConnection(conn, ps, null);
        }
    }

    /**
     * Deletes an existing admin account.
     *
     * @param admin_ID Unique identifier of the admin record.
     * @return true if delete is successful, false otherwise.
     */
    public boolean deleteAdmin(int admin_ID) {
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = DBConnectionUtil.getConnection();
            ps = conn.prepareStatement(DELETE_ADMIN);

            ps.setInt(1, admin_ID);

            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("AdminDAO Error in deleteAdmin: " + e.getMessage());
            return false;
        } finally {
            DBConnectionUtil.closeConnection(conn, ps, null);
        }
    }

    /**
     * Utility method to map a ResultSet row to an Admin entity.
     */
    private Admin extractAdminFromResultSet(ResultSet rs) throws SQLException {
        int adminID = rs.getInt("admin_ID");
        int userID = rs.getInt("user_ID");
        String firstname = rs.getString("firstname");
        String surname = rs.getString("surname");
        String contactNumber = rs.getString("contact_number");
        String jobTitle = rs.getString("job_title");
        int branchID = rs.getInt("branch_ID");

        return new Admin(adminID, userID, firstname, surname, contactNumber, jobTitle, branchID);
    }

}
