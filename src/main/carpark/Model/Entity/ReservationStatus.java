package Model.Entity;

public enum ReservationStatus {
    ACTIVE,
    COMPLETED,
    CANCELLED,
    NO_SHOW;

    /**
     * Safely converts a database string to the enum.
     */
    public static ReservationStatus fromString(String text) {
        if (text != null) {
            for (ReservationStatus b : ReservationStatus.values()) {
                if (text.equalsIgnoreCase(b.name())) {
                    return b;
                }
            }
        }
        return ACTIVE; // Default case
    }
}
