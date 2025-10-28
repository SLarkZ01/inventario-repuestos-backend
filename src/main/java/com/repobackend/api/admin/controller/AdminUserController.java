package com.repobackend.api.admin.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.repobackend.api.auth.dto.AdminRequests.CreateAdminRequest;
import com.repobackend.api.auth.dto.AdminRequests.CreateAdminResponse;
import com.repobackend.api.admin.service.AdminUserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {
    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @PostMapping
    public ResponseEntity<?> createAdmin(@Valid @RequestBody CreateAdminRequest req, Authentication authentication) {
        // Determine caller privileges: if authentication != null and has ROLE_ADMIN
        boolean callerIsAdmin = false;
        String callerId = null;
        if (authentication != null && authentication.isAuthenticated()) {
            callerId = authentication.getName();
            callerIsAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        }
        CreateAdminResponse resp = adminUserService.createAdmin(req, callerId, callerIsAdmin);
        return ResponseEntity.status(201).body(resp);
    }
}

