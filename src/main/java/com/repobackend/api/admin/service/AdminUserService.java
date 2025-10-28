package com.repobackend.api.admin.service;

import java.util.Date;

import org.springframework.core.env.Environment;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.repobackend.api.auth.dto.AdminRequests.CreateAdminRequest;
import com.repobackend.api.auth.dto.AdminRequests.CreateAdminResponse;
import com.repobackend.api.auth.model.User;
import com.repobackend.api.auth.repository.UserRepository;

import org.springframework.http.HttpStatus;

@Service
public class AdminUserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final Environment env;

    public AdminUserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, Environment env) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.env = env;
    }

    /**
     * Crea un usuario con rol admin (u otros roles) validando permisos.
     * callerIsAdmin: true si el llamador ya es ADMIN (puede crear admins sin adminKey).
     * Si no existe ningún admin en la BD, se permite crear uno con adminKey coincidente con APP_ADMIN_REGISTRATION_KEY.
     */
    public CreateAdminResponse createAdmin(CreateAdminRequest req, String callerId, boolean callerIsAdmin) {
        // validations
        if (userRepository.existsByEmail(req.email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email ya esta registrado");
        }
        if (userRepository.existsByUsername(req.username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Nombre de usuario ya esta en uso");
        }

        boolean wantsAdmin = req.roles == null || req.roles.isEmpty() || req.roles.contains("ADMIN");

        if (wantsAdmin && !callerIsAdmin) {
            // Check bootstrap: if no admin exists, allow creation only with adminKey
            long adminCount = userRepository.countByRole("ADMIN");
            String envAdminKey = env.getProperty("APP_ADMIN_REGISTRATION_KEY");
            if (adminCount > 0) {
                // There is already at least one admin -> caller must be admin
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Permisos insuficientes para crear usuario admin");
            }
            // no admin exists; require adminKey
            if (envAdminKey == null || !envAdminKey.equals(req.adminKey)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Clave de administrador invalida para crear el primer usuario admin");
            }
            // else allowed
        }

        User u = new User();
        u.setUsername(req.username);
        u.setEmail(req.email);
        u.setPassword(passwordEncoder.encode(req.password));
        u.setNombre(req.nombre);
        u.setApellido(req.apellido);
        if (req.roles == null || req.roles.isEmpty()) {
            u.setRoles(java.util.List.of("ADMIN"));
        } else {
            // sanitize roles: keep as provided
            u.setRoles(req.roles);
        }
        u.setActivo(true);
        u.setFechaCreacion(new Date());
        userRepository.save(u);

        CreateAdminResponse resp = new CreateAdminResponse();
        resp.id = u.getId();
        resp.username = u.getUsername();
        resp.email = u.getEmail();
        resp.roles = u.getRoles();
        return resp;
    }
}
