package com.repobackend.api.dto;

public class AuthRequests {
    public static class LoginRequest {
        public String usernameOrEmail;
        public String password;
        public String device;
    }

    public static class RegisterRequest {
        public String username;
        public String email;
        public String password;
        public String nombre;
        public String apellido;
        public String rol; // optional
    }
}
