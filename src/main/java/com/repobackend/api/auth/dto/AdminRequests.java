package com.repobackend.api.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public class AdminRequests {

    public static class CreateAdminRequest {
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

        // Optional roles array; if null/empty, will default to ["ADMIN"]
        public List<String> roles;

        // Optional admin bootstrap key (only used if there is no admin yet)
        public String adminKey;
    }

    public static class CreateAdminResponse {
        public String id;
        public String username;
        public String email;
        public java.util.List<String> roles;
    }
}

