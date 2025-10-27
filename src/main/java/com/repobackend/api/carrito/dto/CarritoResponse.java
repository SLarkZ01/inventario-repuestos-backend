package com.repobackend.api.dto;

import java.util.Date;
import java.util.List;

public class CarritoResponse {
    private String id;
    private String usuarioId;
    private List<CarritoItemResponse> items;
    private String realizadoPor;
    private Date creadoEn;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsuarioId() { return usuarioId; }
    public void setUsuarioId(String usuarioId) { this.usuarioId = usuarioId; }

    public List<CarritoItemResponse> getItems() { return items; }
    public void setItems(List<CarritoItemResponse> items) { this.items = items; }

    public String getRealizadoPor() { return realizadoPor; }
    public void setRealizadoPor(String realizadoPor) { this.realizadoPor = realizadoPor; }

    public Date getCreadoEn() { return creadoEn; }
    public void setCreadoEn(Date creadoEn) { this.creadoEn = creadoEn; }
}
