package com.repobackend.api.factura.model;

public class FacturaItem {
    private String productoId;
    private String nombreProducto; // snapshot del nombre para histórico
    private String codigoProducto; // código/SKU si existe
    private Integer cantidad;
    private Double precioUnitario;
    private Double descuento; // descuento aplicado (valor o %)
    private Double baseImponible; // base antes de impuestos
    private Double tasaIva; // 0, 5, 19, etc. (porcentaje)
    private Double valorIva; // valor calculado del IVA
    private Double subtotal; // cantidad * precioUnitario - descuento
    private Double totalItem; // subtotal + valorIva

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
