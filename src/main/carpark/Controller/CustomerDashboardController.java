package Controller;

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


public class CustomerDashboardController {

    @FXML
    private Button homeButton;

    @FXML
    private Button reservationButton;

    @FXML
    private Button locationsBUTTON;

    @FXML
    private Button pricesBUTTON;

    @FXML
    private MenuButton accountBUTTON;

    @FXML
    private MenuItem profileMENUITEM;

    @FXML
    private MenuItem logoutMENUITEM;

    @FXML
    private AnchorPane mainContentArea;

    /**
     * Initializes the controller class. This method is automatically called
     * after the FXML file has been loaded.
     */
    @FXML
    public void initialize() {
        handleHomeClick(null);
    }

    @FXML
    private void handleHomeClick(ActionEvent event) {
        System.out.println("Home clicked");
        loadPage("/fxml/home_page.fxml");
    }

    @FXML
    private void handleReservationClick(ActionEvent event) {
        System.out.println("Reservation clicked");
        loadPage("/fxml/reservation_page.fxml");
    }

    @FXML
    private void handleLocationsClick(ActionEvent event) {
        System.out.println("Locations clicked");
        loadPage("/fxml/locations_page.fxml");
    }

    @FXML
    private void handlePricesClick(ActionEvent event) {
        System.out.println("Prices clicked");
        loadPage("/fxml/prices_page");
    }

    @FXML
    private void handleProfileClick(ActionEvent event) {
        System.out.println("Profile clicked");
        loadPage("/fxml/account_page.fxml");
    }

    @FXML
    private void handleLogoutClick(ActionEvent event) {
        SessionManager.logout();

        Stage stage = (Stage) accountBUTTON.getScene().getWindow();

        try {
            URL fxmlUrl = getClass().getResource("/fxml/login_scene.fxml");
            if (fxmlUrl == null) {
                System.err.println("Cannot find /fxml/login_scene.fxml");
                return;
            }
            Parent root = FXMLLoader.load(fxmlUrl);
            Scene scene = new Scene(root);

            // 4. Apply CSS if it exists
            try {
                URL cssUrl = Thread.currentThread().getContextClassLoader().getResource("css/styles.css");
                if (cssUrl != null) {
                    scene.getStylesheets().add(cssUrl.toExternalForm());
                }
            } catch (Exception e) {
                System.err.println("Error loading CSS for login scene: " + e.getMessage());
            }

            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            System.err.println("Failed to load login_scene.fxml");
            e.printStackTrace();
        }
    }

    private void loadPage(String fxmlPath) {
        if (mainContentArea == null) {
            System.err.println("Main Content Area is null");
            return;
        }

        try {
            Parent page = FXMLLoader.load(getClass().getResource(fxmlPath));

            mainContentArea.getChildren().setAll(page);

            AnchorPane.setTopAnchor(page, 0.0);
            AnchorPane.setLeftAnchor(page, 0.0);
            AnchorPane.setRightAnchor(page, 0.0);
            AnchorPane.setBottomAnchor(page, 0.0);
        } catch (IOException e) {
            System.err.println("Error loading page: " + fxmlPath);
            e.printStackTrace();
        } catch (NullPointerException e) {
            System.err.println("Cannot find FXML file: " + fxmlPath);
            mainContentArea.getChildren().setAll(new Label("Cannot find FXML file: " + fxmlPath));
        }
    }
}
