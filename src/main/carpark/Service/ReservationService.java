package Service;

import DAO.ParkingDAO;
import DAO.ReservationDAO;
import DAO.DBConnectionUtil;
import Model.Entity.Reservation;
import Model.Entity.ParkingSlot;
import Model.Entity.ReservationStatus;
import Model.Entity.SlotType;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


public class ReservationService {
    
    private ReservationDAO reservationDAO;
    private ParkingDAO parkingDAO;

    /**
     * Constructor that sets up the DAOs we need to talk to the database.
     * Just initializes the reservation and parking data access objects.
     */
    public ReservationService() {
        this.reservationDAO = new ReservationDAO();
        this.parkingDAO = new ParkingDAO();
    }

   
    public Optional<Reservation> createReservation(int userID, int vehicleID, int branchID, SlotType slotType, LocalDateTime expectedTimeIn) {
        Connection conn = null;
        
        try {
            // Step 1: Start the database transaction so everything happens together
            conn = DBConnectionUtil.getConnection();
            conn.setAutoCommit(false);

            // Step 2: Check what slots are available at this branch for the requested type
            List<ParkingSlot> availableSlots = parkingDAO.getAvailableSlots(branchID, slotType);
            
            if (availableSlots.isEmpty()) {
                System.err.println("ReservationService: No available slots for branch " + branchID + " and type " + slotType);
                rollbackTransaction(conn);
                return Optional.empty();
            }

            ParkingSlot selectedSlot = availableSlots.get(0);
            String spotId = selectedSlot.getSpot_ID();

            // Step 3: Create the reservation record in the database
            Reservation newReservation = new Reservation(
                    vehicleID,
                    spotId,
                    expectedTimeIn,
                    LocalDateTime.now(),
                    ReservationStatus.ACTIVE
            );
            
            Optional<Reservation> createdReservation = reservationDAO.insertReservation(newReservation, conn);
            
            if (createdReservation.isEmpty()) {
                System.err.println("ReservationService: Failed to insert reservation");
                conn.rollback();
                return Optional.empty();
            }

            // Step 4: Mark the slot as taken so no one else can book it
            boolean slotUpdated = parkingDAO.updateSlotAvailability(spotId, false, conn);
            
            if (!slotUpdated) {
                System.err.println("ReservationService: Failed to update slot availability");
                conn.rollback();
                return Optional.empty();
            }

            conn.commit();
            System.out.println("ReservationService: Reservation created successfully - ID: " + createdReservation.get().getReservationID());
            
            return createdReservation;

        } catch (SQLException e) {
            System.err.println("ReservationService Error in createReservation: " + e.getMessage());
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            return Optional.empty();
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    DBConnectionUtil.closeConnection(conn);
                } catch (SQLException e) {
                    System.err.println("Error closing connection: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Cancels an existing parking reservation.
     * This is Transaction 4.4 from our project specs - handles the process
     * of cancelling a reservation and freeing up the parking slot for others.
     * 
     * @param reservationID The ID of the reservation to cancel
     * @param userID The ID of the user requesting cancellation (for authorization)
     * @return true if cancellation was successful, false if failed
     */
    public boolean cancelReservation(int reservationID, int userID) {
        Connection conn = null;
        
        try {
            // Step 1: Start Transaction
            conn = DBConnectionUtil.getConnection();
            conn.setAutoCommit(false);

            // Step 2: Verify Status - Get the reservation and check it can be cancelled
            Optional<Reservation> reservationOpt = reservationDAO.getReservationByID(reservationID);
            
            if (reservationOpt.isEmpty()) {
                System.err.println("ReservationService: Reservation not found - " + reservationID);
                rollbackTransaction(conn);
                return false;
            }

            if (reservationOpt.get().getStatus() != ReservationStatus.ACTIVE) {
                System.err.println("ReservationService: Cannot cancel. Status is " + reservationOpt.get().getStatus());
                conn.rollback();
                return false;
            }

            boolean statusUpdated = reservationDAO.updateReservationStatus(reservationID, ReservationStatus.CANCELLED, conn);
            boolean slotReleased = parkingDAO.updateSlotAvailability(reservationOpt.get().getSpotID(), true, conn);

            if (statusUpdated && slotReleased) {
                conn.commit();
                System.out.println("ReservationService: Reservation " + reservationID + " cancelled successfully.");
                return true;
            } else {
                conn.rollback();
                return false;
            }

        } catch (SQLException e) {
            System.err.println("ReservationService Error in cancelReservation: " + e.getMessage());
            rollbackTransaction(conn);
            return false;
        } finally {
            closeConnection(conn);
        }
    }

    /**
     * Gets a reservation by its ID - useful for displaying reservation details
     * 
     * @param reservationID The ID of the reservation to retrieve
     * @return Optional containing the reservation if found, empty if not found
     */
    public Optional<Reservation> getReservation(int reservationID) {
        return reservationDAO.getReservationByID(reservationID);
    }

    // === HELPER METHODS ===

    /**
     * If something goes wrong during the reservation process, this method
     * undoes all the database changes we made so we don't leave things half-done.
     * 
     * @param conn The database connection to rollback
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
     * Cleans up the database connection when we're done with it.
     * Makes sure to reset auto-commit and close the connection properly.
     * 
     * @param conn The database connection to close
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
