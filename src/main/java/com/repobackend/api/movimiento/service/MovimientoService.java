package com.repobackend.api.movimiento.service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.repobackend.api.producto.service.ProductoService;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.repobackend.api.movimiento.dto.MovimientoRequest;
import com.repobackend.api.movimiento.dto.MovimientoResponse;
import com.repobackend.api.movimiento.model.Movimiento;
import com.repobackend.api.movimiento.model.MovimientoTipo;
import com.repobackend.api.movimiento.repository.MovimientoRepository;

@Service
public class MovimientoService {
    private static final Logger logger = LoggerFactory.getLogger(MovimientoService.class);

    private final MovimientoRepository movimientoRepository;
    private final ProductoService productoService; // para ajustar stock cuando se cree movimiento

    public MovimientoService(MovimientoRepository movimientoRepository, ProductoService productoService) {
        this.movimientoRepository = movimientoRepository;
        this.productoService = productoService;
    }

    public MovimientoResponse actualizarMovimiento(String id, MovimientoRequest req) {
        var maybe = movimientoRepository.findById(id);
        if (maybe.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Movimiento no encontrado");
        }
        Movimiento old = maybe.get();

        MovimientoTipo oldTipo = MovimientoTipo.fromStringIgnoreCase(old.getTipo());
        MovimientoTipo newTipo = MovimientoTipo.fromStringIgnoreCase(req.tipo);
        if (newTipo == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tipo de movimiento inválido");
        if (oldTipo == null) oldTipo = newTipo; // si el registro antiguo tiene un tipo extraño, asumimos nuevo para cálculo

        // calcular efecto anterior y nuevo usando signo del enum
        int oldCantidad = old.getCantidad() == null ? 0 : old.getCantidad();
        int oldEffect = oldTipo.efectoSigno() * oldCantidad;

        int newCantidad = req.cantidad == null ? 0 : req.cantidad;
        int newEffect = newTipo.efectoSigno() * newCantidad;

        try {
            // si cambia producto, revertir efecto en producto antiguo y aplicar en nuevo
            if (old.getProductoId() == null || !old.getProductoId().equals(req.productoId)) {
                if (old.getProductoId() != null) {
                    productoService.ajustarStock(old.getProductoId(), -oldEffect);
                }
                productoService.ajustarStock(req.productoId, newEffect);
            } else {
                // mismo producto, ajustar la diferencia
                int delta = newEffect - oldEffect;
                if (delta != 0) productoService.ajustarStock(req.productoId, delta);
            }

            // actualizar campos
            old.setTipo(req.tipo);
            old.setProductoId(req.productoId);
            old.setCantidad(req.cantidad);
            old.setReferencia(req.referencia);
            old.setNotas(req.notas);
            if (req.realizadoPor == null) old.setRealizadoPor(null);
            else {
                try { old.setRealizadoPor(new ObjectId(req.realizadoPor)); }
                catch (IllegalArgumentException iae) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "realizadoPor invalido");
                }
            }

            Movimiento saved = movimientoRepository.save(old);
            return toResponse(saved);
        } catch (ResponseStatusException rse) {
            throw rse;
        } catch (Exception ex) {
            logger.error("Error actualizando movimiento", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error actualizando movimiento");
        }
    }

    /**
     * Método antiguo que recibe un Map. Para evitar duplicación delegamos al DTO.
     */
    public Map<String, Object> crearMovimiento(Map<String, Object> body) {
        // Map-based API usada por código interno o listeners; convertimos a DTO y delegamos
        MovimientoRequest req = new MovimientoRequest();
        req.tipo = (String) body.get("tipo");
        req.productoId = (String) body.get("productoId");
        Number n = (Number) body.getOrDefault("cantidad", 0);
        req.cantidad = n == null ? null : n.intValue();
        req.referencia = (String) body.getOrDefault("referencia", null);
        req.notas = (String) body.getOrDefault("notas", null);
        Object rp = body.get("realizadoPor");
        req.realizadoPor = rp == null ? null : rp.toString();

        MovimientoResponse resp = crearMovimiento(req);
        return Map.of("movimiento", resp);
    }

    // Nuevo: crear usando DTO
    public MovimientoResponse crearMovimiento(MovimientoRequest req) {
        logger.info("crearMovimiento DTO req={}", req);
        // validaciones anotadas en DTO con @Valid en controlador
        MovimientoTipo tipo = MovimientoTipo.fromStringIgnoreCase(req.tipo);
        if (tipo == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tipo de movimiento inválido");

        // Si se especifica almacenId, rechazamos aquí y pedimos usar StockService.adjustStock
        if (req.almacenId != null && !req.almacenId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Para ajustes por almacén use /api/stock/adjust (StockService) para asegurar atomicidad y auditoría");
        }

        try {
            Movimiento m = new Movimiento();
            // Normalizamos el tipo a la representación del enum (mayúsculas) para mantener consistencia
            m.setTipo(tipo.name());
            m.setProductoId(req.productoId);
            m.setCantidad(req.cantidad);
            m.setReferencia(req.referencia);
            m.setNotas(req.notas);
            if (req.realizadoPor == null) {
                m.setRealizadoPor(null);
            } else {
                try {
                    m.setRealizadoPor(new ObjectId(req.realizadoPor));
                } catch (IllegalArgumentException iae) {
                    logger.warn("realizadoPor invalido en DTO: {}", req.realizadoPor);
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "realizadoPor invalido");
                }
            }
            m.setCreadoEn(new Date());

            Movimiento saved = movimientoRepository.save(m);

            // ajustar stock acorde al signo del tipo
            int signo = tipo.efectoSigno();
            productoService.ajustarStock(req.productoId, signo * req.cantidad);

            MovimientoResponse resp = toResponse(saved);
            return resp;
        } catch (ResponseStatusException rse) {
            throw rse;
        } catch (Exception ex) {
            logger.error("Error al crear movimiento DTO", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al crear movimiento");
        }
    }

    /**
     * Crea un movimiento en la colección sin tocar el stock (permite que el caller lo haga
     * dentro de una misma transacción). Retorna el movimiento guardado.
     */
    public Movimiento crearMovimientoSinAjuste(String tipo, String productoId, int cantidad, String realizadoPorHex, String referencia, String notas) {
        Movimiento m = new Movimiento();
        m.setTipo(tipo);
        m.setProductoId(productoId);
        m.setCantidad(cantidad);
        m.setReferencia(referencia);
        m.setNotas(notas);
        if (realizadoPorHex == null) m.setRealizadoPor(null);
        else {
            try { m.setRealizadoPor(new ObjectId(realizadoPorHex)); } catch (IllegalArgumentException iae) { m.setRealizadoPor(null); }
        }
        m.setCreadoEn(new Date());
        return movimientoRepository.save(m);
    }

    private MovimientoResponse toResponse(Movimiento m) {
        MovimientoResponse r = new MovimientoResponse();
        r.id = m.getId();
        r.tipo = m.getTipo();
        r.productoId = m.getProductoId();
        r.cantidad = m.getCantidad();
        r.referencia = m.getReferencia();
        r.notas = m.getNotas();
        r.creadoEn = m.getCreadoEn();
        if (m.getRealizadoPor() != null) r.realizadoPor = m.getRealizadoPor().toHexString();
        else r.realizadoPor = null;
        return r;
    }

    public Optional<Movimiento> getById(String id) {
        return movimientoRepository.findById(id);
    }

    public List<Movimiento> listarPorProducto(String productoId) {
        return movimientoRepository.findByProductoId(productoId);
    }

    public List<Movimiento> listarPorTipo(String tipo) {
        return movimientoRepository.findByTipo(tipo);
    }

    public List<Movimiento> listarTodos() {
        return movimientoRepository.findAll();
    }
}
