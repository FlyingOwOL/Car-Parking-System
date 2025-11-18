package Service;

import DAO.VehicleDAO;
import Model.Entity.Vehicle;

import java.sql.SQLException;
import java.util.Optional;

public class CustomerService {

    VehicleDAO vehicleDAO = new VehicleDAO();

    /*
    Method: registerAccount(...), updateProfile?, addVehicle()?
    TODO: SY
    4.1 User Account Registration as a Transaction
     */
    public void registerAccount(){

    }

    public void updateProfile(){

    }

    public boolean addVehicle(int userID, String plateNumber, String brand, String type) throws SQLException {
        Vehicle vehicle = new Vehicle(0, userID, plateNumber, type, brand);
        Optional<Vehicle> added = vehicleDAO.addVehicle(vehicle);
        return added.isPresent();
    }

    public boolean deleteVehicle(int vehicleID) throws SQLException {
        return vehicleDAO.deleteVehicle(vehicleID);
    }
}
