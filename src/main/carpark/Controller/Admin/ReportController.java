package Controller.Admin;

import Model.DTO.DurationReportDTO;
import Model.DTO.OccupancyReportDTO;
import Model.DTO.RevenueReportDTO;
import Model.DTO.SlotUtilizationDTO;
import Model.Entity.User;
import Service.Admin.ReportService;
import Service.UserService;
import Utilities.SessionManager;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ReportController {
    @FXML private ComboBox<String> monthComboBox;
    @FXML private ComboBox<Integer> yearComboBox;
    @FXML private Label statusLabel;

    @FXML private TableView<OccupancyReportDTO> occupancyTable;
    @FXML private TableView<RevenueReportDTO> revenueTable;
    @FXML private TableView<DurationReportDTO> durationTable;
    @FXML private TableView<SlotUtilizationDTO> utilizationTable;

    @FXML private TableColumn<OccupancyReportDTO, String> branchOccColumn;
    @FXML private TableColumn<OccupancyReportDTO, Integer> maxSlotsColumn;
    @FXML private TableColumn<OccupancyReportDTO, Integer> occupiedCountColumn;
    @FXML private TableColumn<OccupancyReportDTO, Double> occupancyPercentColumn;

    @FXML private TableColumn<RevenueReportDTO, String> branchRevColumn;
    @FXML private TableColumn<RevenueReportDTO, String> totalRevenueColumn;
    @FXML private TableColumn<RevenueReportDTO, String> avgPaymentColumn;

    @FXML private TableColumn<DurationReportDTO, String> branchDurColumn;
    @FXML private TableColumn<DurationReportDTO, Double> avgDurationColumn;

    @FXML private TableColumn<SlotUtilizationDTO, String> slotTypeUtilColumn;
    @FXML private TableColumn<SlotUtilizationDTO, Integer> reservationCountColumn;


    private ReportService reportService;
    private User adminUser;
    private UserService userService;

    @FXML
    public void initialize() {
        this.reportService = new ReportService();
        this.adminUser = SessionManager.getCurrentUser();
        this.userService = new UserService();

        setupFilters();

        setupTableColumns();

        handleGenerateReport(null);
    }

    private void setupFilters() {
        List<String> months = List.of("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December");
        monthComboBox.setItems(FXCollections.observableArrayList(months));

        int currentYear = YearMonth.now().getYear();
        List<Integer> years = IntStream.rangeClosed(currentYear - 3, currentYear)
                .boxed().collect(Collectors.toList());
        yearComboBox.setItems(FXCollections.observableArrayList(years));

        monthComboBox.getSelectionModel().select(YearMonth.now().getMonthValue() - 1);
        yearComboBox.getSelectionModel().selectLast();
    }

    private void setupTableColumns() {
        // Occupancy Table
        branchOccColumn.setCellValueFactory(new PropertyValueFactory<>("branchName"));
        maxSlotsColumn.setCellValueFactory(new PropertyValueFactory<>("maxSlots"));
        occupiedCountColumn.setCellValueFactory(new PropertyValueFactory<>("occupiedCount"));
        occupancyPercentColumn.setCellValueFactory(new PropertyValueFactory<>("occupancyPercentage"));

        // Revenue Table
        branchRevColumn.setCellValueFactory(new PropertyValueFactory<>("branchName"));
        totalRevenueColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                "₱" + String.format("%,.2f", cellData.getValue().getTotalRevenue())
        ));
        avgPaymentColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                "₱" + String.format("%,.2f", cellData.getValue().getAveragePayment())
        ));

        // Duration Table
        branchDurColumn.setCellValueFactory(new PropertyValueFactory<>("branchName"));
        avgDurationColumn.setCellValueFactory(new PropertyValueFactory<>("averageDurationHours"));

        // Utilization Table
        slotTypeUtilColumn.setCellValueFactory(new PropertyValueFactory<>("slotType"));
        reservationCountColumn.setCellValueFactory(new PropertyValueFactory<>("reservationCount"));
    }

    @FXML
    private void handleGenerateReport(ActionEvent event) {
        statusLabel.setText(""); // Clear status

        Integer year = yearComboBox.getValue();
        String monthString = monthComboBox.getValue();

        if (year == null || monthString == null) {
            statusLabel.setText("Please select a month and year.");
            statusLabel.setTextFill(javafx.scene.paint.Color.RED);
            return;
        }

        int month = monthComboBox.getSelectionModel().getSelectedIndex() + 1;

        try {
            // Check authorization once
            if (!userService.isAdmin(adminUser)) {
                throw new SecurityException("You do not have permission to run reports.");
            }

            // --- 1. Occupancy Report ---
            List<OccupancyReportDTO> occData = reportService.generateOccupancyReport(adminUser, month, year);
            occupancyTable.setItems(FXCollections.observableArrayList(occData));

            // --- 2. Revenue Report ---
            List<RevenueReportDTO> revData = reportService.generateRevenueReport(adminUser, month, year);
            revenueTable.setItems(FXCollections.observableArrayList(revData));

            // --- 3. Duration Report ---
            List<DurationReportDTO> durData = reportService.generateDurationReport(adminUser, month, year);
            durationTable.setItems(FXCollections.observableArrayList(durData));

            // --- 4. Utilization Report ---
            List<SlotUtilizationDTO> utilData = reportService.generateSlotUtilization(adminUser, month, year);
            utilizationTable.setItems(FXCollections.observableArrayList(utilData));

            statusLabel.setText("Reports generated successfully for " + monthString + ", " + year + ".");
            statusLabel.setTextFill(javafx.scene.paint.Color.GREEN);

        } catch (SecurityException e) {
            statusLabel.setText("ERROR: Authorization Failed.");
            statusLabel.setTextFill(javafx.scene.paint.Color.RED);
        } catch (Exception e) {
            statusLabel.setText("ERROR: Data could not be retrieved from the database.");
            e.printStackTrace();
            statusLabel.setTextFill(javafx.scene.paint.Color.RED);
        }
    }
}
