package com.repobackend.api.categoria.dto;

import jakarta.validation.constraints.NotBlank;

public class CategoriaRequest {
    private String idString;

    @NotBlank
    private String nombre;

    private String descripcion;

    private Integer iconoRecurso;
    private String tallerId;
    private String mappedGlobalCategoryId;

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

    public String getTallerId() {
        return tallerId;
    }

    public void setTallerId(String tallerId) {
        this.tallerId = tallerId == null ? null : tallerId.trim();
    }

    public String getMappedGlobalCategoryId() {
        return mappedGlobalCategoryId;
    }

    public void setMappedGlobalCategoryId(String mappedGlobalCategoryId) {
        this.mappedGlobalCategoryId = mappedGlobalCategoryId == null ? null : mappedGlobalCategoryId.trim();
    }
}
