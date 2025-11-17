package Model.Entity;

import java.time.Duration;
import java.time.LocalDateTime;

public class Reservation {
    // SY
    private int           reservation_ID;
    private int           branchID;
    private SlotType      slotType;
    private int           vehicle_ID;
    private String        spot_ID;
    private LocalDateTime expected_time_in;
    private LocalDateTime dateReserved;
    private boolean       isAdvanceReserve;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private String        status;

    // Make new reservation
    public Reservation(int           branchID, 
                       SlotType      slotType, 
                       int           vehicle_ID, 
                       String        spot_ID, 
                       LocalDateTime checkInTime){
                        
        this.branchID    = branchID;
        this.slotType    = slotType;
        this.vehicle_ID  = vehicle_ID;
        this.spot_ID     = spot_ID;
        this.checkInTime = checkInTime;
    }
    // loading reservation
    public Reservation(int           reservation_ID, 
                       int           branchID, 
                       SlotType      slotType, 
                       int           vehicle_ID, 
                       String        spot_ID, 
                       LocalDateTime checkInTime, 
                       LocalDateTime checkOutTime,
                       String        status){

        this.reservation_ID = reservation_ID;
        this.branchID       = branchID;
        this.slotType       = slotType;
        this.vehicle_ID     = vehicle_ID;
        this.spot_ID        = spot_ID;
        this.checkInTime    = checkInTime;
        this.checkOutTime   = checkOutTime;
        this.status         = status;
    }
    // Make/Load Reservation with an advance
    public Reservation(int           reservation_ID, 
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

        this.reservation_ID   = reservation_ID;
        this.branchID         = branchID;
        this.slotType         = slotType;
        this.vehicle_ID       = vehicle_ID;
        this.spot_ID          = spot_ID;
        this.expected_time_in = expected_time_in;
        this.dateReserved     = dateReserved;
        this.isAdvanceReserve = isAdvanceReserve;
        this.checkInTime      = checkInTime;
        this.checkOutTime     = checkOutTime;   
        this.status           = status;                     
    }

    public int           getBranchID()       {return this.branchID;}
    public SlotType      getSlotType()       {return this.slotType;}
    public int           getID()             {return this.reservation_ID;}
    public int           getVehicleID()      {return this.vehicle_ID;}
    public String        getSpotID()         {return this.spot_ID;}
    public LocalDateTime getExpectedTimeIn() {return this.expected_time_in;}
    public LocalDateTime getDateReserved()   {return this.dateReserved;}
    public boolean       isAdvanceReserve()  {return this.isAdvanceReserve;}
    public String        getStatus()         {return this.status;}

    public LocalDateTime getCheckInTime(){
       if (isAdvanceReserve()){
            return this.expected_time_in;
       } else {
            return this.checkInTime;
       }
    }
    public LocalDateTime getTimeOut(){
        return this.checkOutTime;
    }
    public int getReserved_hours() {
        if (checkInTime != null && 
            checkOutTime != null && 
            checkOutTime.isAfter(checkInTime)) {

            Duration duration = Duration.between(checkInTime, checkOutTime);
            return (int) duration.toHours(); 
        }
        return 0;  
    }

    // Use to update reservations
    public void setCheckOutTime (LocalDateTime checkOut){
        this.checkOutTime = checkOut;
    }
}
