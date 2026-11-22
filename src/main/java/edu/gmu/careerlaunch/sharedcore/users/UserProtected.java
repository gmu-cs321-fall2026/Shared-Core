package edu.gmu.careerlaunch.sharedcore.users;

public class UserProtected extends User {
    private String hashed_password;

    public UserProtected(String first_name, String last_name, String email, String role, String status, String hashed_password)
    {
        super(first_name, last_name, email, role, status);
        this.hashed_password = hashed_password;
    }

    public String getHashword()
    {
        return hashed_password;
    }
}
