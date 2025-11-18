package Controller;

import Model.Entity.Reservation;
import Utilities.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
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
        loadPageWithControllerInjection("/fxml/home_page.fxml");
    }

    @FXML
    private void handleReservationClick(ActionEvent event) {
        System.out.println("Reservation clicked");
        loadPageWithControllerInjection("/fxml/reservation_page.fxml");
    }

    @FXML
    private void handleProfileClick(ActionEvent event) {
        System.out.println("Profile clicked");
        loadPageWithControllerInjection("/fxml/profile_page.fxml");
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

            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            System.err.println("Failed to load login_scene.fxml");
            e.printStackTrace();
        }
    }

    public void loadPaymentPage(Reservation reservation) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/payment.fxml"));
            Parent page = loader.load();

            PaymentPageController controller = loader.getController();
            if (controller != null) {
                controller.setReservationData(reservation);
                controller.setDashboardController(this);
            } else {
                System.err.println("Error: PaymentPageController is null.");
            }

            setAnchors(page);
            mainContentArea.getChildren().setAll(page);

        } catch (IOException e) {
            System.err.println("Failed to load payment page FXML.");
            e.printStackTrace();
            mainContentArea.getChildren().setAll(new Label("Error: Cannot load payment page."));
        }
    }

    public void returnToHome() {
        loadPageWithControllerInjection("/fxml/home_page.fxml");
    }

    public void returnToReservationPage() {
        loadPageWithControllerInjection("/fxml/reservation_page.fxml");
    }

    private void loadPageWithControllerInjection(String fxmlPath) {
        if (mainContentArea == null) return;
        try {
            URL fxmlUrl = getClass().getResource(fxmlPath);
            if (fxmlUrl == null) return;

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent page = loader.load();

            // Check if the loaded controller is the ReservationController
            Object controller = loader.getController();
            if (controller instanceof ReservationPageController) {
                ((ReservationPageController) controller).setMainDashboardController(this);
            }

            setAnchors(page);
            mainContentArea.getChildren().setAll(page);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setAnchors(Node page) {
        AnchorPane.setTopAnchor(page, 0.0);
        AnchorPane.setBottomAnchor(page, 0.0);
        AnchorPane.setLeftAnchor(page, 0.0);
        AnchorPane.setRightAnchor(page, 0.0);
    }
}