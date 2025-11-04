package main.java.Model.Entity;

/**
 * Maps directly to the ENUM('Regular', 'PWD', 'Motorcycle', 'VIP')
 * column in the 'parking_slots' table.
 */
public enum SlotType {
    REGULAR,
    PWD, // Persons With Disabilities
    MOTORCYCLE,
    VIP;

    /**
     * Converts a String value from the database into the Java Enum.
     * @param typeString The string value read from the MySQL 'slot_type' column.
     * @return The corresponding SlotType enum value.
     */
    public static SlotType fromString(String typeString) {
        if (typeString != null) {
            try {
                // Converts DB string (e.g., "Regular") to enum (REGULAR)
                return SlotType.valueOf(typeString.toUpperCase());
            } catch (IllegalArgumentException e) {
                System.err.println("Unknown slot type found: " + typeString);
            }
        }
        return null;
    }
}
