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
    private static final String SELECT_RESERVATION_BY_ID = "SELECT * FROM reservations WHERE transact_ID = ?";
    private static final String UPDATE_RESERVATION       = "UPDATE reservations SET status = ? WHERE transact_ID = ?";


    Connection        conn = null;
    PreparedStatement ps   = null;
    ResultSet         rs   = null;
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
            int           branchID         = rs.getInt("branchID");  
            int           id               = rs.getInt("transact_ID");  
            int           vehicle_ID       = rs.getInt("vehicle_ID");
            String        spot_ID          = rs.getString("spot_ID");
            boolean       isAdvanceReserve = rs.getBoolean("isAdvanceReserve");
            LocalDateTime checkInTime      = dateChecker(rs.getTimestamp("checkInTime"));
            LocalDateTime checkOutTime     = dateChecker(rs.getTimestamp("checkOutTime"));
            String        status           = rs.getString("status"); 
            SlotType      slotType         = SlotType.valueOf(rs.getString("slotType"));
            
            LocalDateTime expected_time_in = dateChecker(rs.getTimestamp("expected_time_in"));
            LocalDateTime dateReserved     = dateChecker(rs.getTimestamp("dateReserved"));

            Reservation   reservation      = createReservationInstace(id, branchID, slotType, vehicle_ID, 
                                                                      spot_ID, checkInTime, checkOutTime, 
                                                                      isAdvanceReserve, expected_time_in, 
                                                                      dateReserved, status);
            return Optional.of(reservation);
        } catch(SQLException err) {
            System.err.println("ReservationDAO Error in getReservationByID: " + err.getMessage());
            return Optional.empty(); 
        } finally {
            DBConnectionUtil.closeConnection(conn, ps, rs);
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
            ps.setString(8, STATUS_COMPLETED);      
            int rowsAffected = ps.executeUpdate();  
            return rowsAffected > 0;  
        } catch(SQLException err) {
            System.err.println("ReservationDAO Error in updateReservationStatus: " + err.getMessage());
            return false; 
        } finally {
            DBConnectionUtil.closeConnection(conn, ps, rs);
        }        
    }
    //helper functions
    private LocalDateTime dateChecker(java.sql.Timestamp date) {return date != null ? date.toLocalDateTime() : null;}

    private Reservation createReservationInstace(int           id, 
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
