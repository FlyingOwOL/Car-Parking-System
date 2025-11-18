package DAO;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;

import Model.Entity.Reservation;
import Model.Entity.ReservationStatus;
import Model.Entity.SlotType;

import javax.naming.NamingEnumeration;

public class ReservationDAO {
    // SY

    // === SQL QUERRIES === //
    private static final String SELECT_RESERVATION_BY_ID = "SELECT * FROM reservations WHERE transact_ID = ? ";
    private static final String UPDATE_RESERVATION       = "UPDATE reservations SET status = ? WHERE transact_ID = ? ";
    private static final String GET_EXTRA_PROPERTIES     = "SELECT branch_ID, slot_type " + 
                                                           "FROM parking_slots " + 
                                                           "WHERE spot_ID = ?";
    //(vehicle_ID, spot_ID, expected_time_in, dateReserved, check_in_time, timeOut, status)
    private static final String INSERT_RESERVATION       = "INSERT INTO reservations "+
                                                           "(vehicle_ID, spot_ID, expected_time_in, dateReserved, status) "+
                                                           "VALUES (?, ?, ?, ?, ?)";

    Connection        conn = null;
    PreparedStatement ps   = null;
    PreparedStatement ex   = null;
    ResultSet         rs   = null;
    ResultSet         rs2  = null;
    /**
     * 
     * @param reservation_ID
     * @return
     */
    public Optional<Reservation> getReservationByID(int reservation_ID){
        try{
            conn = DBConnectionUtil.getConnection();
            ps   = conn.prepareStatement(SELECT_RESERVATION_BY_ID);
            ps.setInt(1, reservation_ID); 
            rs   = ps.executeQuery();

            if (rs.next()) {
                return Optional.of(mapRowToReservation(rs));
            }
        } catch(SQLException err) {
            System.err.println("ReservationDAO Error in getReservationByID: " + err.getMessage());
            return Optional.empty(); 
        } finally {
            DBConnectionUtil.closeConnection(conn, ps, rs);
        }
        return Optional.empty();
    }

    /**
     * 
     * @param reservation_ID
     * @param newStatus
     * @return
     */
    public boolean updateReservationStatus(int reservation_ID, ReservationStatus newStatus, Connection conn){
        try{
            conn = DBConnectionUtil.getConnection();
            ps   = conn.prepareStatement(UPDATE_RESERVATION);
            ps.setString(1, newStatus.name());
            ps.setInt(2, reservation_ID);
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch(SQLException err) {
            System.err.println("ReservationDAO Error in updateReservationStatus: " + err.getMessage());
            return false; 
        } finally {
            DBConnectionUtil.closeConnection(conn, ps, rs);
        }        
    }

    public Optional<Reservation> insertReservation(Reservation newReservation, 
                                                   Connection  conn){
        
        PreparedStatement ps = null;
        ResultSet generatedKeys = null;

        try {
            ps = conn.prepareStatement(INSERT_RESERVATION, PreparedStatement.RETURN_GENERATED_KEYS); 

            ps.setInt(1, newReservation.getVehicleID());                         // vehicle_ID
            ps.setString(2, newReservation.getSpotID());                         // spot_ID
            ps.setTimestamp(3, dateChecker(newReservation.getExpectedTimeIn())); // expected_time_in
            ps.setTimestamp(4, dateChecker(newReservation.getDateReserved()));   // dateReserved
            ps.setString(5, newReservation.getStatus().name());                         // status

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected == 0) {
                System.err.println("ReservationDAO: Insertion failed, no rows affected.");
                return Optional.empty();
            }

            generatedKeys = ps.getGeneratedKeys();
            if (generatedKeys.next()) {
                int newId = generatedKeys.getInt(1);
                newReservation.setTransactID(newId); // Update the object with the ID
                return Optional.of(newReservation);
            } else {
                System.err.println("ReservationDAO: Insertion failed, no ID obtained.");
                return Optional.empty();
            }
        } catch (SQLException err) {
            System.out.println("ReservationDAO Error in insertReservation: " + err.getMessage());
            return Optional.empty();
        } finally {
            if (generatedKeys != null) try { generatedKeys.close(); } catch (SQLException e) {}
            if (ps != null) try { ps.close(); } catch (SQLException e) {}
        }
    }
    //helper functions
    private Reservation mapRowToReservation(ResultSet rs) throws SQLException {
        int transactID = rs.getInt("transact_ID");
        int vehicleId = rs.getInt("vehicle_ID");
        String spotId = rs.getString("spot_ID");

        // Handle nullable timestamps
        Timestamp expectedTs = rs.getTimestamp("expected_time_in");
        LocalDateTime expected = expectedTs != null ? expectedTs.toLocalDateTime() : null;

        Timestamp checkInTs = rs.getTimestamp("check_in_time");
        LocalDateTime checkIn = checkInTs != null ? checkInTs.toLocalDateTime() : null;

        Timestamp outTs = rs.getTimestamp("time_Out");
        LocalDateTime out = outTs != null ? outTs.toLocalDateTime() : null;

        Timestamp reservedTs = rs.getTimestamp("dateReserved");
        LocalDateTime reserved = reservedTs != null ? reservedTs.toLocalDateTime() : null;

        ReservationStatus status = ReservationStatus.valueOf(rs.getString("status"));

        return new Reservation(
                transactID, vehicleId, spotId, expected, checkIn, out, reserved, status
        );
    }

    private java.sql.Timestamp dateChecker(LocalDateTime date) {return date != null ? java.sql.Timestamp.valueOf(date) : null;}
    private LocalDateTime dateChecker(java.sql.Timestamp date) {return date != null ? date.toLocalDateTime() : null;}
}
