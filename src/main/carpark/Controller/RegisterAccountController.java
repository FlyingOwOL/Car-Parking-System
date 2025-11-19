package Controller;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;
import Model.Entity.UserRole;
import Model.Entity.Admin;
import Model.Entity.User;
import Service.CustomerService;
import Service.UserService;
import Service.Admin.AdminService;
import Utilities.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class RegisterAccountController {
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private ComboBox<String> roleField;
    private Label errorLabel;
    @FXML 
    private Button registerButton;
    @FXML
    private Button returnButton;
    @FXML
    private TextField firstnameField;
    @FXML
    private TextField surnameField;
    @FXML
    private TextField contactNumberField;
    @FXML
    private TextField jobTitleField;
    @FXML
    private TextField branchIDField;

    private UserService     userService;
    private CustomerService customerService;
    private AdminService    adminService;

    @FXML
    public void initialize(){
        this.userService     = new UserService();
        this.customerService = new CustomerService();
        this.adminService    = new AdminService();
        returnButton.setOnAction(this::handleReturnToLoginAction);
        registerButton.setOnAction(this::handleRegisterButtonAction);
        roleField.getItems().addAll("Admin", "Customer");
        errorLabel.setText("");
    }

    /**
     * Handles the action event when the "Register" button is clicked
     * This method is linked from the 'onAction' property of the Button in FXML.
     * 
     * @param event
     */
    @FXML
    protected void handleRegisterButtonAction(ActionEvent event){
        String email         = emailField.getText();
        String password      = passwordField.getText();
        String role          = roleField.getValue().toUpperCase();  
        String firstname     = firstnameField.getText();
        String surname       = surnameField.getText();
        String contactNumber = contactNumberField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Email, password, and role must not be empty.");
            return;
        }
        if (firstname.isEmpty() || surname.isEmpty() || contactNumber.isEmpty()){
            showError("Firstname, surname, and contact number must not be empty.");
            return;
        }
        if (!validNumber(contactNumber)){
            showError("Contact number must be 11 long");
            return;
        }
        try {
            Optional<User> userOptional = userService.register(email, password, role);
            User user = userOptional.get();
            boolean isSuccesful = false;

            if (role == "Admin"){
                isSuccesful = addAdmin(user, email, password, firstname, surname, contactNumber);
            } else {
                isSuccesful = addCustomer(user, firstname, surname, contactNumber);
            }

            if (isSuccesful){
                SessionManager.login(user);
                redirectToDashboard(user, event);                
            }
        } catch (Exception e) {
            showError("An unexpected error occured. Please try again.");
            e.printStackTrace();
        }
    }

    private boolean addAdmin(User user, String email, String password, String firstname, String surname, String contactNumber){
        String jobTitle = jobTitleField.getText();
        int    branchID = Integer.parseInt(branchIDField.getText());

        Admin newAdmin = new Admin();
        newAdmin.setJob_title(jobTitle);
        newAdmin.setBranch_ID(branchID);
        newAdmin.setFirstname(firstname);
        newAdmin.setSurname(surname);
        newAdmin.setContact_number(contactNumber);

        boolean isSuccesful = adminService.createNewAdmin(user, email, password, newAdmin);

        return isSuccesful;
    }

    private boolean addCustomer(User user, String firstname, String surname, String contactNumber){
        boolean isSuccesful = customerService.registerAccount(firstname, surname, contactNumber, user);
        return isSuccesful;
    }
    private boolean validNumber (String number){
        return number.length() == 11;
    }
    @FXML
    protected void handleReturnToLoginAction(ActionEvent event){
        try{
            redirectToLogin(event);
        } catch (Exception e){
            showError("Cannot go back to Login Page.");
            e.printStackTrace();
        }
    }

    private void redirectToLogin(ActionEvent event){
        String fxmlPath = "/fxml/login_scene.fxml";

        try {
            loadScene(fxmlPath, event);
        } catch (IOException e){
            showError("Error: Could not load the login scene.");
            e.printStackTrace();
        }
    }
    private void showError(String message) {
        errorLabel.setText(message);
    }

    private void redirectToDashboard(User user, ActionEvent event) {
        String fxmlPath = "";

        // 1. Check the user's role
        if (userService.isAdmin(user)) {
            // Admin Role (Task 4.4)
            fxmlPath = "/fxml/admin_dashboard.fxml"; // Path to Admin Dashboard
        } else {
            // Customer Role
            fxmlPath = "/fxml/customer_dashboard.fxml"; // Path to Customer Dashboard
        }

        // 2. Load the new scene
        try {
            loadScene(fxmlPath, event);
        } catch (IOException e) {
            showError("Error: Could not load the dashboard.");
            e.printStackTrace();
        }
    }
    private void loadScene(String fxmlPath, ActionEvent event) throws IOException {
        // Load the new FXML file
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();

        // Get the current stage (window) from the event source
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        // Set the new scene on the stage
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}
