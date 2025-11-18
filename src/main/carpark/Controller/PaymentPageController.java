package Controller;

import DAO.ParkingDAO;
import DAO.VehicleDAO;
import Model.Entity.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import Model.Entity.Payment.ModeOfPayment;
import Service.PaymentService;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Controller for the payment.fxml file.
 * Handles payment processing and confirmation for parking reservations.
 * Task 4.3: Payment Processing
 */
public class PaymentPageController {

    @FXML
    private Label reservationIdLabel;
    @FXML
    private Label branchNameLabel;
    @FXML
    private Label slotTypeLabel;
    @FXML
    private Label vehiclePlateLabel;
    @FXML
    private Label checkInTimeLabel;
    @FXML
    private Label checkOutTimeLabel;
    @FXML
    private Label totalHoursLabel;
    @FXML
    private Label hourlyRateLabel;
    @FXML
    private Label totalAmountLabel;
    @FXML
    private RadioButton cashRadioButton;
    @FXML
    private RadioButton ewalletRadioButton;
    @FXML
    private RadioButton creditCardRadioButton;
    @FXML
    private Button confirmPaymentButton;
    @FXML
    private Button backButton;
    @FXML
    private Label statusLabel;

    // Payment detail sections
    @FXML
    private VBox cardDetailsSection;
    @FXML
    private VBox ewalletDetailsSection;

    // Payment input fields
    @FXML
    private TextField cardNumberField;
    @FXML
    private TextField expiryDateField;
    @FXML
    private TextField cvvField;
    @FXML
    private TextField ewalletNumberField;
    @FXML
    private TextField ewalletPinField;

    private PaymentService paymentService;
    private ParkingDAO parkingDAO;
    private VehicleDAO vehicleDAO;
    private Reservation currentReservation;
    private float totalAmount;
    private int currentReservationId;
    private ToggleGroup paymentMethodGroup;

    private CustomerDashboardController dashboardController;

    /**
     * Initializes the controller class.
     * This method is automatically called after the FXML file has been loaded.
     */
    @FXML
    public void initialize() {
        this.paymentService = new PaymentService();
        this.parkingDAO = new ParkingDAO();
        this.vehicleDAO = new VehicleDAO();

        // Set up payment method selection
        paymentMethodGroup = new ToggleGroup();
        cashRadioButton.setToggleGroup(paymentMethodGroup);
        ewalletRadioButton.setToggleGroup(paymentMethodGroup);
        creditCardRadioButton.setToggleGroup(paymentMethodGroup);
        
        // Set default selection
        cashRadioButton.setSelected(true);
        
        // Set up payment detail visibility
        setupPaymentDetailVisibility();
        
        // Add listener to show/hide payment details based on selection
        paymentMethodGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            updatePaymentDetailVisibility();
        });
        
        statusLabel.setText("");
    }

    /**
     * Sets up initial visibility of payment detail sections
     */
    private void setupPaymentDetailVisibility() {
        // Initially hide all payment detail sections
        cardDetailsSection.setVisible(false);
        cardDetailsSection.setManaged(false);
        ewalletDetailsSection.setVisible(false);
        ewalletDetailsSection.setManaged(false);
        
        // Clear all input fields
        clearPaymentInputs();
    }

    /**
     * Updates visibility of payment detail sections based on selected method
     */
    private void updatePaymentDetailVisibility() {
        // Hide all sections first
        cardDetailsSection.setVisible(false);
        cardDetailsSection.setManaged(false);
        ewalletDetailsSection.setVisible(false);
        ewalletDetailsSection.setManaged(false);
        
        // Show relevant section based on selection
        if (creditCardRadioButton.isSelected()) {
            cardDetailsSection.setVisible(true);
            cardDetailsSection.setManaged(true);
        } else if (ewalletRadioButton.isSelected()) {
            ewalletDetailsSection.setVisible(true);
            ewalletDetailsSection.setManaged(true);
        }
        
        // Clear inputs when switching methods
        clearPaymentInputs();
    }

    /**
     * Clears all payment input fields
     */
    private void clearPaymentInputs() {
        cardNumberField.clear();
        expiryDateField.clear();
        cvvField.clear();
        ewalletNumberField.clear();
        ewalletPinField.clear();
    }

    /**
     * Handles the action event when the "Confirm Payment" button is clicked.
     * This method is linked from the 'onAction' property of the Button in FXML.
     *
     * @param event The ActionEvent from the button click.
     */
    @FXML
    protected void handleConfirmationBtn(ActionEvent event) {
        System.out.println("=== PAYMENT PROCESS STARTED ===");
        statusLabel.setText("");
        
        // Validate that we have reservation data
        if (currentReservation == null) {
            System.out.println("ERROR: No reservation data");
            showError("No reservation data available. Please create a reservation first.");
            return;
        }
        System.out.println("Reservation found: " + currentReservation.getReservationID());

        String paymentMethod = getSelectedPaymentMethod();
        System.out.println("Payment method: " + paymentMethod);
        
        if (paymentMethod == null) {
            showError("Please select a payment method.");
            return;
        }

        // Validate payment details based on selected method
        if (!validatePaymentDetails()) {
            return;
        }

        try {
            ModeOfPayment modeOfPayment = ModeOfPayment.valueOf(paymentMethod);
            int adminId = 1;

            System.out.println("Processing payment with: ReservationID=" + currentReservationId + 
                             ", Method=" + modeOfPayment + ", AdminID=" + adminId);

            // Process payment with actual user input
            Optional<Payment> payment = paymentService.processPayment(
                currentReservationId, modeOfPayment, adminId
            );

            if (payment.isPresent()) {
                System.out.println("Payment successful! Payment ID: " + payment.get().getPayment_ID());
                showPaymentSuccess(payment.get());
            } else {
                System.out.println("Payment failed - returned empty");
                showError("Payment failed. Please try again.");
            }

        } catch (Exception e) {
            System.out.println("Payment error: " + e.getMessage());
            e.printStackTrace();
            showError("An unexpected error occurred. Please try again.");
        }
    }

    /**
     * Validates payment details based on selected method
     */
    private boolean validatePaymentDetails() {
        if (creditCardRadioButton.isSelected()) {
            // Validate credit card details
            if (cardNumberField.getText().isEmpty() || 
                expiryDateField.getText().isEmpty() || 
                cvvField.getText().isEmpty()) {
                showError("Please fill all credit card details.");
                return false;
            }
            
            // Validate card number format (16 digits)
            String cardNumber = cardNumberField.getText().replaceAll("\\s", "");
            if (cardNumber.length() != 16 || !cardNumber.matches("\\d+")) {
                showError("Please enter a valid 16-digit card number.");
                return false;
            }
            
            // Validate expiry date format (MM/YY)
            if (!expiryDateField.getText().matches("(0[1-9]|1[0-2])/[0-9]{2}")) {
                showError("Please enter expiry date in MM/YY format.");
                return false;
            }
            
            // Validate CVV (3 digits)
            if (!cvvField.getText().matches("\\d{3}")) {
                showError("Please enter a valid 3-digit CVV.");
                return false;
            }
            
        } else if (ewalletRadioButton.isSelected()) {
            // Validate e-wallet details
            if (ewalletNumberField.getText().isEmpty() || 
                ewalletPinField.getText().isEmpty()) {
                showError("Please fill all e-wallet details.");
                return false;
            }
            
            // Validate e-wallet number format (Philippine mobile number)
            if (!ewalletNumberField.getText().matches("09\\d{9}")) {
                showError("Please enter a valid Philippine mobile number (09XXXXXXXXX).");
                return false;
            }
            
            // Validate PIN (4-6 digits)
            if (!ewalletPinField.getText().matches("\\d{4,6}")) {
                showError("Please enter a valid 4-6 digit MPIN.");
                return false;
            }
        }
        
        return true;
    }

    /**
     * Masks credit card number for security (shows only last 4 digits)
     */
    private String maskCardNumber(String cardNumber) {
        if (cardNumber.length() <= 4) return cardNumber;
        return "****-****-****-" + cardNumber.substring(cardNumber.length() - 4);
    }

    /**
     * Handles the action event when the "Back" button is clicked.
     * This method is linked from the 'onAction' property of the Button in FXML.
     *
     * @param event The ActionEvent from the button click.
     */
    @FXML
    protected void handleBackBtn(ActionEvent event) {
        if (this.dashboardController != null) {
            this.dashboardController.returnToReservationPage();
        } else {
            System.err.println("CRITICAL: Cannot find dashboard controller to navigate back.");
        }
    }

    /**
     * Sets the reservation data to display on the payment page.
     * This should be called when navigating from the reservation page.
     *
     * @param reservation The reservation object containing booking details
     */
    public void setReservationData(Reservation reservation) {
        this.currentReservation = reservation;
        this.currentReservationId = reservation.getReservationID();

        String spotId = reservation.getSpotID();
        String branchName = "--";
        String vehiclePlate = "--";
        float calculatedAmount = 0.0f;

        try {
            // 1. Fetch auxiliary data (Branch Name, Vehicle Plate)
            Optional<Vehicle> vehicleOpt = vehicleDAO.findVehicleById(reservation.getVehicleID()); // Assuming a new DAO method

            Optional<ParkingSlot> slotOpt = parkingDAO.getSlotByID(reservation.getSpotID());
            Optional<Branch> branchOpt = parkingDAO.getAllBranches().stream()
                    .filter(b -> slotOpt.isPresent() && b.getBranch_ID() == slotOpt.get().getBranch_ID())
                    .findFirst();

            // 2. Calculate Final Amount (Using the service logic for accuracy)
            if (slotOpt.isPresent()) {
                var pricingOpt = parkingDAO.getPricingRule(slotOpt.get().getBranch_ID(), slotOpt.get().getSlot_type());
                if (pricingOpt.isPresent()) {
                    calculatedAmount = paymentService.calculateTotalFee(reservation, pricingOpt.get());
                }
            }

            branchName = branchOpt.map(Branch::getName).orElse("N/A");
            vehiclePlate = vehicleOpt.map(Vehicle::getPlate_number).orElse("N/A");
            this.totalAmount = calculatedAmount;

        } catch (Exception e) {
            System.err.println("Error fetching auxiliary data for payment page: " + e.getMessage());
        }

        updateReservationDisplay(branchName, vehiclePlate);
    }

    /**
     * Updates the UI with reservation details and fee information.
     */
    private void updateReservationDisplay(String branchName, String vehiclePlate) {
        if (currentReservation != null) {
            reservationIdLabel.setText("RES-" + currentReservation.getReservationID());
            branchNameLabel.setText(branchName);
            slotTypeLabel.setText(currentReservation.getSpotID());
            vehiclePlateLabel.setText(vehiclePlate);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            checkInTimeLabel.setText(currentReservation.getCheckInTime() != null ? currentReservation.getCheckInTime().format(formatter) : "N/A");
            checkOutTimeLabel.setText(currentReservation.getTimeOut() != null ? currentReservation.getTimeOut().format(formatter) : "Not Checked Out");

            totalHoursLabel.setText(currentReservation.getReserved_hours() + " hours");
            totalAmountLabel.setText("₱" + String.format("%,.2f", totalAmount));

            if (currentReservation.getReserved_hours() > 0) {
                float hourlyRate = totalAmount / currentReservation.getReserved_hours();
                hourlyRateLabel.setText("₱" + String.format("%,.2f", hourlyRate) + "/hour");
            }
        } else {
            // Show default values if no reservation data
            reservationIdLabel.setText("--");
            branchNameLabel.setText("--");
            slotTypeLabel.setText("--");
            vehiclePlateLabel.setText("--");
            checkInTimeLabel.setText("--");
            checkOutTimeLabel.setText("--");
            totalHoursLabel.setText("--");
            hourlyRateLabel.setText("--");
            totalAmountLabel.setText("--");
        }
    }

    /**
     * Gets the selected payment method from radio buttons.
     *
     * @return The selected payment method as a string, or null if none selected
     */
    private String getSelectedPaymentMethod() {
        if (cashRadioButton.isSelected()) {
            return "CASH";
        } else if (ewalletRadioButton.isSelected()) {
            return "E_WALLET";
        } else if (creditCardRadioButton.isSelected()) {
            return "CREDIT_CARD";
        }
        return null;
    }

    /**
     * Shows payment success message and redirects to dashboard.
     */
    private void showPaymentSuccess(Payment payment) {
        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
        successAlert.setTitle("Payment Successful");
        successAlert.setHeaderText("Payment Completed Successfully!");
        successAlert.setContentText(
            "Payment ID: " + payment.getPayment_ID() + "\n" +
            "Amount Paid: ₱" + String.format("%.2f", payment.getAmount_paid()) + "\n" +
            "Payment Method: " + payment.getMode_of_payment() + "\n" +
            "Reservation ID: " + currentReservationId + "\n" +
            "Thank you for your payment!"
        );

        // Wait for user to close the alert, then navigate
        successAlert.showAndWait().ifPresent(response -> {
            if (this.dashboardController != null) {
                // FIX: Navigate using the public method on the parent controller
                this.dashboardController.returnToHome();
            } else {
                // Fallback (Should not be hit if injection is correct)
                System.err.println("CRITICAL: Dashboard controller missing.");
            }
        });
    }

    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
    }

    private void loadScene(String fxmlPath, ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();

        Stage stage;
        if (event != null) {
            stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        } else {
            stage = (Stage) confirmPaymentButton.getScene().getWindow();
        }

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void setDashboardController(CustomerDashboardController controller) {
        this.dashboardController = controller;
    }

}
