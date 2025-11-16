package com.repobackend.api.categoria.dto;

import java.util.List;
import java.util.Map;

import jakarta.validation.constraints.NotBlank;

public class CategoriaRequest {
    private String idString;

    @NotBlank
    private String nombre;

    private String descripcion;

    private Integer iconoRecurso;
    // tallerId ahora es obligatorio para nuevas categorías (pero nullable para migración de categorías globales legacy)
    private String tallerId;
    private String mappedGlobalCategoryId;

    // soportar lista de medios (imagenes) opcional
    private List<Map<String, Object>> listaMedios;

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

    public List<Map<String, Object>> getListaMedios() { return listaMedios; }
    public void setListaMedios(List<Map<String, Object>> listaMedios) { this.listaMedios = listaMedios; }
}
