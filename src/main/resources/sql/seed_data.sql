-- =====================================================================
-- 2_seed_data.sql (FIXED VERSION)
-- Populates the dbCar_parking_system with test data.
-- Fixes all user_ID and vehicle_ID inconsistencies.
-- =====================================================================

USE dbCar_parking_system;

-- Disable foreign key checks to truncate tables in any order
SET FOREIGN_KEY_CHECKS=0;

-- Truncate all tables to ensure a clean slate
TRUNCATE TABLE payments;
TRUNCATE TABLE reservations;
TRUNCATE TABLE pricing;
TRUNCATE TABLE parking_slots;
TRUNCATE TABLE vehicles;
TRUNCATE TABLE admins;
TRUNCATE TABLE customers;
TRUNCATE TABLE users;
TRUNCATE TABLE branches;

-- Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS=1;

-- =====================================================================
-- 1. INSERT BRANCHES
-- =====================================================================
-- (branch_ID will be 1)
INSERT INTO branches (`name`, contact_number, email, max_slots, `location`, opening_time, closing_time)
VALUES ('Main St. Garage', '09171234567', 'main@park.com', 100, '123 Main St, Manila, NCR', '06:00:00', '22:00:00');

-- =====================================================================
-- 2. INSERT USERS (2 Admins, 8 Customers)
-- IDs are now sequential (1-10)
-- =====================================================================
INSERT INTO users (user_ID, email, password_hash, `role`, join_date)
VALUES
    (1, 'admin@park.com', 'admin123', 'Admin', NOW()),
    (2, 'regina@park.com', 'regina123', 'Admin', NOW()),
    (3, 'kyle@email.com', 'kyle123', 'Customer', NOW()),
    (4, 'jason@email.com', 'jason123', 'Customer', NOW()),
    (5, 'user@email.com', 'pass123', 'Customer', NOW()),
    (6, 'test@email.com', 'test123', 'Customer', NOW()),
    (7, 'anna@email.com', 'anna123', 'Customer', NOW()),
    (8, 'bob@email.com', 'bob123', 'Customer', NOW()),
    (9, 'charlie@email.com', 'charlie123', 'Customer', NOW()),
    (10, 'diana@email.com', 'diana123', 'Customer', NOW());

-- =====================================================================
-- 3. INSERT PROFILES (Admin and Customer)
-- Now correctly references user_IDs 1-10
-- =====================================================================
-- Admin Profiles
INSERT INTO admins (user_ID, firstname, surname, contact_number, job_title, branch_ID)
VALUES
    (1, 'Jeroen', 'Tenorio', '09171112222', 'System Administrator', 1),
    (2, 'Regina', 'Miral', '09173334444', 'Branch Manager', 1);

-- Customer Profiles
INSERT INTO customers (user_ID, firstname, surname, contact_number)
VALUES
    (3, 'Kyle', 'Rubia', '09221112222'),
    (4, 'Jason', 'Sy', '09223334444'),
    (5, 'Default', 'User', '09331112222'),
    (6, 'Test', 'Account', '09333334444'),
    (7, 'Anna', 'Smith', '09441112222'),
    (8, 'Bob', 'Johnson', '09443334444'),
    (9, 'Charlie', 'Lee', '09551112222'),
    (10, 'Diana', 'Ross', '09553334444');

-- =====================================================================
-- 4. INSERT ASSETS (Vehicles, Slots, Pricing)
-- =====================================================================
-- Vehicles (for some customers)
-- NOTE: The database will assign vehicle_ID = 1, 2, 3, 4
INSERT INTO vehicles (user_ID, plate_number, vehicle_Type, vehicle_Brand)
VALUES
    (3, 'ABC-123', 'SUV', 'Toyota'),      -- This will be vehicle_ID = 1
    (3, 'XYZ-789', 'Sedan', 'Honda'),      -- This will be vehicle_ID = 2
    (4, 'JKL-456', 'Motorcycle', 'Yamaha'), -- This will be vehicle_ID = 3
    (7, 'QWE-555', 'Hatchback', 'Suzuki');   -- This will be vehicle_ID = 4

-- Parking Slots (for branch_ID 1)
INSERT INTO parking_slots (spot_ID, branch_ID, floor_level, slot_type, availability)
VALUES
    ('A01', 1, 1, 'Regular', TRUE),
    ('A02', 1, 1, 'Regular', TRUE),
    ('A03', 1, 1, 'Regular', TRUE),
    ('B01', 1, 2, 'VIP', TRUE),
    ('B02', 1, 2, 'VIP', FALSE), -- One slot already taken
    ('P01', 1, 1, 'PWD', TRUE),
    ('M01', 1, 1, 'Motorcycle', TRUE);

-- Pricing Rules (for branch_ID 1)
INSERT INTO pricing (branch_ID, slot_type, hourly_rate, overtime_rate)
VALUES
    (1, 'Regular', 50.00, 75.00),
    (1, 'PWD', 40.00, 60.00),
    (1, 'Motorcycle', 30.00, 45.00),
    (1, 'VIP', 100.00, 150.00);

-- =====================================================================
-- 5. INSERT TRANSACTIONS (Reservations and Payments)
-- Now correctly references vehicle_IDs 1, 2, 3, 4
-- =====================================================================

-- A) A COMPLETED past reservation for user 3 (Kyle)
-- Uses vehicle_ID = 1 ('ABC-123')
INSERT INTO reservations (transact_ID, vehicle_ID, spot_ID, expected_time_in, check_in_time, time_Out, dateReserved, `status`)
VALUES
    (1, 1, 'A01', '2025-11-10 09:00:00', '2025-11-10 08:55:00', '2025-11-10 11:05:00', '2025-11-09 10:00:00', 'Completed');

INSERT INTO payments (transact_ID, amount_To_Pay, amount_paid, payment_date, payment_status, mode_of_payment)
VALUES
    (1, 150.00, 150.00, '2025-11-10 11:06:00', 'Paid', 'Cash');

-- B) An ACTIVE reservation for user 4 (Jason)
-- Uses vehicle_ID = 3 ('JKL-456')
INSERT INTO reservations (transact_ID, vehicle_ID, spot_ID, expected_time_in, check_in_time, time_Out, dateReserved, `status`)
VALUES
    (2, 3, 'M01', '2025-11-16 10:00:00', '2025-11-16 10:02:00', NULL, '2025-11-16 08:00:00', 'Active');

-- C) A CANCELLED reservation for user 7 (Anna)
-- Uses vehicle_ID = 4 ('QWE-555')
INSERT INTO reservations (transact_ID, vehicle_ID, spot_ID, expected_time_in, check_in_time, time_Out, dateReserved, `status`)
VALUES
    (3, 4, 'P01', '2025-11-17 14:00:00', NULL, NULL, '2025-11-16 10:00:00', 'Cancelled');

-- D) An ACTIVE (but taken) reservation for user 3 (Kyle)
-- Uses vehicle_ID = 2 ('XYZ-789') for the busy slot
INSERT INTO reservations (transact_ID, vehicle_ID, spot_ID, expected_time_in, check_in_time, time_Out, dateReserved, `status`)
VALUES
    (4, 2, 'B02', '2025-11-16 12:00:00', '2025-11-16 12:05:00', NULL, '2025-11-15 17:00:00', 'Active');


SELECT 'Seed data populated successfully!' AS status;