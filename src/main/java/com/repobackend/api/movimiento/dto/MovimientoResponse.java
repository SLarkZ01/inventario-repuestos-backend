package com.repobackend.api.movimiento.dto;

import java.util.Date;

public class MovimientoResponse {
    public String id;
    public String tipo;
    public String productoId;
    public Integer cantidad;
    public String referencia;
    public String notas;
    public String realizadoPor; // hex string or null
    public Date creadoEn;

    public MovimientoResponse() {}
}
