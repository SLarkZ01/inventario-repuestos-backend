package com.repobackend.api.movimiento.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class MovimientoRequest {
    @NotBlank
    public String tipo;

    @NotBlank
    public String productoId;

    @Min(1)
    public Integer cantidad;

    public String referencia;
    public String notas;

    // hex string of ObjectId
    @Pattern(regexp = "^[a-fA-F0-9]{24}$")
    public String realizadoPor;
}
