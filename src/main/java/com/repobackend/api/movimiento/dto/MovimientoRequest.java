package com.repobackend.api.movimiento.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * DTO para crear/recibir movimientos desde la API.
 * Comentarios en español y validaciones básicas con Jakarta Validation.
 */
public class MovimientoRequest {
    @NotBlank
    // Acepta: INGRESO, EGRESO, VENTA, DEVOLUCION, AJUSTE (case-insensitive)
    @Pattern(regexp = "(?i)^(INGRESO|EGRESO|VENTA|DEVOLUCION|AJUSTE)$")
    public String tipo;

    @NotBlank
    public String productoId;

    @NotNull
    @Min(1)
    public Integer cantidad;

    public String referencia;
    public String notas;

    // hex string of ObjectId (usuario que realiza la acción)
    @Pattern(regexp = "^[a-fA-F0-9]{24}$")
    public String realizadoPor;

    // Opcional: si se especifica, indica el almacén donde aplicar el movimiento.
    // Se recomienda no usar este endpoint para ajustes por almacén; en su lugar usar /api/stock/adjust
    @Pattern(regexp = "^[a-fA-F0-9]{24}$")
    public String almacenId;
}
