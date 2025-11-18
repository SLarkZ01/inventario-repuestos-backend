package com.repobackend.api.factura.service;

import java.util.*;

import com.repobackend.api.carrito.model.Carrito;
import com.repobackend.api.carrito.service.CarritoService;
import com.repobackend.api.common.service.SequenceGeneratorService;
import com.repobackend.api.factura.dto.FacturaRequest;
import com.repobackend.api.factura.dto.FacturaResponse;
import com.repobackend.api.factura.model.Factura;
import com.repobackend.api.factura.model.FacturaItem;
import com.repobackend.api.factura.repository.FacturaRepository;
import com.repobackend.api.producto.model.Producto;
import com.repobackend.api.producto.service.ProductoService;
import com.repobackend.api.stock.service.StockService;
import com.repobackend.api.cliente.model.ClienteEmbebido;
import com.repobackend.api.auth.model.User;
import com.repobackend.api.auth.repository.UserRepository;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de Facturación ROBUSTO.
 * REGLAS IMPERATIVAS:
 * 1. SIEMPRE descuenta stock al crear factura (no hay opción de omitir)
 * 2. SIEMPRE toma precios e IVA del producto (ignora precios del cliente)
 * 3. SIEMPRE valida stock suficiente ANTES de crear
 * 4. SIEMPRE calcula totales e IVA en servidor
 * 5. SOLO estado EMITIDA descuenta stock; BORRADOR no descuenta
 */
@Service
public class FacturaServiceV2 {
    private static final Logger logger = LoggerFactory.getLogger(FacturaServiceV2.class);

    private final FacturaRepository facturaRepository;
    private final UserRepository userRepository;
    private final SequenceGeneratorService sequenceGeneratorService;
    private final CarritoService carritoService;
    private final ProductoService productoService;
    private final StockService stockService;
    private final FacturaCalculoService calculoService;

    public FacturaServiceV2(
        FacturaRepository facturaRepository,
        UserRepository userRepository,
        SequenceGeneratorService sequenceGeneratorService,
        CarritoService carritoService,
        ProductoService productoService,
        StockService stockService,
        FacturaCalculoService calculoService
    ) {
        this.facturaRepository = facturaRepository;
        this.userRepository = userRepository;
        this.sequenceGeneratorService = sequenceGeneratorService;
        this.carritoService = carritoService;
        this.productoService = productoService;
        this.stockService = stockService;
        this.calculoService = calculoService;
    }

    /**
     * Crea una factura en BORRADOR (sin descontar stock).
     * Útil para cotizaciones o facturas pendientes de aprobación.
     */
    @Transactional
    public FacturaResponse crearBorrador(FacturaRequest req, String realizadoPorHex) {
        Factura factura = construirFacturaDesdeRequest(req, realizadoPorHex);
        factura.setEstado("BORRADOR");
        // No descuenta stock en borrador
        Factura saved = facturaRepository.save(factura);
        logger.info("Factura BORRADOR creada: {}", saved.getId());
        return toResponse(saved);
    }

    /**
     * Crea y EMITE una factura (DESCUENTA STOCK obligatoriamente).
     * Este es el método principal para crear facturas definitivas.
     */
    @Transactional
    public FacturaResponse crearYEmitir(FacturaRequest req, String realizadoPorHex) {
        Factura factura = construirFacturaDesdeRequest(req, realizadoPorHex);

        // VALIDAR Y DESCONTAR STOCK (obligatorio)
        descontarStockFactura(factura, realizadoPorHex);

        // Marcar como emitida
        factura.setEstado("EMITIDA");
        factura.setEmitidaEn(new Date());

        Factura saved = facturaRepository.save(factura);
        logger.info("Factura EMITIDA creada y stock descontado: {}", saved.getId());
        return toResponse(saved);
    }

    /**
     * Emite un borrador existente (descuenta stock y cambia estado).
     */
    @Transactional
    public FacturaResponse emitirBorrador(String facturaId, String realizadoPorHex) {
        Optional<Factura> maybe = facturaRepository.findById(facturaId);
        if (maybe.isEmpty()) throw new IllegalArgumentException("Factura no encontrada");

        Factura factura = maybe.get();
        if (!"BORRADOR".equals(factura.getEstado())) {
            throw new IllegalStateException("Solo se pueden emitir facturas en estado BORRADOR");
        }

        // DESCONTAR STOCK
        descontarStockFactura(factura, realizadoPorHex);

        factura.setEstado("EMITIDA");
        factura.setEmitidaEn(new Date());

        Factura saved = facturaRepository.save(factura);
        logger.info("Factura borrador {} emitida y stock descontado", facturaId);
        return toResponse(saved);
    }

    /**
     * Checkout de carrito: crea factura EMITIDA desde carrito y descuenta stock.
     */
    @Transactional
    public FacturaResponse checkout(String carritoId, String usuarioIdHex) {
        Optional<Carrito> maybeCarrito = carritoService.getById(carritoId);
        if (maybeCarrito.isEmpty()) throw new IllegalArgumentException("Carrito no encontrado");

        Carrito carrito = maybeCarrito.get();
        if (carrito.getItems() == null || carrito.getItems().isEmpty()) {
            throw new IllegalArgumentException("Carrito vacío");
        }

        // Construir factura desde items del carrito
        Factura factura = new Factura();
        long seq = sequenceGeneratorService.generateSequence("factura");
        factura.setNumeroFactura(String.valueOf(seq));
        factura.setClienteId(usuarioIdHex);

        // Intentar llenar datos del cliente
        if (usuarioIdHex != null) {
            Optional<User> maybeUser = userRepository.findById(usuarioIdHex);
            if (maybeUser.isPresent()) {
                factura.setCliente(construirClienteDesdeUser(maybeUser.get()));
            }
        }

        // Construir items desde productos (agrupando cantidades)
        Map<String, Integer> cantidadesPorProducto = new HashMap<>();
        for (var carItem : carrito.getItems()) {
            String pid = carItem.getProductoId();
            int qty = carItem.getCantidad() == null ? 0 : carItem.getCantidad();
            if (qty > 0) cantidadesPorProducto.merge(pid, qty, Integer::sum);
        }

        List<FacturaItem> items = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : cantidadesPorProducto.entrySet()) {
            String productoId = entry.getKey();
            int cantidad = entry.getValue();

            Producto producto = productoService.getById(productoId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + productoId));

            FacturaItem item = calculoService.construirItemDesdeProducto(producto, cantidad, null);
            items.add(item);
        }

        factura.setItems(items);
        calculoService.calcularTotales(factura);

        // DESCONTAR STOCK
        descontarStockFactura(factura, usuarioIdHex);

        factura.setEstado("EMITIDA");
        factura.setEmitidaEn(new Date());
        factura.setCreadoEn(new Date());
        if (usuarioIdHex != null) {
            try {
                factura.setRealizadoPor(new ObjectId(usuarioIdHex));
            } catch (IllegalArgumentException ignored) {}
        }

        Factura saved = facturaRepository.save(factura);

        // Limpiar carrito
        carritoService.clear(carritoId);

        logger.info("Checkout completado - Factura {} emitida desde carrito {}", saved.getId(), carritoId);
        return toResponse(saved);
    }

    /**
     * Anular una factura emitida (NO devuelve stock - requiere ajuste manual o movimiento).
     */
    @Transactional
    public FacturaResponse anular(String facturaId, String motivo) {
        Optional<Factura> maybe = facturaRepository.findById(facturaId);
        if (maybe.isEmpty()) throw new IllegalArgumentException("Factura no encontrada");

        Factura factura = maybe.get();
        if (!"EMITIDA".equals(factura.getEstado())) {
            throw new IllegalStateException("Solo se pueden anular facturas EMITIDAS");
        }

        factura.setEstado("ANULADA");
        // TODO: registrar motivo de anulación en un campo adicional o log

        Factura saved = facturaRepository.save(factura);
        logger.warn("Factura {} ANULADA - Motivo: {}", facturaId, motivo);
        return toResponse(saved);
    }

    /**
     * Eliminar una factura en estado BORRADOR.
     * Solo se pueden eliminar facturas que no han sido emitidas (cotizaciones rechazadas).
     */
    @Transactional
    public void eliminarBorrador(String facturaId) {
        Optional<Factura> maybe = facturaRepository.findById(facturaId);
        if (maybe.isEmpty()) throw new IllegalArgumentException("Factura no encontrada");

        Factura factura = maybe.get();
        if (!"BORRADOR".equals(factura.getEstado())) {
            throw new IllegalStateException("Solo se pueden eliminar facturas en estado BORRADOR. Use /anular para facturas emitidas.");
        }

        facturaRepository.deleteById(facturaId);
        logger.info("Factura BORRADOR {} eliminada (cotización rechazada/descartada)", facturaId);
    }

    public Optional<Factura> getById(String id) {
        return facturaRepository.findById(id);
    }

    public Factura findByNumeroFactura(String numero) {
        return facturaRepository.findByNumeroFactura(numero);
    }

    public List<Factura> listarPorUsuario(String userId) {
        try {
            ObjectId oid = new ObjectId(userId);
            // Buscar facturas donde el usuario es el cliente O quien la realizó
            // Esto permite que:
            // - Next.js muestre facturas creadas por vendedores Android para ese cliente
            // - Android muestre facturas que el vendedor creó
            return facturaRepository.findByClienteIdOrRealizadoPor(userId, oid);
        } catch (IllegalArgumentException iae) {
            return List.of();
        }
    }

    public List<Factura> listarTodas() {
        return facturaRepository.findAll();
    }

    // ========== MÉTODOS PRIVADOS AUXILIARES ==========

    private Factura construirFacturaDesdeRequest(FacturaRequest req, String realizadoPorHex) {
        if (req.getItems() == null || req.getItems().isEmpty()) {
            throw new IllegalArgumentException("La factura debe tener al menos un item");
        }

        Factura factura = new Factura();

        // Número y resolución
        if (req.getNumeroFactura() == null || req.getNumeroFactura().isBlank()) {
            long seq = sequenceGeneratorService.generateSequence("factura");
            factura.setNumeroFactura(String.valueOf(seq));
        } else {
            factura.setNumeroFactura(req.getNumeroFactura());
        }

        // Cliente
        if (req.getCliente() != null) {
            ClienteEmbebido c = new ClienteEmbebido();
            c.setId(req.getCliente().getId());
            c.setUsername(req.getCliente().getUsername());
            c.setEmail(req.getCliente().getEmail());
            c.setNombre(req.getCliente().getNombre());
            c.setApellido(req.getCliente().getApellido());
            c.setFechaCreacion(req.getCliente().getFechaCreacion());
            factura.setCliente(c);
        } else if (req.getClienteId() != null) {
            factura.setClienteId(req.getClienteId());
            Optional<User> maybeUser = userRepository.findById(req.getClienteId());
            if (maybeUser.isPresent()) {
                factura.setCliente(construirClienteDesdeUser(maybeUser.get()));
            }
        }

        // Construir items desde productos (IGNORANDO precios del cliente)
        List<FacturaItem> items = new ArrayList<>();
        for (var reqItem : req.getItems()) {
            String productoId = reqItem.getProductoId();
            int cantidad = reqItem.getCantidad() == null ? 0 : reqItem.getCantidad();

            if (cantidad <= 0) {
                throw new IllegalArgumentException("Cantidad inválida para producto " + productoId);
            }

            Producto producto = productoService.getById(productoId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + productoId));

            // Usar precio del PRODUCTO (no del request)
            FacturaItem item = calculoService.construirItemDesdeProducto(producto, cantidad, null);
            items.add(item);
        }

        factura.setItems(items);
        calculoService.calcularTotales(factura);

        // Validar total si el cliente lo envió
        if (req.getTotal() != null) {
            calculoService.validarTotal(factura, req.getTotal());
        }

        // Realizar por
        if (realizadoPorHex != null) {
            try {
                factura.setRealizadoPor(new ObjectId(realizadoPorHex));
            } catch (IllegalArgumentException ignored) {}
        }

        factura.setCreadoEn(new Date());
        return factura;
    }

    /**
     * DESCUENTA STOCK de forma atómica con fallback inteligente:
     * 1. Si hay stock por almacén → descuenta de almacenes
     * 2. Si NO hay almacenes → descuenta de producto.stock (modo simple)
     * Lanza excepción si no hay stock suficiente.
     */
    private void descontarStockFactura(Factura factura, String realizadoPorHex) {
        // Agrupar cantidades por producto
        Map<String, Integer> cantidadesPorProducto = new HashMap<>();
        for (FacturaItem item : factura.getItems()) {
            cantidadesPorProducto.merge(item.getProductoId(), item.getCantidad(), Integer::sum);
        }

        // Descontar de almacenes o fallback a producto.stock
        for (Map.Entry<String, Integer> entry : cantidadesPorProducto.entrySet()) {
            String productoId = entry.getKey();
            int cantidadTotal = entry.getValue();

            // Obtener stock por almacén
            var stockRows = stockService.getStockByProducto(productoId);

            if (stockRows.isEmpty()) {
                // FALLBACK: No hay almacenes configurados → usar producto.stock directamente
                logger.info("Producto {} sin almacenes configurados, usando producto.stock (modo simple)", productoId);
                var producto = productoService.decreaseStockIfAvailable(productoId, cantidadTotal);
                if (producto == null) {
                    throw new IllegalStateException(
                        String.format("Stock insuficiente para producto %s (se requieren %d unidades)",
                            productoId, cantidadTotal)
                    );
                }
                logger.info("Stock descontado de producto.stock: {} unidades de producto {}", cantidadTotal, productoId);
                continue; // Siguiente producto
            }

            // Descontar de almacenes (modo avanzado)
            int restante = cantidadTotal;
            for (var stockRow : stockRows) {
                if (restante <= 0) break;

                int disponible = stockRow.getCantidad() == null ? 0 : stockRow.getCantidad();
                if (disponible <= 0) continue;

                int tomar = Math.min(disponible, restante);

                // Ajustar stock (sin validar permisos porque es checkout/factura)
                var resultado = stockService.adjustStock(
                    productoId,
                    stockRow.getAlmacenId(),
                    -tomar,
                    realizadoPorHex,
                    false // skipPermissionCheck
                );

                if (resultado.containsKey("error")) {
                    throw new IllegalStateException(
                        "Error al descontar stock: " + resultado.get("error")
                    );
                }

                restante -= tomar;
            }

            if (restante > 0) {
                throw new IllegalStateException(
                    String.format("Stock insuficiente para producto %s (faltan %d unidades)",
                        productoId, restante)
                );
            }
        }

        logger.info("Stock descontado exitosamente para factura con {} items", factura.getItems().size());
    }

    private ClienteEmbebido construirClienteDesdeUser(User user) {
        ClienteEmbebido c = new ClienteEmbebido();
        c.setId(user.getId());
        c.setUsername(user.getUsername());
        c.setEmail(user.getEmail());
        String nombre = (user.getNombre() != null ? user.getNombre() : "");
        String apellido = (user.getApellido() != null ? user.getApellido() : "");
        if ((nombre + " " + apellido).trim().isEmpty()) {
            c.setNombre(user.getUsername());
        } else {
            c.setNombre((nombre + " " + apellido).trim());
        }
        c.setApellido(user.getApellido());
        c.setFechaCreacion(user.getFechaCreacion());
        return c;
    }

    private FacturaResponse toResponse(Factura f) {
        FacturaResponse r = new FacturaResponse();
        r.setId(f.getId());
        r.setNumeroFactura(f.getNumeroFactura());
        r.setPrefijo(f.getPrefijo());
        r.setResolucionDian(f.getResolucionDian());
        r.setFechaResolucion(f.getFechaResolucion());
        r.setRangoAutorizado(f.getRangoAutorizado());
        r.setClienteId(f.getClienteId());

        if (f.getCliente() != null) {
            com.repobackend.api.cliente.dto.ClienteResponse cr = new com.repobackend.api.cliente.dto.ClienteResponse();
            cr.setId(f.getCliente().getId());
            cr.setUsername(f.getCliente().getUsername());
            cr.setEmail(f.getCliente().getEmail());
            cr.setNombre(f.getCliente().getNombre());
            cr.setApellido(f.getCliente().getApellido());
            cr.setFechaCreacion(f.getCliente().getFechaCreacion());
            r.setCliente(cr);
        }

        if (f.getItems() != null) {
            List<com.repobackend.api.factura.dto.FacturaItemResponse> items = new ArrayList<>();
            for (FacturaItem fi : f.getItems()) {
                com.repobackend.api.factura.dto.FacturaItemResponse ir = new com.repobackend.api.factura.dto.FacturaItemResponse();
                ir.setProductoId(fi.getProductoId());
                ir.setNombreProducto(fi.getNombreProducto());
                ir.setCodigoProducto(fi.getCodigoProducto());
                ir.setCantidad(fi.getCantidad());
                ir.setPrecioUnitario(fi.getPrecioUnitario());
                ir.setDescuento(fi.getDescuento());
                ir.setBaseImponible(fi.getBaseImponible());
                ir.setTasaIva(fi.getTasaIva());
                ir.setValorIva(fi.getValorIva());
                ir.setSubtotal(fi.getSubtotal());
                ir.setTotalItem(fi.getTotalItem());
                items.add(ir);
            }
            r.setItems(items);
        }

        r.setSubtotal(f.getSubtotal());
        r.setTotalDescuentos(f.getTotalDescuentos());
        r.setBaseImponible(f.getBaseImponible());
        r.setTotalIva(f.getTotalIva());
        r.setTotal(f.getTotal());
        r.setRealizadoPor(f.getRealizadoPor() == null ? null : f.getRealizadoPor().toHexString());
        r.setEstado(f.getEstado());
        r.setCreadoEn(f.getCreadoEn());
        r.setEmitidaEn(f.getEmitidaEn());
        r.setCufe(f.getCufe());
        r.setQrCode(f.getQrCode());
        r.setXmlUrl(f.getXmlUrl());
        r.setPdfUrl(f.getPdfUrl());

        return r;
    }
}
