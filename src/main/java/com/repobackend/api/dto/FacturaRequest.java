package com.repobackend.api.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

public class FacturaRequest {
    private String numeroFactura;
    private String clienteId;
    @Valid
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
