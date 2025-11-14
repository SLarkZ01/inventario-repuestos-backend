package com.repobackend.api.taller.dto;

import jakarta.validation.constraints.NotBlank;

public class AlmacenRequest {
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;
    private String ubicacion;

    public AlmacenRequest() {}
    public AlmacenRequest(String nombre, String ubicacion) { this.nombre = nombre; this.ubicacion = ubicacion; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }
}

