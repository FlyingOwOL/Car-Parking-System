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
    -- TODO:
);

-- Payment Record: Tracks payments details for a reservation
DROP TABLE IF EXISTS `payments`;
CREATE TABLE `payments` (
    -- TODO:
);

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