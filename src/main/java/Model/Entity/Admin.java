package main.java.Model.Entity;


/**
 * Maps to the 'admins' table in the database.
 * Represents administrative staff information with branch and job role details.
 */
public class Admin {
    private int admin_ID;
    private int user_ID;
    private String firstname;
    private String surname;
    private String contact_number;
    private String job_title;
    private int branch_ID;

    // === CONSTRUCTORS ===

    /**
     * Default constructor
     */
    public Admin() {}

    /**
     * Constructor for existing admins
     * @param admin_ID The admin's unique identifier
     * @param user_ID The associated User account ID
     * @param firstname The admin's first name
     * @param surname The admin's surname
     * @param contact_number The admin's contact number
     * @param job_title The admin's job title
     * @param branch_ID The associated Branch ID where the admin works
     */
    public Admin(int admin_ID, int user_ID, String firstname, String surname, String contact_number, String job_title, int branch_ID) {
        this.admin_ID = admin_ID;
        this.user_ID = user_ID;
        this.firstname = firstname;
        this.surname = surname;
        this.contact_number = contact_number;
        this.job_title = job_title;
        this.branch_ID = branch_ID;
    }

    /**
     * Constructor for newly registered admins
     * @param user_ID The associated User account ID
     * @param firstname The admin's first name
     * @param surname The admin's surname
     * @param contact_number The admin's contact number
     * @param job_title The admin's job title
     * @param branch_ID The associated Branch ID where the admin works
     */
    public Admin(int user_ID, String firstname, String surname, String contact_number, String job_title, int branch_ID) {
        this.user_ID = user_ID;
        this.firstname = firstname;
        this.surname = surname;
        this.contact_number = contact_number;
        this.job_title = job_title;
        this.branch_ID = branch_ID;
    }

    // === GETTERS AND SETTERS ===

    public int getAdmin_ID() {return admin_ID;}
    public void setAdmin_ID(int admin_ID) {this.admin_ID = admin_ID;}

    public int getUser_ID() {return user_ID;}
    public void setUser_ID(int user_ID) {this.user_ID = user_ID;}

    public String getFirstname() {return firstname;}
    public void setFirstname(String firstname) {this.firstname = firstname;}

    public String getSurname() {return surname;}
    public void setSurname(String surname) {this.surname = surname;}

    public String getContact_number() {return contact_number;}
    public void setContact_number(String contact_number) {this.contact_number = contact_number;}

    public String getJob_title() {return job_title;}
    public void setJob_title(String job_title) {this.job_title = job_title;}

    public int getBranch_ID() {return branch_ID;}
    public void setBranch_ID(int branch_ID) {this.branch_ID = branch_ID;}

    @Override
    public String toString() {
        return "Admin{" +
                "Admin ID: " + admin_ID +
                ", User ID: " + user_ID +
                ", First name: " + firstname +
                ", Surname: " + surname +
                ", Contact number: " + contact_number +
                ", Job title: " + job_title +
                ", Branch ID: " + branch_ID + '}';
    }
}
