package com.repobackend.api.carrito.dto;

import java.util.List;

import jakarta.validation.Valid;

/**
 * DTO para crear/actualizar un carrito.
 * - `usuarioId` es opcional: si se omite se crea un carrito anónimo que el cliente puede sincronizar tras login.
 * - `items` es opcional (permitimos crear carrito vacío o con items).
 */
public class CarritoRequest {
    private String usuarioId; // hex string of ObjectId (opcional)

    @Valid
    private List<CarritoItemRequest> items;

    private String realizadoPor; // optional hex string

    public String getUsuarioId() { return usuarioId; }
    public void setUsuarioId(String usuarioId) { this.usuarioId = usuarioId; }

    public List<CarritoItemRequest> getItems() { return items; }
    public void setItems(List<CarritoItemRequest> items) { this.items = items; }

    public String getRealizadoPor() { return realizadoPor; }
    public void setRealizadoPor(String realizadoPor) { this.realizadoPor = realizadoPor; }
}
