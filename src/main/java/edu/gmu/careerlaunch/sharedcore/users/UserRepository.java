package edu.gmu.careerlaunch.sharedcore.users;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final RowMapper<User> userMapper = (rs, rowNum) -> {
        return new User(
            rs.getString("first_name"),
            rs.getString("last_name"),
            rs.getString("email"),
            rs.getString("role"),
            rs.getString("status")
        );
    };

    public List<User> getUserByEmail(String email) 
    {
        String sql = "SELECT * FROM users WHERE email = ?";
        return jdbcTemplate.query(sql, userMapper, email);
    }

    public int registerUser(UserProtected user) 
    {
        String sql = "INSERT INTO users (email, password_hash, first_name, last_name) VALUES (?, ?, ?, ?)";
        return jdbcTemplate.update(sql, user.getEmail(), user.getHashword(), user.getFirstName(), user.getLastName());
    }

    public int changeUserDisplayName(String first_name, String last_name, String email)
    {
        String sql = "UPDATE users SET first_name = ?, last_name = ? WHERE email = ?";
        return jdbcTemplate.update(sql, first_name, last_name, email);
    }
}