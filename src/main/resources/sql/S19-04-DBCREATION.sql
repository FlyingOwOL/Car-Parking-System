-- ===== DATABASE CREATION =====

CREATE DATABASE IF NOT EXISTS `dbCar_parking_system`;
USE dbCar_parking_system;

-- ===== CORE USERS =====
-- User Record: Single source for login and identity
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
    `user_ID` INT(11) NOT NULL AUTO_INCREMENT,
    `email` VARCHAR(100) NOT NULL UNIQUE,
    `password_hash` VARCHAR(255) NOT NULL,
    `role` ENUM('Admin', 'Customer') NOT NULL,
    `join_date` DATE NOT NULL,

    PRIMARY KEY(`user_ID`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- Customer Record: Stores customer-specific details (1:1 with users WHERE role=`Customer`)
DROP TABLE IF EXISTS `customers`;
CREATE TABLE `customers` (
    `customer_ID` INT(11) NOT NULL AUTO_INCREMENT,
    `user_ID` INT(11) NOT NULL,
    `firstname` VARCHAR(50) NOT NULL,
    `surname` VARCHAR(50) NOT NULL,
    `contact_number` VARCHAR(11),

    PRIMARY KEY(`customer_ID`),
    FOREIGN KEY (`user_ID`) REFERENCES `users`(`user_ID`)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;


-- Branches Record: defines physical location and maximum slots
DROP TABLE IF EXISTS `branches`;
CREATE TABLE `branches` (
    `branch_ID` INT(11) NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(50) NOT NULL,
    `contact_number` VARCHAR(11) NOT NULL UNIQUE,
    `email` VARCHAR(100) NOT NULL UNIQUE,
    `max_slots` INT NOT NULL,
    `location` VARCHAR(100),
    `opening_time` TIME,
    `closing_time` TIME,

    PRIMARY KEY (`branch_ID`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- Admin Record: Stores admin-specific details (1:1 with users WHERE role=`Admin`)
DROP TABLE IF EXISTS `admins`;
CREATE TABLE `admins` (
    `admin_ID` INT(11) NOT NULL AUTO_INCREMENT,
    `user_ID` INT(11) NOT NULL UNIQUE,
    `firstname` VARCHAR(50) NOT NULL,
    `surname` VARCHAR(50) NOT NULL,
    `contact_number` VARCHAR(11),
    `job_title` VARCHAR(50) NOT NULL,
    `branch_ID` INT(11) NOT NULL,

    PRIMARY KEY(`admin_ID`),
    FOREIGN KEY(`user_ID`) REFERENCES `users`(`user_ID`)
        ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY(`branch_ID`) REFERENCES `branches`(`branch_ID`)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- ===== ASSETS TABLES =====
-- Vehicle Record: Link vehicles to their owner (user_ID)
DROP TABLE IF EXISTS `vehicles`;
CREATE TABLE `vehicles` (
    `vehicle_ID` INT(11) NOT NULL AUTO_INCREMENT,
    `user_ID` INT(11) NOT NULL,
    `plate_number` VARCHAR(10) NOT NULL UNIQUE,
    `vehicle_Type` VARCHAR(20),
    `vehicle_Brand` VARCHAR(50),

    PRIMARY KEY(`vehicle_ID`),
    FOREIGN KEY(`user_ID`) REFERENCES `users`(`user_ID`)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- Parking Slot Record: Defines individual physical slots
DROP TABLE IF EXISTS `parking_slots`;
CREATE TABLE `parking_slots` (
    `spot_ID` VARCHAR(11) NOT NULL,
    `branch_ID` INT(11) NOT NULL,
    `floor_level` INT NOT NULL,
    `slot_type` ENUM('Regular', 'PWD', 'Motorcycle', 'VIP') NOT NULL,
    `availability` BOOLEAN NOT NULL DEFAULT TRUE,

    PRIMARY KEY (`spot_ID`),
    FOREIGN KEY (`branch_ID`) REFERENCES `branches`(`branch_ID`)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- ===== TRANSACTIONAL TABLES =====
-- Reservation Record: Tracks booking History
DROP TABLE IF EXISTS `reservations`;
CREATE TABLE `reservations` (
    transact_ID INT(11) NOT NULL AUTO_INCREMENT,
    vehicle_ID INT(11) NOT NULL,
    spot_ID VARCHAR(10) NOT NULL,
    expected_time_in DATETIME,
    check_in_time DATETIME,
    time_Out DATETIME,
    dateReserved DATETIME NOT NULL,
    status ENUM ('Active', 'Completed', 'Cancelled', 'No-Show') NOT NULL,

    PRIMARY KEY(transact_ID),
    FOREIGN KEY(vehicle_ID) REFERENCES `vehicles`(`vehicle_ID`)
        ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY(spot_ID) REFERENCES `parking_slots`(`spot_ID`)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- Payment Record: Tracks payments details for a reservation
DROP TABLE IF EXISTS `payments`;
CREATE TABLE `payments` (
    payment_ID INT(11) NOT NULL AUTO_INCREMENT,
    transact_ID INT(11) NOT NULL UNIQUE,
    amount_To_Pay DECIMAL(6, 2) NOT NULL,
    amount_paid DECIMAL(6, 2),
    payment_date DATETIME,
    payment_status ENUM('Pending', 'Paid', 'Refunded') NOT NULL,
    mode_of_payment ENUM('Cash', 'E-wallet', 'Credit Card'),

    PRIMARY KEY(payment_ID),
    FOREIGN KEY(transact_ID) REFERENCES `reservations`(`transact_ID`)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- Pricing Record: Stores the hourly rates
-- Idea pa lng still looking how this can be integrated
DROP TABLE IF EXISTS `pricing`;
CREATE TABLE `pricing` (
    `pricing_ID` INT(11) NOT NULL AUTO_INCREMENT,
    `branch_ID` INT(11) NOT NULL,
    `slot_type` ENUM('Regular', 'PWD', 'Motorcycle', 'VIP') NOT NULL,
    `hourly_rate` DECIMAL(6,2) NOT NULL,
    `overtime_rate` DECIMAL(6,2) NOT NULL,

    PRIMARY KEY (`pricing_ID`),
    UNIQUE KEY `uk_branch_slot` (`branch_ID`, `slot_type`), -- Ensures one price per slot type per branch
    FOREIGN KEY (`branch_ID`) REFERENCES `branches`(`branch_ID`)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- ===== DATABASE POPULATE =====

SET FOREIGN_KEY_CHECKS=0;

TRUNCATE TABLE payments;
TRUNCATE TABLE reservations;
TRUNCATE TABLE pricing;
TRUNCATE TABLE parking_slots;
TRUNCATE TABLE vehicles;
TRUNCATE TABLE admins;
TRUNCATE TABLE customers;
TRUNCATE TABLE users;
TRUNCATE TABLE branches;

ALTER TABLE users AUTO_INCREMENT = 1;
ALTER TABLE customers AUTO_INCREMENT = 1;
ALTER TABLE admins AUTO_INCREMENT = 1;
ALTER TABLE branches AUTO_INCREMENT = 1;
ALTER TABLE vehicles AUTO_INCREMENT = 1;
ALTER TABLE reservations AUTO_INCREMENT = 1;
ALTER TABLE payments AUTO_INCREMENT = 1;
ALTER TABLE pricing AUTO_INCREMENT = 1;

SET FOREIGN_KEY_CHECKS=1;

-- ===== INSERT BRANCHES=====
INSERT INTO branches (`name`, contact_number, email, max_slots, `location`, opening_time, closing_time)
VALUES ('DLSU-M Garage', '09171234567', 'DLSU.taft@park.com', 100, '2401 Taft Avenue, Manila 1004, Philippines', '06:00:00', '22:00:00');

-- Parking Slots (for branch_ID 1)
INSERT INTO parking_slots (spot_ID, branch_ID, floor_level, slot_type, availability)
VALUES
    -- Floor 1 - PWD (4)
    ('1-P-1-001',1,1,'PWD',TRUE),
    ('1-P-1-002',1,1,'PWD',TRUE),
    ('1-P-1-003',1,1,'PWD',TRUE),
    ('1-P-1-004',1,1,'PWD',TRUE),

    -- Floor 1 - VIP (4)
    ('1-V-1-005',1,1,'VIP',TRUE),
    ('1-V-1-006',1,1,'VIP',TRUE),
    ('1-V-1-007',1,1,'VIP',TRUE),
    ('1-V-1-008',1,1,'VIP',TRUE),

    -- Floor 1 - REGULAR (indices 8..39 => 32 slots)
    ('1-R-1-009',1,1,'Regular',TRUE),
    ('1-R-1-010',1,1,'Regular',TRUE),
    ('1-R-1-011',1,1,'Regular',TRUE),
    ('1-R-1-012',1,1,'Regular',TRUE),
    ('1-R-1-013',1,1,'Regular',TRUE),
    ('1-R-1-014',1,1,'Regular',TRUE),
    ('1-R-1-015',1,1,'Regular',TRUE),
    ('1-R-1-016',1,1,'Regular',TRUE),
    ('1-R-1-017',1,1,'Regular',TRUE),
    ('1-R-1-018',1,1,'Regular',TRUE),
    ('1-R-1-019',1,1,'Regular',TRUE),
    ('1-R-1-020',1,1,'Regular',TRUE),
    ('1-R-1-021',1,1,'Regular',TRUE),
    ('1-R-1-022',1,1,'Regular',TRUE),
    ('1-R-1-023',1,1,'Regular',TRUE),
    ('1-R-1-024',1,1,'Regular',TRUE),
    ('1-R-1-025',1,1,'Regular',TRUE),
    ('1-R-1-026',1,1,'Regular',TRUE),
    ('1-R-1-027',1,1,'Regular',TRUE),
    ('1-R-1-028',1,1,'Regular',TRUE),
    ('1-R-1-029',1,1,'Regular',TRUE),
    ('1-R-1-030',1,1,'Regular',TRUE),
    ('1-R-1-031',1,1,'Regular',TRUE),
    ('1-R-1-032',1,1,'Regular',TRUE),
    ('1-R-1-033',1,1,'Regular',TRUE),
    ('1-R-1-034',1,1,'Regular',TRUE),
    ('1-R-1-035',1,1,'Regular',TRUE),
    ('1-R-1-036',1,1,'Regular',TRUE),
    ('1-R-1-037',1,1,'Regular',TRUE),
    ('1-R-1-038',1,1,'Regular',TRUE),
    ('1-R-1-039',1,1,'Regular',TRUE),
    ('1-R-1-040',1,1,'Regular',TRUE),

    -- Floor 1 - MOTORCYCLE (20 slots indices 40..59 -> 20)
    ('1-M-1-041',1,1,'Motorcycle',TRUE),
    ('1-M-1-042',1,1,'Motorcycle',TRUE),
    ('1-M-1-043',1,1,'Motorcycle',TRUE),
    ('1-M-1-044',1,1,'Motorcycle',TRUE),
    ('1-M-1-045',1,1,'Motorcycle',TRUE),
    ('1-M-1-046',1,1,'Motorcycle',TRUE),
    ('1-M-1-047',1,1,'Motorcycle',TRUE),
    ('1-M-1-048',1,1,'Motorcycle',TRUE),
    ('1-M-1-049',1,1,'Motorcycle',TRUE),
    ('1-M-1-050',1,1,'Motorcycle',TRUE),
    ('1-M-1-051',1,1,'Motorcycle',TRUE),
    ('1-M-1-052',1,1,'Motorcycle',TRUE),
    ('1-M-1-053',1,1,'Motorcycle',TRUE),
    ('1-M-1-054',1,1,'Motorcycle',TRUE),
    ('1-M-1-055',1,1,'Motorcycle',TRUE),
    ('1-M-1-056',1,1,'Motorcycle',TRUE),
    ('1-M-1-057',1,1,'Motorcycle',TRUE),
    ('1-M-1-058',1,1,'Motorcycle',TRUE),
    ('1-M-1-059',1,1,'Motorcycle',TRUE),
    ('1-M-1-060',1,1,'Motorcycle',TRUE),

    ('1-P-2-001',1,2,'PWD',TRUE),
    ('1-P-2-002',1,2,'PWD',TRUE),
    ('1-P-2-003',1,2,'PWD',TRUE),
    ('1-P-2-004',1,2,'PWD',TRUE),

    ('1-V-2-005',1,2,'VIP',TRUE),
    ('1-V-2-006',1,2,'VIP',TRUE),
    ('1-V-2-007',1,2,'VIP',TRUE),
    ('1-V-2-008',1,2,'VIP',TRUE),

    ('1-R-2-009',1,2,'Regular',TRUE),
    ('1-R-2-010',1,2,'Regular',TRUE),
    ('1-R-2-011',1,2,'Regular',TRUE),
    ('1-R-2-012',1,2,'Regular',TRUE),
    ('1-R-2-013',1,2,'Regular',TRUE),
    ('1-R-2-014',1,2,'Regular',TRUE),
    ('1-R-2-015',1,2,'Regular',TRUE),
    ('1-R-2-016',1,2,'Regular',TRUE),
    ('1-R-2-017',1,2,'Regular',TRUE),
    ('1-R-2-018',1,2,'Regular',TRUE),
    ('1-R-2-019',1,2,'Regular',TRUE),
    ('1-R-2-020',1,2,'Regular',TRUE),
    ('1-R-2-021',1,2,'Regular',TRUE),
    ('1-R-2-022',1,2,'Regular',TRUE),
    ('1-R-2-023',1,2,'Regular',TRUE),
    ('1-R-2-024',1,2,'Regular',TRUE),
    ('1-R-2-025',1,2,'Regular',TRUE),
    ('1-R-2-026',1,2,'Regular',TRUE),
    ('1-R-2-027',1,2,'Regular',TRUE),
    ('1-R-2-028',1,2,'Regular',TRUE),
    ('1-R-2-029',1,2,'Regular',TRUE),
    ('1-R-2-030',1,2,'Regular',TRUE),
    ('1-R-2-031',1,2,'Regular',TRUE),
    ('1-R-2-032',1,2,'Regular',TRUE),
    ('1-R-2-033',1,2,'Regular',TRUE),
    ('1-R-2-034',1,2,'Regular',TRUE),
    ('1-R-2-035',1,2,'Regular',TRUE),
    ('1-R-2-036',1,2,'Regular',TRUE),
    ('1-R-2-037',1,2,'Regular',TRUE),
    ('1-R-2-038',1,2,'Regular',TRUE),
    ('1-R-2-039',1,2,'Regular',TRUE),
    ('1-R-2-040',1,2,'Regular',TRUE);

-- ===== INSERT USERS=====
INSERT INTO users (user_ID, email, password_hash, `role`, join_date)
VALUES
    (1, 'admin@park.com', 'admin123', 'Admin', NOW()),
    (2, 'DLSU@park.com', 'DLSU123', 'Admin', NOW()),
    (3, 'rubia@email.com', 'password', 'CUSTOMER', NOW()),
    (4, 'sy@email.com', 'password', 'CUSTOMER', NOW()),
    (5, 'miral@email.com', 'password', 'CUSTOMER', NOW()),
    (6, 'account@email.com', 'password', 'CUSTOMER', NOW()),
    (7, 'smith@email.com', 'password', 'CUSTOMER', NOW()),
    (8, 'johnson@email.com', 'password', 'CUSTOMER', NOW()),
    (9, 'lee@email.com', 'password', 'CUSTOMER', NOW()),
    (10, 'ross@email.com', 'password', 'CUSTOMER', NOW()),
    (11, 'adams@email.com', 'password', 'CUSTOMER', NOW()),
    (12, 'burns@email.com', 'password', 'CUSTOMER', NOW()),
    (13, 'hopper@email.com', 'password', 'CUSTOMER', NOW()),
    (14, 'hill@email.com', 'password', 'CUSTOMER', NOW()),
    (15, 'wong@email.com', 'password', 'CUSTOMER', NOW()),
    (16, 'bauer@email.com', 'password', 'CUSTOMER', NOW()),
    (17, 'lee2@email.com', 'password', 'CUSTOMER', NOW()),
    (18, 'ng@email.com', 'password', 'CUSTOMER', NOW()),
    (19, 'cruz@email.com', 'password', 'CUSTOMER', NOW()),
    (20, 'reyes@email.com', 'password', 'CUSTOMER', NOW()),
    (21, 'diaz@email.com', 'password', 'CUSTOMER', NOW()),
    (22, 'luna@email.com', 'password', 'CUSTOMER', NOW()),
    (23, 'lopez@email.com', 'password', 'CUSTOMER', NOW()),
    (24, 'tan@email.com', 'password', 'CUSTOMER', NOW()),
    (25, 'velasquez@email.com', 'password', 'CUSTOMER', NOW()),
    (26, 'garcia@email.com', 'password', 'CUSTOMER', NOW()),
    (27, 'patel@email.com', 'password', 'CUSTOMER', NOW()),
    (28, 'shah@email.com', 'password', 'CUSTOMER', NOW()),
    (29, 'park@email.com', 'password', 'CUSTOMER', NOW()),
    (30, 'reid@email.com', 'password', 'CUSTOMER', NOW()),
    (31, 'mendoza@email.com', 'password', 'CUSTOMER', NOW()),
    (32, 'cortes@email.com', 'password', 'CUSTOMER', NOW()),
    (33, 'lopez2@email.com', 'password', 'CUSTOMER', NOW()),
    (34, 'dela@email.com', 'password', 'CUSTOMER', NOW()),
    (35, 'ramos@email.com', 'password', 'CUSTOMER', NOW()),
    (36, 'ortega@email.com', 'password', 'CUSTOMER', NOW()),
    (37, 'lim@email.com', 'password', 'CUSTOMER', NOW()),
    (38, 'gomez@email.com', 'password', 'CUSTOMER', NOW()),
    (39, 'torres@email.com', 'password', 'CUSTOMER', NOW()),
    (40, 'ilagan@email.com', 'password', 'CUSTOMER', NOW()),
    (41, 'martinez@email.com', 'password', 'CUSTOMER', NOW()),
    (42, 'cruz2@email.com', 'password', 'CUSTOMER', NOW()),
    (43, 'delgado@email.com', 'password', 'CUSTOMER', NOW()),
    (44, 'santos@email.com', 'password', 'CUSTOMER', NOW()),
    (45, 'perez@email.com', 'password', 'CUSTOMER', NOW()),
    (46, 'valdez@email.com', 'password', 'CUSTOMER', NOW()),
    (47, 'guzman@email.com', 'password', 'CUSTOMER', NOW()),
    (48, 'lopez3@email.com', 'password', 'CUSTOMER', NOW()),
    (49, 'tan2@email.com', 'password', 'CUSTOMER', NOW()),
    (50, 'ibrahim@email.com', 'password', 'CUSTOMER', NOW());

-- ===== INSERT PROFILES =====
INSERT INTO admins (user_ID, firstname, surname, contact_number, job_title, branch_ID)
VALUES
    (1, 'Jeroen', 'Tenorio', '09171112222', 'System Administrator', 1),
    (2, 'DLSU', 'Archers', '09173334444', 'Branch Manager', 1);

-- Customer Profiles
INSERT INTO customers (user_ID, firstname, surname, contact_number)
VALUES
    (3,'Kyle','Rubia','09221112222'),
    (4,'Jason','Sy','09223334444'),
    (5,'Regina','Miral','09331112222'),
    (6,'Test','Account','09333334444'),
    (7,'Anna','Smith','09441112222'),
    (8,'Bob','Johnson','09443334444'),
    (9,'Charlie','Lee','09551112222'),
    (10,'Diana','Ross','09553334444'),
    (11,'Eve','Adams','09661112222'),
    (12,'Frank','Burns','09663334444'),
    (13,'Grace','Hopper','09771112222'),
    (14,'Hank','Hill','09773334444'),
    (15,'Ivy','Wong','09881112222'),
    (16,'Jack','Bauer','09883334444'),
    (17,'Kim','Lee','09991112222'),
    (18,'Liam','Ng','09993334444'),
    (19,'Maya','Cruz','09180011222'),
    (20,'Nora','Reyes','09180022333'),
    (21,'Omar','Diaz','09180033444'),
    (22,'Paula','Luna','09180044555'),
    (23,'Quinn','Lopez','09180055666'),
    (24,'Rico','Tan','09180066777'),
    (25,'Sara','Velasquez','09180077888'),
    (26,'Toby','Garcia','09180088999'),
    (27,'Uma','Patel','09180111222'),
    (28,'Vik','Shah','09180122333'),
    (29,'Wendy','Park','09180133444'),
    (30,'Xander','Reid','09180144555'),
    (31,'Yara','Mendoza','09180155666'),
    (32,'Zane','Cortes','09180166777'),
    (33,'Aaron','Lopez','09180177888'),
    (34,'Bella','Dela','09180188999'),
    (35,'Carl','Ramos','09180211222'),
    (36,'Dana','Ortega','09180222333'),
    (37,'Ethan','Lim','09180233444'),
    (38,'Fiona','Gomez','09180244555'),
    (39,'Gabe','Torres','09180255666'),
    (40,'Hazel','Ilagan','09180266777'),
    (41,'Ian','Martinez','09180277888'),
    (42,'Jill','Cruz','09180388999'),
    (43,'Kris','Delgado','09180311222'),
    (44,'Lana','Santos','09180322333'),
    (45,'Milo','Perez','09180333444'),
    (46,'Nina','Valdez','09180344555'),
    (47,'Orion','Guzman','09180355666'),
    (48,'Pia','Lopez','09180366777'),
    (49,'Ralph','Tan','09180377888'),
    (50,'Sofia','Ibrahim','09180388999');

-- ===== INSERT VEHICLES=====
INSERT INTO vehicles (user_ID, plate_number, vehicle_Type, vehicle_Brand)
VALUES
    (3,  'ABC-123', 'Sedan', 'Toyota'),
    (3,  'XYZ-789', 'SUV', 'Mercedes'),
    (4,  'JKL-456', 'Motorcycle', 'Yamaha'),
    (7,  'QWE-555', 'Hatchback', 'Suzuki'),
    (5,  'CAR-501', 'Sedan', 'Honda'),
    (6,  'TST-001', 'Sedan', 'Toyota'),
    (8,  'BEE-202', 'SUV', 'Ford'),
    (9,  'CHA-303', 'SUV', 'Nissan'),
    (10, 'DIA-404', 'Sedan', 'Hyundai'),
    (11, 'EVE-505', 'Hatchback', 'Kia'),
    (12, 'FRK-606', 'SUV', 'Chevrolet'),
    (13, 'GRA-707', 'Sedan', 'Mazda'),
    (14, 'HNK-808', 'SUV', 'Isuzu'),
    (15, 'IVY-909', 'Sedan', 'Subaru'),
    (16, 'JCK-010', 'Truck', 'Mitsubishi'),
    (17, 'KIM-111', 'Sedan', 'Lexus'),
    (18, 'LIA-212', 'Hatchback', 'Honda'),
    (19, 'MAY-313', 'SUV', 'Toyota'),
    (20, 'NOR-414', 'Sedan', 'Honda'),
    (21, 'OMA-515', 'Motorcycle', 'Kawasaki'),
    (22, 'PAU-616', 'SUV', 'Ford'),
    (23, 'QUI-717', 'Sedan', 'Toyota'),
    (24, 'RIC-818', 'SUV', 'Hyundai'),
    (25, 'SAR-919', 'Hatchback', 'Kia'),
    (26, 'TOB-020', 'Sedan', 'Nissan'),
    (27, 'UMA-121', 'SUV', 'Mitsubishi'),
    (28, 'VIK-222', 'Sedan', 'Toyota'),
    (29, 'WEN-323', 'Hatchback', 'Honda'),
    (30, 'XAN-424', 'SUV', 'Mazda'),
    (31, 'YAR-525', 'Sedan', 'Ford'),
    (32, 'ZAN-626', 'SUV', 'Chevrolet'),
    (33, 'AAR-727', 'Motorcycle', 'Honda'),
    (34, 'BEL-828', 'Hatchback', 'Suzuki'),
    (35, 'CAR-929', 'Sedan', 'Hyundai'),
    (36, 'DAN-030', 'Sedan', 'Kia'),
    (37, 'ETH-131', 'SUV', 'Toyota'),
    (38, 'FIO-232', 'Sedan', 'Honda');

-- Pricing Rules (for branch_ID 1)
INSERT INTO pricing (branch_ID, slot_type, hourly_rate, overtime_rate)
VALUES
    (1, 'Regular', 20.00, 50.00),
    (1, 'PWD', 10.00, 20.00),
    (1, 'Motorcycle', 10.00, 20.00),
    (1, 'VIP', 50.00, 50.00);

-- ===== RESERVATIONS & PAYMENTS =====
INSERT INTO reservations (vehicle_ID, spot_ID, expected_time_in, check_in_time, time_Out, dateReserved, `status`)
VALUES
    (1, '1-R-1-009', '2025-11-01 08:00:00', '2025-11-01 07:55:00', '2025-11-01 10:30:00', '2025-10-31 12:00:00', 'Completed'),
    (2, '1-R-1-010', '2025-11-02 09:00:00', '2025-11-02 08:55:00', '2025-11-02 12:15:00', '2025-11-01 09:00:00', 'Completed'),
    (3, '1-M-1-041', '2025-11-02 14:00:00', '2025-11-02 13:50:00', '2025-11-02 15:00:00', '2025-11-01 10:00:00', 'Completed'),
    (4, '1-P-1-001', '2025-11-03 10:00:00', '2025-11-03 09:55:00', '2025-11-03 13:00:00', '2025-11-02 08:00:00', 'Completed'),
    (5, '1-R-1-011', '2025-11-04 07:00:00', '2025-11-04 06:50:00', '2025-11-04 09:30:00', '2025-11-03 07:00:00', 'Completed'),
    (6, '1-R-1-012', '2025-11-04 15:00:00', '2025-11-04 14:45:00', '2025-11-04 18:30:00', '2025-11-03 11:00:00', 'Completed'),
    (7, '1-R-1-013', '2025-11-05 08:00:00', '2025-11-05 07:55:00', '2025-11-05 10:30:00', '2025-11-04 12:00:00', 'Completed'),
    (8, '1-R-1-014', '2025-11-06 09:00:00', '2025-11-06 08:55:00', '2025-11-06 12:00:00', '2025-11-05 09:00:00', 'Completed'),
    (9, '1-R-1-015', '2025-11-06 10:00:00', '2025-11-06 09:50:00', '2025-11-06 12:45:00', '2025-11-05 10:00:00', 'Completed'),
    (10,'1-R-1-016', '2025-11-07 07:00:00', '2025-11-07 06:55:00', '2025-11-07 10:10:00', '2025-11-06 08:30:00', 'Completed'),
    (1,'1-R-1-017', '2025-11-07 11:00:00', '2025-11-07 10:55:00', '2025-11-07 14:00:00', '2025-11-06 10:00:00', 'Completed'),
    (12,'1-R-1-018', '2025-11-08 08:00:00', '2025-11-08 07:50:00', '2025-11-08 09:30:00', '2025-11-07 09:00:00', 'Completed'),
    (13,'1-R-1-019', '2025-11-08 13:00:00', '2025-11-08 12:55:00', '2025-11-08 15:20:00', '2025-11-07 12:00:00', 'Completed'),
    (2,'1-R-1-020', '2025-11-09 07:30:00', '2025-11-09 07:20:00', '2025-11-09 10:00:00', '2025-11-08 08:00:00', 'Completed'),
    (15,'1-R-1-021', '2025-11-09 12:00:00', '2025-11-09 11:50:00', '2025-11-09 14:30:00', '2025-11-08 12:00:00', 'Completed'),
    (16,'1-R-1-022', '2025-11-10 09:00:00', '2025-11-10 08:50:00', '2025-11-10 11:40:00', '2025-11-09 09:00:00', 'Completed'),
    (17,'1-R-1-023', '2025-11-10 14:00:00', '2025-11-10 13:55:00', '2025-11-10 16:20:00', '2025-11-09 13:00:00', 'Completed'),
    (18,'1-R-1-024', '2025-11-11 08:15:00', '2025-11-11 08:05:00', '2025-11-11 11:00:00', '2025-11-10 09:30:00', 'Completed'),
    (19,'1-R-1-025', '2025-11-11 13:00:00', '2025-11-11 12:55:00', '2025-11-11 15:10:00', '2025-11-10 13:00:00', 'Completed'),
    (20,'1-R-1-026', '2025-11-12 07:00:00', '2025-11-12 06:50:00', '2025-11-12 10:00:00', '2025-11-11 07:00:00', 'Completed'),
    (21,'1-R-1-027', '2025-11-12 09:00:00', '2025-11-12 08:55:00', '2025-11-12 12:12:00', '2025-11-11 09:00:00', 'Completed'),
    (22,'1-R-1-028', '2025-11-13 08:00:00', '2025-11-13 07:50:00', '2025-11-13 10:30:00', '2025-11-12 08:00:00', 'Completed'),
    (23,'1-R-1-029', '2025-11-13 11:00:00', '2025-11-13 10:55:00', '2025-11-13 13:30:00', '2025-11-12 10:00:00', 'Completed'),
    (24,'1-R-1-030', '2025-11-14 07:00:00', '2025-11-14 06:50:00', '2025-11-14 09:15:00', '2025-11-13 07:00:00', 'Completed'),
    (25,'1-R-1-031', '2025-11-14 13:00:00', '2025-11-14 12:55:00', '2025-11-14 15:20:00', '2025-11-13 12:00:00', 'Completed'),
    (26,'1-R-1-032', '2025-11-15 09:00:00', '2025-11-15 08:45:00', '2025-11-15 11:40:00', '2025-11-14 09:00:00', 'Completed'),
    (27,'1-R-1-033', '2025-11-15 14:00:00', '2025-11-15 13:55:00', '2025-11-15 16:05:00', '2025-11-14 13:00:00', 'Completed'),
    (28,'1-R-1-034', '2025-11-16 08:00:00', '2025-11-16 07:55:00', '2025-11-16 10:30:00', '2025-11-15 08:00:00', 'Completed'),
    (29,'1-R-1-035', '2025-11-16 11:00:00', '2025-11-16 10:55:00', '2025-11-16 13:40:00', '2025-11-15 10:00:00', 'Completed'),
    (30,'1-R-1-036', '2025-11-17 07:30:00', '2025-11-17 07:20:00', '2025-11-17 09:50:00', '2025-11-16 07:00:00', 'Completed'),
    (31,'1-R-1-037', '2025-11-17 12:00:00', '2025-11-17 11:50:00', '2025-11-17 14:30:00', '2025-11-16 11:00:00', 'Completed'),
    (32,'1-R-1-038', '2025-11-18 08:00:00', '2025-11-18 07:55:00', '2025-11-18 10:20:00', '2025-11-17 08:00:00', 'Completed'),
    (33,'1-R-1-039', '2025-11-18 13:00:00', '2025-11-18 12:55:00', '2025-11-18 15:10:00', '2025-11-17 12:00:00', 'Completed'),
    (34,'1-R-1-040', '2025-11-19 09:00:00', '2025-11-19 08:55:00', '2025-11-19 11:15:00', '2025-11-18 09:00:00', 'Completed'),
    (35,'1-M-1-042', '2025-11-19 10:00:00', '2025-11-19 09:55:00', '2025-11-19 11:05:00', '2025-11-18 10:00:00', 'Completed'),
    (36,'1-R-2-009', '2025-11-20 08:00:00', '2025-11-20 07:55:00', '2025-11-20 10:00:00', '2025-11-19 08:00:00', 'Completed'),
    (1, '1-R-2-010', '2025-11-20 11:00:00', '2025-11-20 10:55:00', '2025-11-20 13:30:00', '2025-11-19 11:00:00', 'Completed'),
    (2, '1-R-2-011', '2025-11-21 07:30:00', '2025-11-21 07:20:00', '2025-11-21 09:40:00', '2025-11-20 07:00:00', 'Completed'),
    (3, '1-R-2-012', '2025-11-21 12:00:00', '2025-11-21 11:50:00', '2025-11-21 14:30:00', '2025-11-20 10:00:00', 'Completed'),
    (4, '1-P-2-001', '2025-11-22 08:00:00', '2025-11-22 07:55:00', '2025-11-22 10:10:00', '2025-11-21 08:00:00', 'Completed'),
    (5, '1-V-2-005', '2025-11-22 14:00:00', '2025-11-22 13:55:00', '2025-11-22 16:00:00', '2025-11-21 12:00:00', 'Completed'),
    (6, '1-R-2-013', '2025-11-23 09:00:00', '2025-11-23 08:55:00', '2025-11-23 11:45:00', '2025-11-22 09:00:00', 'Completed'),
    (7, '1-R-2-014', '2025-11-23 13:00:00', '2025-11-23 12:50:00', '2025-11-23 15:20:00', '2025-11-22 13:00:00', 'Completed'),
    (8, '1-R-2-015', '2025-11-24 07:00:00', '2025-11-24 06:50:00', '2025-11-24 09:30:00', '2025-11-23 07:00:00', 'Completed'),
    (9, '1-R-2-016', '2025-11-24 10:00:00', '2025-11-24 09:55:00', '2025-11-24 12:45:00', '2025-11-23 10:00:00', 'Completed'),
    (10,'1-R-2-017', '2025-11-25 08:00:00', '2025-11-25 07:50:00', '2025-11-25 10:15:00', '2025-11-24 08:30:00', 'Completed'),
    (11,'1-R-2-018', '2025-11-25 11:00:00', '2025-11-25 10:55:00', '2025-11-25 13:30:00', '2025-11-24 10:00:00', 'Completed'),
    (12,'1-R-2-019', '2025-11-26 09:00:00', '2025-11-26 08:55:00', '2025-11-26 11:40:00', '2025-11-25 09:00:00', 'Completed'),
    (13,'1-R-2-020', '2025-11-26 14:00:00', '2025-11-26 13:55:00', '2025-11-26 16:30:00', '2025-11-25 13:00:00', 'Completed'),
    (14,'1-R-2-021', '2025-11-27 07:00:00', '2025-11-27 06:50:00', '2025-11-27 10:08:00', '2025-11-26 07:00:00', 'Completed'),
    (15,'1-R-2-022', '2025-11-27 12:00:00', '2025-11-27 11:50:00', '2025-11-27 14:40:00', '2025-11-26 11:00:00', 'Completed'),
    (16,'1-R-2-023', '2025-11-28 08:00:00', '2025-11-28 07:55:00', '2025-11-28 10:30:00', '2025-11-27 09:00:00', 'Completed'),
    (17,'1-R-2-024', '2025-11-28 13:00:00', '2025-11-28 12:55:00', '2025-11-28 15:10:00', '2025-11-27 12:00:00', 'Completed'),
    (18,'1-R-2-025', '2025-11-29 09:00:00', '2025-11-29 08:50:00', '2025-11-29 11:30:00', '2025-11-28 09:00:00', 'Completed'),
    (19,'1-R-2-026', '2025-11-29 14:00:00', '2025-11-29 13:55:00', '2025-11-29 16:00:00', '2025-11-28 13:00:00', 'Completed'),
    (1,'1-R-2-027', '2025-11-30 07:00:00', '2025-11-30 06:50:00', '2025-11-30 09:40:00', '2025-11-29 07:00:00', 'Completed'),
    (21,'1-R-2-028', '2025-11-30 11:00:00', '2025-11-30 10:50:00', '2025-11-30 13:30:00', '2025-11-29 10:00:00', 'Completed'),
    (22,'1-R-2-029', '2025-12-01 08:00:00', '2025-12-01 07:55:00', '2025-12-01 10:20:00', '2025-11-30 08:00:00', 'Completed'),
    (23,'1-R-2-030', '2025-12-01 13:00:00', '2025-12-01 12:55:00', '2025-12-01 15:10:00', '2025-11-30 12:00:00', 'Completed'),
    (24,'1-R-2-031', '2025-12-02 09:00:00', '2025-12-02 08:55:00', '2025-12-02 11:40:00', '2025-12-01 09:00:00', 'Completed'),
    (25,'1-R-2-032', '2025-12-02 14:00:00', '2025-12-02 13:55:00', '2025-12-02 16:10:00', '2025-12-01 13:00:00', 'Completed'),
    (26,'1-R-2-033', '2025-12-03 08:00:00', '2025-12-03 07:55:00', '2025-12-03 10:25:00', '2025-12-02 08:00:00', 'Completed'),
    (27,'1-R-2-034', '2025-12-03 12:00:00', '2025-12-03 11:50:00', '2025-12-03 14:20:00', '2025-12-02 10:00:00', 'Completed'),
    (28,'1-R-2-035', '2025-12-04 07:00:00', '2025-12-04 06:55:00', '2025-12-04 09:10:00', '2025-12-03 07:00:00', 'Completed');

INSERT INTO payments (transact_ID, amount_To_Pay, amount_paid, payment_date, payment_status, mode_of_payment)
VALUES
    (1, 150.00, 150.00, '2025-11-01 10:35:00', 'Paid', 'Cash'),
    (2, 200.00, 200.00, '2025-11-02 12:20:00', 'Paid', 'Credit Card'),
    (3, 70.00, 70.00, '2025-11-02 15:10:00', 'Paid', 'E-wallet'),
    (4, 180.00, 180.00, '2025-11-03 13:05:00', 'Paid', 'Cash'),
    (5, 120.00, 120.00, '2025-11-04 09:40:00', 'Paid', 'E-wallet'),
    (6, 250.00, 250.00, '2025-11-04 18:35:00', 'Paid', 'Credit Card'),
    (7, 140.00, 140.00, '2025-11-05 10:40:00', 'Paid', 'Cash'),
    (8, 160.00, 160.00, '2025-11-06 12:10:00', 'Paid', 'Credit Card'),
    (9, 155.00, 155.00, '2025-11-06 13:00:00', 'Paid', 'Cash'),
    (10,200.00, 200.00, '2025-11-07 10:20:00', 'Paid', 'E-wallet'),
    (11,175.00,175.00, '2025-11-07 14:05:00', 'Paid', 'Credit Card'),
    (12,110.00,110.00, '2025-11-08 09:40:00', 'Paid', 'Cash'),
    (13,145.00,145.00, '2025-11-08 15:30:00', 'Paid', 'Credit Card'),
    (14,135.00,135.00, '2025-11-09 10:15:00', 'Paid', 'Cash'),
    (15,168.00,168.00, '2025-11-09 14:40:00', 'Paid', 'E-wallet'),
    (16,120.00,120.00, '2025-11-10 11:50:00', 'Paid', 'Cash'),
    (17,210.00,210.00, '2025-11-10 16:40:00', 'Paid', 'Credit Card'),
    (18,130.00,130.00, '2025-11-11 11:10:00', 'Paid', 'E-wallet'),
    (19,155.00,155.00, '2025-11-11 15:20:00', 'Paid', 'Cash'),
    (20,145.00,145.00, '2025-11-12 10:10:00', 'Paid', 'Credit Card'),
    (21,160.00,160.00, '2025-11-12 12:30:00', 'Paid', 'E-wallet'),
    (22,150.00,150.00, '2025-11-13 11:05:00', 'Paid', 'Cash'),
    (23,140.00,140.00, '2025-11-13 13:45:00', 'Paid', 'Credit Card'),
    (24,110.00,110.00, '2025-11-14 09:20:00', 'Paid', 'E-wallet'),
    (25,175.00,175.00, '2025-11-14 15:45:00', 'Paid', 'Credit Card'),
    (26,160.00,160.00, '2025-11-15 11:50:00', 'Paid', 'Cash'),
    (27,170.00,170.00, '2025-11-15 16:10:00', 'Paid', 'Credit Card'),
    (28,150.00,150.00, '2025-11-16 10:20:00', 'Paid', 'E-wallet'),
    (29,155.00,155.00, '2025-11-16 13:50:00', 'Paid', 'Cash'),
    (30,138.00,138.00, '2025-11-17 10:00:00', 'Paid', 'Credit Card'),
    (31,145.00,145.00, '2025-11-17 14:35:00', 'Paid', 'E-wallet'),
    (32,152.00,152.00, '2025-11-18 10:40:00', 'Paid', 'Credit Card'),
    (33,160.00,160.00, '2025-11-18 15:20:00', 'Paid', 'Cash'),
    (34,175.00,175.00, '2025-11-19 11:25:00', 'Paid', 'Credit Card'),
    (35,65.00,65.00,    '2025-11-19 11:10:00', 'Paid', 'E-wallet'),
    (36,140.00,140.00, '2025-11-20 13:35:00', 'Paid', 'Credit Card'),
    (37,150.00,150.00, '2025-11-20 13:40:00', 'Paid', 'Cash'),
    (38,155.00,155.00, '2025-11-21 09:25:00', 'Paid', 'E-wallet'),
    (39,145.00,145.00, '2025-11-21 14:10:00', 'Paid', 'Credit Card'),
    (40,165.00,165.00, '2025-11-22 11:40:00', 'Paid', 'Cash'),
    (41,130.00,130.00, '2025-11-22 15:10:00', 'Paid', 'E-wallet'),
    (42,120.00,120.00, '2025-11-23 10:40:00', 'Paid', 'Credit Card'),
    (43,160.00,160.00, '2025-11-23 15:25:00', 'Paid', 'Cash'),
    (44,140.00,140.00, '2025-11-24 10:05:00', 'Paid', 'Credit Card'),
    (45,150.00,150.00, '2025-11-24 12:25:00', 'Paid', 'E-wallet'),
    (46,145.00,145.00, '2025-11-25 10:35:00', 'Paid', 'Cash'),
    (47,155.00,155.00, '2025-11-25 14:50:00', 'Paid', 'Credit Card'),
    (48,156.00,156.00, '2025-11-26 11:45:00', 'Paid', 'E-wallet'),
    (49,148.00,148.00, '2025-11-26 15:15:00', 'Paid', 'Credit Card'),
    (50,135.00,135.00, '2025-11-27 10:00:00', 'Paid', 'Cash'),
    (51,142.00,142.00, '2025-11-27 14:25:00', 'Paid', 'E-wallet'),
    (52,150.00,150.00, '2025-11-28 12:00:00', 'Paid', 'Credit Card'),
    (53,158.00,158.00, '2025-11-28 15:55:00', 'Paid', 'Cash'),
    (54,140.00,140.00, '2025-11-29 11:30:00', 'Paid', 'E-wallet'),
    (55,150.00,150.00, '2025-11-29 16:05:00', 'Paid', 'Credit Card'),
    (56,145.00,145.00, '2025-11-30 11:40:00', 'Paid', 'Cash'),
    (57,155.00,155.00, '2025-11-30 15:10:00', 'Paid', 'Credit Card'),
    (58,160.00,160.00, '2025-12-01 11:35:00', 'Paid', 'E-wallet'),
    (59,145.00,145.00, '2025-12-01 14:50:00', 'Paid', 'Credit Card'),
    (60,130.00,130.00, '2025-12-02 10:20:00', 'Paid', 'Cash');

INSERT INTO reservations (vehicle_ID, spot_ID, expected_time_in, check_in_time, time_Out, dateReserved, `status`)
VALUES
    (10, '1-P-1-001', '2025-12-10 09:00:00', '2025-12-10 08:50:00', NULL, '2025-12-09 09:00:00', 'Active'),
    (5,  '1-P-1-002', '2025-12-11 10:00:00', '2025-12-11 09:55:00', NULL, '2025-12-10 09:50:00', 'Active'),
    (8,  '1-M-1-045', '2025-12-12 11:00:00', '2025-12-12 10:55:00', NULL, '2025-12-11 10:00:00', 'Active'),
    (11, '1-P-1-003', '2025-12-12 13:00:00', '2025-12-12 12:55:00', NULL, '2025-12-11 12:00:00', 'Active'),
    (14, '1-P-1-004', '2025-12-13 08:00:00', '2025-12-13 07:55:00', NULL, '2025-12-12 08:00:00', 'Active'),
    (17, '1-V-1-005', '2025-12-13 12:00:00', '2025-12-13 11:50:00', NULL, '2025-12-12 11:00:00', 'Active'),
    (20, '1-V-1-006', '2025-12-14 09:00:00', '2025-12-14 08:55:00', NULL, '2025-12-13 09:00:00', 'Active'),
    (22, '1-V-1-007', '2025-12-14 14:00:00', '2025-12-14 13:55:00', NULL, '2025-12-13 13:00:00', 'Active'),
    (24, '1-V-1-008', '2025-12-15 07:30:00', '2025-12-15 07:25:00', NULL, '2025-12-14 07:00:00', 'Active'),
    (26, '1-R-1-021', '2025-12-15 10:00:00', '2025-12-15 09:55:00', NULL, '2025-12-14 09:00:00', 'Active'),
    (28, '1-R-1-022', '2025-12-16 09:00:00', '2025-12-16 08:55:00', NULL, '2025-12-15 09:00:00', 'Active'),
    (30, '1-R-1-023', '2025-12-16 11:00:00', '2025-12-16 10:55:00', NULL, '2025-12-15 10:00:00', 'Active'),
    (32, '1-R-1-024', '2025-12-17 08:00:00', '2025-12-17 07:55:00', NULL, '2025-12-16 08:00:00', 'Active'),
    (34, '1-R-1-025', '2025-12-17 13:00:00', '2025-12-17 12:55:00', NULL, '2025-12-16 12:00:00', 'Active'),
    (36, '1-M-1-055', '2025-12-18 09:00:00', '2025-12-18 08:55:00', NULL, '2025-12-17 09:00:00', 'Active'),
    (1,  '1-P-2-001', '2025-12-18 12:00:00', '2025-12-18 11:50:00', NULL, '2025-12-17 11:00:00', 'Active'),
    (3,  '1-P-2-002', '2025-12-19 07:00:00', '2025-12-19 06:55:00', NULL, '2025-12-18 07:00:00', 'Active'),
    (6,  '1-P-2-003', '2025-12-19 09:00:00', '2025-12-19 08:55:00', NULL, '2025-12-18 09:00:00', 'Active'),
    (9,  '1-P-2-004', '2025-12-20 10:00:00', '2025-12-20 09:55:00', NULL, '2025-12-19 09:00:00', 'Active'),
    (12, '1-V-2-005', '2025-12-20 11:30:00', '2025-12-20 11:20:00', NULL, '2025-12-19 10:00:00', 'Active');

INSERT INTO reservations (vehicle_ID, spot_ID, expected_time_in, check_in_time, time_Out, dateReserved, `status`)
VALUES
    (13,'1-V-2-006','2025-11-05 10:00:00',NULL,NULL,'2025-11-04 09:00:00','Cancelled'),
    (14,'1-V-2-007','2025-11-06 12:00:00',NULL,NULL,'2025-11-05 10:00:00','Cancelled'),
    (15,'1-V-2-008','2025-11-07 09:00:00',NULL,NULL,'2025-11-06 08:00:00','Cancelled'),
    (16,'1-R-2-009','2025-11-08 11:00:00',NULL,NULL,'2025-11-07 09:00:00','Cancelled'),
    (17,'1-R-2-010','2025-11-09 07:00:00',NULL,NULL,'2025-11-08 07:00:00','Cancelled'),
    (18,'1-R-2-011','2025-11-10 08:00:00',NULL,NULL,'2025-11-09 08:00:00','Cancelled'),
    (19,'1-R-2-012','2025-11-11 09:00:00',NULL,NULL,'2025-11-10 09:00:00','Cancelled'),
    (20,'1-R-2-013','2025-11-12 10:00:00',NULL,NULL,'2025-11-11 10:00:00','Cancelled'),
    (21,'1-R-2-014','2025-11-13 11:00:00',NULL,NULL,'2025-11-12 11:00:00','Cancelled'),
    (22,'1-R-2-015','2025-11-14 12:00:00',NULL,NULL,'2025-11-13 12:00:00','Cancelled'),
    (23,'1-R-2-016','2025-11-15 13:00:00',NULL,NULL,'2025-11-14 13:00:00','Cancelled'),
    (24,'1-R-2-017','2025-11-16 14:00:00',NULL,NULL,'2025-11-15 14:00:00','Cancelled'),
    (25,'1-R-2-018','2025-11-17 15:00:00',NULL,NULL,'2025-11-16 15:00:00','Cancelled'),
    (26,'1-R-2-019','2025-11-18 16:00:00',NULL,NULL,'2025-11-17 16:00:00','Cancelled'),
    (27,'1-R-2-020','2025-11-19 17:00:00',NULL,NULL,'2025-11-18 17:00:00','Cancelled');

INSERT INTO reservations (vehicle_ID, spot_ID, expected_time_in, check_in_time, time_Out, dateReserved, `status`)
VALUES
    (28,'1-R-2-021','2025-11-20 09:00:00',NULL,NULL,'2025-11-19 09:00:00','No-Show'),
    (29,'1-R-2-022','2025-11-21 10:00:00',NULL,NULL,'2025-11-20 10:00:00','No-Show'),
    (30,'1-R-2-023','2025-11-22 11:00:00',NULL,NULL,'2025-11-21 11:00:00','No-Show'),
    (31,'1-R-2-024','2025-11-23 12:00:00',NULL,NULL,'2025-11-22 12:00:00','No-Show'),
    (32,'1-R-2-025','2025-11-24 13:00:00',NULL,NULL,'2025-11-23 13:00:00','No-Show');

UPDATE parking_slots ps
    JOIN (SELECT DISTINCT spot_ID FROM reservations WHERE `status` = 'Active') r ON ps.spot_ID = r.spot_ID
SET ps.availability = FALSE;

SELECT 'Seed data populated successfully!' AS status;