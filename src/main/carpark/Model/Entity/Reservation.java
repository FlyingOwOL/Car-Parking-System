package Model.Entity;

import java.time.LocalDateTime;

public class Reservation {
    //TODO: SY
    private int reservation_ID;
    private int branchID;
    private SlotType slotType;
    private int vehicle_ID;
    private String spot_ID;
    private LocalDateTime expected_time_in;
    private LocalDateTime dateReserved;
    private boolean isAdvanceReserve;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private String status;

    public Reservation(){
        
    }

    public int getBranchID(){
        return this.branchID;
    }
    public SlotType getSlotType(){
        return null;
    }
    public int getID(){
        return 0;
    }
    public LocalDateTime getCheckInTime(){
        return LocalDateTime.now();
    }
    public LocalDateTime getTimeOut(){
        return LocalDateTime.now();
    }
    public int getReserved_hours(){
        return 0;
    }
}
