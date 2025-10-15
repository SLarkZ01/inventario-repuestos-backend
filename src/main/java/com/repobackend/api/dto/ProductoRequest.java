package com.repobackend.api.dto;

import java.util.List;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class ProductoRequest {
    private String idString;

    @NotBlank
    private String nombre;

    private String descripcion;

    private Double precio;

    @Min(0)
    private Integer stock;

    private String categoriaId;

    private Integer imagenRecurso;

    private List<java.util.Map<String, Object>> listaMedios;

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

    public Double getPrecio() {
        return precio;
    }

    public void setPrecio(Double precio) {
        this.precio = precio;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public String getCategoriaId() {
        return categoriaId;
    }

    public void setCategoriaId(String categoriaId) {
        this.categoriaId = categoriaId;
    }

    public Integer getImagenRecurso() {
        return imagenRecurso;
    }

    public void setImagenRecurso(Integer imagenRecurso) {
        this.imagenRecurso = imagenRecurso;
    }

    public List<java.util.Map<String, Object>> getListaMedios() {
        return listaMedios;
    }

    public void setListaMedios(List<java.util.Map<String, Object>> listaMedios) {
        this.listaMedios = listaMedios;
    }
}
