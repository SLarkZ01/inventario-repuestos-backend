package com.repobackend.api.factura.model;

import java.util.Date;
import java.util.List;

import com.repobackend.api.cliente.model.ClienteEmbebido;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "facturas")
public class Factura {
    @Id
    private String id; // Mongo ObjectId as hex string

    private String numeroFactura; // número consecutivo autorizado o interno
    private String prefijo; // prefijo resolución DIAN (ej: "SETT")
    private String resolucionDian; // número resolución DIAN
    private Date fechaResolucion; // fecha resolución DIAN
    private String rangoAutorizado; // ej: "del 1 al 5000"

    private ClienteEmbebido cliente;
    private String clienteId; // referencia al user.id si aplica
    private List<FacturaItem> items;

    // Totales y tributario
    private Double subtotal; // suma de subtotales de items (antes de IVA)
    private Double totalDescuentos; // suma de descuentos aplicados
    private Double baseImponible; // subtotal - descuentos
    private Double totalIva; // suma de IVA de todos los items
    private Double total; // baseImponible + totalIva

    private ObjectId realizadoPor; // user id stored as ObjectId or null
    private String estado; // BORRADOR, EMITIDA, ACEPTADA, RECHAZADA, ANULADA
    private Date creadoEn = new Date();
    private Date emitidaEn; // fecha de emisión oficial

    // Campos para DIAN (futuros)
    private String cufe; // Código Único de Factura Electrónica
    private String qrCode; // código QR en base64 o URL
    private String xmlUrl; // URL del XML oficial
    private String pdfUrl; // URL del PDF oficial (representación gráfica)
    private String dianResponse; // respuesta del OFE/DIAN (JSON o XML)

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNumeroFactura() { return numeroFactura; }
    public void setNumeroFactura(String numeroFactura) { this.numeroFactura = numeroFactura; }

    public String getPrefijo() { return prefijo; }
    public void setPrefijo(String prefijo) { this.prefijo = prefijo; }

    public String getResolucionDian() { return resolucionDian; }
    public void setResolucionDian(String resolucionDian) { this.resolucionDian = resolucionDian; }

    public Date getFechaResolucion() { return fechaResolucion; }
    public void setFechaResolucion(Date fechaResolucion) { this.fechaResolucion = fechaResolucion; }

    public String getRangoAutorizado() { return rangoAutorizado; }
    public void setRangoAutorizado(String rangoAutorizado) { this.rangoAutorizado = rangoAutorizado; }

    public ClienteEmbebido getCliente() { return cliente; }
    public void setCliente(ClienteEmbebido cliente) { this.cliente = cliente; }

    public String getClienteId() { return clienteId; }
    public void setClienteId(String clienteId) { this.clienteId = clienteId; }

    public List<FacturaItem> getItems() { return items; }
    public void setItems(List<FacturaItem> items) { this.items = items; }

    public Double getSubtotal() { return subtotal; }
    public void setSubtotal(Double subtotal) { this.subtotal = subtotal; }

    public Double getTotalDescuentos() { return totalDescuentos; }
    public void setTotalDescuentos(Double totalDescuentos) { this.totalDescuentos = totalDescuentos; }

    public Double getBaseImponible() { return baseImponible; }
    public void setBaseImponible(Double baseImponible) { this.baseImponible = baseImponible; }

    public Double getTotalIva() { return totalIva; }
    public void setTotalIva(Double totalIva) { this.totalIva = totalIva; }

    public Double getTotal() { return total; }
    public void setTotal(Double total) { this.total = total; }

    public ObjectId getRealizadoPor() { return realizadoPor; }
    public void setRealizadoPor(ObjectId realizadoPor) { this.realizadoPor = realizadoPor; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Date getCreadoEn() { return creadoEn; }
    public void setCreadoEn(Date creadoEn) { this.creadoEn = creadoEn; }

    public Date getEmitidaEn() { return emitidaEn; }
    public void setEmitidaEn(Date emitidaEn) { this.emitidaEn = emitidaEn; }

    public String getCufe() { return cufe; }
    public void setCufe(String cufe) { this.cufe = cufe; }

    public String getQrCode() { return qrCode; }
    public void setQrCode(String qrCode) { this.qrCode = qrCode; }

    public String getXmlUrl() { return xmlUrl; }
    public void setXmlUrl(String xmlUrl) { this.xmlUrl = xmlUrl; }

    public String getPdfUrl() { return pdfUrl; }
    public void setPdfUrl(String pdfUrl) { this.pdfUrl = pdfUrl; }

    public String getDianResponse() { return dianResponse; }
    public void setDianResponse(String dianResponse) { this.dianResponse = dianResponse; }
}
