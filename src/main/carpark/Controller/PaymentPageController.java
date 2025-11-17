package Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import Model.Entity.Payment;
import Model.Entity.Reservation;
import Model.Entity.Payment.ModeOfPayment;
import Service.PaymentService;
import Utilities.SessionManager;

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
    private Reservation currentReservation;
    private float totalAmount;
    private int currentReservationId;
    private ToggleGroup paymentMethodGroup;

    /**
     * Initializes the controller class.
     * This method is automatically called after the FXML file has been loaded.
     */
    @FXML
    public void initialize() {
        this.paymentService = new PaymentService();
        
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
        System.out.println("Reservation found: " + currentReservation.getID());

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
            // Convert string to enum
            ModeOfPayment modeOfPayment = ModeOfPayment.valueOf(paymentMethod);
            int adminId = SessionManager.getCurrentUser().getUser_ID();

            System.out.println("Processing payment with: ReservationID=" + currentReservationId + 
                             ", Method=" + modeOfPayment + ", AdminID=" + adminId);

            // Process payment with actual user input
            Optional<Payment> payment = paymentService.processPayment(
                currentReservationId, modeOfPayment, adminId
            );

            if (payment.isPresent()) {
                System.out.println("Payment successful! Payment ID: " + payment.get().getPayment_ID());
                // Store payment details (in a real app, you'd save this to database)
                storePaymentDetails(paymentMethod);
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
     * Stores payment details (in a real application, this would save to database)
     */
    private void storePaymentDetails(String paymentMethod) {
        String paymentDetails = "";
        
        if (creditCardRadioButton.isSelected()) {
            paymentDetails = String.format(
                "Credit Card - Number: %s, Expiry: %s, CVV: %s",
                maskCardNumber(cardNumberField.getText()),
                expiryDateField.getText(),
                "***" // Never store actual CVV
            );
        } else if (ewalletRadioButton.isSelected()) {
            paymentDetails = String.format(
                "E-Wallet - Number: %s",
                ewalletNumberField.getText()
            );
        } else {
            paymentDetails = "Cash Payment";
        }
        
        System.out.println("Payment details stored: " + paymentDetails);
        // In a real app: Save to database or payment log
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
        try {
            loadScene("/fxml/reservation_page.fxml", event);
        } catch (IOException e) {
            showError("Error: Could not load the reservation page.");
            e.printStackTrace();
        }
    }

    /**
     * Sets the reservation data to display on the payment page.
     * This should be called when navigating from the reservation page.
     *
     * @param reservation The reservation object containing booking details
     * @param amount The total amount to be paid
     * @param reservationId The ID of the reservation
     * @param branchName The name of the branch
     * @param vehiclePlate The plate number of the vehicle
     */
    public void setReservationData(Reservation reservation, float amount, int reservationId, String branchName, String vehiclePlate) {
        this.currentReservation = reservation;
        this.totalAmount = amount;
        this.currentReservationId = reservationId;
        
        // Update the UI with reservation details
        updateReservationDisplay(branchName, vehiclePlate);
    }

    /**
     * Updates the UI with reservation details and fee information.
     */
    private void updateReservationDisplay(String branchName, String vehiclePlate) {
        if (currentReservation != null) {
            reservationIdLabel.setText("RES-" + currentReservation.getID());
            branchNameLabel.setText(branchName);
            slotTypeLabel.setText(currentReservation.getSlotType().toString());
            vehiclePlateLabel.setText(vehiclePlate);
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            checkInTimeLabel.setText(currentReservation.getCheckInTime().format(formatter));
            
            if (currentReservation.getTimeOut() != null) {
                checkOutTimeLabel.setText(currentReservation.getTimeOut().format(formatter));
            } else {
                checkOutTimeLabel.setText("Not checked out");
            }
            
            totalHoursLabel.setText(currentReservation.getReserved_hours() + " hours");
            totalAmountLabel.setText("₱" + String.format("%.2f", totalAmount));
            
            // Calculate and display hourly rate
            if (currentReservation.getReserved_hours() > 0) {
                float hourlyRate = totalAmount / currentReservation.getReserved_hours();
                hourlyRateLabel.setText("₱" + String.format("%.2f", hourlyRate) + "/hour");
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
            try {
                // Navigate to dashboard using the confirm payment button's scene
                Stage stage = (Stage) confirmPaymentButton.getScene().getWindow();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/customer_dashboard.fxml"));
                Parent root = loader.load();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.show();
                System.out.println("Navigation to dashboard successful!");
            } catch (IOException e) {
                e.printStackTrace();
                showError("Error: Could not load the dashboard.");
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
}
