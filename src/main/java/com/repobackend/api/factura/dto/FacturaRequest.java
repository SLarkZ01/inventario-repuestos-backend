package com.repobackend.api.factura.dto;

import java.util.List;

import com.repobackend.api.cliente.dto.ClienteRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "FacturaRequest", description = "Solicitud para crear o emitir una factura. Los precios e IVA se calculan en el servidor a partir de los productos.",
        example = "{\n  \"items\": [ { \"productoId\": \"507f1f77bcf86cd799439011\", \"cantidad\": 2 } ],\n  \"cliente\": { \"id\": \"690d34252d7f9613780df590\", \"username\": \"jdoe\", \"email\": \"jdoe@example.com\", \"nombre\": \"Juan\", \"apellido\": \"Pérez\", \"fechaCreacion\": \"2024-10-01T12:34:56.789Z\" }\n}")
public class FacturaRequest {
    private String numeroFactura;
    private String clienteId;
    @Valid
    @Schema(description = "Snapshot opcional del cliente para almacenar en la factura (si se proporciona, se usará tal cual)")
    private ClienteRequest cliente;

    @NotEmpty
    @Valid
    private List<FacturaItemRequest> items;

    private Double total;
    private String realizadoPor; // hex string
    private String estado;

    public String getNumeroFactura() { return numeroFactura; }
    public void setNumeroFactura(String numeroFactura) { this.numeroFactura = numeroFactura; }

    public String getClienteId() { return clienteId; }
    public void setClienteId(String clienteId) { this.clienteId = clienteId; }

    public ClienteRequest getCliente() { return cliente; }
    public void setCliente(ClienteRequest cliente) { this.cliente = cliente; }

    public List<FacturaItemRequest> getItems() { return items; }
    public void setItems(List<FacturaItemRequest> items) { this.items = items; }

    public Double getTotal() { return total; }
    public void setTotal(Double total) { this.total = total; }

    public String getRealizadoPor() { return realizadoPor; }
    public void setRealizadoPor(String realizadoPor) { this.realizadoPor = realizadoPor; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
