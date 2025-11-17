package com.repobackend.api.configuracion.model;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Configuración global del sistema.
 * Solo debe existir UN documento de este tipo en la BD con clave "GLOBAL".
 */
@Document(collection = "configuracion_global")
public class ConfiguracionGlobal {

    @Id
    private ObjectId id;

    /**
     * Clave única para identificar la configuración global.
     * Siempre debe ser "GLOBAL".
     */
    private String clave = "GLOBAL";

    /**
     * Tasa de IVA por defecto para productos nuevos (en porcentaje).
     * Ejemplo: 19.0 para 19%
     */
    private Double tasaIvaPorDefecto;

    /**
     * Nombre o razón social de la empresa.
     */
    private String nombreEmpresa;

    /**
     * NIT de la empresa (sin dígito de verificación).
     */
    private String nit;

    /**
     * Dígito de verificación del NIT.
     */
    private String digitoVerificacion;

    /**
     * Dirección de la empresa.
     */
    private String direccion;

    /**
     * Teléfono de la empresa.
     */
    private String telefono;

    /**
     * Email de contacto de la empresa.
     */
    private String email;

    /**
     * Ciudad de la empresa.
     */
    private String ciudad;

    /**
     * Departamento de la empresa.
     */
    private String departamento;

    /**
     * Prefijo para numeración de facturas.
     * Ejemplo: "FV" → FV-0001
     */
    private String prefijoFactura;

    /**
     * Número de la resolución de facturación DIAN.
     */
    private String resolucionDian;

    /**
     * Fecha de la resolución DIAN.
     */
    private LocalDateTime fechaResolucionDian;

    /**
     * Rango inicial autorizado para facturación.
     */
    private Long rangoFacturaInicio;

    /**
     * Rango final autorizado para facturación.
     */
    private Long rangoFacturaFin;

    /**
     * Próximo número de factura a asignar.
     */
    private Long proximoNumeroFactura;

    /**
     * Fecha de última modificación.
     */
    private LocalDateTime actualizadoEn;

    // Constructors
    public ConfiguracionGlobal() {
        this.clave = "GLOBAL";
        this.tasaIvaPorDefecto = 19.0; // Valor por defecto Colombia
        this.proximoNumeroFactura = 1L;
        this.actualizadoEn = LocalDateTime.now();
    }

    // Getters y Setters
    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    public Double getTasaIvaPorDefecto() {
        return tasaIvaPorDefecto;
    }

    public void setTasaIvaPorDefecto(Double tasaIvaPorDefecto) {
        this.tasaIvaPorDefecto = tasaIvaPorDefecto;
    }

    public String getNombreEmpresa() {
        return nombreEmpresa;
    }

    public void setNombreEmpresa(String nombreEmpresa) {
        this.nombreEmpresa = nombreEmpresa;
    }

    public String getNit() {
        return nit;
    }

    public void setNit(String nit) {
        this.nit = nit;
    }

    public String getDigitoVerificacion() {
        return digitoVerificacion;
    }

    public void setDigitoVerificacion(String digitoVerificacion) {
        this.digitoVerificacion = digitoVerificacion;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getDepartamento() {
        return departamento;
    }

    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }

    public String getPrefijoFactura() {
        return prefijoFactura;
    }

    public void setPrefijoFactura(String prefijoFactura) {
        this.prefijoFactura = prefijoFactura;
    }

    public String getResolucionDian() {
        return resolucionDian;
    }

    public void setResolucionDian(String resolucionDian) {
        this.resolucionDian = resolucionDian;
    }

    public LocalDateTime getFechaResolucionDian() {
        return fechaResolucionDian;
    }

    public void setFechaResolucionDian(LocalDateTime fechaResolucionDian) {
        this.fechaResolucionDian = fechaResolucionDian;
    }

    public Long getRangoFacturaInicio() {
        return rangoFacturaInicio;
    }

    public void setRangoFacturaInicio(Long rangoFacturaInicio) {
        this.rangoFacturaInicio = rangoFacturaInicio;
    }

    public Long getRangoFacturaFin() {
        return rangoFacturaFin;
    }

    public void setRangoFacturaFin(Long rangoFacturaFin) {
        this.rangoFacturaFin = rangoFacturaFin;
    }

    public Long getProximoNumeroFactura() {
        return proximoNumeroFactura;
    }

    public void setProximoNumeroFactura(Long proximoNumeroFactura) {
        this.proximoNumeroFactura = proximoNumeroFactura;
    }

    public LocalDateTime getActualizadoEn() {
        return actualizadoEn;
    }

    public void setActualizadoEn(LocalDateTime actualizadoEn) {
        this.actualizadoEn = actualizadoEn;
    }
}

