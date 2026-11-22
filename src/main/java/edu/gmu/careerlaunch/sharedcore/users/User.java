package edu.gmu.careerlaunch.sharedcore.users;

public class User {
    private String first_name;
    private String last_name;
    private String email;
    private String role;
    private String status;

    public User(String first_name, String last_name, String email, String role, String status) {
        this.first_name = first_name;
        this.last_name = last_name;
        this.email = email;
        this.role = role;
        this.status = status;
    }

    public String getFirstName() {
        return first_name;
    }

    public String getLastName() {
        return last_name;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public String getStatus() {
        return status;
    }
}