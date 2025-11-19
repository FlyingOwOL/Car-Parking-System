package Model.Entity;

/**
 * Maps to the 'customers' table in the database.
 * Represents customer profile information associated with a User account.
 */
public class Customer {
    private int customer_ID;
    private int user_ID;
    private String firstname;
    private String surname;
    private String contact_number;

    // === CONSTRUCTORS ===

    /**
     * Default constructor
     */
    public Customer() {}

    /**
     * Constructor for new customers
     * 
     * @param firstname
     * @param surname
     * @param contact_number
     */
    public Customer(String firstname, String surname, String contact_number){
        this.firstname = firstname;
        this.surname = surname;
        this.contact_number = contact_number;
    }

    /**
     * Constructor for existing customers
     * @param customer_ID The customer's unique identifier
     * @param user_ID The associated User account ID
     * @param firstname The customer's first name
     * @param surname The customer's surname
     * @param contact_number The customer's contact number
     */
    public Customer(int customer_ID, int user_ID, String firstname, String surname, String contact_number) {
        this.customer_ID = customer_ID;
        this.user_ID = user_ID;
        this.firstname = firstname;
        this.surname = surname;
        this.contact_number = contact_number;
    }

    /**
     * Constructor for existing customers
     * @param user_ID The associated User account ID
     * @param firstname The customer's first name
     * @param surname The customer's surname
     * @param contact_number The customer's contact number
     */
    public Customer(int user_ID, String firstname, String surname, String contact_number) {
        this.user_ID = user_ID;
        this.firstname = firstname;
        this.surname = surname;
        this.contact_number = contact_number;
    }

    // === GETTERS AND SETTERS ===

    public int getCustomer_ID() {return customer_ID;}
    public void setCustomer_ID(int customer_ID) {this.customer_ID = customer_ID;}

    public int getUser_ID() {return user_ID;}
    public void setUser_ID(int user_ID) {this.user_ID = user_ID;}

    public String getFirstname() {return firstname;}
    public void setFirstname(String firstname) {this.firstname = firstname;}

    public String getSurname() {return surname;}
    public void setSurname(String surname) {this.surname = surname;}

    public String getContact_number() {return contact_number;}
    public void setContact_number(String contact_number) {this.contact_number = contact_number;}

    @Override
    public String toString() {
        return "Customer{" +
                "Customer ID: " + customer_ID +
                ", User ID: " + user_ID +
                ", First name: " + firstname +
                ", Surname: " + surname +
                ", Contact number: " + contact_number + '}';
    }
}
