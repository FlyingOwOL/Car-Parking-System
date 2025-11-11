package DAO;

import Model.Entity.Customer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

/**
 * CustomerDAO handles data access for the 'customers' table.
 * This class links customer profiles to existing user accounts.
 */
public class CustomerDAO {

    // === SQL QUERIES ===
    private static final String INSERT_CUSTOMER = "INSERT INTO customers (user_ID, firstname, surname, contact_number) VALUES (?, ?, ?, ?)";
    private static final String SELECT_CUSTOMER_BY_USER_ID = "SELECT * FROM customers WHERE user_ID = ?";
    private static final String SELECT_BY_CUSTOMER_ID = "SELECT * FROM customers WHERE customer_ID = ?";
    private static final String UPDATE_CUSTOMER = "UPDATE customers SET firstname = ?, surname = ?, contact_number = ? WHERE customer_ID = ?";
    private static final String DELETE_CUSTOMER = "DELETE from customers WHERE customer_ID = ?";

    /**
     * Retrieves a customer profile by its associated user_ID.
     * Used after login to load customer-specific information.
     *
     * @param user_ID The unique identifier of the user account
     * @return Optional containing the Customer if found, empty Optional otherwise
     */
    public Optional<Customer> findCustomerByUserID(int user_ID) {
        return findSingleCustomer(SELECT_CUSTOMER_BY_USER_ID, user_ID, "findCustomerByUserID");
    }

    /**
     * Retrieves a customer profile by customer_ID.
     * Useful when referencing specific customers in admin views.
     *
     * @param customer_ID The unique identifier of the customer record
     * @return Optional containing the Customer if found, empty Optional otherwise
     */
    public Optional<Customer> findCustomerByCustomerID(int customer_ID) {
        return findSingleCustomer(SELECT_BY_CUSTOMER_ID, customer_ID, "findCustomerByCustomerID");
    }

    /**
     * Shared helper method to retrieve a single Customer.
     *
     * @param query The SQL SELECT query to execute
     * @param id The ID to bind the id (either user_ID or customer_ID)
     * @param func The name of the calling function
     * @return Optional containing the Customer if found, empty Optional otherwise
     */
    private Optional<Customer> findSingleCustomer(String query, int id, String func) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBConnectionUtil.getConnection();
            ps = conn.prepareStatement(query);
            ps.setInt(1, id);

            rs = ps.executeQuery();

            if (rs.next()) {
                Customer customer = extractCustomerFromResultSet(rs);
                return Optional.of(customer);
            }
            return Optional.empty();
        } catch (SQLException e) {
            System.err.println("CustomerDAO Error in " + func + ": " + e.getMessage());
            return Optional.empty();
        } finally {
            DBConnectionUtil.closeConnection(conn, ps, rs);
        }
    }

    /**
     * Inserts a new Customer record linked to an existing user_ID.
     *
     * @param customer The Customer object to insert (must have user_ID set).
     * @return Optional containing the new Customer with generated customer_ID.
     */
    public Optional<Customer> addCustomer(Customer customer) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        int generatedID = -1;

        try {
            conn = DBConnectionUtil.getConnection();
            ps = conn.prepareStatement(INSERT_CUSTOMER, Statement.RETURN_GENERATED_KEYS);

            ps.setInt(1, customer.getUser_ID());
            ps.setString(2, customer.getFirstname());
            ps.setString(3, customer.getSurname());
            ps.setString(4, customer.getContact_number());

            int affectedRows = ps.executeUpdate();

            if (affectedRows > 0) {
                rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    generatedID = rs.getInt(1);
                }
            }

            if (generatedID > 0) {
                Customer newCustomer = new Customer(
                        generatedID,
                        customer.getUser_ID(),
                        customer.getFirstname(),
                        customer.getSurname(),
                        customer.getContact_number()
                );

                return Optional.of(newCustomer);
            }
        } catch (SQLException e) {
            System.err.println("CustomerDAO Error in addCustomer: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBConnectionUtil.closeConnection(conn, ps, rs);
        }
        return Optional.empty();
    }

    /**
     * Updates an existing customer's profile information.
     *
     * @param customer Customer object with updated fields and existing customer_ID.
     * @return true if update is successful, false otherwise.
     */
    public boolean updateCustomer(Customer customer) {
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = DBConnectionUtil.getConnection();
            ps = conn.prepareStatement(UPDATE_CUSTOMER);

            ps.setString(1, customer.getFirstname());
            ps.setString(2, customer.getSurname());
            ps.setString(3, customer.getContact_number());
            ps.setInt(4, customer.getCustomer_ID());

            int affectedRows = ps.executeUpdate();

            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("CustomerDAO Error in updateCustomer: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            DBConnectionUtil.closeConnection(conn, ps, null);
        }
    }

    /**
     * Deletes an existing customer account.
     *
     * @param customerID Unique identifier of the customer.
     * @return true if delete is successful, false otherwise.
     */
    public boolean deleteCustomer(int customerID) {
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = DBConnectionUtil.getConnection();
            ps = conn.prepareStatement(DELETE_CUSTOMER);

            ps.setInt(1, customerID);

            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("CustomerDAO Error in deleteCustomer: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            DBConnectionUtil.closeConnection(conn, ps, null);
        }
    }

    /**
     * Utility method to map a ResultSet row to a Customer entity.
     */
    private Customer extractCustomerFromResultSet(ResultSet rs) throws SQLException {
        int customerID = rs.getInt("customer_ID");
        int userID = rs.getInt("user_ID");
        String firstname = rs.getString("firstname");
        String surname = rs.getString("surname");
        String contactNumber = rs.getString("contact_number");

        return new Customer(customerID, userID, firstname, surname, contactNumber);
    }
}