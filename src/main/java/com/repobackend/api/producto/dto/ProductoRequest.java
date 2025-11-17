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

    @JsonAlias({"tasaIva", " tasaIva ", "iva"})
    private Double tasaIva; // Tasa de IVA en porcentaje (0, 5, 19, etc.)

    @Min(0)
    @JsonAlias({"stock", " stock "})
    private Integer stock;

    @JsonAlias({"categoriaId", " categoriaId "})
    private String categoriaId;


    @JsonAlias({"listaMedios", " listaMedios "})
    private List<java.util.Map<String, Object>> listaMedios;

    // Especificaciones técnicas (clave -> valor) por ejemplo: {"Marca":"Yamaha", "Cilindraje":"150cc"}
    private java.util.Map<String, String> specs;
    @JsonAlias({"tallerId"," tallerId "})
    private String tallerId;

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

    public Double getTasaIva() {
        return tasaIva;
    }

    public void setTasaIva(Double tasaIva) {
        this.tasaIva = tasaIva;
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


    public List<java.util.Map<String, Object>> getListaMedios() {
        return listaMedios;
    }

    public void setListaMedios(List<java.util.Map<String, Object>> listaMedios) {
        this.listaMedios = listaMedios;
    }

    public java.util.Map<String, String> getSpecs() {
        return specs;
    }

    public void setSpecs(java.util.Map<String, String> specs) {
        this.specs = specs;
    }

    public String getTallerId() { return tallerId; }
    public void setTallerId(String tallerId) { this.tallerId = tallerId == null ? null : tallerId.trim(); }
}
