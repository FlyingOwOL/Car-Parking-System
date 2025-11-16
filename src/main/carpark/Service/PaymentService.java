package Service;

import DAO.DBConnectionUtil;
import DAO.ReservationDAO;
import DAO.ParkingDAO;
import DAO.PaymentDAO;

import Model.Entity.Branch;
import Model.Entity.Payment;
import Model.Entity.Reservation;
import Model.Entity.Pricing;

import javax.swing.text.html.Option;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

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


    private final ReservationDAO reservationDAO;
    private final PaymentDAO paymentDAO;
    private final ParkingDAO parkingDAO;

    private static final String STATUS_COMPLETED = "Completed";
    private static final String PAYMENT_STATUS_PAID = "Paid";

    public PaymentService() {
        this.paymentDAO = new PaymentDAO();
        this.reservationDAO = new ReservationDAO();
        this.parkingDAO = new ParkingDAO();
    }

    public Optional<Payment> processPayment(int reservation_ID, int admin_ID) {
        Optional<Reservation> reservationOpt = reservationDAO.getReservationByID(reservation_ID);
        if (!reservationOpt.isPresent()) { return Optional.empty(); }
        Reservation reservation = reservationOpt.get();

        try {
            Optional<Pricing> pricingOpt = ParkingDAO.getPricingRule(reservation.getBranchID(), reservation.getSlotType());
            if (!pricingOpt.isPresent()) { return Optional.empty(); }
            Pricing pricing = pricingOpt.get();

            Double amountToPay = calculateTotalFee(reservation, pricing.getHourlyRate());

            Optional<Payment> paymentOpt = PaymentDAO.insertPayment(reservation.getID(), amountToPay, amountToPay, modeOfPayment, admin_ID);
            if (!paymentOpt.isPresent()) { return Optional.empty(); }

            boolean statusUpdated = reservationDAO.updateReservationStatus(reservation_ID, STATUS_COMPLETED);
            if (!statusUpdated) { return Optional.empty(); }

            return paymentOpt;
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }

        return Optional.empty();
    }

    public Double calculateTotalFee(Reservation reservation, Double hourlyRate) {
        LocalDateTime checkIn = reservation.getCheckInTime();
        LocalDateTime timeOut = reservation.getTimeOut();

        if (checkIn == null || timeOut == null || hourlyRate == null || hourlyRate <= 0) {
            return 0.0;
        }

        if (checkIn.isBefore(timeOut)) { return 0.0; }

        Duration duration = Duration.between(checkIn, timeOut);

        long minutes = duration.toMinutes();
        long billedHours = (minutes + 59) / 60;

        Double totalFee = hourlyRate * billedHours;

        return Math.round(totalFee * 100.0) / 100.0;
    }





}