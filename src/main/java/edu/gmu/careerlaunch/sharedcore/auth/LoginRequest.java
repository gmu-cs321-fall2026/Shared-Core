package edu.gmu.careerlaunch.sharedcore.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * The JSON body for POST /auth/login: {"email": "...", "password": "..."}
 * The annotations are the validation rules. If one fails, the caller gets a 400.
 */
public record LoginRequest(
        @NotBlank(message = "required") @Email(message = "must be a valid email") @Size(max = 255) String email,
        @NotBlank(message = "required") @Size(max = 200) String password) {
}
