package com.repobackend.api.movimiento.model;

/**
 * Enum que representa los tipos de movimiento reconocidos por el sistema.
 * Incluye utilidad para determinar si el movimiento incrementa o decrementa stock.
 */
public enum MovimientoTipo {
    INGRESO,
    EGRESO,
    VENTA,
    DEVOLUCION,
    AJUSTE;

    /**
     * Indica el signo del efecto sobre el stock: +1 para entradas, -1 para salidas.
     */
    public int efectoSigno() {
        switch (this) {
            case INGRESO:
            case DEVOLUCION:
                return +1;
            case EGRESO:
            case VENTA:
            case AJUSTE:
            default:
                return -1;
        }
    }

    public static MovimientoTipo fromStringIgnoreCase(String s) {
        if (s == null) return null;
        try {
            return MovimientoTipo.valueOf(s.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}

