package Controller;

import java.io.IOException;
import java.util.Optional;

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

    private UserService userService;
    private CustomerService customerService;
    private AdminService adminService;

    @FXML
    public void initialize(){
        this.userService = new UserService();
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
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Email and password must not be empty.");
            return;
        }
        try {
            
        } catch (Exception e) {
            showError("An unexpected error occured. Please try again.");
            e.printStackTrace();
        }
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
