package com.repobackend.api.categoria.dto;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class CategoriaResponse {
    private String id;
    private String idString;
    private String nombre;
    private String descripcion;
    private Integer iconoRecurso;
    private String tallerId;
    private String mappedGlobalCategoryId;
    private Date creadoEn;

    // lista de medios (imagenes)
    private List<Map<String, Object>> listaMedios;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public String getTallerId() { return tallerId; }
    public void setTallerId(String tallerId) { this.tallerId = tallerId; }

    public String getMappedGlobalCategoryId() { return mappedGlobalCategoryId; }
    public void setMappedGlobalCategoryId(String mappedGlobalCategoryId) { this.mappedGlobalCategoryId = mappedGlobalCategoryId; }

    public Date getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(Date creadoEn) {
        this.creadoEn = creadoEn;
    }

    public List<Map<String, Object>> getListaMedios() { return listaMedios; }
    public void setListaMedios(List<Map<String, Object>> listaMedios) { this.listaMedios = listaMedios; }
}
