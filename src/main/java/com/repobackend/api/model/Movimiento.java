package com.repobackend.api.model;

import java.util.Date;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "movimientos")
public class Movimiento {
    @Id
    private String id; // Mongo ObjectId as hex string

    private String tipo; // "entrada" o "salida"
    private String productoId; // referencia al idString del producto o id Mongo
    private Integer cantidad;
    private String referencia;
    private String notas;
    private ObjectId realizadoPor; // almacena ObjectId o null
    private Date creadoEn = new Date();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getProductoId() { return productoId; }
    public void setProductoId(String productoId) { this.productoId = productoId; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

    public String getReferencia() { return referencia; }
    public void setReferencia(String referencia) { this.referencia = referencia; }

    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }

    public ObjectId getRealizadoPor() { return realizadoPor; }
    public void setRealizadoPor(ObjectId realizadoPor) { this.realizadoPor = realizadoPor; }

    public Date getCreadoEn() { return creadoEn; }
    public void setCreadoEn(Date creadoEn) { this.creadoEn = creadoEn; }
}
