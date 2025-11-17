package Model.Entity;

import java.time.LocalDate;

/**
 * Maps to the 'payments' table in the database.
 * Represents a financial transaction record for parking services.
 */
public class Payment {

    /**
     * Enum representing the current status of a payment.
     */
    public enum PaymentStatus {
        PENDING,    // Payment has not been completed
        PAID,       // Payment has been successfully processed
        REFUNDED    // Payment has been returned to the customer
    }

    /**
     * Enum representing the method used for payment.
     */
    public enum ModeOfPayment {
        CASH,       // Physical cash payment
        E_WALLET,   // Digital wallet payment (GCash, Maya Wallet, etc.)
        CREDIT_CARD // Credit/Debit card payment
    }

    private int payment_ID;
    private int transact_ID;
    private float amount_to_pay;
    private float amount_paid;
    private LocalDate payment_date;
    private PaymentStatus payment_status;
    private ModeOfPayment mode_of_payment;
    private int processed_by;


    // === CONSTRUCTORS ===

    /**
     * Default Constructor
     */
    public Payment() {}

    /**
     * Constructor for creating a new payment before insertion (payment_ID is auto-generated).
     * @param transact_ID The ID of the associated transaction
     * @param amount_to_pay The total amount due for the service
     * @param amount_paid The amount actually paid by the customer
     * @param payment_date The date when the payment was made
     * @param payment_status The current status of the payment
     * @param mode_of_payment The payment method used
     * @param processed_by The ID of the staff who processed the payment (FK)
     */
    public Payment(int transact_ID, float amount_to_pay, float amount_paid, LocalDate payment_date, PaymentStatus payment_status, ModeOfPayment mode_of_payment, int processed_by) {
        this.transact_ID = transact_ID;
        this.amount_to_pay = amount_to_pay;
        this.amount_paid = amount_paid;
        this.payment_date = payment_date;
        this.payment_status = payment_status;
        this.mode_of_payment = mode_of_payment;
        this.processed_by = processed_by;
    }

    /**
     * Full Constructor for reading existing payment records from the database.
     * @param payment_ID The unique identifier of the payment (PK)
     * @param transact_ID The ID of the associated transaction (FK)
     * @param amount_to_pay The total amount due for the service
     * @param amount_paid The amount actually paid by the customer
     * @param payment_date The date when the payment was made
     * @param payment_status The current status of the payment
     * @param mode_of_payment The payment method used
     * @param processed_by The ID of the staff who processed the payment (FK)
     */
    public Payment(int payment_ID, int transact_ID, float amount_to_pay, float amount_paid, LocalDate payment_date, PaymentStatus payment_status, ModeOfPayment mode_of_payment, int processed_by) {
        this.payment_ID = payment_ID;
        this.transact_ID = transact_ID;
        this.amount_to_pay = amount_to_pay;
        this.amount_paid = amount_paid;
        this.payment_date = payment_date;
        this.payment_status = payment_status;
        this.mode_of_payment = mode_of_payment;
        this.processed_by = processed_by;
    }

    // === GETTERS AND SETTERS ===

    public int getPayment_ID() { return payment_ID; }
    public void setPayment_ID(int payment_ID) {  this.payment_ID = payment_ID; }

    public int getTransact_ID() { return transact_ID; }
    public void setTransact_ID(int transact_ID) {  this.transact_ID = transact_ID; }

    public float getAmount_To_Pay() { return amount_to_pay; }
    public void setAmount_To_Pay(float amount_to_pay) { this.amount_to_pay = amount_to_pay; }

    public float getAmount_paid() { return amount_paid; }
    public void setAmount_paid(float amount_paid) { this.amount_paid = amount_paid; }

    public LocalDate getPayment_date() { return payment_date; }
    public void setPayment_date(LocalDate payment_date) { this.payment_date = payment_date; }

    public PaymentStatus getPayment_status() { return payment_status; }
    public void setPayment_status(PaymentStatus payment_status) { this.payment_status = payment_status; }

    public ModeOfPayment getMode_of_payment() { return mode_of_payment; }
    public void setMode_of_payment(ModeOfPayment mode_of_payment) { this.mode_of_payment = mode_of_payment; }

    public int getProcessed_by() { return processed_by; }
    public void setProcessed_by(int processed_by) { this.processed_by = processed_by; }

    @Override
    public String toString() {
        return "Payment {" +
                "Payment ID: " + payment_ID +
                ", Transact ID: " + transact_ID +
                ", Amount_To Pay: " + amount_to_pay +
                ", Amount_paid: " + amount_paid +
                ", Payment_Date: " + payment_date +
                ", Payment_Status: " + payment_status +
                ", Mode_Of Payment: " + mode_of_payment +
                ", Processed By: " + processed_by + '}';
    }
}
