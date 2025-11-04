package main.java.Model.Entity;

import main.java.Model.Entity.UserRole;
import java.time.LocalDate;

public class User {
    private int user_id;
    private String email;
    private String passwordHash;
    private UserRole role;
    private LocalDate joinDate;

    /**
     * Default constructor
     */
    public User() {}
}