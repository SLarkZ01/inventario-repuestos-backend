package com.repobackend.api.dto;

import java.util.Date;
import java.util.List;

/**
 * DTO que representa el perfil público del usuario que se expone al cliente.
 */
public class UserProfile {
    public String id;
    public String username;
    public String email;
    public String nombre;
    public String apellido;
    public List<String> roles;
    public boolean activo;
    public Date fechaCreacion;

    public UserProfile() {}
}
