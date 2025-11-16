package Service.Admin;

import DAO.DBConnectionUtil;
import DAO.ParkingDAO;
import Model.Entity.*;
import Service.UserService;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;


/**
 * BranchManagementService handles all administrative business logic related to
 * fixed assets (Branches, Parking Slots, Pricing Rules).
 * It enforces mandatory authorization checks using the UserService.
 */
public class BranchManagementService {

    private final ParkingDAO parkingDAO;
    private final UserService userService;

    // Constants for multi-level slot generation ---
    private static final int CAR_SLOTS_PER_FLOOR = 40;
    private static final int MOTORCYCLE_SLOTS_PER_FLOOR = 20;
    private static final int SLOTS_PER_FLOOR = CAR_SLOTS_PER_FLOOR + MOTORCYCLE_SLOTS_PER_FLOOR; // 60

    // Ratios *within* 40 car slots
    private static final double PWD_RATIO = 0.10; // 10% of 40 = 4 slots
    private static final double VIP_RATIO = 0.10; // 10% of 40 = 4 slots
    // Remainder (85%) will be REGULAR

    // These rates will be applied to every new branch automatically.
    private static final BigDecimal REGULAR_RATE = new BigDecimal("50.00");
    private static final BigDecimal PWD_RATE = new BigDecimal("40.00");
    private static final BigDecimal MOTORCYCLE_RATE = new BigDecimal("30.00");
    private static final BigDecimal VIP_RATE = new BigDecimal("100.00");

    /**
     * Constructor initializes DAO and Service dependencies.
     */
    public BranchManagementService() {
        this.parkingDAO = new ParkingDAO();
        this.userService = new UserService();
    }

    private void authorizeAdmin(User user) throws SecurityException{
        if (!userService.isAdmin(user)) {
            throw new SecurityException("Access Denied: User does not have administrative privilage");
        }
    }

    public int createNewBranch(User admin, Branch newBranch) {
        authorizeAdmin(admin);
        Connection conn = null;
        int newBranchId = -1;

        try {
            conn = DBConnectionUtil.getConnection();
            conn.setAutoCommit(false);

            newBranchId = parkingDAO.insertBranch(newBranch);

            if (newBranchId > 0) {
                int totalSlotsToCreate = newBranch.getMax_slots();

                for (int i = 0; i < totalSlotsToCreate; i++) {
                    int currentFloor = (i / SLOTS_PER_FLOOR) + 1;
                    int slotIndexOnFloor = i % SLOTS_PER_FLOOR;

                    String spotID;
                    SlotType slotType;

                    if (slotIndexOnFloor < CAR_SLOTS_PER_FLOOR) {
                        spotID = currentFloor + "A" + String.format("%02d", slotIndexOnFloor + 1);

                        // Assign type based on ratio *within* the 40 car slots
                        if (slotIndexOnFloor < CAR_SLOTS_PER_FLOOR * PWD_RATIO) {
                            slotType = SlotType.PWD;
                        } else if (slotIndexOnFloor < CAR_SLOTS_PER_FLOOR * (PWD_RATIO + VIP_RATIO)) {
                            slotType = SlotType.VIP;
                        } else {
                            slotType = SlotType.REGULAR;
                        }
                    } else {
                        int motorcycleSlotNumber = (slotIndexOnFloor - CAR_SLOTS_PER_FLOOR) + 1;
                        spotID = currentFloor + "M" + String.format("%02d", motorcycleSlotNumber);
                        slotType = SlotType.MOTORCYCLE;
                    }

                    ParkingSlot newSlot = new ParkingSlot(spotID, newBranchId, currentFloor, slotType);

                    if (!parkingDAO.insertSlot(newSlot, conn)) {
                        throw new SQLException("Failed to insert slot " + spotID + ". Rolling back transaction.");
                    }
                }

                // Regular
                parkingDAO.insertOrUpdatePricing(new Pricing(newBranchId, SlotType.REGULAR, REGULAR_RATE, REGULAR_RATE.multiply(new BigDecimal("1.5"))));
                // PWD
                parkingDAO.insertOrUpdatePricing(new Pricing(newBranchId, SlotType.PWD, PWD_RATE, PWD_RATE.multiply(new BigDecimal("1.5"))));
                // Motorcycle
                parkingDAO.insertOrUpdatePricing(new Pricing(newBranchId, SlotType.MOTORCYCLE, MOTORCYCLE_RATE, MOTORCYCLE_RATE.multiply(new BigDecimal("1.5"))));
                // VIP
                parkingDAO.insertOrUpdatePricing(new Pricing(newBranchId, SlotType.VIP, VIP_RATE, VIP_RATE.multiply(new BigDecimal("1.5"))));

                conn.commit();
                return newBranchId;
            }

            conn.rollback();
            return -1;

        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException rollbackEx) {
                System.err.println("BranchManagementService Rollback failed: " + rollbackEx.getMessage());
            }
            System.err.println("BranchManagementService Transaction Error: " + e.getMessage());
            return -1;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    DBConnectionUtil.closeConnection(conn);
                }
            } catch (SQLException e) {
                System.err.println("Error restoring connection state: " + e.getMessage());
            }
        }
    }

    public boolean setPricing(User admin, Pricing newPricing) {
        try {
            authorizeAdmin(admin);
            return parkingDAO.insertOrUpdatePricing(newPricing);
        } catch (SecurityException e) {
            System.err.println(e.getMessage());
            return false;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return false;
        }
    }

    public boolean updateSlotType(User admin, String spotId, SlotType newType) {
        try {

            authorizeAdmin(admin);

            Optional<ParkingSlot> slotOpt = parkingDAO.getSlotByID(spotId);
            if (slotOpt.isPresent() && !slotOpt.get().isAvailability()) {
                System.err.println("Cannot change type: Slot " + spotId + " is currently occupied");
                return false;
            }

            return parkingDAO.updateSlotType(spotId, newType);
        } catch (SecurityException e) {
            System.err.println(e.getMessage());
            return false;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return false;
        }
    }
}
