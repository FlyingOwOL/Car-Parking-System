-- ===== DATABASE CREATION =====

CREATE DATABASE IF NOT EXISTS 'dbCar_parking_system';
USE dbCar_parking_system;

-- ===== CORE USERS =====
-- User Record: Single source for login and identity
DROP TABLE IF EXISTS 'users';
CREATE TABLE 'users' (
    -- TODO:
);

-- Customer Record: Stores customer-specific details (1:1 with users WHERE role='Customer')
DROP TABLE IF EXISTS 'customers';
CREATE TABLE 'customers' (
    -- TODO:
);

-- Branches Record: defines physical location and maximum slots
DROP TABLE IF EXISTS 'branches';
CREATE TABLE 'branches' (
    -- TODO:
);

-- Admin Record: Stores admin-specific details (1:1 with users WHERE role='Admin')
DROP TABLE IF EXISTS 'admins';
CREATE TABLE 'admins' (
    -- TODO:
);

-- ===== ASSETS TABLES =====
-- Vehicle Record: Link vehicles to their owner (user_ID)
DROP TABLE IF EXISTS 'vehicles';
CREATE TABLE 'vehicles' (
    -- TODO:
);

-- Parking Slot Record: Defines individual physical slots
DROP TABLE IF EXISTS 'parking_slots';
CREATE TABLE 'parking_slots' (
    -- TODO:
);

-- ===== TRANSACTIONAL TABLES =====
-- Reservation Record: Tracks booking History
DROP TABLE IF EXISTS 'reservations';
CREATE TABLE 'reservations' (
    -- TODO:
);

-- Payment Record: Tracks payments details for a reservation
DROP TABLE IF EXISTS 'payments';
CREATE TABLE 'payments' (
    -- TODO:
);

-- Pricing Record: Stores the hourly rates
-- Idea pa lng still looking how this can be integrated
DROP TABLE IF EXISTS 'pricing';
CREATE TABLE 'pricing' (
    -- TODO:
);