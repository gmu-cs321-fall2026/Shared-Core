package edu.gmu.careerlaunch.sharedcore.auth;
//Claude: package edu.gmu.careerlaunch.sharedcore.registration.repository; IDW

//Claude: import edu.gmu.careerlaunch.sharedcore.registration.entity.UserCore; IDW
import org.springframework.data.jpa.repository.JpaRepository;


//Handles database operations

public interface UserRepository extends JpaRepository<UserCore, Long>
{
    UserCore findByEmail(String email);
    boolean existsByEmail(String email);
}


//IDW = Import/Package Doesn't Work