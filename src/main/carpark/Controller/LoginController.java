package Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import Model.Entity.User;
import Service.UserService;
import Utilities.SessionManager;

import java.io.IOException;
import java.util.Optional;

/**
 * Controller for the login_scene.fxml file.
 * Handles user authentication by calling the UserService and manages navigation.
 * Task 4.1: Login & Session Management
 */
public class LoginController {

    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label errorLabel;

    private UserService userService;

    /**
     * Initializes the controller class.
     * This method is automatically called after the FXML file has been loaded.
     */
    @FXML
    public void initialize() {
        this.userService = new UserService();

        errorLabel.setText("");
    }

    /**
     * Handles the action event when the "Login" button is clicked.
     * This method is linked from the 'onAction' property of the Button in FXML.
     *
     * @param event The ActionEvent from the button click.
     */
    @FXML
    protected void handleLoginButtonAction(ActionEvent event) {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Email and password must not be empty.");
            return;
        }

        try {
            Optional<User> userOptional = userService.login(email, password);

            if (userOptional.isPresent()) {
                User user = userOptional.get();
                SessionManager.login(user);

                redirectToDashboard(user, event);
            } else {
                showError("Access Denied: Invalid email or password.");
            }
        } catch (Exception e) {
            showError("An unexpected error occured. Please try again.");
            e.printStackTrace();
        }
    }

    /**
     * Handles the mouse click event on the "Register here" label.
     * This method is linked from the 'onMouseClicked' property in FXML.
     *
     * @param event The MouseEvent from the label click.
     */
    @FXML
    protected void handleRegisterLink(MouseEvent event) {
        System.out.println("Register link clicked. Loading registration scene...");
        // TODO: Implement navigation to the Registration FXML scene
        showError("Registration scene not yet implemented.");
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
