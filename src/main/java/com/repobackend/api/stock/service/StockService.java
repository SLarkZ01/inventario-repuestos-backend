package com.repobackend.api.stock.service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import com.repobackend.api.stock.model.Stock;
import com.repobackend.api.stock.repository.StockRepository;
import com.repobackend.api.producto.repository.ProductoRepository;
import com.repobackend.api.producto.model.Producto;
import com.repobackend.api.stock.event.StockAdjustmentEvent;
import com.repobackend.api.taller.model.Almacen;
import com.repobackend.api.taller.service.TallerService;

@Service
public class StockService {
    private final StockRepository stockRepository;
    private final ProductoRepository productoRepository;
    private final MongoTemplate mongoTemplate;
    private final ApplicationEventPublisher eventPublisher;
    private final TallerService tallerService;

    public StockService(StockRepository stockRepository, ProductoRepository productoRepository, MongoTemplate mongoTemplate, ApplicationEventPublisher eventPublisher, TallerService tallerService) {
        this.stockRepository = stockRepository;
        this.productoRepository = productoRepository;
        this.mongoTemplate = mongoTemplate;
        this.eventPublisher = eventPublisher;
        this.tallerService = tallerService;
    }

    // Obtener el desglose de stock por almacén para un producto
    public List<Stock> getStockByProducto(String productoId) {
        return stockRepository.findByProductoId(productoId);
    }

    // Obtener cantidad total sumando todos los almacenes
    public int getTotalStock(String productoId) {
        List<Stock> rows = stockRepository.findByProductoId(productoId);
        return rows.stream().mapToInt(s -> s.getCantidad() == null ? 0 : s.getCantidad()).sum();
    }

    /**
     * Ajusta el stock atómicamente en (productoId, almacenId).
     * - Si delta > 0: incrementa (crea el registro si no existe).
     * - Si delta < 0: decrementa de forma condicional (fallará si no hay suficiente cantidad).
     * Devuelve mapa { stock: Stock, total: int } o { error: msg } en caso de error.
     * Además registra un Movimiento de tipo entrada/salida con quien realizó la acción (realizadoPorUserId).
     */
    public Map<String, Object> adjustStock(String productoId, String almacenId, int delta, String realizadoPorUserId) {
        if (productoId == null || almacenId == null) return Map.of("error", "productoId y almacenId son requeridos");

        // Si la acción viene de un usuario autenticado, validar que tenga permiso en el taller correspondiente
        if (realizadoPorUserId != null) {
            Optional<Almacen> mayAl = tallerService.findAlmacenById(almacenId);
            if (mayAl.isEmpty()) return Map.of("error", "Almacen no encontrado");
            String tallerId = mayAl.get().getTallerId();
            boolean allowed = tallerService.isUserMemberWithAnyRole(realizadoPorUserId, tallerId, java.util.List.of("VENDEDOR", "ADMIN"));
            if (!allowed) return Map.of("error", "Permisos insuficientes para modificar stock en este almacen");
        }

        // delta positivo: upsert increment
        if (delta > 0) {
            Query q = Query.query(Criteria.where("productoId").is(productoId).and("almacenId").is(almacenId));
            Update u = new Update().inc("cantidad", delta).set("actualizadoEn", new Date()).setOnInsert("productoId", productoId).setOnInsert("almacenId", almacenId);
            FindAndModifyOptions opts = FindAndModifyOptions.options().returnNew(true).upsert(true);
            Stock updated = mongoTemplate.findAndModify(q, u, opts, Stock.class);
            int total = getTotalStock(productoId);
            syncProductStock(productoId, total);
            // publicar evento de auditoría: tipo entrada (listener creará el movimiento sin ajustar stock)
            try { eventPublisher.publishEvent(new StockAdjustmentEvent(this, "entrada", productoId, delta, realizadoPorUserId, "ajuste", null)); } catch (Exception ex) { /* no bloquear por fallo de auditoría */ }
            return Map.of("stock", updated, "total", total);
        }

        // delta == 0 -> no-op
        if (delta == 0) {
            Stock s = stockRepository.findByProductoIdAndAlmacenId(productoId, almacenId);
            int total = getTotalStock(productoId);
            return Map.of("stock", s, "total", total);
        }

        // delta < 0: decremento condicional (no crear registro)
        int need = -delta; // cantidad a quitar
        Query q = Query.query(Criteria.where("productoId").is(productoId).and("almacenId").is(almacenId).and("cantidad").gte(need));
        Update u = new Update().inc("cantidad", delta).set("actualizadoEn", new Date());
        FindAndModifyOptions opts = FindAndModifyOptions.options().returnNew(true).upsert(false);
        Stock updated = mongoTemplate.findAndModify(q, u, opts, Stock.class);
        if (updated == null) {
            return Map.of("error", "Stock insuficiente en almacen: " + almacenId);
        }
        int total = getTotalStock(productoId);
        syncProductStock(productoId, total);
        // publicar evento de auditoría: tipo salida
        try { eventPublisher.publishEvent(new StockAdjustmentEvent(this, "salida", productoId, need, realizadoPorUserId, "ajuste", null)); } catch (Exception ex) { /* ignore */ }
        return Map.of("stock", updated, "total", total);
    }

    // Wrapper anterior compatible (sin userId) para llamadas internas; registra null como realizadoPor
    public Map<String, Object> adjustStock(String productoId, String almacenId, int delta) {
        return adjustStock(productoId, almacenId, delta, null);
    }

    // Setear stock absoluto para un almacén (sin movimiento atomicidad específica)
    public Map<String, Object> setStock(String productoId, String almacenId, int cantidad, String realizadoPorUserId) {
        if (productoId == null || almacenId == null) return Map.of("error", "productoId y almacenId son requeridos");

        // Validar permisos si la acción viene de un usuario autenticado
        if (realizadoPorUserId != null) {
            Optional<Almacen> mayAl = tallerService.findAlmacenById(almacenId);
            if (mayAl.isEmpty()) return Map.of("error", "Almacen no encontrado");
            String tallerId = mayAl.get().getTallerId();
            boolean allowed = tallerService.isUserMemberWithAnyRole(realizadoPorUserId, tallerId, java.util.List.of("VENDEDOR", "ADMIN"));
            if (!allowed) return Map.of("error", "Permisos insuficientes para modificar stock en este almacen");
        }

        Query q = Query.query(Criteria.where("productoId").is(productoId).and("almacenId").is(almacenId));
        Update u = new Update().set("cantidad", Math.max(0, cantidad)).set("actualizadoEn", new Date()).setOnInsert("productoId", productoId).setOnInsert("almacenId", almacenId);
        FindAndModifyOptions opts = FindAndModifyOptions.options().returnNew(true).upsert(true);
        Stock updated = mongoTemplate.findAndModify(q, u, opts, Stock.class);
        int total = getTotalStock(productoId);
        syncProductStock(productoId, total);
        // registrar movimiento: calcular diferencia para crear entrada/salida
        try {
            // obtener cantidad anterior: es difícil con findAndModify upsert; read existing via repository
            Stock before = stockRepository.findByProductoIdAndAlmacenId(productoId, almacenId);
            int beforeQty = before == null ? 0 : before.getCantidad();
            int diff = Math.max(0, cantidad) - beforeQty;
            if (diff > 0) eventPublisher.publishEvent(new StockAdjustmentEvent(this, "entrada", productoId, diff, realizadoPorUserId, "setStock", "ajuste absoluto"));
            else if (diff < 0) eventPublisher.publishEvent(new StockAdjustmentEvent(this, "salida", productoId, -diff, realizadoPorUserId, "setStock", "ajuste absoluto"));
        } catch (Exception ex) { /* ignore audit failure */ }
        return Map.of("stock", updated, "total", total);
    }

    public Map<String, Object> setStock(String productoId, String almacenId, int cantidad) {
        return setStock(productoId, almacenId, cantidad, null);
    }

    public Map<String, Object> removeStockRecord(String productoId, String almacenId, String realizadoPorUserId) {
        // Validar permisos si la acción viene de un usuario autenticado
        if (realizadoPorUserId != null) {
            Optional<Almacen> mayAl = tallerService.findAlmacenById(almacenId);
            if (mayAl.isEmpty()) return Map.of("error", "Almacen no encontrado");
            String tallerId = mayAl.get().getTallerId();
            boolean allowed = tallerService.isUserMemberWithAnyRole(realizadoPorUserId, tallerId, java.util.List.of("VENDEDOR", "ADMIN"));
            if (!allowed) return Map.of("error", "Permisos insuficientes para modificar stock en este almacen");
        }

        Stock s = stockRepository.findByProductoIdAndAlmacenId(productoId, almacenId);
        if (s == null) return Map.of("error", "Registro de stock no encontrado");
        int removedQty = s.getCantidad() == null ? 0 : s.getCantidad();
        stockRepository.delete(s);
        int total = getTotalStock(productoId);
        syncProductStock(productoId, total);
        try { eventPublisher.publishEvent(new StockAdjustmentEvent(this, "salida", productoId, removedQty, realizadoPorUserId, "removeRecord", "eliminacion registro")); } catch (Exception ex) { /* ignore */ }
        return Map.of("deleted", true, "total", total);
    }

    public Map<String, Object> removeStockRecord(String productoId, String almacenId) {
        return removeStockRecord(productoId, almacenId, null);
    }

    private void syncProductStock(String productoId, int total) {
        try {
            Optional<Producto> may = productoRepository.findById(productoId);
            if (may.isPresent()) {
                Producto p = may.get();
                p.setStock(total);
                productoRepository.save(p);
            }
        } catch (Exception ex) {
            // noop, no bloquear la operación principal
        }
    }
}
