package Service;

import DAO.ReservationDAO;
import DAO.ParkingDAO;
import DAO.PaymentDAO;
import DAO.DBConnectionUtil;

import Model.Entity.*;
import Model.Entity.Payment.PaymentStatus;
import Model.Entity.Payment.ModeOfPayment;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service layer for payment processing and fee calculation.
 * Handles payment transactions for parking reservations.
 */
public class PaymentService {

    private ReservationDAO reservationDAO;
    private PaymentDAO paymentDAO;
    private ParkingDAO parkingDAO;

    public PaymentService() {
        this.paymentDAO = new PaymentDAO();
        this.reservationDAO = new ReservationDAO();
        this.parkingDAO = new ParkingDAO();
    }

    /**
     * Processes payment for a completed reservation.
     * Transaction 4.3 - Creates payment record and updates reservation status.
     *
     * @param reservationID The reservation to process payment for
     * @param modeOfPayment How the customer is paying
     * @param adminID The admin processing the payment
     * @return The created Payment object if successful
     */
    public Optional<Payment> processPayment(int reservationID, ModeOfPayment modeOfPayment, int adminID) {
        Connection conn = null;

        try {
            // Start transaction
            conn = DBConnectionUtil.getConnection();
            conn.setAutoCommit(false);

            // Fetch reservation details
            Optional<Reservation> reservationOpt = reservationDAO.getReservationByID(reservationID);

            if (reservationOpt.isEmpty()) {
                System.err.println("PaymentService: Reservation not found - " + reservationID);
                return Optional.empty();
            }

            Reservation reservation = reservationOpt.get();

            // Get pricing rules
            Optional<ParkingSlot> slotOpt = parkingDAO.getSlotByID(reservation.getSpotID());
            if (slotOpt.isEmpty()) {
                System.err.println("PaymentService: Slot ID not found: " + reservation.getSpotID());
                conn.rollback();
                return Optional.empty();
            }
            ParkingSlot slot = slotOpt.get();

            Optional<Pricing> pricingOpt = parkingDAO.getPricingRule(
                    slot.getBranch_ID(),
                    slot.getSlot_type()
            );

            if (pricingOpt.isEmpty()) {
                System.err.println("PaymentService: Pricing rule not found");
                rollbackTransaction(conn);
                return Optional.empty();
            }

            Pricing pricing = pricingOpt.get();

            // Calculate total fee
            float totalAmount = calculateTotalFee(reservation, pricing);

            // Create payment record
            Payment payment = new Payment(
                    reservation.getReservationID(),
                    new BigDecimal(totalAmount),
                    new BigDecimal(totalAmount),
                    LocalDate.now(),
                    PaymentStatus.PAID,
                    modeOfPayment
            );

            boolean paymentInserted = paymentDAO.insertPayment(payment);
            if (!paymentInserted) {
                System.err.println("PaymentService: Failed to create payment");
                conn.rollback();
                return Optional.empty();
            }

            // Update reservation status to completed
            boolean updated = reservationDAO.updateReservationStatus(reservationID, ReservationStatus.COMPLETED, conn);

            if (!updated) {
                System.err.println("PaymentService: Failed to update reservation status");
                rollbackTransaction(conn);
                return Optional.empty();
            }

            // Commit transaction
            conn.commit();
            return Optional.of(payment);

        } catch (SQLException e) {
            System.err.println("PaymentService Error: " + e.getMessage());
            rollbackTransaction(conn);
            return Optional.empty();
        } finally {
            closeConnection(conn);
        }
    }

    /**
     * Calculates the total parking fee based on duration.
     * Uses hourly rate for reserved time and overtime rate if customer exceeds reservation.
     *
     * @param reservation Contains check-in, check-out times, and reserved hours
     * @param pricing Contains the pricing rates
     * @return Total calculated fee
     */
    public float calculateTotalFee(Reservation reservation, Pricing pricing) {
        LocalDateTime checkIn = reservation.getCheckInTime();
        LocalDateTime checkOut = reservation.getTimeOut();
        int reservedHours = reservation.getReserved_hours();

        if (checkIn == null || checkOut == null) {
            return 0.0f;
        }

        // Check if times are valid
        if (checkIn.isAfter(checkOut) || checkIn.isEqual(checkOut)) {
            System.err.println("PaymentService: Invalid parking duration");
            return 0.0f;
        }

        // Calculate actual duration in hours (rounded up)
        Duration duration = Duration.between(checkIn, checkOut);
        long minutes = duration.toMinutes();
        long actualHours = (minutes + 59) / 60; // Round up to nearest hour

        if (actualHours == 0) {
            actualHours = 1; // Default
        }

        float total;

        // Check if customer stayed longer than reserved duration
        if (actualHours <= reservedHours) {
            total = pricing.getHourly_rate().floatValue() * actualHours;
        } else {
            float standardFee = pricing.getHourly_rate().floatValue() * reservedHours;
            long overtimeHours = actualHours - reservedHours;
            float overtimeFee = pricing.getOvertime_rate().floatValue() * overtimeHours;

            total = standardFee + overtimeFee;
        }

        return Math.round(total * 100) / 100.0f;
    }

    /**
     * Processes a refund for a cancelled or disputed payment.
     * Updates payment status to REFUNDED.
     *
     * @param paymentID The payment to refund
     * @param adminID The admin processing the refund
     * @return true if refund was successful
     */
    public boolean processRefund(int paymentID, int adminID) {
        Connection conn = null;

        try {
            conn = DBConnectionUtil.getConnection();
            conn.setAutoCommit(false);

            // Get the payment
            Optional<Payment> paymentOpt = paymentDAO.getPaymentById(paymentID);

            if (!paymentOpt.isPresent()) {
                System.err.println("PaymentService: Payment not found - " + paymentID);
                return false;
            }

            Payment payment = paymentOpt.get();

            // Check if payment can be refunded
            if (payment.getPayment_status() == PaymentStatus.REFUNDED) {
                System.err.println("PaymentService: Payment already refunded");
                return false;
            }

            if (payment.getPayment_status() != PaymentStatus.PAID) {
                System.err.println("PaymentService: Cannot refund unpaid payment");
                return false;
            }

            // Update payment status to refunded
            boolean updated = paymentDAO.updatePaymentStatus(paymentID, PaymentStatus.REFUNDED);
            if (!updated) {
                System.err.println("PaymentService: Failed to update payment status");
                rollbackTransaction(conn);
                return false;
            }

            // update reservation status back to cancelled
            // reservationDAO.updateReservationStatus(payment.getTransact_ID(), "Cancelled");

            conn.commit();
            System.out.println("PaymentService: Refund processed for payment " + paymentID);
            return true;

        } catch (SQLException e) {
            System.err.println("PaymentService Error in processRefund: " + e.getMessage());
            rollbackTransaction(conn);
            return false;
        } finally {
            closeConnection(conn);
        }
    }

    /**
     * Gets a payment by the paymentID.
     */
    public Optional<Payment> getPaymentById(int paymentID) {
        return paymentDAO.getPaymentById(paymentID);
    }

    /**
     * Gets all payments for a specific transaction.
     */
    public List<Payment> getPaymentsByTransaction(int transactID) {
        return paymentDAO.getPaymentsByTransactionId(transactID);
    }

    /**
     * Updates payment status (for corrections or status changes).
     */
    public boolean updatePaymentStatus(int paymentID, PaymentStatus newStatus) {
        return paymentDAO.updatePaymentStatus(paymentID, newStatus);
    }

    // === HELPER METHODS ===

    /**
     * Rolls back the current transaction if something goes wrong.
     */
    private void rollbackTransaction(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException e) {
                System.err.println("Rollback failed: " + e.getMessage());
            }
        }
    }

    /**
     * Closes the database connection and resets auto-commit.
     */
    private void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.setAutoCommit(true);
                conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }
}