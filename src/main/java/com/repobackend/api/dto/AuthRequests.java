package com.repobackend.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTOs para endpoints de autenticación.
 */
public class AuthRequests {

    public static class LoginRequest {
        @NotBlank
        public String usernameOrEmail;

        @NotBlank
        public String password;

        public String device;
    }

    public static class RegisterRequest {
        @NotBlank
        public String username;

        @Email
        @NotBlank
        public String email;

        @NotBlank
        @Size(min = 8)
        public String password;

        @NotBlank
        public String nombre;

        @NotBlank
        public String apellido;

        public String inviteCode; // optional: code to accept invitation to a taller
    }

}
