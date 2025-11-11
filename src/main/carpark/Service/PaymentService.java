package Service;

public class PaymentService {
    /*
    Method: processPayment
    Transaction 4.3
    TODO: RUBIA
    DAO to use: ReservationDAO, ParkingDAO, PaymentDAO
    1. Start Transaction.
    2. Retrieve Reservation Data: Call ReservationDAO.getReservationById() (get check_in_time, time_Out, spot_ID).
    3. Retrieve Pricing Data: Use spot_ID to find the Branch and SlotType, then call ParkingDAO.getPricingRule() to get hourly_rate.
    4. Calculate Total Fee: Use Java logic to calculate duration, apply hourly/overtime rates, and determine amount_To_Pay.
    5. Insert Payment: Call PaymentDAO.insertPayment() (must include processed_by Admin ID).
    6. Finalize Reservation: Call ReservationDAO.updateReservationStatus() to 'Completed'.
     */
}
