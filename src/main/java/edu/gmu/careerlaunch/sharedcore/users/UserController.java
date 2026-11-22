package edu.gmu.careerlaunch.sharedcore.users;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController 
{
    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) 
    {
        this.userRepository = userRepository;
    }

    @RequestMapping("/docs/users") 
    public String index() {
        return "index.html";
    }

    /*
    The following annotations (GetMapping, PostMapping, PutMapping) tell Spring that if it receives
    an HTTP request of the specified type (GET, POST, or PUT) with the URI provided to the right of the annotation,
    it should redirect to the annotated method.
     */
    @GetMapping("/users")
    @ResponseBody // This frankly less understood by me but it serializes the return value into the HTTP response.
    public List<User> getUsersByEmail(@RequestParam String email) {
        return userRepository.getUserByEmail(email);
    }

    @PostMapping("/users")
    @ResponseBody
    public int registerUser(@RequestBody UserProtected user)
    {
        System.out.println(user);
        return userRepository.registerUser(user);
        
    }

    @PutMapping("/users")
    @ResponseBody
    public int changeUserDisplayName(@RequestParam String email, @RequestBody Map<String, String> names) 
    {   
        return userRepository.changeUserDisplayName(names.get("first_name"), names.get("last_name"), email);
    }
}