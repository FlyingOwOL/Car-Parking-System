package main.java.DAO;

import main.java.Model.Entity.Vehicle;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * VehicleDAO handles all data access operations related to the 'vehicles' table.
 * This includes registering new vehicles and retrieving/managing a user's fleet.
 */
public class VehicleDAO {

    // === SQL QUERIES ===
    private static final String SELECT_VEHICLE_BY_USERID = "SELECT * FROM vehicles WHERE user_ID = ?";
    private static final String INSERT_VEHICLE = "INSERT INTO vehicles (user_ID, plate_number, vehicle_Type, vehicle_Brand) VALUES (?, ?, ?, ?)";
    private static final String DELETE_VEHICLE = "DELETE FROM vehicles WHERE vehicle_ID = ?";

    /**
     * Retrieves a list of vehicle record from the database using the userID.
     *
     * @param userID The unique email address of the user.
     * @return A List of Vehicle objects; the list will be empty if no vehicles are found or an error occurs.
     */
    public List<Vehicle> getVehicleByUserID(int userID) {
        List<Vehicle> vehicleList = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBConnectionUtil.getConnection();
            ps = conn.prepareStatement(SELECT_VEHICLE_BY_USERID);
            ps.setInt(1, userID);

            rs = ps.executeQuery();

            while (rs.next()) {
                int vehicleId = rs.getInt("vehicle_ID");
                int userId = rs.getInt("user_ID");
                String plateNumber = rs.getString("plate_number");
                String vehicleType = rs.getString("vehicle_Type");
                String vehicleBrand = rs.getString("vehicle_Brand");

                Vehicle vehicle = new Vehicle(vehicleId, userId, plateNumber, vehicleType, vehicleBrand);
                vehicleList.add(vehicle);
            }
            return vehicleList;
        } catch (SQLException e) {
            System.err.println("VehicleDAO Error in getVehicleByUserID: " + e.getMessage());
            return new ArrayList<>();
        } finally {
            DBConnectionUtil.closeConnection(conn, ps, rs);
        }
    }

    /**
     * Inserts a new vehicle record into the database.
     *
     * @param vehicle The Vehicle object containing data to be inserted.
     * @return An Optional containing the new Vehicle object (with the generated ID) if successful, otherwise empty.
     */
    public Optional<Vehicle> addVehicle(Vehicle vehicle) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        int generatedID = -1;

        try {
            conn = DBConnectionUtil.getConnection();
            ps = conn.prepareStatement(INSERT_VEHICLE, Statement.RETURN_GENERATED_KEYS);

            ps.setInt(1, vehicle.getUser_id());
            ps.setString(2, vehicle.getPlate_number());
            ps.setString(3, vehicle.getVehicle_type());
            ps.setString(4, vehicle.getVehicle_brand());

            int affectedRows = ps.executeUpdate();

            if (affectedRows > 0) {
                rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    generatedID = rs.getInt(1);
                }
            }

            if (generatedID > 0) {
                Vehicle newVehicle = new Vehicle(
                        generatedID,
                        vehicle.getUser_id(),
                        vehicle.getPlate_number(),
                        vehicle.getVehicle_type(),
                        vehicle.getVehicle_brand()
                );
                return Optional.of(newVehicle);
            }

        } catch (SQLException e) {
            System.err.println("VehicleDAO Error in addVehicle: " + e.getMessage());
        } finally {
            DBConnectionUtil.closeConnection(conn, ps, rs);
        }
        return Optional.empty();
    }

    /**
     * Deletes a vehicle record from the database by its ID.
     *
     * @param vehicleId The ID of the vehicle to delete.
     * @return true if the deletion was successful, false otherwise.
     */
    public boolean deleteVehicle(int vehicleId) {
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = DBConnectionUtil.getConnection();
            ps = conn.prepareStatement(DELETE_VEHICLE);
            ps.setInt(1, vehicleId);

            int affectedRows = ps.executeUpdate();

            // Note: Due to ON DELETE CASCADE, associated reservations are also removed.
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("VehicleDAO Error in deleteVehicle: " + e.getMessage());
            return false;
        } finally {
            DBConnectionUtil.closeConnection(conn, ps);
        }
    }
}