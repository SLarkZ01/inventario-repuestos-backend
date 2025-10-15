package com.repobackend.api.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public class CarritoRequest {
    @NotBlank
    private String usuarioId; // hex string of ObjectId

    @Valid
    @NotEmpty
    private List<CarritoItemRequest> items;

    private String realizadoPor; // optional hex string

    public String getUsuarioId() { return usuarioId; }
    public void setUsuarioId(String usuarioId) { this.usuarioId = usuarioId; }

    public List<CarritoItemRequest> getItems() { return items; }
    public void setItems(List<CarritoItemRequest> items) { this.items = items; }

    public String getRealizadoPor() { return realizadoPor; }
    public void setRealizadoPor(String realizadoPor) { this.realizadoPor = realizadoPor; }
}
