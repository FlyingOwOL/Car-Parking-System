package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;

import Model.Entity.Reservation;

public class ReservationDAO {
    //TODO: SY

    // === SQL QUERRIES === //
    private static final String SELECT_RESERVATION_BY_ID = "SELECT * FROM reservations WHERE transact_ID = ?";
    private static final String UPDATE_RESERVATION       = "UPDATE reservations ";


    public Optional<Reservation> getReservationByID(int reservation_ID){
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        return null;
    }

    public boolean updateReservationStatus(int reservation_ID, String STATUS_COMPLETED){
        
        return false;
    }
}
