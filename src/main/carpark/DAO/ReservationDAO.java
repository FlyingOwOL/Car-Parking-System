package DAO;


import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import Model.DTO.ReservationSummaryDTO;
import Model.Entity.Reservation;
import Model.Entity.ReservationStatus;

public class ReservationDAO {
    // SY

    // === SQL QUERRIES === //
    private static final String SELECT_RESERVATION_BY_ID = "SELECT * FROM reservations WHERE transact_ID = ? ";
    private static final String UPDATE_RESERVATION       = "UPDATE reservations SET status = ? WHERE transact_ID = ? ";

    //(vehicle_ID, spot_ID, expected_time_in, dateReserved, check_in_time, timeOut, status)
    private static final String INSERT_RESERVATION       = "INSERT INTO reservations " +
                                            "(vehicle_ID, spot_ID, expected_time_in, check_in_time, time_Out, dateReserved, status) " +
                                            "VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String AUTO_COMPLETE_EXPIRED    = "UPDATE reservations SET status = 'Completed' " +
                                                           "WHERE status = 'Active' AND time_Out < NOW()";

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
     * This keeps the database status in sync with real time.
     */
    public void updateExpiredReservations() {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBConnectionUtil.getConnection();
            ps = conn.prepareStatement(AUTO_COMPLETE_EXPIRED);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("System: Auto-completed " + rows + " expired reservations.");
            }
        } catch (SQLException e) {
            System.err.println("ReservationDAO Error in updateExpiredReservations: " + e.getMessage());
        } finally {
            DBConnectionUtil.closeConnection(conn, ps);
        }
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
            ps.setTimestamp(4, Timestamp.valueOf(newReservation.getCheckInTime()));
            ps.setTimestamp(5, Timestamp.valueOf(newReservation.getTimeOut()));
            ps.setTimestamp(6, dateChecker(newReservation.getDateReserved()));   // dateReserved
            ps.setString(7, newReservation.getStatus().name());

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

        ReservationStatus status = ReservationStatus.valueOf(
                rs.getString("status").trim().toUpperCase()
        );

        return new Reservation(
                transactID, vehicleId, spotId, expected, checkIn, out, reserved, status
        );
    }

    public List<ReservationSummaryDTO> findReservationSummariesByUserId(int userId) {
        String sql = """
        SELECT 
            r.*, 
            v.plate_number, 
            COALESCE(p.amount_paid, 0) AS totalPaid
        FROM reservations r
        JOIN vehicles v ON r.vehicle_ID = v.vehicle_ID
        LEFT JOIN payments p ON p.transact_ID = r.transact_ID
        WHERE v.user_ID = ?
        ORDER BY r.dateReserved DESC
    """;

        List<ReservationSummaryDTO> list = new ArrayList<>();

        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                // Build Reservation object from r.* columns
                Reservation reservation = new Reservation(
                        rs.getInt("transact_ID"),
                        rs.getInt("vehicle_ID"),
                        rs.getString("spot_ID"),
                        dateChecker(rs.getTimestamp("expected_time_in")),
                        dateChecker(rs.getTimestamp("check_in_time")),
                        dateChecker(rs.getTimestamp("time_Out")),
                        dateChecker(rs.getTimestamp("dateReserved")),
                        ReservationStatus.valueOf(rs.getString("status").trim().toUpperCase())
                );

                ReservationSummaryDTO dto = new ReservationSummaryDTO(
                        reservation,
                        rs.getString("plate_number"),
                        rs.getBigDecimal("totalPaid")
                );

                list.add(dto);
            }

        } catch (Exception e) {
            System.err.println("Error in findReservationSummariesByUserId: " + e.getMessage());
        }

        return list;
    }

    private java.sql.Timestamp dateChecker(LocalDateTime date) {return date != null ? java.sql.Timestamp.valueOf(date) : null;}
    private LocalDateTime dateChecker(java.sql.Timestamp date) {return date != null ? date.toLocalDateTime() : null;}
}
