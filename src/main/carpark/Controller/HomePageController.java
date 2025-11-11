package Controller;

import Model.Entity.User;
import Utilities.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class HomePageController {

    @FXML
    private Label welcomeMessageLabel;

    @FXML
    private Label emailLabel;

    @FXML
    public void initialize() {
        // Get the currently logged-in user from the session
        User currentUser = SessionManager.getCurrentUser();

        if (currentUser != null) {
            // In a real app, you'd call a CustomerDAO/Service to get the first name
            // For now, the email is the most unique thing we have.
            welcomeMessageLabel.setText("Welcome back to the Car Park System!");
            emailLabel.setText("Logged in as: " + currentUser.getEmail());
        } else {
            // This case should ideally not happen if session management is correct
            welcomeMessageLabel.setText("Welcome, Guest!");
            emailLabel.setText("Not logged in.");
        }
    }
}
