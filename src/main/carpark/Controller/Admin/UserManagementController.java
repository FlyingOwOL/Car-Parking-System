package Controller.Admin;

import DAO.UserDAO;
import Model.Entity.User;
import Utilities.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class UserManagementController {

    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, Integer> idColumn;
    @FXML private TableColumn<User, String> emailColumn;
    @FXML private TableColumn<User, String> roleColumn;
    @FXML private TableColumn<User, String> joinDateColumn;
    @FXML private Label statusLabel;
    @FXML private Button newAdminButton;

    private UserDAO userDAO;
    private ObservableList<User> userList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        this.userDAO = new UserDAO();

        setupTableColumns();
        loadUserData();
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("user_ID"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        joinDateColumn.setCellValueFactory(new PropertyValueFactory<>("join_date"));

        usersTable.setItems(userList);
    }

    /**
     * Fetches all user data from the database and updates the table view.
     */
    public void loadUserData() {
        try {
            List<User> allUsers = userDAO.findAllUsers();
            userList.setAll(allUsers);
            statusLabel.setText("");
        } catch (Exception e) {
            statusLabel.setText("Error loading user data: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: red;");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleNewAdminRegistration(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/register_admin_popup.fxml"));
            Stage popupStage = new Stage();
            popupStage.setScene(new Scene(loader.load()));

            AdminRegistrationController controller = loader.getController();

            controller.setParentController(this);

            popupStage.setTitle("Admin Registration");
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.showAndWait();
        } catch (IOException e) {
            statusLabel.setText("Error loading user data: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: red;");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDeleteUser(ActionEvent event) {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            statusLabel.setText("Please select a user to delete.");
            return;
        }

        // Safety check: Cannot delete the current admin user!
        if (selectedUser.getUser_ID() == SessionManager.getCurrentUser().getUser_ID()) {
            statusLabel.setText("Cannot delete the currently logged-in user.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
                "Are you sure you want to delete user " + selectedUser.getEmail() + "?",
                ButtonType.YES, ButtonType.NO);
        confirmation.showAndWait();

        if (confirmation.getResult() == ButtonType.YES) {
            try {
                userDAO.deleteUser(selectedUser.getUser_ID());
                statusLabel.setText("User deleted successfully.");
                statusLabel.setStyle("-fx-text-fill: green;");
                loadUserData();
            } catch (Exception e) {
                statusLabel.setText("Error deleting user: " + e.getMessage());
                statusLabel.setStyle("-fx-text-fill: red;");
            }
        }
    }
}
