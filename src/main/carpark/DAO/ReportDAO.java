package DAO;

import Model.DTO.DurationReportDTO;
import Model.DTO.OccupancyReportDTO;
import Model.DTO.RevenueReportDTO;
import Model.DTO.SlotUtilizationDTO;
import Model.Entity.SlotType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReportDAO {

    public List<OccupancyReportDTO> getOccupancyReport(int month, int year) {
        List<OccupancyReportDTO> report = new ArrayList<>();
        String sql = "SELECT b.name, b.max_slots, COUNT(r.transact_ID) AS occupied_count " +
                "FROM branches b " +
                "JOIN parking_slots ps ON b.branch_ID = ps.branch_ID " +
                "JOIN reservations r ON ps.spot_ID = r.spot_ID " +
                "WHERE (r.status = 'ACTIVE' OR r.status = 'COMPLETED') " +
                "  AND MONTH(r.dateReserved) = ? AND YEAR(r.dateReserved) = ? " +
                "GROUP BY b.branch_ID, b.name, b.max_slots";

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBConnectionUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, month);
            ps.setInt(2, year);
            rs = ps.executeQuery();

            while (rs.next()) {
                report.add(new OccupancyReportDTO(
                        rs.getString("name"),
                        rs.getInt("max_slots"),
                        rs.getInt("occupied_count")
                ));
            }
        } catch (SQLException e) {
            System.err.println("ReportDAO error in getOccupancyReport: " + e.getMessage());
        } finally {
            DBConnectionUtil.closeConnection(conn, ps, rs);
        }
        return report;
    }

    public List<RevenueReportDTO> getRevenueReport(int month, int year) {
        List<RevenueReportDTO> report = new ArrayList<>();
        // This query joins payments -> reservations -> slots -> branches
        String sql = "SELECT b.name, SUM(p.amount_paid) AS total_revenue, AVG(p.amount_paid) AS avg_payment " +
                "FROM payments p " +
                "JOIN reservations r ON p.transact_ID = r.transact_ID " +
                "JOIN parking_slots ps ON r.spot_ID = ps.spot_ID " +
                "JOIN branches b ON ps.branch_ID = b.branch_ID " +
                "WHERE p.payment_status = 'Paid' " +
                "  AND MONTH(p.payment_date) = ? AND YEAR(p.payment_date) = ? " +
                "GROUP BY b.branch_ID, b.name";

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBConnectionUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, month);
            ps.setInt(2, year);
            rs = ps.executeQuery();

            while (rs.next()) {
                report.add(new RevenueReportDTO(
                        rs.getString("name"),
                        rs.getBigDecimal("total_revenue"),
                        rs.getBigDecimal("avg_payment")
                ));
            }
        } catch (SQLException e) {
            System.err.println("ReportDAO Error in getRevenueReport: " + e.getMessage());
        } finally {
            DBConnectionUtil.closeConnection(conn, ps, rs);
        }
        return report;
    }

    public List<DurationReportDTO> getDurationReport(int month, int year) {
        List<DurationReportDTO> report = new ArrayList<>();
        // This query calculates the average time difference in minutes
        String sql = "SELECT b.name, AVG(TIMESTAMPDIFF(MINUTE, r.check_in_time, r.time_Out)) AS avg_duration_minutes " +
                "FROM reservations r " +
                "JOIN parking_slots ps ON r.spot_ID = ps.spot_ID " +
                "JOIN branches b ON ps.branch_ID = b.branch_ID " +
                "WHERE r.status = 'COMPLETED' " +
                "  AND r.check_in_time IS NOT NULL AND r.time_Out IS NOT NULL " +
                "  AND MONTH(r.time_Out) = ? AND YEAR(r.time_Out) = ? " +
                "GROUP BY b.branch_ID, b.name";

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBConnectionUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, month);
            ps.setInt(2, year);
            rs = ps.executeQuery();

            while (rs.next()) {
                report.add(new DurationReportDTO(
                        rs.getString("name"),
                        rs.getDouble("avg_duration_minutes")
                ));
            }
        } catch (SQLException e) {
            System.err.println("ReportDAO Error in getDurationReport: " + e.getMessage());
        } finally {
            DBConnectionUtil.closeConnection(conn, ps, rs);
        }
        return report;
    }

    public List<SlotUtilizationDTO> getSlotUtilizationReport(int month, int year) {
        List<SlotUtilizationDTO> report = new ArrayList<>();
        String sql = "SELECT ps.slot_type, COUNT(r.transact_ID) AS reservation_count " +
                "FROM reservations r " +
                "JOIN parking_slots ps ON r.spot_ID = ps.spot_ID " +
                "WHERE MONTH(r.dateReserved) = ? AND YEAR(r.dateReserved) = ? " +
                "GROUP BY ps.slot_type";

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBConnectionUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, month);
            ps.setInt(2, year);
            rs = ps.executeQuery();

            while (rs.next()) {
                report.add(new SlotUtilizationDTO(
                        SlotType.fromString(rs.getString("slot_type")), // Use helper
                        rs.getInt("reservation_count")
                ));
            }
        } catch (SQLException e) {
            System.err.println("ReportDAO Error in getSlotUtilizationReport: " + e.getMessage());
        } finally {
            DBConnectionUtil.closeConnection(conn, ps, rs);
        }
        return report;
    }
}
