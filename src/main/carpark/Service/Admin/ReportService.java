package Service.Admin;

import DAO.ReportDAO;
import Model.DTO.DurationReportDTO;
import Model.DTO.OccupancyReportDTO;
import Model.DTO.RevenueReportDTO;
import Model.DTO.SlotUtilizationDTO;
import Model.Entity.User;
import Service.UserService;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public class ReportService {
    private ReportDAO reportDAO;
    private UserService userService;

    public ReportService() {
        this.reportDAO = new ReportDAO();
        this.userService = new UserService();
    }

    private void authorizeAdmin(User user) {
        if (!userService.isAdmin(user)) {
            throw new SecurityException("You are not Admin");
        }
    }

    public List<OccupancyReportDTO> generateOccupancyReport(User adminUser, int month, int year) {
        try{
            authorizeAdmin(adminUser);
            System.out.println("Generating Occupancy Report for " + month + "/" + year);
            return reportDAO.getOccupancyReport(month, year);
        } catch (SecurityException e) {
            System.out.println("Security Exception: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<RevenueReportDTO> generateRevenueReport(User adminUser, int month, int year) {
        try{
            authorizeAdmin(adminUser);
            System.out.println("Generating Revenue Report for " + month + "/" + year);
            return reportDAO.getRevenueReport(month, year);
        } catch (SecurityException e) {
            System.out.println("Security Exception: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<DurationReportDTO> generateDurationReport(User adminUser, int month, int year) {
        try{
            authorizeAdmin(adminUser);
            System.out.println("Generating Duration Report for " + month + "/" + year);
            return reportDAO.getDurationReport(month, year);
        } catch (SecurityException e) {
            System.out.println("Security Exception: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<SlotUtilizationDTO> generateSlotUtilization(User adminUser, int month, int year) {
        try{
            authorizeAdmin(adminUser);
            System.out.println("Generating Slot Utilization Report for " + month + "/" + year);
            return reportDAO.getSlotUtilizationReport(month, year);
        } catch (SecurityException e) {
            System.out.println("Security Exception: " + e.getMessage());
            return Collections.emptyList();
        }
    }
}
