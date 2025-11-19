package Service;

import DAO.CustomerDAO;
import DAO.VehicleDAO;
import Model.Entity.Customer;
import Model.Entity.User;
import Model.Entity.Vehicle;

import java.sql.SQLException;
import java.util.Optional;

public class CustomerService {

    VehicleDAO vehicleDAO = new VehicleDAO();
    CustomerDAO customerDAO = new CustomerDAO();

    /*
    Method: registerAccount(...), updateProfile?, addVehicle()?
     SY
    4.1 User Account Registration as a Transaction
     */
    public boolean registerAccount(String firstname, String surname, String contact_number, User user){
        Customer newCustomer = new Customer(firstname, surname, contact_number);
        int      userId = user.getUser_ID();

        newCustomer.setUser_ID(userId);

        Optional<Customer> added = customerDAO.addCustomer(newCustomer);
        return added.isPresent();
    }

    public boolean updateProfile(Customer customer){
        return customerDAO.updateCustomer(customer);
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
