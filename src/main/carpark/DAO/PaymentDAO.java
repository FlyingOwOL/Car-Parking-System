package DAO;

import Model.Entity.Payment;
import Model.Entity.Payment.PaymentStatus;
import Model.Entity.Payment.ModeOfPayment;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PaymentDAO {

    // === SQL QUERIES ====
    private static final String INSERT_PAYMENT =
            "INSERT INTO payments (transact_ID, amount_to_pay, amount_paid, payment_date, payment_status, " +
            "mode_of_payment, processed_by) VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String SELECT_PAYMENT_BY_ID =
            "SELECT payment_ID, transact_ID, amount_to_pay, amount_paid, payment_date, " +
                    "payment_status, mode_of_payment, processed_by FROM payments WHERE payment_ID = ?";

    // when will this be used?
    private static final String SELECT_ALL_PAYMENTS =
            "SELECT payment_ID, transact_ID, amount_to_pay, amount_paid, payment_date, " +
                    "payment_status, mode_of_payment, processed_by FROM payments";

    // can u even update a payment?
    private static final String UPDATE_PAYMENT =
            "UPDATE payments SET transact_ID = ?, amount_to_pay = ?, amount_paid = ?, " +
                    "payment_date = ?, payment_status = ?, mode_of_payment = ?, processed_by = ? " +
                    "WHERE payment_ID = ?";

    // in what situation will u delete a payment
    private static final String DELETE_PAYMENT =
            "DELETE FROM payments WHERE payment_ID = ?";

    // ask
    private static final String SELECT_PAYMENTS_BY_TRANSACTION =
            "SELECT payment_ID, transact_ID, amount_to_pay, amount_paid, payment_date, " +
                    "payment_status, mode_of_payment, processed_by FROM payments WHERE transact_ID = ?";

    // ask
    private static final String SELECT_PAYMENTS_BY_STATUS =
            "SELECT payment_ID, transact_ID, amount_to_pay, amount_paid, payment_date, " +
                    "payment_status, mode_of_payment, processed_by FROM payments WHERE payment_status = ?";

    private static final String UPDATE_PAYMENT_STATUS =
            "UPDATE payments SET payment_status = ? WHERE payment_ID = ?";

    // ask
    private static final String SELECT_PAYMENTS_BY_DATE_RANGE =
            "SELECT payment_ID, transact_ID, amount_to_pay, amount_paid, payment_date, " +
                    "payment_status, mode_of_payment, processed_by FROM payments " +
                    "WHERE payment_date BETWEEN ? AND ?";


    /**
     * Maps a ResultSet row to a Payment object.
     * @param rs The ResultSet containing payment data.
     * @return A fully populated Payment object.
     * @throws SQLException if database access error occurs.
     */
    private Payment mapRowToPayment(ResultSet rs) throws SQLException {
        int paymentID = rs.getInt("payment_ID");
        int transactID = rs.getInt("transact_ID");
        float amountToPay = rs.getFloat("amount_to_pay");
        float amountPaid = rs.getFloat("amount_paid");
        LocalDate paymentDate = rs.getDate("payment_date").toLocalDate();
        PaymentStatus paymentStatus = PaymentStatus.valueOf(rs.getString("payment_status"));
        ModeOfPayment modeOfPayment = ModeOfPayment.valueOf(rs.getString("mode_of_payment"));
        int processedBy = rs.getInt("processed_by");

        return new Payment(paymentID, transactID, amountToPay, amountPaid,
                paymentDate, paymentStatus, modeOfPayment, processedBy);
    }

    /**
     * Inserts a new payment record into the database.
     * Used by the PaymentService when processing new payments.
     *
     * @param payment The Payment object to insert (payment_ID will be auto-generated).
     * @return The generated payment_ID if successful, -1 otherwise.
     */
    public int insertPayment(Payment payment) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        int generatedID = -1;

        try {
            conn = DBConnectionUtil.getConnection();
            ps = conn.prepareStatement(INSERT_PAYMENT, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, payment.getTransact_ID());
            ps.setFloat(2, payment.getAmount_To_Pay());
            ps.setFloat(3, payment.getAmount_paid());
            ps.setDate(4, Date.valueOf(payment.getPayment_date()));
            ps.setString(5, payment.getPayment_status().name());
            ps.setString(6, payment.getMode_of_payment().name());
            ps.setInt(7, payment.getProcessed_by());

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    generatedID = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("PaymentDAO Error in insertPayment: " + e.getMessage());
        } finally {
            DBConnectionUtil.closeConnection(conn, ps, rs);
        }
        return generatedID;
    }

    /**
     * Retrieves a payment by its ID.
     * Used by the PaymentService for payment lookup and verification.
     *
     * @param paymentId The ID of the payment to retrieve.
     * @return An Optional containing the Payment if found.
     */
    public Optional<Payment> getPaymentById(int paymentId) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBConnectionUtil.getConnection();
            ps = conn.prepareStatement(SELECT_PAYMENT_BY_ID);
            ps.setInt(1, paymentId);

            rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRowToPayment(rs));
            }
        } catch (SQLException e) {
            System.err.println("PaymentDAO Error in getPaymentById: " + e.getMessage());
        } finally {
            DBConnectionUtil.closeConnection(conn, ps, rs);
        }
        return Optional.empty();
    }

    /**
     * Retrieves all payment records from the database.
     * Used for administrative reporting and financial overview.
     *
     * @return A list of all Payment objects.
     */
    public List<Payment> getAllPayments() {
        List<Payment> payments = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBConnectionUtil.getConnection();
            ps = conn.prepareStatement(SELECT_ALL_PAYMENTS);

            rs = ps.executeQuery();
            while (rs.next()) {
                payments.add(mapRowToPayment(rs));
            }
        } catch (SQLException e) {
            System.err.println("PaymentDAO Error in getAllPayments: " + e.getMessage());
        } finally {
            DBConnectionUtil.closeConnection(conn, ps, rs);
        }
        return payments;
    }

    /**
     * Retrieves all payments associated with a specific transaction.
     * Used to view payment history for a particular parking transaction.
     *
     * @param transact_id The transaction ID to search for.
     * @return A list of Payment objects linked to the transaction.
     */
    public List<Payment> getPaymentsByTransactionId(int transact_id) {
        List<Payment> payments = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBConnectionUtil.getConnection();
            ps = conn.prepareStatement(SELECT_PAYMENTS_BY_TRANSACTION);
            ps.setInt(1, transact_id);

            rs = ps.executeQuery();
            while (rs.next()) {
                payments.add(mapRowToPayment(rs));
            }
        } catch (SQLException e) {
            System.err.println("PaymentDAO Error in getPaymentsByTransactionId: " + e.getMessage());
        } finally {
            DBConnectionUtil.closeConnection(conn, ps, rs);
        }
        return payments;
    }

    // ??
    /**
     * Retrieves all payments with a specific status.
     * Used for filtering payments (e.g., pending payments, refunded payments).
     *
     * @param status The payment status to filter by.
     * @return A list of Payment objects with the specified status.
     */
    public List<Payment> getPaymentsByStatus(PaymentStatus status) {
        List<Payment> payments = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBConnectionUtil.getConnection();
            ps = conn.prepareStatement(SELECT_PAYMENTS_BY_STATUS);
            ps.setString(1, status.name());

            rs = ps.executeQuery();
            while (rs.next()) {
                payments.add(mapRowToPayment(rs));
            }
        } catch (SQLException e) {
            System.err.println("PaymentDAO Error in getPaymentsByStatus: " + e.getMessage());
        } finally {
            DBConnectionUtil.closeConnection(conn, ps, rs);
        }
        return payments;
    }

    // ??
    /**
     * Retrieves payments within a specific date range.
     * Used for financial reporting and analytics.
     *
     * @param startDate The start date of the range (inclusive).
     * @param endDate The end date of the range (inclusive).
     * @return A list of Payment objects within the date range.
     */
    public List<Payment> getPaymentsByDateRange(LocalDate startDate, LocalDate endDate) {
        List<Payment> payments = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBConnectionUtil.getConnection();
            ps = conn.prepareStatement(SELECT_PAYMENTS_BY_DATE_RANGE);
            ps.setDate(1, Date.valueOf(startDate));
            ps.setDate(2, Date.valueOf(endDate));

            rs = ps.executeQuery();
            while (rs.next()) {
                payments.add(mapRowToPayment(rs));
            }
        } catch (SQLException e) {
            System.err.println("PaymentDAO Error in getPaymentsByDateRange: " + e.getMessage());
        } finally {
            DBConnectionUtil.closeConnection(conn, ps, rs);
        }
        return payments;
    }

    /**
     * Updates an existing payment record in the database.
     * Used when payment details need to be modified.
     *
     * @param payment The Payment object with updated information.
     * @return true if the update was successful.
     */
    public boolean updatePayment(Payment payment) {
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = DBConnectionUtil.getConnection();
            ps = conn.prepareStatement(UPDATE_PAYMENT);
            ps.setInt(1, payment.getTransact_ID());
            ps.setFloat(2, payment.getAmount_To_Pay());
            ps.setFloat(3, payment.getAmount_paid());
            ps.setDate(4, Date.valueOf(payment.getPayment_date()));
            ps.setString(5, payment.getPayment_status().name());
            ps.setString(6, payment.getMode_of_payment().name());
            ps.setInt(7, payment.getProcessed_by());
            ps.setInt(8, payment.getPayment_ID());

            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("PaymentDAO Error in updatePayment: " + e.getMessage());
            return false;
        } finally {
            DBConnectionUtil.closeConnection(conn, ps);
        }
    }

    /**
     * Updates only the payment status of a specific payment.
     * Used by the PaymentService when marking payments as paid or refunded.
     *
     * @param paymentId The ID of the payment to update.
     * @param status The new payment status.
     * @return true if the update was successful.
     */
    public boolean updatePaymentStatus(int paymentId, PaymentStatus status) {
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = DBConnectionUtil.getConnection();
            ps = conn.prepareStatement(UPDATE_PAYMENT_STATUS);
            ps.setString(1, status.name());
            ps.setInt(2, paymentId);

            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("PaymentDAO Error in updatePaymentStatus: " + e.getMessage());
            return false;
        } finally {
            DBConnectionUtil.closeConnection(conn, ps);
        }
    }

    // ??
    /**
     * Deletes a payment record from the database.
     * Used for administrative purposes or correcting errors.
     *
     * @param payment_id The ID of the payment to delete.
     * @return true if the deletion was successful.
     */
    public boolean deletePayment(int payment_id) {
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = DBConnectionUtil.getConnection();
            ps = conn.prepareStatement(DELETE_PAYMENT);
            ps.setInt(1, payment_id);

            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("PaymentDAO Error in deletePayment: " + e.getMessage());
            return false;
        } finally {
            DBConnectionUtil.closeConnection(conn, ps);
        }
    }
}


