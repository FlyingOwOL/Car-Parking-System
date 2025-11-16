package Controller.Admin;

import DAO.ParkingDAO;
import Model.Entity.Branch;
import Model.Entity.Pricing;
import Model.Entity.SlotType;
import Model.Entity.User;
import Service.Admin.BranchManagementService;
import Utilities.SessionManager;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

public class BranchManagementController {

    @FXML private TextField branchNameField;
    @FXML private TextField maxSlotsField;
    @FXML private TextField branchContactField;
    @FXML private TextField branchEmailField;
    @FXML private TextField branchLocationField;
    @FXML private TextField openingTimeField;
    @FXML private TextField closingTimeField;
    @FXML private Label branchStatusLabel;

    @FXML private ComboBox<Branch> pricingBranchComboBox;
    @FXML private ComboBox<SlotType> pricingSlotTypeComboBox;
    @FXML private TextField hourlyRateField;
    @FXML private TextField overtimeRateField;
    @FXML private Label pricingStatusLabel;

    @FXML private ComboBox<Branch> configBranchComboBox;
    @FXML private TextField spotIdField;
    @FXML private ComboBox<SlotType> newSlotTypeComboBox;
    @FXML private Label configStatusLabel;


    private BranchManagementService managementService;
    private ParkingDAO parkingDAO;
    private User adminUser;

    @FXML
    public void initialize() {
        this.managementService = new BranchManagementService();
        this.parkingDAO = new ParkingDAO();
        this.adminUser = SessionManager.getCurrentUser();

        loadBranchAndSlotData();
    }

    /**
     * Loads branches and slot types into all necessary ComboBoxes.
     */
    private void loadBranchAndSlotData() {
        List<Branch> allBranches = parkingDAO.getAllBranches();

        pricingBranchComboBox.setItems(FXCollections.observableArrayList(allBranches));
        configBranchComboBox.setItems(FXCollections.observableArrayList(allBranches));

        StringConverter<Branch> branchConverter = new StringConverter<>() {
            @Override public String toString(Branch b) { return b == null ? null : b.getName(); }
            @Override public Branch fromString(String s) { return null; }
        };
        pricingBranchComboBox.setConverter(branchConverter);
        configBranchComboBox.setConverter(branchConverter);

        pricingSlotTypeComboBox.setItems(FXCollections.observableArrayList(SlotType.values()));
        newSlotTypeComboBox.setItems(FXCollections.observableArrayList(SlotType.values()));
    }

    @FXML
    private void handleCreateNewBranch(ActionEvent event) {
        branchStatusLabel.setText(""); // Clear status

        try {
            String name = branchNameField.getText();
            int maxSlots = Integer.parseInt(maxSlotsField.getText());
            String contact = branchContactField.getText();
            String email = branchEmailField.getText();
            String location = branchLocationField.getText();
            LocalTime open = LocalTime.parse(openingTimeField.getText());
            LocalTime close = LocalTime.parse(closingTimeField.getText());

            Branch newBranch = new Branch(name, contact, email, maxSlots, location, open, close);

            int newId = managementService.createNewBranch(adminUser, newBranch);

            if (newId > 0) {
                branchStatusLabel.setText("SUCCESS: Branch '" + name + "' created.");
                branchStatusLabel.setStyle("-fx-text-fill: green;");
                loadBranchAndSlotData();
            } else {
                branchStatusLabel.setText("ERROR: Failed to create branch.");
                branchStatusLabel.setStyle("-fx-text-fill: red;");
            }

        } catch (NumberFormatException e) {
            branchStatusLabel.setText("Error: Max Slots must be a number.");
            branchStatusLabel.setStyle("-fx-text-fill: red;");
        } catch (SecurityException e) {
            branchStatusLabel.setText("ERROR: Authorization failed.");
            branchStatusLabel.setStyle("-fx-text-fill: red;");
        } catch (Exception e) {
            branchStatusLabel.setText("ERROR: " + e.getMessage());
            branchStatusLabel.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML
    private void handleSetPricingRule(ActionEvent event) {
        pricingStatusLabel.setText(""); // Clear status

        try {
            Branch branch = pricingBranchComboBox.getValue();
            SlotType slotType = pricingSlotTypeComboBox.getValue();
            BigDecimal hourlyRate = new BigDecimal(hourlyRateField.getText());
            BigDecimal overtimeRate = new BigDecimal(overtimeRateField.getText());

            if (branch == null || slotType == null || hourlyRate.signum() <= 0) {
                pricingStatusLabel.setText("Error: Fill all fields with positive rates.");
                return;
            }

            // Create Pricing object
            Pricing newRule = new Pricing(branch.getBranch_ID(), slotType, hourlyRate, overtimeRate);

            boolean success = managementService.setPricing(adminUser, newRule);

            if (success) {
                pricingStatusLabel.setText("SUCCESS: Pricing rule set for " + branch.getName() + " (" + slotType + ").");
                pricingStatusLabel.setStyle("-fx-text-fill: green;");
            } else {
                pricingStatusLabel.setText("ERROR: Failed to update pricing rule.");
                pricingStatusLabel.setStyle("-fx-text-fill: red;");
            }

        } catch (NumberFormatException e) {
            pricingStatusLabel.setText("Error: Rates must be valid numbers (e.g., 50.00).");
            pricingStatusLabel.setStyle("-fx-text-fill: red;");
        } catch (SecurityException e) {
            pricingStatusLabel.setText("ERROR: Authorization failed.");
            pricingStatusLabel.setStyle("-fx-text-fill: red;");
        } catch (Exception e) {
            pricingStatusLabel.setText("ERROR: " + e.getMessage());
            pricingStatusLabel.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML
    private void handleUpdateSlotType(ActionEvent event) {
        configStatusLabel.setText("");

        try {
            Branch branch = configBranchComboBox.getValue();
            SlotType newType = newSlotTypeComboBox.getValue();
            String spotId = spotIdField.getText();

            if (branch == null || newType == null || spotId.isEmpty()) {
                configStatusLabel.setText("Error: Select branch, spot ID, and new type.");
                return;
            }

            // Call Service
            boolean success = managementService.updateSlotType(adminUser, spotId, newType);

            if (success) {
                configStatusLabel.setText("SUCCESS: Slot " + spotId + " updated to " + newType + ".");
                configStatusLabel.setStyle("-fx-text-fill: green;");
            } else {
                configStatusLabel.setText("ERROR: Update failed. Slot may be occupied or ID is invalid.");
                configStatusLabel.setStyle("-fx-text-fill: red;");
            }

        } catch (SecurityException e) {
            configStatusLabel.setText("ERROR: Authorization failed.");
            configStatusLabel.setStyle("-fx-text-fill: red;");
        } catch (Exception e) {
            configStatusLabel.setText("ERROR: " + e.getMessage());
            configStatusLabel.setStyle("-fx-text-fill: red;");
        }
    }
}
