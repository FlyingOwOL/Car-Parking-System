package Controller;

import DAO.ParkingDAO;
import DAO.VehicleDAO;
import Model.Entity.*;
import Service.ReservationService;
import Utilities.SessionManager;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ReservationPageController {

    @FXML private ComboBox<Vehicle> vehicleComboBox;
    @FXML private ComboBox<Branch> locationComboBox;
    @FXML private ComboBox<SlotType> slotTypeComboBox;
    @FXML private Label availableSpacesLabel;
    @FXML private DatePicker entryDatePicker;
    @FXML private ComboBox<Integer> entryTimeHour;
    @FXML private ComboBox<Integer> entryTimeMinute;
    @FXML private DatePicker exitDatePicker;
    @FXML private ComboBox<Integer> exitTimeHour;
    @FXML private ComboBox<Integer> exitTimeMinute;
    @FXML private Label hourlyRateLabel;
    @FXML private Label totalHoursLabel;
    @FXML private Label computedPriceLabel;
    @FXML private Button proceedToPaymentButton;

    // DAOs and Services
    private VehicleDAO vehicleDAO;
    private ParkingDAO parkingDAO;
    private ReservationService reservationService;

    @FXML
    public void initialize() {
        this.vehicleDAO = new VehicleDAO();
        this.parkingDAO = new ParkingDAO();
        this.reservationService = new ReservationService();

        // 1. Populate Vehicle ComboBox
        int currentUserId = SessionManager.getCurrentUser().getUser_ID();
        List<Vehicle> userVehicles = vehicleDAO.getVehicleByUserID(currentUserId);
        vehicleComboBox.setItems(FXCollections.observableArrayList(userVehicles));
        vehicleComboBox.setConverter(new StringConverter<>() {
            @Override public String toString(Vehicle v) { return v == null ? null : v.getPlate_number() + " (" + v.getVehicle_type() + ")"; }
            @Override public Vehicle fromString(String s) { return null; }
        });

        // 2. Populate Location ComboBox
        List<Branch> allBranches = parkingDAO.getAllBranches();
        locationComboBox.setItems(FXCollections.observableArrayList(allBranches));
        locationComboBox.setConverter(new StringConverter<>() {
            @Override public String toString(Branch b) { return b == null ? null : b.getName(); }
            @Override public Branch fromString(String s) { return null; }
        });

        // 3. Populate Slot Type ComboBox
        slotTypeComboBox.setItems(FXCollections.observableArrayList(SlotType.values()));

        // 4. Populate Time ComboBoxes
        List<Integer> hours = IntStream.range(0, 24).boxed().collect(Collectors.toList());
        List<Integer> minutes = IntStream.range(0, 60).boxed().filter(m -> m % 5 == 0).collect(Collectors.toList()); // 0, 15, 30, 45
        entryTimeHour.setItems(FXCollections.observableArrayList(hours));
        exitTimeHour.setItems(FXCollections.observableArrayList(hours));
        entryTimeMinute.setItems(FXCollections.observableArrayList(minutes));
        exitTimeMinute.setItems(FXCollections.observableArrayList(minutes));

        // 5. Set default times
        entryDatePicker.setValue(LocalDate.now());
        exitDatePicker.setValue(LocalDate.now().plusDays(1));

        // 6. Add listener to update available spaces
        slotTypeComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> updateAvailableSpaces());
        locationComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> updateAvailableSpaces());

        // 7. Initial compute
        computePrice();
    }

    @FXML
    private void updateAvailableSpaces() {
        Branch branch = locationComboBox.getValue();
        SlotType slotType = slotTypeComboBox.getValue();
        if (branch != null && slotType != null) {
            List<ParkingSlot> slots = parkingDAO.getAvailableSlots(branch.getBranch_ID(), slotType);
            availableSpacesLabel.setText("Available Spaces: " + (slots != null ? slots.size() : 0));
        } else {
            availableSpacesLabel.setText("Available Spaces: N/A");
        }
    }

    @FXML
    private void computePrice(ActionEvent event) {
        computePrice();
    }

    private void computePrice() {
        Branch branch = locationComboBox.getValue();
        SlotType slotType = slotTypeComboBox.getValue();

        if (branch == null || slotType == null || entryDatePicker.getValue() == null || exitDatePicker.getValue() == null
                || entryTimeHour.getValue() == null || entryTimeMinute.getValue() == null
                || exitTimeHour.getValue() == null || exitTimeMinute.getValue() == null)  {
            hourlyRateLabel.setText("Hourly Rate: ₱0.00");
            totalHoursLabel.setText("Total Hours: 0");
            computedPriceLabel.setText("TOTAL PRICE: ₱0.00");
            return;
        }

        // Combine Date and Time
        LocalDateTime entry = entryDatePicker.getValue().atTime(entryTimeHour.getValue(), entryTimeMinute.getValue());
        LocalDateTime exit = exitDatePicker.getValue().atTime(exitTimeHour.getValue(), exitTimeMinute.getValue());

        if (entry.isAfter(exit) || entry.isEqual(exit)) {
            hourlyRateLabel.setText("Hourly Rate: ₱0.00");
            totalHoursLabel.setText("Total Hours: 0");
            computedPriceLabel.setText("Error: Exit time must be after entry time.");
            return;
        }

        // Get pricing rule from DAO
        Optional<Pricing> ruleOpt = parkingDAO.getPricingRule(branch.getBranch_ID(), slotType);
        if (ruleOpt.isEmpty()) {
            hourlyRateLabel.setText("Hourly Rate: N/A");
            totalHoursLabel.setText("Total Hours: N/A");
            computedPriceLabel.setText("No pricing available for this slot type.");
            return;
        }

        // --- THIS IS THE PRICE BREAKDOWN LOGIC ---
        Pricing rule = ruleOpt.get();
        long hours = ChronoUnit.HOURS.between(entry, exit);

        // Check if there are any remaining minutes and round up
        if (ChronoUnit.MINUTES.between(entry, exit) % 60 > 0) {
            hours++; // Round up to the next full hour
        }

        if (hours == 0) {
            hours = 1; // Minimum 1 hour charge
        }

        BigDecimal price = ruleOpt.get().getHourly_rate().multiply(new BigDecimal(hours));
        hourlyRateLabel.setText("Hourly Rate: ₱" + String.format("%.2f", rule.getHourly_rate()));
        totalHoursLabel.setText("Total Hours: " + hours);
        computedPriceLabel.setText("TOTAL PRICE: ₱" + String.format("%.2f", price));

    }

    /*
    @FXML
    private void handleProceedToPaymentClick(ActionEvent event) {

        // 1. Get all data from fields
        Vehicle vehicle = vehicleComboBox.getValue();
        Branch branch = locationComboBox.getValue();
        SlotType slotType = slotTypeComboBox.getValue();
        LocalDateTime entry = entryDatePicker.getValue().atTime(entryTimeHour.getValue(), entryTimeMinute.getValue());

        // 2. Call the ReservationService
        Optional<Reservation> newReservation = reservationService.createReservation(
                SessionManager.getCurrentUser().getUser_ID(),
                vehicle.getVehicle_id(),
                branch.getBranch_ID(),
                slotType,
                entry
        );

        if (newReservation.isPresent()) {
            // 3. Navigate to the actual payment page
            System.out.println("Reservation created! ID: " + newReservation.get().getTransactNo());
            // TODO: Load the payment_page.fxml here
            // We pass the new reservation object to the payment controller
        } else {
            // Show error (e.g., no slots left)
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Reservation Failed");
            alert.setHeaderText("No available slots found.");
            alert.setContentText("Could not find an available slot for the selected branch and type.");
            alert.showAndWait();
        }
    }

     */
}