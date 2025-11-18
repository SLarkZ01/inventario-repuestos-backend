package com.repobackend.api.cliente.dto;

import java.util.Date;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ClienteRequest", description = "Datos del cliente que se pueden enviar como snapshot al crear una factura")
public class ClienteRequest {
    @Schema(description = "ID del usuario (opcional, hex de Mongo)")
    private String id;

    @Schema(description = "Username del usuario")
    private String username;

    @Schema(description = "Correo electrónico del cliente")
    private String email;

    @Schema(description = "Nombre de pila del cliente")
    private String nombre;

    @Schema(description = "Apellido o apellidos del cliente")
    private String apellido;

    @Schema(description = "Fecha de creación del usuario (ISO 8601)")
    private Date fechaCreacion;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public Date getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Date fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}
