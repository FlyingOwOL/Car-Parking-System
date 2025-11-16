package Controller.Admin;

import DAO.ParkingDAO;
import Model.Entity.Admin;
import Model.Entity.Branch;
import Model.Entity.User;
import Service.Admin.AdminService;
import Utilities.SessionManager;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.util.List;

public class AdminRegistrationController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField contactField;
    @FXML private TextField jobTitleField;
    @FXML private ComboBox<Branch> branchComboBox;
    @FXML private Label statusLabel;

    private AdminService adminService;
    private ParkingDAO parkingDAO;
    private UserManagementController parentController; // Reference to refresh main table

    @FXML
    public void initialize() {
        this.adminService = new AdminService();
        this.parkingDAO = new ParkingDAO();

        loadBranches();
    }

    /**
     * Called by the parent controller to pass a reference for table refreshing.
     */
    public void setParentController(UserManagementController parentController) {
        this.parentController = parentController;
    }

    private void loadBranches() {
        List<Branch> allBranches = parkingDAO.getAllBranches();
        branchComboBox.setItems(FXCollections.observableArrayList(allBranches));

        // Define how Branch objects are displayed in the ComboBox
        branchComboBox.setConverter(new StringConverter<>() {
            @Override public String toString(Branch b) { return b == null ? null : b.getName(); }
            @Override public Branch fromString(String s) { return null; }
        });

        // Add a "No Branch Assigned" option for optional assignment
        branchComboBox.getItems().add(0, null);
        branchComboBox.getSelectionModel().select(0);
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        statusLabel.setText("");

        String email = emailField.getText();
        String password = passwordField.getText();
        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();
        String contact = contactField.getText();
        String jobTitle = jobTitleField.getText();
        Branch selectedBranch = branchComboBox.getValue();

        if (email.isEmpty() || password.isEmpty() || firstName.isEmpty() || jobTitle.isEmpty()) {
            statusLabel.setText("Error: Email, password, first name, and job title are required.");
            return;
        }

        int branchId = selectedBranch != null ? selectedBranch.getBranch_ID() : 0; // Use 0 if null for non-assignment
        Admin newProfile = new Admin(
                0, // user_ID is placeholder until DB returns it
                firstName, lastName, contact, jobTitle, branchId
        );

        try {
            User callingAdmin = SessionManager.getCurrentUser();
            boolean success = adminService.createNewAdmin(callingAdmin, email, password, newProfile);

            if (success) {
                statusLabel.setText("SUCCESS: New admin registered!");
                statusLabel.setStyle("-fx-text-fill: green;");

                if (parentController != null) {
                    parentController.loadUserData();
                }
                handleCancel(event);

            } else {
                statusLabel.setText("ERROR: Registration failed. Email may already be in use.");
                statusLabel.setStyle("-fx-text-fill: red;");
            }
        } catch (SecurityException e) {
            statusLabel.setText("ERROR: Authorization failed.");
            statusLabel.setStyle("-fx-text-fill: red;");
        } catch (Exception e) {
            statusLabel.setText("ERROR: An unexpected error occurred.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        // Close the current stage (window)
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }
}
