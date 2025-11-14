package com.repobackend.api.taller.dto;

import jakarta.validation.constraints.NotBlank;

public class TallerRequest {
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    public TallerRequest() {}
    public TallerRequest(String nombre) { this.nombre = nombre; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
}

