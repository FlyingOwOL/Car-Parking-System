package Model.Entity;

import java.time.Duration;
import java.time.LocalDateTime;

public class Reservation {
    // SY
    private int           reservation_ID;
    private int           vehicle_ID;
    private String        spot_ID;
    private LocalDateTime expected_time_in;
    private LocalDateTime dateReserved;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private ReservationStatus status;

    // Make new reservation
    public Reservation(int           vehicleID,
                       String        spot_ID, 
                       LocalDateTime expected_time_in,
                       LocalDateTime dateReserved,
                       ReservationStatus status){
                        
        this.vehicle_ID = vehicleID;
        this.spot_ID = spot_ID;
        this.expected_time_in = expected_time_in;
        this.dateReserved = dateReserved;
        this.status = status;
    }
    // Reading from Database
    public Reservation(int           reservation_ID, 
                       int           vehicle_ID,
                       String        spot_ID,
                       LocalDateTime expected_time_in,
                       LocalDateTime checkInTime, 
                       LocalDateTime checkOutTime,
                       LocalDateTime dateReserved,
                       ReservationStatus status){

        this.reservation_ID = reservation_ID;
        this.vehicle_ID     = vehicle_ID;
        this.spot_ID        = spot_ID;
        this.expected_time_in = expected_time_in;
        this.checkInTime    = checkInTime;
        this.checkOutTime   = checkOutTime;
        this.dateReserved = dateReserved;
        this.status         = status;
    }

    public int           getReservationID()  {return this.reservation_ID;}
    public int           getVehicleID()      {return this.vehicle_ID;}
    public String        getSpotID()         {return this.spot_ID;}
    public LocalDateTime getExpectedTimeIn() {return this.expected_time_in;}
    public LocalDateTime getDateReserved()   {return this.dateReserved;}
    public ReservationStatus getStatus()     {return this.status;}
    public LocalDateTime getTimeOut()        {return this.checkOutTime;}
    public LocalDateTime getCheckInTime(){return this.checkInTime;}

    public void setTransactID(int transactID) { this.reservation_ID = transactID; }

    // Use to update reservations
    public void setCheckOutTime (LocalDateTime checkOut){
        this.checkOutTime = checkOut;
    }
    public void setID(int ID){
        this.reservation_ID = ID;
    }

    public int getReserved_hours() {
        if (this.getCheckInTime() != null && this.checkOutTime != null &&
                this.checkOutTime.isAfter(this.getCheckInTime())) {

            Duration duration = Duration.between(this.getCheckInTime(), this.checkOutTime);
            long minutes = duration.toMinutes();
            return (int) Math.max(1, (minutes + 59) / 60);
        }
        return 0;
    }
}
