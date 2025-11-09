package Model.Entity;

/**
 * Maps to the 'parking_slots' table in the database.
 * Represents a physical, reservable space within a branch.
 */
public class ParkingSlot {
    private String spot_ID;
    private int branch_ID;
    private int floor_level;
    private SlotType slot_type;
    private boolean availability;

    // === CONSTRUCTORS ===

    /**
     * Default Constructors
     */
    public ParkingSlot() {}

    /**
     * Full Constructor for reading existing records from the database.
     * @param spot_ID The unique ID of the parking spot (PK)
     * @param branch_ID The ID of the branch the slot belongs tp (FK)
     * @param floor_level the floor number where the slot is located
     * @param slot_type The type of slot
     * @param availability The current availability status
     */
    public ParkingSlot(String spot_ID, int branch_ID, int floor_level, SlotType slot_type, boolean availability) {
        this.spot_ID = spot_ID;
        this.branch_ID = branch_ID;
        this.floor_level = floor_level;
        this.slot_type = slot_type;
        this.availability = availability;
    }

    /**
     * Constructor for creating a new slot before insertion (availability defaults to true).
     * @param spot_ID The unique ID of the parking spot (PK)
     * @param branch_ID The ID of the branch the slot belongs tp (FK)
     * @param floor_level the floor number where the slot is located
     * @param slot_type The type of slot
     */
    public ParkingSlot(String spot_ID, int branch_ID, int floor_level, SlotType slot_type) {
        this.spot_ID = spot_ID;
        this.branch_ID = branch_ID;
        this.floor_level = floor_level;
        this.slot_type = slot_type;
        this.availability = true; // Default availability for new slots
    }

    // === GETTERS and SETTERS ===

    public String getSpot_ID() {return spot_ID;}
    public int getBranch_ID() {return branch_ID;}
    public int getFloor_level() {return floor_level;}
    public SlotType getSlot_type() {return slot_type;}
    public boolean isAvailability() {return availability;}

    public void setSpot_ID(String spot_ID) {this.spot_ID = spot_ID;}
    public void setBranch_ID(int branch_ID) {this.branch_ID = branch_ID;}
    public void setFloor_level(int floor_level) {this.floor_level = floor_level;}
    public void setSlot_type(SlotType slot_type) {this.slot_type = slot_type;}
    public void setAvailability(boolean availability) {this.availability = availability;}

    @Override
    public String toString() {
        return "ParkingSlot{" +
                "Spot ID: " + spot_ID +
                ", Branch ID: " + branch_ID +
                ", Floor Level: " + floor_level +
                ", Slot Type: " + slot_type +
                ", Availability: " + availability + '}';
    }
}
