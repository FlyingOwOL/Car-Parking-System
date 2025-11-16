package Service.Admin;

import DAO.AdminDAO;
import DAO.DBConnectionUtil;
import DAO.UserDAO;
import Model.Entity.Admin;
import Model.Entity.User;
import Model.Entity.UserRole;
import Service.UserService;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;

public class AdminService {
    private UserDAO userDAO;
    private AdminDAO adminDAO;
    private UserService userService;

    public AdminService() {
        this.userDAO = new UserDAO();
        this.adminDAO = new AdminDAO();
        this.userService = new UserService();
    }

    public boolean createNewAdmin(User callingAdmin, String email, String password, Admin profile) {
        if (!userService.isAdmin(callingAdmin)) {
            System.err.println("SECURITY: Non-admin user attempted to create a new admin");
            return false;
        }

        if (userDAO.findUserByEmail(email).isPresent()) {
            System.err.println("AdminService Email already in use");
            return false;
        }

        String hashedPassword = password;

        User newUser = new User();
        newUser.setEmail(email);
        newUser.setPassword_hash(hashedPassword);
        newUser.setRole(UserRole.ADMIN);
        newUser.setJoin_date(LocalDate.now());

        Connection conn = null;
        Optional<User> newUserOpt;
        try {
            conn = DBConnectionUtil.getConnection();
            conn.setAutoCommit(false);

            newUserOpt = userDAO.addUser(newUser);
            if (newUserOpt.isPresent()) {
                int newUserID = newUserOpt.get().getUser_ID(); // Extract the newly generated ID

                profile.setUser_ID(newUserID);
                Optional<Admin> profileCreated = adminDAO.addAdmin(profile);

                if (profileCreated.isPresent()) {
                    conn.commit();
                    return true;
                }
            }

            conn.rollback();
            return false;
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException rollbackEx) {
                System.err.println("AdminService Error in rollback: " + e.getMessage());
            }
            System.err.println("AdminService Error in addUser: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("AdminService Error in addUser: " + e.getMessage());
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    DBConnectionUtil.closeConnection(conn);
                }
            } catch (SQLException e) {
                System.err.println("AdminService Error in addUser: " + e.getMessage());
            }
        }
    }

    public Optional<Admin> getAdminProfile(User adminUser) {
        if (!userService.isAdmin(adminUser)) {
            return Optional.empty();
        }
        return adminDAO.findAdminByUserID(adminUser.getUser_ID());
    }
}
