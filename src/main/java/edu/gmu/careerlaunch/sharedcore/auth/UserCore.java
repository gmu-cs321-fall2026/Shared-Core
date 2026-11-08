package edu.gmu.careerlaunch.sharedcore.auth;
//Or: package edu.gmu.careerlaunch.sharedcore.registration.entity (By Claude); IDW

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Email;
//import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Users")
@Data
@NoArgsConstructor
@AllArgsConstructor
//This class represents the database table for each user

public class UserCore {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotBlank(message = "Email is required")
    @Size(max = 200)
    @Email(message = "Email should be valid")
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank(message = "Password is required")
    @Size(max = 200)
    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String[] roles = {"Application & Readiness Tracker", "Career Events & Resources", " Mentor Matching", 
        "Community Forum & Discussion", "Notifications & Communications", "Platform Admin & Analytics", 
        "Mock Interview & Peer Practice"
    };
    
    @Column(nullable = false)
    private String status;
    
    @Column(name = "created_at")
    private java.time.LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = java.time.LocalDateTime.now();
    }
}