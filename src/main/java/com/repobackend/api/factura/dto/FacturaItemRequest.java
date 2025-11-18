package com.repobackend.api.factura.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "FacturaItemRequest", description = "Item de la factura. Solo se envían productoId y cantidad; el precio e IVA se toman del producto en el servidor.")
public class FacturaItemRequest {
    @NotBlank
    @Schema(description = "ID del producto a facturar", example = "507f1f77bcf86cd799439011")
    private String productoId;

    @NotNull
    @Min(1)
    @Schema(description = "Cantidad a facturar", example = "2")
    private Integer cantidad;

    public String getProductoId() { return productoId; }
    public void setProductoId(String productoId) { this.productoId = productoId; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
}
