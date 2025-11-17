package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;

import Model.Entity.Reservation;
import Model.Entity.SlotType;

public class ReservationDAO {
    // SY

    // === SQL QUERRIES === //
    private static final String SELECT_RESERVATION_BY_ID = "SELECT * FROM reservations WHERE transact_ID = ? ";
    private static final String UPDATE_RESERVATION       = "UPDATE reservations SET status = ? WHERE transact_ID = ? ";
    private static final String GET_EXTRA_PROPERTIES     = "SELECT branch_ID, slot_type " + 
                                                           "FROM parking_slots " + 
                                                           "WHERE spot_ID = ?";
    //(vehicle_ID, spot_ID, expected_time_in, dateReserved, isAdvanced, check_in_time, timeOut, status)
    private static final String INSERT_RESERVATION       = "INSERT INTO reservations "+
                                                           "(vehicle_ID, spot_ID, expected_time_in, dateReserved, "+ 
                                                           " isAdvanced, check_in_time, timeOut, status) "+
                                                           "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

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

            if (!rs.next()){
                return Optional.empty();
            }

            //These are found in reservations table
            int           id               = rs.getInt("transact_ID");  
            int           vehicle_ID       = rs.getInt("vehicle_ID");
            String        spot_ID          = rs.getString("spot_ID");
            LocalDateTime expected_time_in = dateChecker(rs.getTimestamp("expected_time_in"));
            LocalDateTime dateReserved     = dateChecker(rs.getTimestamp("dateReserved"));
            boolean       isAdvanceReserve = rs.getBoolean("isAdvanceReserve");
            LocalDateTime checkInTime      = dateChecker(rs.getTimestamp("checkInTime"));
            LocalDateTime checkOutTime     = dateChecker(rs.getTimestamp("checkOutTime"));
            String        status           = rs.getString("status"); 

            //These are found in parking_slots table
            ex   = conn.prepareStatement(GET_EXTRA_PROPERTIES);
            ex.setString(1, spot_ID);
            rs2  = ex.executeQuery();

            if (!rs2.next()){
                return Optional.empty();
            }

            SlotType      slotType         = SlotType.valueOf(rs2.getString("slot_Type"));
            int           branchID         = rs2.getInt("branch_ID");              

            Reservation   reservation      = createReservationInstance(id, branchID, slotType, vehicle_ID, 
                                                                      spot_ID, checkInTime, checkOutTime, 
                                                                      isAdvanceReserve, expected_time_in, 
                                                                      dateReserved, status);
            return Optional.of(reservation);
        } catch(SQLException err) {
            System.err.println("ReservationDAO Error in getReservationByID: " + err.getMessage());
            return Optional.empty(); 
        } finally {
            DBConnectionUtil.closeConnection(conn, ps, rs);
            if (ex != null) try { ex.close(); } catch (SQLException e) {}
            if (rs2 != null) try { rs2.close(); } catch (SQLException e) {}
        }
    }

    /**
     * 
     * @param reservation_ID
     * @param STATUS_COMPLETED
     * @return
     */
    public boolean updateReservationStatus(int reservation_ID, String STATUS_COMPLETED){
        try{
            conn = DBConnectionUtil.getConnection();
            ps   = conn.prepareStatement(UPDATE_RESERVATION);
            ps.setInt(1, reservation_ID); 
            ps.setString(2, STATUS_COMPLETED);      
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
        
        PreparedStatement ps = null;  // Made local
        ResultSet generatedKeys = null;
        try {
            ps = conn.prepareStatement(INSERT_RESERVATION, PreparedStatement.RETURN_GENERATED_KEYS); 


            ps.setInt(1, newReservation.getVehicleID());                         // vehicle_ID
            ps.setString(2, newReservation.getSpotID());                         // spot_ID
            ps.setTimestamp(3, dateChecker(newReservation.getExpectedTimeIn())); // expected_time_in
            ps.setTimestamp(4, dateChecker(newReservation.getDateReserved()));   // dateReserved
            ps.setBoolean(5, newReservation.isAdvanceReserve());                 // isAdvanceReserve
            ps.setTimestamp(6, dateChecker(newReservation.getCheckInTime()));    // timeIn
            ps.setTimestamp(7, dateChecker(newReservation.getTimeOut()));        // timeOut
            ps.setString(8, newReservation.getStatus());                         // status

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected == 0) {
                System.out.println("No rows affected");
                return Optional.empty();
            }
            // Retrieve the auto-generated transact_ID
            generatedKeys = ps.getGeneratedKeys();
            if (!generatedKeys.next()) {
                System.out.println("Error getting generated keys");
                return Optional.empty();
            }

            int generatedId = generatedKeys.getInt(1);  // First column is the generated key
            newReservation.setID(generatedId);
            
            return Optional.of(newReservation);            
        } catch (SQLException err) {
            System.out.println("ReservationDAO Error in insertReservation: " + err.getMessage());
            return Optional.empty();
        } finally {
            if (generatedKeys != null) try { generatedKeys.close(); } catch (SQLException e) {}
            if (ps != null) try { ps.close(); } catch (SQLException e) {}
        }
    }
    //helper functions
    private java.sql.Timestamp dateChecker(LocalDateTime date) {return date != null ? java.sql.Timestamp.valueOf(date) : null;}
    private LocalDateTime dateChecker(java.sql.Timestamp date) {return date != null ? date.toLocalDateTime() : null;}

    private Reservation createReservationInstance(int           id, 
                                                 int           branchID, 
                                                 SlotType      slotType, 
                                                 int           vehicle_ID, 
                                                 String        spot_ID, 
                                                 LocalDateTime checkInTime, 
                                                 LocalDateTime checkOutTime, 
                                                 boolean       isAdvanceReserve,
                                                 LocalDateTime expected_time_in, 
                                                 LocalDateTime dateReserved,
                                                 String        status){

        Reservation reservation;
        if(isAdvanceReserve){
            reservation = new Reservation(id, branchID, slotType, vehicle_ID, 
                                            spot_ID, checkInTime, checkOutTime, 
                                            isAdvanceReserve, expected_time_in, 
                                            dateReserved, status);

        } else {
            reservation = new Reservation(id, branchID, slotType, vehicle_ID, 
                                            spot_ID, checkInTime, checkOutTime, 
                                            status);
        }
        return reservation;
    }
}
