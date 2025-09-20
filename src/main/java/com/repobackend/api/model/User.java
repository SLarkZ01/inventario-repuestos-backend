package com.repobackend.api.model;

import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Document(collection = "users")
public class User {
    @Id
    private String id;
    private String username;
    private String email;
    @JsonIgnore
    private String password; // bcrypt hash
    private String nombre;
    private String apellido;
    private List<String> roles;
    private boolean activo = true;
    private Date fechaCreacion = new Date();
    private Date fechaUltimaConexion;
    private Date passwordChangedAt;

    // getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }
    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public Date getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Date fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public Date getFechaUltimaConexion() { return fechaUltimaConexion; }
    public void setFechaUltimaConexion(Date fechaUltimaConexion) { this.fechaUltimaConexion = fechaUltimaConexion; }
    public Date getPasswordChangedAt() { return passwordChangedAt; }
    public void setPasswordChangedAt(Date passwordChangedAt) { this.passwordChangedAt = passwordChangedAt; }
}
