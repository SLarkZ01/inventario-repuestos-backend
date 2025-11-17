package com.repobackend.api.configuracion.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConfiguracionGlobalResponse {

    private String id;
    private Double tasaIvaPorDefecto;
    private String nombreEmpresa;
    private String nit;
    private String digitoVerificacion;
    private String direccion;
    private String telefono;
    private String email;
    private String ciudad;
    private String departamento;
    private String prefijoFactura;
    private String resolucionDian;
    private LocalDateTime fechaResolucionDian;
    private Long rangoFacturaInicio;
    private Long rangoFacturaFin;
    private Long proximoNumeroFactura;
    private LocalDateTime actualizadoEn;

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

