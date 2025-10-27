package com.repobackend.api.carrito.model;

public class CarritoItem {
    private String productoId;
    private Integer cantidad;

    public String getProductoId() { return productoId; }
    public void setProductoId(String productoId) { this.productoId = productoId; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
}
