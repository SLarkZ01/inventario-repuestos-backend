package com.repobackend.api.wishlist.model;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "favorites")
public class Favorite {
    @Id
    private String id;
    private String usuarioId;
    private String productoId;
    private Date creadoEn = new Date();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsuarioId() { return usuarioId; }
    public void setUsuarioId(String usuarioId) { this.usuarioId = usuarioId; }

    public String getProductoId() { return productoId; }
    public void setProductoId(String productoId) { this.productoId = productoId; }

    public Date getCreadoEn() { return creadoEn; }
    public void setCreadoEn(Date creadoEn) { this.creadoEn = creadoEn; }
}
