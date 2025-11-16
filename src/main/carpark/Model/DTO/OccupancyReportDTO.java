package Model.DTO;

public class OccupancyReportDTO {
    private String branchName;
    private int maxSlots;
    private int occupiedCount;
    private double occupancyPercentage;

    public OccupancyReportDTO(String branchName, int maxSlots, int occupiedCount) {
        this.branchName = branchName;
        this.maxSlots = maxSlots;
        this.occupiedCount = occupiedCount;
        if (maxSlots > 0) {
            this.occupancyPercentage = ((double) occupiedCount / maxSlots) * 100;
        } else {
            this.occupancyPercentage = 0.0;
        }
    }

    public String getBranchName() {return branchName;}
    public int getMaxSlots() {return maxSlots;}
    public int getOccupiedCount() {return occupiedCount;}
    public double getOccupancyPercentage() {return occupancyPercentage;}
}
