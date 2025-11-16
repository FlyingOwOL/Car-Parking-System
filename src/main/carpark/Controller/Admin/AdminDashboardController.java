package Controller.Admin;

import Model.Entity.User;
import Service.Admin.AdminService;
import Utilities.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class AdminDashboardController {

    @FXML private Button branchesButton;
    @FXML private Button reportsButton;
    @FXML private Button usersButton;
    @FXML private MenuButton accountButton;
    @FXML private MenuItem profileMenuItem;
    @FXML private MenuItem logoutMenuItem;

    @FXML private AnchorPane mainContentArea;

    private final AdminService adminService;
    private final User currentUser;

    public AdminDashboardController() {
        this.adminService = new AdminService();
        this.currentUser = SessionManager.getCurrentUser();
    }

    /**
     * Initializes the controller class.
     * Loads the default dashboard view upon successful login.
     */
    @FXML
    public void initialize() {
        handleReportsClick(null);
    }

    @FXML
    private void handleBranchesClick(ActionEvent event) {
        System.out.println("Manage Branches clicked.");
        loadPage("/fxml/admin/branch_management_page.fxml");
    }

    @FXML
    private void handleReportsClick(ActionEvent event) {
        System.out.println("View Reports clicked.");
        loadPage("/fxml/admin/reports_page.fxml");
    }

    @FXML
    private void handleUsersClick(ActionEvent event) {
        System.out.println("Manage Users clicked.");
        loadPage("/fxml/admin/user_management_page.fxml"); // TODO: Create this FXML
    }

    @FXML
    private void handleProfileClick(ActionEvent event) {
        System.out.println("Admin Profile clicked.");
        loadPage("/fxml/admin/admin_profile_page.fxml"); // TODO: Create this FXML
    }

    @FXML
    private void handleLogoutClick(ActionEvent event) {
        SessionManager.logout();

        Stage stage = (Stage) accountButton.getScene().getWindow();

        try {
            URL fxmlUrl = getClass().getResource("/fxml/login_scene.fxml");
            if (fxmlUrl == null) throw new IOException("Cannot find login_scene.fxml");

            Parent root = FXMLLoader.load(fxmlUrl);
            Scene scene = new Scene(root);

            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            System.err.println("Failed to load login scene after logout.");
            e.printStackTrace();
        }
    }

    /**
     * Helper method to load a new FXML file into the mainContentArea.
     */
    private void loadPage(String fxmlPath) {
        if (mainContentArea == null) {
            System.err.println("mainContentArea is null. Check FXML file structure.");
            return;
        }

        try {
            URL fxmlUrl = getClass().getResource(fxmlPath);
            if (fxmlUrl == null) {
                mainContentArea.getChildren().setAll(new Label("Error: Page not found (" + fxmlPath + ")"));
                System.err.println("Cannot find FXML file: " + fxmlPath);
                return;
            }

            Parent page = FXMLLoader.load(fxmlUrl);

            mainContentArea.getChildren().setAll(page);

            AnchorPane.setTopAnchor(page, 0.0);
            AnchorPane.setBottomAnchor(page, 0.0);
            AnchorPane.setLeftAnchor(page, 0.0);
            AnchorPane.setRightAnchor(page, 0.0);

        } catch (IOException e) {
            System.err.println("Error loading page: " + fxmlPath);
            e.printStackTrace();
        }
    }
}