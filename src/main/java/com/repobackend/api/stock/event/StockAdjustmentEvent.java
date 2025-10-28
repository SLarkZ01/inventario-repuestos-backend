package com.repobackend.api.stock.event;

import org.springframework.context.ApplicationEvent;

public class StockAdjustmentEvent extends ApplicationEvent {
    public final String tipo; // "entrada" o "salida"
    public final String productoId;
    public final int cantidad;
    public final String realizadoPor; // puede ser null
    public final String referencia;
    public final String notas;

    public StockAdjustmentEvent(Object source, String tipo, String productoId, int cantidad, String realizadoPor, String referencia, String notas) {
        super(source);
        this.tipo = tipo;
        this.productoId = productoId;
        this.cantidad = cantidad;
        this.realizadoPor = realizadoPor;
        this.referencia = referencia;
        this.notas = notas;
    }
}

