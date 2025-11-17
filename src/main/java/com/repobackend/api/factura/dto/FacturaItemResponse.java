package com.repobackend.api.factura.dto;

public class FacturaItemResponse {
    private String productoId;
    private String nombreProducto;
    private String codigoProducto;
    private Integer cantidad;
    private Double precioUnitario;
    private Double descuento;
    private Double baseImponible;
    private Double tasaIva;
    private Double valorIva;
    private Double subtotal;
    private Double totalItem;

    public String getProductoId() { return productoId; }
    public void setProductoId(String productoId) { this.productoId = productoId; }

    public String getNombreProducto() { return nombreProducto; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }

    public String getCodigoProducto() { return codigoProducto; }
    public void setCodigoProducto(String codigoProducto) { this.codigoProducto = codigoProducto; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

    public Double getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(Double precioUnitario) { this.precioUnitario = precioUnitario; }

    public Double getDescuento() { return descuento; }
    public void setDescuento(Double descuento) { this.descuento = descuento; }

    public Double getBaseImponible() { return baseImponible; }
    public void setBaseImponible(Double baseImponible) { this.baseImponible = baseImponible; }

    public Double getTasaIva() { return tasaIva; }
    public void setTasaIva(Double tasaIva) { this.tasaIva = tasaIva; }

    public Double getValorIva() { return valorIva; }
    public void setValorIva(Double valorIva) { this.valorIva = valorIva; }

    public Double getSubtotal() { return subtotal; }
    public void setSubtotal(Double subtotal) { this.subtotal = subtotal; }

    public Double getTotalItem() { return totalItem; }
    public void setTotalItem(Double totalItem) { this.totalItem = totalItem; }
}
