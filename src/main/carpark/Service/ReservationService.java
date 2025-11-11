package Service;

public class ReservationService {
    /*
    Method: createReservation
    Transaction 4.2: Reserving a Parking Slot
    TODO: MIRAL
    DAO to use: ReservationDAO, ParkingDAO, VehicleDAO
    1. Start Transaction.
    2. Check Availability: Call ParkingDAO.getAvailableSlots().
    Select one spot_ID. (If none, throw error).
    3. Insert Reservation: Call ReservationDAO.insertReservation().
    4. Lock Slot: Call  ParkingDAO.updateSlotAvailability(spotId, false)
    to mark it unavailable.
     */

    /*
    Method: cancelReservation
    Transaction 4.4: Cancelling a Reservation
    TODO: TENORIO
    DAO to use: ReservationDAO, ParkingDAO
    1. Start Transaction.
    2. Verify Status: Call ReservationDAO.getReservationById() and ensure it's not already completed/paid.
    3. Update Status: Call ReservationDAO.updateReservationStatus() to 'Cancelled'.
    4. Release Slot: Call ParkingDAO.updateSlotAvailability(spotId, true) to mark it available.
     */
 }
