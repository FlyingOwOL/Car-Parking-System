package Controller;

import java.io.IOException;
import java.util.Optional;
import Model.Entity.User;
import Service.CustomerService;
import Service.UserService;
import Utilities.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
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

    private UserService     userService;
    private CustomerService customerService;

    @FXML
    public void initialize(){
        this.userService     = new UserService();
        this.customerService = new CustomerService();

        returnButton.setOnAction(this::handleReturnToLoginAction);
        registerButton.setOnAction(this::handleRegisterButtonAction);

        errorLabel.setText("");
    }

    @FXML
    protected void handleRegisterButtonAction(ActionEvent event){
        String email         = emailField.getText();
        String password      = passwordField.getText();
        String firstname     = firstnameField.getText();
        String surname       = surnameField.getText();
        String contactNumber = contactNumberField.getText();

        // FIX: Hardcode the role to "Customer"
        String role = "Customer";

        if (email.isEmpty() || password.isEmpty()) {
            showError("Email and password must not be empty.");
            return;
        }
        if (firstname.isEmpty() || surname.isEmpty() || contactNumber.isEmpty()){
            showError("Firstname, surname, and contact number must not be empty.");
            return;
        }
        if (!validNumber(contactNumber)){
            showError("Contact number must be 11 digits long");
            return;
        }

        try {
            Optional<User> userOptional = userService.register(email, password, role);

            if (userOptional.isPresent()) {
                User user = userOptional.get();

                boolean isSuccesful = addCustomer(user, firstname, surname, contactNumber);

                if (isSuccesful){
                    SessionManager.login(user);
                    redirectToDashboard(user, event);
                }
            } else {
                showError("Registration failed. Email might already exist.");
            }
        } catch (Exception e) {
            showError("An unexpected error occurred.");
            e.printStackTrace();
        }
    }

    private boolean addCustomer(User user, String firstname, String surname, String contactNumber){
        return customerService.registerAccount(firstname, surname, contactNumber, user);
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
        String fxmlPath = "/fxml/customer_dashboard.fxml";

        try {
            loadScene(fxmlPath, event);
        } catch (IOException e) {
            showError("Error: Could not load the dashboard.");
            e.printStackTrace();
        }
    }

    private void loadScene(String fxmlPath, ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}