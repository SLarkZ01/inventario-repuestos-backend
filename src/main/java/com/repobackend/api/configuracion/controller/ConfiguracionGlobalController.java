package com.repobackend.api.configuracion.controller;

import com.repobackend.api.configuracion.dto.ConfiguracionGlobalRequest;
import com.repobackend.api.configuracion.dto.ConfiguracionGlobalResponse;
import com.repobackend.api.configuracion.service.ConfiguracionGlobalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para gestionar la configuración global del sistema.
 * Solo accesible por usuarios con rol ADMIN.
 */
@RestController
@RequestMapping("/api/configuracion")
@Tag(name = "Configuración Global", description = "Endpoints para gestionar la configuración del sistema (IVA, datos de empresa, resolución DIAN, etc.)")
@SecurityRequirement(name = "bearerAuth")
public class ConfiguracionGlobalController {

    @Autowired
    private ConfiguracionGlobalService service;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Obtener configuración global",
        description = """
            Obtiene la configuración global del sistema, incluyendo:
            - **tasaIvaPorDefecto**: Tasa de IVA por defecto para productos nuevos (%)
            - **Datos de empresa**: nombre, NIT, dirección, teléfono, etc.
            - **Resolución DIAN**: prefijo, número de resolución, rango autorizado
            
            Si no existe configuración, se crea automáticamente con valores por defecto (IVA 19%).
            
            **Uso en Frontend**: El valor de `tasaIvaPorDefecto` se debe usar al crear nuevos productos
            y puede ser modificado desde el panel de configuración por un ADMIN.
            """,
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Configuración obtenida exitosamente",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ConfiguracionGlobalResponse.class),
                    examples = @ExampleObject(value = """
                        {
                          "id": "507f191e810c19729de860ea",
                          "tasaIvaPorDefecto": 19.0,
                          "nombreEmpresa": "Repuestos ABC S.A.S",
                          "nit": "900123456",
                          "digitoVerificacion": "7",
                          "direccion": "Calle 123 #45-67",
                          "telefono": "3001234567",
                          "email": "contacto@repuestos.com",
                          "ciudad": "Bogotá",
                          "departamento": "Cundinamarca",
                          "prefijoFactura": "FV",
                          "resolucionDian": "18764123456789",
                          "rangoFacturaInicio": 1,
                          "rangoFacturaFin": 5000,
                          "proximoNumeroFactura": 1,
                          "actualizadoEn": "2025-01-17T10:30:00"
                        }
                        """)
                )
            ),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No autorizado (requiere rol ADMIN)")
        }
    )
    public ResponseEntity<ConfiguracionGlobalResponse> obtenerConfiguracion() {
        return ResponseEntity.ok(service.obtenerConfiguracion());
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Actualizar configuración global",
        description = """
            Actualiza la configuración global del sistema.
            Solo se actualizan los campos enviados (permite actualización parcial).
            
            **Casos de uso principales**:
            - Cambiar tasa de IVA por defecto cuando cambie la legislación colombiana
            - Configurar datos de la empresa para facturas
            - Configurar resolución DIAN para facturación electrónica
            
            **IMPORTANTE**: El cambio de `tasaIvaPorDefecto` NO afecta productos existentes,
            solo se aplica a productos creados después del cambio.
            """,
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Campos a actualizar (todos opcionales)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ConfiguracionGlobalRequest.class),
                examples = {
                    @ExampleObject(
                        name = "Cambiar IVA",
                        description = "Ejemplo: Cambiar IVA de 19% a 21%",
                        value = "{\"tasaIvaPorDefecto\": 21.0}"
                    ),
                    @ExampleObject(
                        name = "Configuración completa",
                        value = """
                            {
                              "tasaIvaPorDefecto": 19.0,
                              "nombreEmpresa": "Repuestos ABC S.A.S",
                              "nit": "900123456",
                              "digitoVerificacion": "7",
                              "direccion": "Calle 123 #45-67",
                              "telefono": "3001234567",
                              "email": "contacto@repuestos.com",
                              "ciudad": "Bogotá",
                              "departamento": "Cundinamarca",
                              "prefijoFactura": "FV",
                              "resolucionDian": "18764123456789",
                              "rangoFacturaInicio": 1,
                              "rangoFacturaFin": 5000
                            }
                            """
                    )
                }
            )
        ),
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Configuración actualizada exitosamente",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ConfiguracionGlobalResponse.class)
                )
            ),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No autorizado (requiere rol ADMIN)")
        }
    )
    public ResponseEntity<ConfiguracionGlobalResponse> actualizarConfiguracion(
            @RequestBody ConfiguracionGlobalRequest request) {
        return ResponseEntity.ok(service.actualizarConfiguracion(request));
    }

    @GetMapping("/iva-defecto")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    @Operation(
        summary = "Obtener tasa de IVA por defecto",
        description = """
            Obtiene únicamente la tasa de IVA por defecto configurada en el sistema.
            
            **Uso**: Útil para formularios de creación de productos donde se necesita
            pre-cargar el campo de IVA con el valor por defecto.
            
            Retorna 19.0% si no hay configuración.
            """,
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Tasa de IVA obtenida",
                content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(value = "{\"tasaIvaPorDefecto\": 19.0}")
                )
            )
        }
    )
    public ResponseEntity<?> obtenerIvaDefecto() {
        return ResponseEntity.ok(
            new IvaPorDefectoResponse(service.obtenerTasaIvaPorDefecto())
        );
    }

    // Clase interna para respuesta de IVA
    private record IvaPorDefectoResponse(Double tasaIvaPorDefecto) {}
}

