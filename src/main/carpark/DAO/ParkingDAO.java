    package DAO;

    import Model.Entity.Branch;
    import Model.Entity.ParkingSlot;
    import Model.Entity.Pricing;
    import Model.Entity.SlotType;

    import java.math.BigDecimal;
    import java.sql.*;
    import java.time.LocalTime;
    import java.util.ArrayList;
    import java.util.List;
    import java.util.Optional;

    /**
     * ParkingDAO manages data access for fixed assets: 'branches', 'parking_slots', and 'pricing_rules'.
     * This DAO supports both customer lookups (available slots) and administrative configuration.
     */
    public class ParkingDAO {

        // === SQL QUERIES ===
        private static final String SELECT_AVAILABLE_SLOTS =
                "SELECT * FROM parking_slots WHERE availability = TRUE AND branch_ID = ? AND slot_type = ?";
        private static final String UPDATE_SLOT_AVAILABILITY =
                "UPDATE parking_slots SET availability = ? WHERE spot_ID = ?";

        // --- Pricing Rule ---
        private static final String SELECT_PRICING_RULE =
                "SELECT pricing_ID, branch_ID, slot_type, hourly_rate, overtime_rate FROM pricing " +
                        "WHERE branch_ID = ? AND slot_type = ?";

        // --- Branch Queries ---
        private static final String SELECT_ALL_BRANCHES =
                "SELECT branch_ID, name, contact_number, email, max_slots, location, opening_time, closing_time FROM branches";
        private static final String INSERT_BRANCH =
                "INSERT INTO branches (name, contact_number, email, max_slots, location, opening_time, closing_time) VALUES (?, ?, ?, ?, ?, ?, ?)";
        private static final String INSERT_SLOT =
                "INSERT INTO parking_slots (spot_ID, branch_ID, floor_level, slot_type, availability) VALUES (?, ?, ?, ?, ?)";
        private static final String UPSERT_PRICING =
                "INSERT INTO pricing (branch_ID, slot_type, hourly_rate, overtime_rate) VALUES (?, ?, ?, ?)" +
                        "ON DUPLICATE KEY UPDATE hourly_rate = VALUES(hourly_rate), overtime_rate = VALUES(overtime_rate)";
        private static final String SELECT_SLOT_BY_ID =
                "SELECT * FROM parking_slots WHERE slot_ID = ?";
        private static final String UPDATE_SLOT_TYPE =
                "UPDATE parking_slots SET slot_type = ? WHERE slot_ID = ?";

        private ParkingSlot mapRowToParkingSlot(ResultSet rs) throws SQLException {
            String spotId = rs.getString("spot_ID");
            int branchId = rs.getInt("branch_ID");
            int floorLevel = rs.getInt("floor_level");

            SlotType slotType = SlotType.fromString(rs.getString("slot_type"));
            boolean availability = rs.getBoolean("availability");

            return new ParkingSlot(spotId, branchId, floorLevel, slotType, availability);
        }

        private Pricing mapRowToPricingRule(ResultSet rs) throws SQLException {
            int pricingId = rs.getInt("pricing_ID");
            int branchId = rs.getInt("branch_ID");
            SlotType slotType = SlotType.fromString(rs.getString("slot_type"));
            BigDecimal hourlyRate = rs.getBigDecimal("hourly_rate");
            BigDecimal overtimeRate = rs.getBigDecimal("overtime_rate");

            return new Pricing(pricingId, branchId, slotType, hourlyRate, overtimeRate);
        }

        /**
         * Retrieves all available parking slots matching the branch and type criteria.
         * This is used by the ReservationService.
         *
         * @param branchId The branch ID to search within.
         * @param slotType The desired type of slot (e.g., REGULAR, VIP).
         * @return A list of available ParkingSlot objects.
         */
        public List<ParkingSlot> getAvailableSlots(int branchId, SlotType slotType) {
            List<ParkingSlot> slots = new ArrayList<>();
            Connection conn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                conn = DBConnectionUtil.getConnection();
                ps = conn.prepareStatement(SELECT_AVAILABLE_SLOTS);
                ps.setInt(1, branchId);
                ps.setString(2, slotType.name()); // Use the enum name as the DB string

                rs = ps.executeQuery();
                while (rs.next()) {
                    slots.add(mapRowToParkingSlot(rs));
                }
            } catch (SQLException e) {
                System.err.println("parkingDAO Error in getAvailableSlots: " + e.getMessage());
            } finally {
                DBConnectionUtil.closeConnection(conn, ps, rs);
            }
            return slots;
        }

        /**
         * Updates the availability status of a specific parking slot.
         * This is used by the ReservationService (to reserve) and Admin Service (for maintenance).
         *
         * @param spotId The ID of the slot to update.
         * @param isAvailable The new availability status (true or false).
         * @return true if the update was successful.
         */
        public boolean updateSlotAvailability(String spotId, boolean isAvailable, Connection conn) throws SQLException {
            PreparedStatement ps = null;

            try {
                ps = conn.prepareStatement(UPDATE_SLOT_AVAILABILITY);
                ps.setBoolean(1, isAvailable);
                ps.setString(2, spotId);

                return ps.executeUpdate() > 0;

            } catch (SQLException e) {
                System.err.println("parkingDAO Error in updateSlotAvailability: " + e.getMessage());
                throw e;
            } finally {
                if (ps != null) ps.close();
            }
        }

        /**
         * Retrieves the pricing rule for a specific branch and slot type.
         * This is crucial for the PaymentService to calculate fees.
         *
         * @param branchId The branch ID.
         * @param slotType The type of slot.
         * @return An Optional containing the PricingRule if found.
         */
        public Optional<Pricing> getPricingRule(int branchId, SlotType slotType) {
            Connection conn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                conn = DBConnectionUtil.getConnection();
                ps = conn.prepareStatement(SELECT_PRICING_RULE);
                ps.setInt(1, branchId);
                ps.setString(2, slotType.name());

                rs = ps.executeQuery();
                if (rs.next()) {
                    return Optional.of(mapRowToPricingRule(rs));
                }
            } catch (SQLException e) {
                System.err.println("parkingDAO Error in getPricingRule: " + e.getMessage());
            } finally {
                DBConnectionUtil.closeConnection(conn, ps, rs);
            }
            return Optional.empty();
        }

        /**
         * Retrieves all registered branches in the system.
         * Used for initial UI population (e.g., dropdowns) and administrative views.
         *
         * @return A list of all Branch objects.
         */
        public List<Branch> getAllBranches() {
            List<Branch> branches = new ArrayList<>();
            Connection conn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                conn = DBConnectionUtil.getConnection();
                ps = conn.prepareStatement(SELECT_ALL_BRANCHES);

                rs = ps.executeQuery();
                while (rs.next()) {
                    int branchId = rs.getInt("branch_ID");
                    String name = rs.getString("name");
                    String contactNumber = rs.getString("contact_number");
                    String email = rs.getString("email");
                    int maxSlots = rs.getInt("max_slots");
                    String location = rs.getString("location");
                    // Use getObject for LocalTime mapping (JDBC 4.2 standard)
                    LocalTime openingTime = rs.getObject("opening_time", LocalTime.class);
                    LocalTime closingTime = rs.getObject("closing_time", LocalTime.class);

                    branches.add(new Branch(branchId, name, contactNumber, email, maxSlots, location, openingTime, closingTime));
                }
            } catch (SQLException e) {
                System.err.println("parkingDAO Error in getAllBranches: " + e.getMessage());
            } finally {
                DBConnectionUtil.closeConnection(conn, ps, rs);
            }
            return branches;
        }

        public boolean insertSlot(ParkingSlot slot, Connection conn) throws SQLException {
            PreparedStatement ps = null;
            try {
                // Use the shared connection, DO NOT close it here
                ps = conn.prepareStatement(INSERT_SLOT);
                ps.setString(1, slot.getSpot_ID());
                ps.setInt(2, slot.getBranch_ID());
                ps.setInt(3, slot.getFloor_level());
                ps.setString(4, slot.getSlot_type().name());
                ps.setBoolean(5, slot.isAvailability());

                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                System.err.println("ParkingDAO Error in insertSlot: " + e.getMessage());
                throw e;
            } finally {
                if (ps != null) ps.close();
            }
        }

        /**
         * Inserts a new Branch into the database.
         * @param branch The Branch object to insert (ID will be ignored).
         * @return true if successful.
         */
        public int insertBranch(Branch branch) throws SQLException {
            Connection conn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            int generatedID = -1;
            try {
                conn = DBConnectionUtil.getConnection();
                ps = conn.prepareStatement(INSERT_BRANCH, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, branch.getName());
                ps.setString(2, branch.getContact_number());
                ps.setString(3, branch.getEmail());
                ps.setInt(4, branch.getMax_slots());
                ps.setString(5, branch.getLocation());
                ps.setObject(6, branch.getOpening_time());
                ps.setObject(7, branch.getClosing_time());

                int affectedRows = ps.executeUpdate();
                if (affectedRows > 0) {
                    rs = ps.getGeneratedKeys();
                    if (rs.next()) {
                        generatedID = rs.getInt(1);
                    }
                }
            } catch (SQLException e) {
                System.err.println("parkingDAO Error in insertBranch: " + e.getMessage());
                throw e;
            } finally {
                DBConnectionUtil.closeConnection(conn, ps);
            }
            return generatedID;
        }

        public boolean insertOrUpdatePricing(Pricing rule) throws SQLException {
            Connection conn = null;
            PreparedStatement ps = null;
            try {
                conn = DBConnectionUtil.getConnection();
                ps = conn.prepareStatement(UPSERT_PRICING);
                ps.setInt(1, rule.getBranch_ID());
                ps.setString(2, rule.getSlot_type().name());
                ps.setBigDecimal(3, rule.getHourly_rate());
                ps.setBigDecimal(4, rule.getOvertime_rate());

                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                System.err.println("parkingDAO Error in insertOrUpdatePricing: " + e.getMessage());
                throw e;
            } finally {
                DBConnectionUtil.closeConnection(conn, ps);
            }
        }

        public Optional<ParkingSlot> getSlotByID(String spotID) throws SQLException {
            Connection conn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                conn = DBConnectionUtil.getConnection();
                ps = conn.prepareStatement(SELECT_SLOT_BY_ID);
                ps.setString(1, spotID);

                rs = ps.executeQuery();
                if (rs.next()) {
                    return Optional.of(mapRowToParkingSlot(rs));
                }
            } catch (SQLException e) {
                System.err.println("parkingDAO Error in insertOrUpdatePricing: " + e.getMessage());
            } finally {
                DBConnectionUtil.closeConnection(conn, ps);
            }
            return Optional.empty();
        }

        public boolean updateSlotType(String spotID, SlotType newType) throws SQLException {
            Connection conn = null;
            PreparedStatement ps = null;
            try {
                conn = DBConnectionUtil.getConnection();
                ps = conn.prepareStatement(UPDATE_SLOT_TYPE);
                ps.setString(1, newType.name());
                ps.setString(2, spotID);

                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                System.err.println("parkingDAO Error in updateSlotType: " + e.getMessage());
                throw e;
            } finally {
                DBConnectionUtil.closeConnection(conn, ps);
            }
        }


    }
