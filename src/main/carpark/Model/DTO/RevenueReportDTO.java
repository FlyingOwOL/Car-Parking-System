package Model.DTO;

import java.math.BigDecimal;

public class RevenueReportDTO {
    private String branchName;
    private BigDecimal totalRevenue;
    private BigDecimal averagePayment;

    public RevenueReportDTO(String branchName, BigDecimal totalRevenue, BigDecimal averagePayment) {
        this.branchName = branchName;
        this.totalRevenue = totalRevenue;
        this.averagePayment = averagePayment;
    }

    public String getBranchName() {return branchName;}
    public BigDecimal getTotalRevenue() {return totalRevenue;}
    public BigDecimal getAveragePayment() {return averagePayment;}
}
