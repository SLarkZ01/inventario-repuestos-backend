package com.repobackend.api.producto.dto;

import java.util.Date;
import java.util.List;

public class ProductoResponse {
    private String id;
    private String idString;
    private String nombre;
    private String descripcion;
    private Double precio;
    private Double tasaIva;
    private Integer stock;
    private String categoriaId;
    private List<java.util.Map<String, Object>> listaMedios;
    // Especificaciones técnicas estructuradas
    private java.util.Map<String, String> specs;
    // thumbnailUrl: URL directa o publicId de la primera imagen para mostrar en listados
    private String thumbnailUrl;
    // Stock total (suma de almacenes)
    private Integer totalStock;
    // Desglose por almacén: lista de objetos { almacenId, cantidad }
    private java.util.List<java.util.Map<String, Object>> stockByAlmacen;
    private Date creadoEn;

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
        this.categoriaId = categoriaId;
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

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public Integer getTotalStock() {
        return totalStock;
    }

    public void setTotalStock(Integer totalStock) {
        this.totalStock = totalStock;
    }

    public java.util.List<java.util.Map<String, Object>> getStockByAlmacen() {
        return stockByAlmacen;
    }

    public void setStockByAlmacen(java.util.List<java.util.Map<String, Object>> stockByAlmacen) {
        this.stockByAlmacen = stockByAlmacen;
    }

    public Date getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(Date creadoEn) {
        this.creadoEn = creadoEn;
    }
}
