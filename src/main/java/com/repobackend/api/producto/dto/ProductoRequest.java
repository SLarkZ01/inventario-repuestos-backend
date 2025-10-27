package com.repobackend.api.producto.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAlias;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class ProductoRequest {
    @JsonAlias({"id", "idString"})
    private String idString;

    @NotBlank(message = "nombre no debe estar vacío")
    @JsonAlias({"nombre", " nombre "})
    private String nombre;

    @JsonAlias({"descripcion", " descripcion "})
    private String descripcion;

    @JsonAlias({"precio", " precio "})
    private Double precio;

    @Min(0)
    @JsonAlias({"stock", " stock "})
    private Integer stock;

    @JsonAlias({"categoriaId", " categoriaId "})
    private String categoriaId;

    @JsonAlias({"imagenRecurso", " imagenRecurso "})
    private Integer imagenRecurso;

    @JsonAlias({"listaMedios", " listaMedios "})
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
        this.nombre = nombre == null ? null : nombre.trim();
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion == null ? null : descripcion.trim();
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
        this.categoriaId = categoriaId == null ? null : categoriaId.trim();
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
