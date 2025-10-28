package com.repobackend.api.movimiento.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.stereotype.Component;

import com.repobackend.api.movimiento.service.MovimientoService;
import com.repobackend.api.stock.event.StockAdjustmentEvent;

@Component
public class MovimientoEventListener {
    private static final Logger logger = LoggerFactory.getLogger(MovimientoEventListener.class);
    private final MovimientoService movimientoService;

    public MovimientoEventListener(MovimientoService movimientoService) {
        this.movimientoService = movimientoService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onStockAdjustment(StockAdjustmentEvent ev) {
        try {
            logger.debug("Evento de stock recibido: tipo={} productoId={} cantidad={} referencia={}", ev.tipo, ev.productoId, ev.cantidad, ev.referencia);
            // crear movimiento sin ajustar stock adicionalmente (el stock ya fue ajustado por StockService)
            movimientoService.crearMovimientoSinAjuste(ev.tipo, ev.productoId, ev.cantidad, ev.realizadoPor, ev.referencia, ev.notas);
        } catch (Exception ex) {
            // No queremos que una excepción en el listener afecte la operación original
            logger.error("Error al crear movimiento desde evento de stock", ex);
        }
    }
}
