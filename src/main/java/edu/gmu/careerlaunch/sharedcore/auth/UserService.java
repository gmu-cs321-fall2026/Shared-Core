package edu.gmu.careerlaunch.sharedcore.auth;
//Claude: package edu.gmu.careerlaunch.sharedcore.registration.service; IDW

// import edu.gmu.careerlaunch.sharedcore.registration.entity.UserCore; //IDW
// import edu.gmu.careerlaunch.sharedcore.registration.repository.UserRepository; //IDW
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

//Class for business logic
@Service 
public class UserService {
    @Autowired
    private UserRepository userRepository;
    
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(); //IDW

    public UserCore registerUser(UserCore user) {
        // Check if email already exists
        if (userRepository.existsByEmail(user.getEmail())) { //IDW
            throw new IllegalArgumentException("Email already exists");
        }
        
        // Encrypt password
        user.setPassword(passwordEncoder.encode(user.getPassword())); //IDW
        
        // Save user to database
        return userRepository.save(user); //IDW
    }

    public UserCore getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    public boolean validatePassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword); //IDW
    }
}

//Uncomment every line of code later for debugging
