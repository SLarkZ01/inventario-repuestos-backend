package com.repobackend.api.model;

import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "facturas")
public class Factura {
    @Id
    private String id; // Mongo ObjectId as hex string

    private String numeroFactura;
    private ClienteEmbebido cliente;
    private String clienteId; // referencia al user.id si aplica
    private List<FacturaItem> items;
    private Double total;
    private ObjectId realizadoPor; // user id stored as ObjectId or null
    private String estado;
    private Date creadoEn = new Date();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNumeroFactura() { return numeroFactura; }
    public void setNumeroFactura(String numeroFactura) { this.numeroFactura = numeroFactura; }

    public ClienteEmbebido getCliente() { return cliente; }
    public void setCliente(ClienteEmbebido cliente) { this.cliente = cliente; }

    public String getClienteId() { return clienteId; }
    public void setClienteId(String clienteId) { this.clienteId = clienteId; }

    public List<FacturaItem> getItems() { return items; }
    public void setItems(List<FacturaItem> items) { this.items = items; }

    public Double getTotal() { return total; }
    public void setTotal(Double total) { this.total = total; }

    public ObjectId getRealizadoPor() { return realizadoPor; }
    public void setRealizadoPor(ObjectId realizadoPor) { this.realizadoPor = realizadoPor; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Date getCreadoEn() { return creadoEn; }
    public void setCreadoEn(Date creadoEn) { this.creadoEn = creadoEn; }
}
