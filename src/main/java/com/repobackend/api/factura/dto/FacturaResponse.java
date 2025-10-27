package com.repobackend.api.factura.dto;

import com.repobackend.api.cliente.dto.ClienteResponse;

import java.util.Date;
import java.util.List;

public class FacturaResponse {
    private String id;
    private String numeroFactura;
    private ClienteResponse cliente;
    private String clienteId;
    private List<FacturaItemResponse> items;
    private Double total;
    private String realizadoPor;
    private String estado;
    private Date creadoEn;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNumeroFactura() { return numeroFactura; }
    public void setNumeroFactura(String numeroFactura) { this.numeroFactura = numeroFactura; }

    public ClienteResponse getCliente() { return cliente; }
    public void setCliente(ClienteResponse cliente) { this.cliente = cliente; }

    public String getClienteId() { return clienteId; }
    public void setClienteId(String clienteId) { this.clienteId = clienteId; }

    public List<FacturaItemResponse> getItems() { return items; }
    public void setItems(List<FacturaItemResponse> items) { this.items = items; }

    public Double getTotal() { return total; }
    public void setTotal(Double total) { this.total = total; }

    public String getRealizadoPor() { return realizadoPor; }
    public void setRealizadoPor(String realizadoPor) { this.realizadoPor = realizadoPor; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Date getCreadoEn() { return creadoEn; }
    public void setCreadoEn(Date creadoEn) { this.creadoEn = creadoEn; }
}
