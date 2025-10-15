package com.repobackend.api.dto;

import jakarta.validation.constraints.NotBlank;

public class CategoriaRequest {
    private String idString;

    @NotBlank
    private String nombre;

    private String descripcion;

    private Integer iconoRecurso;

    public String getIdString() {
        return idString;
    }

    public void setIdString(String idString) {
        this.idString = idString;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Integer getIconoRecurso() {
        return iconoRecurso;
    }

    public void setIconoRecurso(Integer iconoRecurso) {
        this.iconoRecurso = iconoRecurso;
    }
}
