package Model.DTO;

import Model.Entity.SlotType;

public class SlotUtilizationDTO {
    private SlotType slotType;
    private int reservationCount;

    public SlotUtilizationDTO(SlotType slotType, int reservationCount) {
        this.slotType = slotType;
        this.reservationCount = reservationCount;
    }

    public SlotType getSlotType() {return slotType;}
    public int getReservationCount() {return reservationCount;}
}
