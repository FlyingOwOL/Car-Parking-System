package main.java.Model.Entity;

/**
 * Maps directly to the ENUM('Admin', 'Customer') column in the 'users' table.
 * Provides a type-safe way to handle user roles in Java.
 */
public enum UserRole {
    ADMIN,
    CUSTOMER;

    /**
     * Converts a String value from the database into the Java Enum.
     * @param roleString The string value read from the MySQL 'role' column.
     * @return The corresponding UserRole enum value.
     */
    public static UserRole fromString(String roleString) {
        if (roleString != null) {
            try {
                // Converts "Admin" or "Customer" from DB to enum ADMIN or CUSTOMER
                return UserRole.valueOf(roleString.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Handle case where DB might have an unexpected role value
                System.err.println("Unknown user role found: " + roleString);
            }
        }
        return null; // Handle null case or unknown role
    }
}