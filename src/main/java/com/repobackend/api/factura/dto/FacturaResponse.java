package com.repobackend.api.factura.dto;

import com.repobackend.api.cliente.dto.ClienteResponse;

import java.util.Date;
import java.util.List;

public class FacturaResponse {
    private String id;
    private String numeroFactura;
    private String prefijo;
    private String resolucionDian;
    private Date fechaResolucion;
    private String rangoAutorizado;

    private ClienteResponse cliente;
    private String clienteId;
    private List<FacturaItemResponse> items;

    private Double subtotal;
    private Double totalDescuentos;
    private Double baseImponible;
    private Double totalIva;
    private Double total;

    private String realizadoPor;
    private String estado;
    private Date creadoEn;
    private Date emitidaEn;

    private String cufe;
    private String qrCode;
    private String xmlUrl;
    private String pdfUrl;

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

    public ClienteResponse getCliente() { return cliente; }
    public void setCliente(ClienteResponse cliente) { this.cliente = cliente; }

    public String getClienteId() { return clienteId; }
    public void setClienteId(String clienteId) { this.clienteId = clienteId; }

    public List<FacturaItemResponse> getItems() { return items; }
    public void setItems(List<FacturaItemResponse> items) { this.items = items; }

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

    public String getRealizadoPor() { return realizadoPor; }
    public void setRealizadoPor(String realizadoPor) { this.realizadoPor = realizadoPor; }

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
}
