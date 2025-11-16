package Model.DTO;

public class DurationReportDTO {
    private String branchName;
    private double averageDurationHours;

    public DurationReportDTO(String branchName, double averageDurationMinutes) {
        this.branchName = branchName;
        this.averageDurationHours = Math.round((averageDurationMinutes / 60.0) * 100.0) / 100.0;;
    }

    public String getBranchName() {return branchName;}
    public double getAverageDurationHours() {return averageDurationHours;}
}
