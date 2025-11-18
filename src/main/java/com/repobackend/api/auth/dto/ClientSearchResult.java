package com.repobackend.api.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ClientSearchResult", description = "Resultado de búsqueda de cliente para autocompletado")
public class ClientSearchResult {
    @Schema(description = "ID del usuario", example = "690d34252d7f9613780df590")
    private String id;

    @Schema(description = "Username del usuario", example = "jperez")
    private String username;

    @Schema(description = "Email del usuario", example = "jperez@example.com")
    private String email;

    @Schema(description = "Nombre completo del usuario", example = "Juan Pérez")
    private String nombreCompleto;

    public ClientSearchResult() {}

    public ClientSearchResult(String id, String username, String email, String nombreCompleto) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.nombreCompleto = nombreCompleto;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }
}

