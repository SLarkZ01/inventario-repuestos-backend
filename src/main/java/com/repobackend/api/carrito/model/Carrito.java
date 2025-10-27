package com.repobackend.api.model;

import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "carritos")
public class Carrito {
    @Id
    private String id; // ObjectId hex string

    private ObjectId usuarioId;
    private List<CarritoItem> items;
    private ObjectId realizadoPor; // puede ser null
    private Date creadoEn = new Date();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public ObjectId getUsuarioId() { return usuarioId; }
    public void setUsuarioId(ObjectId usuarioId) { this.usuarioId = usuarioId; }

    public List<CarritoItem> getItems() { return items; }
    public void setItems(List<CarritoItem> items) { this.items = items; }

    public ObjectId getRealizadoPor() { return realizadoPor; }
    public void setRealizadoPor(ObjectId realizadoPor) { this.realizadoPor = realizadoPor; }

    public Date getCreadoEn() { return creadoEn; }
    public void setCreadoEn(Date creadoEn) { this.creadoEn = creadoEn; }
}
