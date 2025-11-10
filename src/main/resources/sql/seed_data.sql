-- =====================================================================
-- 2_seed_data.sql
-- Populates the dbCar_parking_system with test data for 1 branch and 10 users.
-- =====================================================================

USE dbCar_parking_system;

-- Disable foreign key checks to allow truncating tables in any order
SET FOREIGN_KEY_CHECKS=0;

-- Truncate all tables to ensure a clean slate (child tables first)
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
INSERT INTO branches (branch_ID, name, contact_number, email, max_slots, location, opening_time, closing_time)
VALUES (1, 'Main St. Garage', '09171234567', 'main@park.com', 100, '123 Main St, Manila, NCR', '06:00:00', '22:00:00');

-- =====================================================================
-- 2. INSERT USERS (2 Admins, 8 Customers)
-- Note: password_hash is set to PLAIN TEXT for development testing.
-- =====================================================================
-- Admins (ID 1, 2)
INSERT INTO users (user_ID, email, password_hash, role, join_date)
VALUES
    (1, 'admin@park.com', 'admin123', 'Admin', NOW()),
    (2, 'regina@park.com', 'regina123', 'Admin', NOW());

-- Customers (ID 3-10)
INSERT INTO users (user_ID, email, password_hash, role, join_date)
VALUES
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
-- =====================================================================
-- Admin Profiles (linked to user_ID 1, 2 and branch_ID 1)
INSERT INTO admins (user_ID, firstname, surname, contact_number, job_title, branch_ID)
VALUES
    (1, 'Jeroen', 'Tenorio', '09171112222', 'System Administrator', 1),
    (2, 'Regina', 'Miral', '09173334444', 'Branch Manager', 1);

-- Customer Profiles (linked to user_ID 3-10)
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
INSERT INTO vehicles (user_ID, plate_number, vehicle_Type, vehicle_Brand)
VALUES
    (3, 'ABC-123', 'SUV', 'Toyota'),
    (3, 'XYZ-789', 'Sedan', 'Honda'),
    (4, 'JKL-456', 'Motorcycle', 'Yamaha'),
    (7, 'QWE-555', 'Hatchback', 'Suzuki');

-- Parking Slots (for branch_ID 1)
INSERT INTO parking_slots (spot_ID, branch_ID, floor_level, slot_type, availability)
VALUES
    ('A01', 1, 1, 'Regular', TRUE),
    ('A02', 1, 1, 'Regular', TRUE),
    ('A03', 1, 1, 'Regular', TRUE),
    ('B01', 1, 2, 'VIP', TRUE),
    ('B02', 1, 2, 'VIP', TRUE),
    ('P01', 1, 1, 'PWD', TRUE),
    ('M01', 1, 1, 'Motorcycle', TRUE);

-- Pricing Rules (for branch_ID 1)
INSERT INTO pricing (branch_ID, slot_type, hourly_rate, overtime_rate)
VALUES
    (1, 'Regular', 50.00, 75.00),
    (1, 'PWD', 40.00, 60.00),
    (1, 'Motorcycle', 30.00, 45.00),
    (1, 'VIP', 100.00, 150.00);

SELECT 'Seed data populated successfully!' AS status;