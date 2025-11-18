package Controller;

import DAO.CustomerDAO;
import DAO.ReservationDAO;
import DAO.VehicleDAO;
import Model.DTO.ReservationSummaryDTO;
import Model.Entity.Customer;
import Model.Entity.Reservation;
import Model.Entity.User;
import Model.Entity.Vehicle;
import Service.CustomerService;
import Service.ReservationService;
import Utilities.SessionManager;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class ProfilePageController {

    // Profile Fields
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private Label emailLabel;
    @FXML private Label joinDateLabel;
    @FXML private Button editProfileButton;
    @FXML private Button saveProfileButton;

    // Vehicle Table
    @FXML private TableView<Vehicle> vehiclesTable;
    @FXML private TableColumn<Vehicle, String> plateColumn;
    @FXML private TableColumn<Vehicle, String> brandColumn;
    @FXML private TableColumn<Vehicle, String> typeColumn;

    // Reservation Table
    @FXML private TableView<ReservationSummaryDTO> reservationsTable;
    @FXML private TableColumn<ReservationSummaryDTO, Integer> transactNoColumn;
    @FXML private TableColumn<ReservationSummaryDTO, String> resPlateColumn;
    @FXML private TableColumn<ReservationSummaryDTO, String> resSlotColumn;
    @FXML private TableColumn<ReservationSummaryDTO, String> resTimeInColumn;
    @FXML private TableColumn<ReservationSummaryDTO, String> resTimeOutColumn;
    @FXML private TableColumn<ReservationSummaryDTO, String> resStatusColumn;
    @FXML private TableColumn<ReservationSummaryDTO, String> resTotalColumn;

    @FXML private Button cancelReservationButton;

    // DAOs and Services
    private CustomerDAO customerDAO;
    private VehicleDAO vehicleDAO;
    private ReservationDAO reservationDAO;
    private ReservationService reservationService; // For cancellation logic
    private CustomerService customerService;

    private User currentUser;
    private ObservableList<Vehicle> vehicleList = FXCollections.observableArrayList();
    private ObservableList<ReservationSummaryDTO> reservationList = FXCollections.observableArrayList();

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    public void initialize() {
        this.customerDAO = new CustomerDAO();
        this.vehicleDAO = new VehicleDAO();
        this.reservationDAO = new ReservationDAO();
        this.reservationService = new ReservationService();
        this.currentUser = SessionManager.getCurrentUser();
        this.customerService = new CustomerService();

        // 1. Setup table columns
        setupTableColumns();

        // 2. Load all data for the current user
        loadProfileData();
        loadVehicleData();
        loadReservationData();
    }

    private void setupTableColumns() {
        // Vehicle Table
        plateColumn.setCellValueFactory(cellData ->
                new ReadOnlyStringWrapper(cellData.getValue().getPlate_number()));

        typeColumn.setCellValueFactory(cellData ->
                new ReadOnlyStringWrapper(cellData.getValue().getVehicle_type()));

        brandColumn.setCellValueFactory(cellData ->
                new ReadOnlyStringWrapper(cellData.getValue().getVehicle_brand()));

        // Reservation Table
        transactNoColumn.setCellValueFactory(new PropertyValueFactory<>("transactNo"));
        resSlotColumn.setCellValueFactory(new PropertyValueFactory<>("resSlotColumn"));
        resStatusColumn.setCellValueFactory(new PropertyValueFactory<>("resStatusColumn"));

        // Format dates for display
        resTimeInColumn.setCellValueFactory(new PropertyValueFactory<>("resTimeInColumn"));
        resTimeOutColumn.setCellValueFactory(new PropertyValueFactory<>("resTimeOutColumn"));

        resPlateColumn.setCellValueFactory(new PropertyValueFactory<>("resPlateColumn"));
        resTotalColumn.setCellValueFactory(new PropertyValueFactory<>("resTotalColumn"));

        reservationsTable.setItems(reservationList);
    }


    private void loadProfileData() {
        if (currentUser == null) return;

        emailLabel.setText(currentUser.getEmail());
        joinDateLabel.setText(currentUser.getJoin_date().toString());

        Optional<Customer> profileOpt = customerDAO.findCustomerByUserID(currentUser.getUser_ID());
        if (profileOpt.isPresent()) {
            Customer profile = profileOpt.get();
            firstNameField.setText(profile.getFirstname());
            lastNameField.setText(profile.getSurname());
        }
    }

    private void loadVehicleData() {
        vehicleList.clear();
        List<Vehicle> vehicles = vehicleDAO.getVehicleByUserID(currentUser.getUser_ID());
        vehicleList.addAll(vehicles);
        vehiclesTable.setItems(vehicleList);
    }


    private void loadReservationData() {
        reservationList.clear();

        List<ReservationSummaryDTO> reservations =
                reservationDAO.findReservationSummariesByUserId(currentUser.getUser_ID());

        reservationList.addAll(reservations);
        reservationsTable.setItems(reservationList);
    }

    @FXML
    private void handleEditProfile(ActionEvent event) {
        firstNameField.setEditable(true);
        lastNameField.setEditable(true);
        saveProfileButton.setVisible(true);
        editProfileButton.setVisible(false);
    }

    @FXML
    private void handleSaveProfile(ActionEvent event) {
        firstNameField.setEditable(false);
        lastNameField.setEditable(false);
        saveProfileButton.setVisible(false);
        editProfileButton.setVisible(true);

        int userID = SessionManager.getCurrentUser().getUser_ID();

        Optional<Customer> customerOpt = customerDAO.findCustomerByUserID(userID);

        if (customerOpt.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Customer profile not found.");
            return;
        }

        Customer customer = customerOpt.get();

        // Update the customer object with new data from the fields
        customer.setFirstname(firstNameField.getText().trim());
        customer.setSurname(lastNameField.getText().trim());

        // Update in the database
        boolean updated = customerDAO.updateCustomer(customer);

        if (updated) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Profile updated successfully!");
        } else {
            showAlert(Alert.AlertType.ERROR, "Update Failed", "Could not update profile. Please try again.");
        }
    }

    @FXML
    private void handleAddVehicle(ActionEvent event) {
        TextInputDialog plateDialog = new TextInputDialog();
        plateDialog.setTitle("Add Vehicle");
        plateDialog.setHeaderText("Enter Plate Number:");
        String plate = plateDialog.showAndWait().orElse("").trim();
        if (plate.isEmpty()) return;

        TextInputDialog brandDialog = new TextInputDialog();
        brandDialog.setTitle("Add Vehicle");
        brandDialog.setHeaderText("Enter Vehicle Brand:");
        String brand = brandDialog.showAndWait().orElse("").trim();

        TextInputDialog typeDialog = new TextInputDialog();
        typeDialog.setTitle("Add Vehicle");
        typeDialog.setHeaderText("Enter Vehicle Type:");
        String type = typeDialog.showAndWait().orElse("").trim();

        try {
            boolean success = customerService.addVehicle(SessionManager.getCurrentUser().getUser_ID(), plate, brand, type);
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Vehicle added successfully!");
                loadVehicleData(); // refresh table
            } else {
                showAlert(Alert.AlertType.ERROR, "Failed", "Could not add vehicle. It may already exist.");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "An unexpected error occurred: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteVehicle(ActionEvent event) {
        Vehicle selectedVehicle = vehiclesTable.getSelectionModel().getSelectedItem();
        if (selectedVehicle == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a vehicle to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Vehicle");
        confirm.setHeaderText("Are you sure you want to delete this vehicle?");
        confirm.setContentText("Plate: " + selectedVehicle.getPlate_number());
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                boolean deleted = customerService.deleteVehicle(selectedVehicle.getVehicle_id());
                if (deleted) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Vehicle deleted successfully.");
                    loadVehicleData();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Failed", "Could not delete vehicle. It may have active reservations.");
                }
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Database Error", e.getMessage());
            }
        }
    }

    @FXML
    private void handleCancelReservation(ActionEvent event) {
        ReservationSummaryDTO selected = reservationsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a reservation to cancel.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Cancellation");
        confirmAlert.setHeaderText("Cancel Reservation");
        confirmAlert.setContentText("Are you sure you want to cancel this reservation?");

        if (confirmAlert.showAndWait().get() != ButtonType.OK) {
            return;
        }

        try {
            int reservationID = selected.getTransactNo();

            int userID = SessionManager.getCurrentUser().getUser_ID();

            boolean success = reservationService.cancelReservation(reservationID, userID);

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Reservation cancelled successfully.");
                loadReservationData(); // Refresh the TableView
            } else {
                showAlert(Alert.AlertType.ERROR, "Cancellation Failed", "Unable to cancel reservation.");
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "An unexpected error occurred: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
