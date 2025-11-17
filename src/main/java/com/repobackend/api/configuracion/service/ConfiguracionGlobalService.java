package com.repobackend.api.configuracion.service;

import com.repobackend.api.configuracion.dto.ConfiguracionGlobalRequest;
import com.repobackend.api.configuracion.dto.ConfiguracionGlobalResponse;
import com.repobackend.api.configuracion.model.ConfiguracionGlobal;
import com.repobackend.api.configuracion.repository.ConfiguracionGlobalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Servicio para gestionar la configuración global del sistema.
 * Inicializa automáticamente con valores por defecto si no existe.
 */
@Service
public class ConfiguracionGlobalService {

    private static final String CLAVE_GLOBAL = "GLOBAL";

    @Autowired
    private ConfiguracionGlobalRepository repository;

    /**
     * Obtiene la configuración global.
     * Si no existe, la crea con valores por defecto.
     */
    public ConfiguracionGlobalResponse obtenerConfiguracion() {
        ConfiguracionGlobal config = repository.findByClave(CLAVE_GLOBAL)
                .orElseGet(this::crearConfiguracionPorDefecto);

        return mapearAResponse(config);
    }

    /**
     * Actualiza la configuración global.
     * Solo actualiza los campos que no sean null en el request.
     */
    public ConfiguracionGlobalResponse actualizarConfiguracion(ConfiguracionGlobalRequest request) {
        ConfiguracionGlobal config = repository.findByClave(CLAVE_GLOBAL)
                .orElseGet(this::crearConfiguracionPorDefecto);

        // Actualizar solo los campos no nulos
        if (request.getTasaIvaPorDefecto() != null) {
            config.setTasaIvaPorDefecto(request.getTasaIvaPorDefecto());
        }
        if (request.getNombreEmpresa() != null) {
            config.setNombreEmpresa(request.getNombreEmpresa());
        }
        if (request.getNit() != null) {
            config.setNit(request.getNit());
        }
        if (request.getDigitoVerificacion() != null) {
            config.setDigitoVerificacion(request.getDigitoVerificacion());
        }
        if (request.getDireccion() != null) {
            config.setDireccion(request.getDireccion());
        }
        if (request.getTelefono() != null) {
            config.setTelefono(request.getTelefono());
        }
        if (request.getEmail() != null) {
            config.setEmail(request.getEmail());
        }
        if (request.getCiudad() != null) {
            config.setCiudad(request.getCiudad());
        }
        if (request.getDepartamento() != null) {
            config.setDepartamento(request.getDepartamento());
        }
        if (request.getPrefijoFactura() != null) {
            config.setPrefijoFactura(request.getPrefijoFactura());
        }
        if (request.getResolucionDian() != null) {
            config.setResolucionDian(request.getResolucionDian());
        }
        if (request.getFechaResolucionDian() != null) {
            config.setFechaResolucionDian(request.getFechaResolucionDian());
        }
        if (request.getRangoFacturaInicio() != null) {
            config.setRangoFacturaInicio(request.getRangoFacturaInicio());
        }
        if (request.getRangoFacturaFin() != null) {
            config.setRangoFacturaFin(request.getRangoFacturaFin());
        }

        config.setActualizadoEn(LocalDateTime.now());
        ConfiguracionGlobal guardada = repository.save(config);

        return mapearAResponse(guardada);
    }

    /**
     * Obtiene la tasa de IVA por defecto configurada.
     * Si no existe configuración, retorna 19.0% (estándar Colombia).
     */
    public Double obtenerTasaIvaPorDefecto() {
        return repository.findByClave(CLAVE_GLOBAL)
                .map(ConfiguracionGlobal::getTasaIvaPorDefecto)
                .orElse(19.0);
    }

    /**
     * Crea la configuración por defecto con valores estándar de Colombia.
     */
    private ConfiguracionGlobal crearConfiguracionPorDefecto() {
        ConfiguracionGlobal config = new ConfiguracionGlobal();
        config.setClave(CLAVE_GLOBAL);
        config.setTasaIvaPorDefecto(19.0);
        config.setProximoNumeroFactura(1L);
        config.setActualizadoEn(LocalDateTime.now());

        return repository.save(config);
    }

    /**
     * Mapea el modelo a DTO de respuesta.
     */
    private ConfiguracionGlobalResponse mapearAResponse(ConfiguracionGlobal config) {
        ConfiguracionGlobalResponse response = new ConfiguracionGlobalResponse();

        if (config.getId() != null) {
            response.setId(config.getId().toHexString());
        }
        response.setTasaIvaPorDefecto(config.getTasaIvaPorDefecto());
        response.setNombreEmpresa(config.getNombreEmpresa());
        response.setNit(config.getNit());
        response.setDigitoVerificacion(config.getDigitoVerificacion());
        response.setDireccion(config.getDireccion());
        response.setTelefono(config.getTelefono());
        response.setEmail(config.getEmail());
        response.setCiudad(config.getCiudad());
        response.setDepartamento(config.getDepartamento());
        response.setPrefijoFactura(config.getPrefijoFactura());
        response.setResolucionDian(config.getResolucionDian());
        response.setFechaResolucionDian(config.getFechaResolucionDian());
        response.setRangoFacturaInicio(config.getRangoFacturaInicio());
        response.setRangoFacturaFin(config.getRangoFacturaFin());
        response.setProximoNumeroFactura(config.getProximoNumeroFactura());
        response.setActualizadoEn(config.getActualizadoEn());

        return response;
    }
}

