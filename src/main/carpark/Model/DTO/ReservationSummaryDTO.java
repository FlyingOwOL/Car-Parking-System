package Model.DTO;

import Model.Entity.Reservation;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

public class ReservationSummaryDTO {

    private final Reservation reservation;
    private final String vehiclePlate;
    private final BigDecimal totalPaid;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public ReservationSummaryDTO(Reservation reservation, String vehiclePlate, BigDecimal totalPaid) {
        this.reservation = reservation;
        this.vehiclePlate = vehiclePlate;
        this.totalPaid = totalPaid;
    }

    // --- Getters for TableView Columns ---

    public int getTransactNo() { return reservation.getReservationID(); }
    public String getResSlotColumn() { return reservation.getSpotID(); }
    public String getResStatusColumn() { return reservation.getStatus().name(); }

    public String getResPlateColumn() { return vehiclePlate; }
    public String getResTotalColumn() { return "₱" + String.format("%,.2f", totalPaid); } // Format as Currency

    public String getResTimeInColumn() {
        return reservation.getCheckInTime() != null ? reservation.getCheckInTime().format(formatter) : "N/A";
    }
    public String getResTimeOutColumn() {
        return reservation.getTimeOut() != null ? reservation.getTimeOut().format(formatter) : "N/A";
    }

}
