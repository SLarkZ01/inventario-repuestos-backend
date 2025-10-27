package com.repobackend.api.factura.service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.repobackend.api.carrito.service.CarritoService;
import com.repobackend.api.movimiento.service.MovimientoService;
import com.repobackend.api.producto.service.ProductoService;
import com.repobackend.api.common.service.SequenceGeneratorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.repobackend.api.cliente.dto.ClienteResponse;
import com.repobackend.api.factura.dto.FacturaItemResponse;
import com.repobackend.api.factura.dto.FacturaRequest;
import com.repobackend.api.factura.dto.FacturaResponse;
import com.repobackend.api.cliente.model.ClienteEmbebido;
import com.repobackend.api.factura.model.Factura;
import com.repobackend.api.factura.model.FacturaItem;
import com.repobackend.api.auth.model.User;
import com.repobackend.api.factura.repository.FacturaRepository;
import com.repobackend.api.auth.repository.UserRepository;

@Service
public class FacturaService {
    private static final Logger logger = LoggerFactory.getLogger(FacturaService.class);

    private final FacturaRepository facturaRepository;
    private final UserRepository userRepository;
    private final SequenceGeneratorService sequenceGeneratorService;
    private final CarritoService carritoService;
    private final ProductoService productoService;
    private final MovimientoService movimientoService;

    public FacturaService(FacturaRepository facturaRepository, UserRepository userRepository, SequenceGeneratorService sequenceGeneratorService,
                          CarritoService carritoService, ProductoService productoService, MovimientoService movimientoService) {
        this.facturaRepository = facturaRepository;
        this.userRepository = userRepository;
        this.sequenceGeneratorService = sequenceGeneratorService;
        this.carritoService = carritoService;
        this.productoService = productoService;
        this.movimientoService = movimientoService;
    }

    // New typed method using DTO
    @Transactional
    public FacturaResponse crearFactura(FacturaRequest req) {
        // reuse existing Map-based logic by mapping request to entity then saving
        try {
            Factura f = toEntity(req);
            // if cliente or clienteId provided but no numeroFactura, generate it
            if (f.getNumeroFactura() == null || f.getNumeroFactura().isBlank()) {
                long seq = sequenceGeneratorService.generateSequence("factura");
                f.setNumeroFactura(String.valueOf(seq));
            }
            Factura saved = facturaRepository.save(f);
            return toResponse(saved);
        } catch (IllegalArgumentException iae) {
            throw iae;
        } catch (Exception ex) {
            logger.error("Error creando factura DTO: {}", ex.getMessage(), ex);
            throw new RuntimeException("Error al crear factura: " + ex.getMessage());
        }
    }

    /**
     * Checkout completo: toma el carrito del usuario, verifica stock y realiza:
     * - decremento condicional de stock por producto
     * - creación de movimiento tipo 'salida' por cada item
     * - creación de la factura con número secuencial
     * - vaciado del carrito
     * Todo dentro de una transacción MongoDB.
     *
     * @param carritoId id del carrito a facturar
     * @param realizadoPor usuario (hex) que realiza la operación (opcional)
     */
    @Transactional
    public FacturaResponse checkout(String carritoId, String realizadoPor) {
        // 1) obtener carrito
        var maybe = carritoService.getById(carritoId);
        if (maybe.isEmpty()) throw new IllegalArgumentException("Carrito no encontrado");
        var carrito = maybe.get();
        var items = carrito.getItems();
        if (items == null || items.isEmpty()) throw new IllegalArgumentException("Carrito vacío");

        // 2) verificar y decrementar stock por producto (agregando cantidades si hay líneas duplicadas)
        double total = 0.0;
        java.util.List<FacturaItem> facturaItems = new java.util.ArrayList<>();

        // Agrupar cantidades por productoId para manejar líneas duplicadas en el carrito
        java.util.Map<String, Integer> cantidadesPorProducto = new java.util.HashMap<>();
        for (var it : items) {
            String productoId = it.getProductoId();
            int qty = it.getCantidad() == null ? 0 : it.getCantidad();
            if (qty <= 0) throw new IllegalArgumentException("Cantidad inválida en carrito");
            cantidadesPorProducto.merge(productoId, qty, Integer::sum);
        }

        // Ahora procesar cada producto una sola vez
        for (var entry : cantidadesPorProducto.entrySet()) {
            String productoId = entry.getKey();
            int totalQty = entry.getValue() == null ? 0 : entry.getValue();

            // intentar decrementar stock condicionalmente una sola vez con la suma de cantidades
            var updated = productoService.decreaseStockIfAvailable(productoId, totalQty);
            if (updated == null) {
                throw new IllegalStateException("Stock insuficiente para producto: " + productoId);
            }

            // crear movimiento de salida (sin ajustar stock porque ya lo hicimos). Usar referencia 'venta' y notas null
            movimientoService.crearMovimientoSinAjuste("salida", productoId, totalQty, realizadoPor, "venta", null);

            // preparar items de factura
            FacturaItem fi = new FacturaItem();
            fi.setProductoId(productoId);
            fi.setCantidad(totalQty);
            fi.setPrecioUnitario(updated.getPrecio() == null ? 0.0 : updated.getPrecio());
            facturaItems.add(fi);
            total += fi.getCantidad() * fi.getPrecioUnitario();
        }

        // 3) crear factura con número secuencial
        Factura f = new Factura();
        long seq = sequenceGeneratorService.generateSequence("factura");
        f.setNumeroFactura(String.valueOf(seq));
        f.setItems(facturaItems);
        f.setTotal(total);
        if (realizadoPor != null) {
            try { f.setRealizadoPor(new org.bson.types.ObjectId(realizadoPor)); } catch (IllegalArgumentException iae) { f.setRealizadoPor(null); }
        }
        f.setCreadoEn(new java.util.Date());
        Factura saved = facturaRepository.save(f);

        // 4) limpiar carrito
        carritoService.clear(carritoId);

        return toResponse(saved);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> crearFactura(Map<String, Object> body) {
        try {
        Factura f = new Factura();
        f.setNumeroFactura((String) body.get("numeroFactura"));
        // manejar cliente embebido o clienteId (referencia a User)
        Object clienteObj = body.get("cliente");
        if (clienteObj instanceof Map) {
            Map<String, Object> m = (Map<String, Object>) clienteObj;
            ClienteEmbebido c = new ClienteEmbebido();
            c.setNombre((String) m.get("nombre"));
            c.setDocumento((String) m.getOrDefault("documento", null));
            c.setDireccion((String) m.getOrDefault("direccion", null));
            f.setCliente(c);
        }
        Object clienteIdObj = body.get("clienteId");
        if (clienteIdObj != null) {
            String clienteId = clienteIdObj.toString();
            f.setClienteId(clienteId);
            // intentar rellenar snapshot desde users
            java.util.Optional<User> maybeUser = userRepository.findById(clienteId);
            if (maybeUser.isPresent()) {
                User u = maybeUser.get();
                ClienteEmbebido c = new ClienteEmbebido();
                String fullName = (u.getNombre() == null ? "" : u.getNombre()) + (u.getApellido() == null ? "" : " " + u.getApellido());
                c.setNombre(fullName.isBlank() ? u.getUsername() : fullName);
                // no hay campo documento ni direccion en User; dejar documento null y direccion null
                c.setDocumento(u.getEmail()); // mapear email a documento por conveniencia
                f.setCliente(c);
            }
        }
        Object itemsObj = body.get("items");
        if (itemsObj instanceof List) {
            List<Map<String, Object>> raw = (List<Map<String, Object>>) itemsObj;
            List<FacturaItem> items = new java.util.ArrayList<>();
            double totalCalc = 0.0;
            for (Map<String, Object> it : raw) {
                FacturaItem fi = new FacturaItem();
                fi.setProductoId((String) it.get("productoId"));
                Number q = (Number) it.getOrDefault("cantidad", 0);
                fi.setCantidad(q == null ? 0 : q.intValue());
                Number p = (Number) it.getOrDefault("precioUnitario", 0);
                fi.setPrecioUnitario(p == null ? 0.0 : p.doubleValue());
                items.add(fi);
                totalCalc += fi.getCantidad() * fi.getPrecioUnitario();
            }
            f.setItems(items);
            Number totalProvided = (Number) body.getOrDefault("total", null);
            // Always recalculate total on server; if client provided total, validate it
            double calc = totalCalc;
            if (totalProvided != null) {
                double provided = totalProvided.doubleValue();
                if (Math.abs(provided - calc) > 0.01) {
                    return Map.of("error", "total proporcionado no coincide con suma de items: proporcionado=" + provided + " calculado=" + calc);
                }
            }
            f.setTotal(calc);
        }
        Object realizadoPorObj = body.get("realizadoPor");
        if (realizadoPorObj != null) {
            String rp = realizadoPorObj.toString();
            try {
                org.bson.types.ObjectId oid = new org.bson.types.ObjectId(rp);
                f.setRealizadoPor(oid);
            } catch (IllegalArgumentException iae) {
                logger.warn("realizadoPor id inválido: {}", rp);
                return Map.of("error", "realizadoPor inválido: " + rp);
            }
        }
        f.setEstado((String) body.getOrDefault("estado", "CREADA"));
    f.setCreadoEn(new Date());
    // Ensure numeroFactura if present in body is preserved, otherwise generated by caller
        Factura saved = facturaRepository.save(f);
        return Map.of("factura", saved);
        } catch (Exception ex) {
            logger.error("Error creando factura: {}", ex.getMessage(), ex);
            return Map.of("error", "Error al crear factura: " + ex.getMessage());
        }
    }

    public Optional<Factura> getById(String id) {
        return facturaRepository.findById(id);
    }

    public Factura findByNumeroFactura(String numero) {
        return facturaRepository.findByNumeroFactura(numero);
    }

    public List<Factura> listarPorUsuario(String userId) {
        return facturaRepository.findByRealizadoPor(userId);
    }

    // Mapping helpers
    private Factura toEntity(FacturaRequest req) {
        Factura f = new Factura();
        f.setNumeroFactura(req.getNumeroFactura());
        if (req.getCliente() != null) {
            ClienteEmbebido c = new ClienteEmbebido();
            c.setNombre(req.getCliente().getNombre());
            c.setDocumento(req.getCliente().getDocumento());
            c.setDireccion(req.getCliente().getDireccion());
            f.setCliente(c);
        }
        if (req.getClienteId() != null) {
            f.setClienteId(req.getClienteId());
            // try to fill snapshot as before
            Optional<User> maybeUser = userRepository.findById(req.getClienteId());
            if (maybeUser.isPresent()) {
                User u = maybeUser.get();
                ClienteEmbebido c = new ClienteEmbebido();
                String fullName = (u.getNombre() == null ? "" : u.getNombre()) + (u.getApellido() == null ? "" : " " + u.getApellido());
                c.setNombre(fullName.isBlank() ? u.getUsername() : fullName);
                c.setDocumento(u.getEmail());
                f.setCliente(c);
            }
        }
        List<FacturaItem> items = new java.util.ArrayList<>();
        double totalCalc = 0.0;
        if (req.getItems() != null) {
            for (var it : req.getItems()) {
                FacturaItem fi = new FacturaItem();
                fi.setProductoId(it.getProductoId());
                fi.setCantidad(it.getCantidad() == null ? 0 : it.getCantidad());
                fi.setPrecioUnitario(it.getPrecioUnitario() == null ? 0.0 : it.getPrecioUnitario());
                items.add(fi);
                totalCalc += fi.getCantidad() * fi.getPrecioUnitario();
            }
        }
        f.setItems(items);
        // Server-side total calculation: always use calculated total; if client sent a total, validate
        double calc = totalCalc;
        if (req.getTotal() != null) {
            double provided = req.getTotal();
            if (Math.abs(provided - calc) > 0.01) {
                throw new IllegalArgumentException("total proporcionado no coincide con suma de items: proporcionado=" + provided + " calculado=" + calc);
            }
        }
        f.setTotal(calc);
        if (req.getRealizadoPor() != null) {
            try {
                org.bson.types.ObjectId oid = new org.bson.types.ObjectId(req.getRealizadoPor());
                f.setRealizadoPor(oid);
            } catch (IllegalArgumentException iae) {
                throw new IllegalArgumentException("realizadoPor inválido: " + req.getRealizadoPor());
            }
        }
        f.setEstado(req.getEstado() == null ? "CREADA" : req.getEstado());
        f.setCreadoEn(new Date());
        return f;
    }

    private FacturaResponse toResponse(Factura f) {
        FacturaResponse r = new FacturaResponse();
        r.setId(f.getId());
        r.setNumeroFactura(f.getNumeroFactura());
        if (f.getCliente() != null) {
            ClienteResponse cr = new ClienteResponse();
            cr.setNombre(f.getCliente().getNombre());
            cr.setDocumento(f.getCliente().getDocumento());
            cr.setDireccion(f.getCliente().getDireccion());
            r.setCliente(cr);
        }
        r.setClienteId(f.getClienteId());
        java.util.List<FacturaItemResponse> items = new java.util.ArrayList<>();
        double totalCalc = 0.0;
        if (f.getItems() != null) {
            for (FacturaItem fi : f.getItems()) {
                FacturaItemResponse ir = new FacturaItemResponse();
                ir.setProductoId(fi.getProductoId());
                ir.setCantidad(fi.getCantidad());
                ir.setPrecioUnitario(fi.getPrecioUnitario());
                double subtotal = (fi.getCantidad() == null ? 0 : fi.getCantidad()) * (fi.getPrecioUnitario() == null ? 0.0 : fi.getPrecioUnitario());
                ir.setSubtotal(subtotal);
                items.add(ir);
                totalCalc += subtotal;
            }
        }
        r.setItems(items);
        r.setTotal(f.getTotal() == null ? totalCalc : f.getTotal());
        r.setRealizadoPor(f.getRealizadoPor() == null ? null : f.getRealizadoPor().toHexString());
        r.setEstado(f.getEstado());
        r.setCreadoEn(f.getCreadoEn());
        return r;
    }
}
