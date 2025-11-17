package Model.Entity;

import java.time.LocalDateTime;

public class Reservation {
    //TODO: SY
    public int getBranchID(){
        return 0;
    }
    public String getSlotType(){
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
}
