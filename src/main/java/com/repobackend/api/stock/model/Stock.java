package com.repobackend.api.stock.model;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "stock")
public class Stock {
    @Id
    private String id;
    private String productoId;
    private String almacenId;
    private Integer cantidad = 0;
    private Date actualizadoEn = new Date();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getProductoId() { return productoId; }
    public void setProductoId(String productoId) { this.productoId = productoId; }

    public String getAlmacenId() { return almacenId; }
    public void setAlmacenId(String almacenId) { this.almacenId = almacenId; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

    public Date getActualizadoEn() { return actualizadoEn; }
    public void setActualizadoEn(Date actualizadoEn) { this.actualizadoEn = actualizadoEn; }
}
