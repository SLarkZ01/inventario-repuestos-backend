package com.repobackend.api.producto.service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;

import com.repobackend.api.producto.dto.ProductoRequest;
import com.repobackend.api.producto.dto.ProductoResponse;
import com.repobackend.api.producto.model.Producto;
import com.repobackend.api.producto.repository.ProductoRepository;
import com.repobackend.api.stock.service.StockService;
import com.repobackend.api.auth.service.AuthorizationService;
import com.repobackend.api.cloud.service.CloudinaryService;
import com.repobackend.api.media.MediaSanitizer;

@Service
public class ProductoService {
    private final ProductoRepository productoRepository;
    private final MongoTemplate mongoTemplate;
    private final StockService stockService;
    private final AuthorizationService authorizationService;
    private final CloudinaryService cloudinaryService;

    public ProductoService(ProductoRepository productoRepository, MongoTemplate mongoTemplate, StockService stockService, AuthorizationService authorizationService, CloudinaryService cloudinaryService) {
        this.productoRepository = productoRepository;
        this.mongoTemplate = mongoTemplate;
        this.stockService = stockService;
        this.authorizationService = authorizationService;
        this.cloudinaryService = cloudinaryService;
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    @SuppressWarnings("unchecked")
    public Map<String, Object> crearProducto(Map<String, Object> body) {
        Producto p = new Producto();
        p.setIdString((String) body.get("id"));
        if (p.getIdString() == null || p.getIdString().isBlank()) {
            p.setIdString(new ObjectId().toHexString());
        }
        p.setNombre((String) body.get("nombre"));
        p.setDescripcion((String) body.getOrDefault("descripcion", null));
        Number precioN = (Number) body.getOrDefault("precio", null);
        if (precioN != null) p.setPrecio(precioN.doubleValue());
        Number stockN = (Number) body.getOrDefault("stock", 0);
        p.setStock(stockN == null ? 0 : stockN.intValue());
        p.setCategoriaId((String) body.get("categoriaId"));
        p.setTallerId((String) body.get("tallerId"));
        Number imagen = (Number) body.getOrDefault("imagenRecurso", null);
        if (imagen != null) p.setImagenRecurso(imagen.intValue());
        Object lm = body.get("listaMedios");
        if (lm instanceof List) {
            p.setListaMedios(MediaSanitizer.sanitize((List<java.util.Map<String, Object>>) lm));
        }
        p.setCreadoEn(new Date());
        // set owner from authentication if available
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            p.setOwnerId(auth.getName());
        }
        // Authorization: ensure caller is platform admin or member of the taller with allowed roles
        String caller = (auth == null) ? null : auth.getName();
        if (!authorizationService.isPlatformAdmin(caller)) {
            String tId = p.getTallerId();
            if (tId == null || !authorizationService.isMemberWithAnyRole(caller, tId, java.util.List.of("VENDEDOR","ADMIN"))) {
                return Map.of("error", "Permisos insuficientes para crear producto en este taller");
            }
        }
        Producto saved = productoRepository.save(p);
        return Map.of("producto", saved);
    }

    // DTO-based creation
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public Map<String, Object> crearProducto(ProductoRequest req) {
        Producto p = toEntity(req);
        if (p.getIdString() == null || p.getIdString().isBlank()) {
            p.setIdString(new ObjectId().toHexString());
        }
        p.setCreadoEn(new Date());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            p.setOwnerId(auth.getName());
        }
        // Authorization: caller must be admin or member of the taller
        String caller = (auth == null) ? null : auth.getName();
        if (!authorizationService.isPlatformAdmin(caller)) {
            String tId = p.getTallerId();
            if (tId == null || !authorizationService.isMemberWithAnyRole(caller, tId, java.util.List.of("VENDEDOR","ADMIN"))) {
                return Map.of("error", "Permisos insuficientes para crear producto en este taller");
            }
        }
        Producto saved = productoRepository.save(p);
        return Map.of("producto", toResponse(saved));
    }

    public Producto toEntity(ProductoRequest req) {
        Producto p = new Producto();
        p.setIdString(req.getIdString());
        p.setNombre(req.getNombre());
        p.setDescripcion(req.getDescripcion());
        p.setPrecio(req.getPrecio());
        Integer s = req.getStock();
        p.setStock(s == null ? 0 : s);
        p.setCategoriaId(req.getCategoriaId());
        p.setImagenRecurso(req.getImagenRecurso());
        p.setListaMedios(req.getListaMedios() == null ? null : MediaSanitizer.sanitize(req.getListaMedios()));
        p.setSpecs(req.getSpecs());
        return p;
    }

    public ProductoResponse toResponse(Producto p) {
        ProductoResponse r = new ProductoResponse();
        r.setId(p.getId());
        r.setIdString(p.getIdString());
        r.setNombre(p.getNombre());
        r.setDescripcion(p.getDescripcion());
        r.setPrecio(p.getPrecio());
        r.setStock(p.getStock());
        r.setCategoriaId(p.getCategoriaId());
        r.setImagenRecurso(p.getImagenRecurso());
        r.setListaMedios(p.getListaMedios());
        // copiar specs estructuradas
        r.setSpecs(p.getSpecs());
        // thumbnailUrl proviene de la primera imagen (puede ser url completa o publicId)
        r.setThumbnailUrl(p.getThumbnailUrl());
        // Rellenar stock total y desglose por almacén desde StockService
        try {
            int total = stockService.getTotalStock(p.getId());
            r.setTotalStock(total);
            var rows = stockService.getStockByProducto(p.getId());
            java.util.List<java.util.Map<String, Object>> breakdown = new java.util.ArrayList<>();
            for (var s : rows) {
                breakdown.add(java.util.Map.of("almacenId", s.getAlmacenId(), "cantidad", s.getCantidad()));
            }
            r.setStockByAlmacen(breakdown);
        } catch (Exception ex) {
            // en caso de fallo al obtener stock, dejar campos nulos y no fallar la respuesta
            r.setTotalStock(null);
            r.setStockByAlmacen(null);
        }
        r.setCreadoEn(p.getCreadoEn());
        return r;
    }

    // Listar con paginación simple: page (0-based) y size
    public java.util.Map<String, Object> listar(int page, int size) {
        if (page < 0) page = 0;
        if (size <= 0) size = 20;
        long total = productoRepository.count();
        org.springframework.data.domain.Pageable pg = org.springframework.data.domain.PageRequest.of(page, size);
        org.springframework.data.domain.Page<Producto> ppage = productoRepository.findAll(pg);
        java.util.List<ProductoResponse> items = ppage.getContent().stream().map(this::toResponse).toList();
        return Map.of("productos", items, "total", total, "page", page, "size", size);
    }

    // Listar por categoria con paginación
    public java.util.Map<String, Object> listarPorCategoriaPaginado(String categoriaId, int page, int size) {
        if (page < 0) page = 0;
        if (size <= 0) size = 20;
        org.springframework.data.domain.Pageable pg = org.springframework.data.domain.PageRequest.of(page, size);
        org.springframework.data.domain.Page<Producto> ppage = productoRepository.findByCategoriaId(categoriaId, pg);
        java.util.List<ProductoResponse> items = ppage.getContent().stream().map(this::toResponse).toList();
        return Map.of("productos", items, "total", ppage.getTotalElements(), "page", page, "size", size);
    }

    public Optional<Producto> getById(String id) {
        return productoRepository.findById(id);
    }

    public Producto findByIdString(String idString) {
        return productoRepository.findByIdString(idString);
    }

    public List<Producto> listarPorCategoria(String categoriaId) {
        return productoRepository.findByCategoriaId(categoriaId);
    }

    public List<Producto> buscarPorNombre(String q) {
        return productoRepository.findByNombreContainingIgnoreCase(q == null ? "" : q);
    }

    @PreAuthorize("hasRole('ADMIN') or @authorizationService.canManageProduct(authentication.name, #id)")
    @SuppressWarnings("unchecked")
    public Map<String, Object> actualizarProducto(String id, Map<String, Object> body) {
        Optional<Producto> maybe = productoRepository.findById(id);
        if (maybe.isEmpty()) return Map.of("error", "Producto no encontrado");
        Producto p = maybe.get();
        if (body.containsKey("nombre")) p.setNombre((String) body.get("nombre"));
        if (body.containsKey("descripcion")) p.setDescripcion((String) body.get("descripcion"));
        if (body.containsKey("precio")) {
            Number n = (Number) body.get("precio");
            p.setPrecio(n == null ? null : n.doubleValue());
        }
        if (body.containsKey("stock")) {
            Number n = (Number) body.get("stock");
            p.setStock(n == null ? 0 : n.intValue());
        }
        if (body.containsKey("categoriaId")) p.setCategoriaId((String) body.get("categoriaId"));
        if (body.containsKey("imagenRecurso")) {
            Number n = (Number) body.get("imagenRecurso");
            p.setImagenRecurso(n == null ? null : n.intValue());
        }
        if (body.containsKey("listaMedios")) {
            Object lm = body.get("listaMedios");
            if (lm instanceof List) p.setListaMedios(MediaSanitizer.sanitize((List<java.util.Map<String, Object>>) lm));
        }
        Producto saved = productoRepository.save(p);
        return Map.of("producto", saved);
    }

    // DTO-based update
    @PreAuthorize("hasRole('ADMIN') or @authorizationService.canManageProduct(authentication.name, #id)")
    public Map<String, Object> actualizarProducto(String id, ProductoRequest req) {
        Optional<Producto> maybe = productoRepository.findById(id);
        if (maybe.isEmpty()) return Map.of("error", "Producto no encontrado");
        Producto p = maybe.get();
        if (req.getNombre() != null) p.setNombre(req.getNombre());
        if (req.getDescripcion() != null) p.setDescripcion(req.getDescripcion());
        if (req.getPrecio() != null) p.setPrecio(req.getPrecio());
        if (req.getStock() != null) p.setStock(req.getStock());
        if (req.getCategoriaId() != null) p.setCategoriaId(req.getCategoriaId());
        if (req.getImagenRecurso() != null) p.setImagenRecurso(req.getImagenRecurso());
        if (req.getListaMedios() != null) p.setListaMedios(MediaSanitizer.sanitize(req.getListaMedios()));
        if (req.getSpecs() != null) p.setSpecs(req.getSpecs());
        Producto saved = productoRepository.save(p);
        return Map.of("producto", toResponse(saved));
    }

    public Map<String, Object> ajustarStock(String id, int delta) {
        // Ajuste atómico en la colección de productos usando findAndModify/inc
        // - delta > 0: incrementa (no necesita comprobar stock previo)
        // - delta < 0: decrementa condicionalmente, sólo si stock >= need
        if (delta == 0) {
            Optional<Producto> maybe = productoRepository.findById(id);
            if (maybe.isEmpty()) return Map.of("error", "Producto no encontrado");
            return Map.of("producto", maybe.get());
        }

        if (delta > 0) {
            Query q = Query.query(Criteria.where("_id").is(id));
            Update u = new Update().inc("stock", delta);
            Producto updated = mongoTemplate.findAndModify(q, u, FindAndModifyOptions.options().returnNew(true), Producto.class);
            if (updated == null) return Map.of("error", "Producto no encontrado");
            return Map.of("producto", updated);
        }

        // delta < 0: decrementar sólo si hay stock suficiente
        int need = -delta;
        Query q = Query.query(Criteria.where("_id").is(id).and("stock").gte(need));
        Update u = new Update().inc("stock", delta);
        Producto updated = mongoTemplate.findAndModify(q, u, FindAndModifyOptions.options().returnNew(true), Producto.class);
        if (updated == null) return Map.of("error", "Producto no encontrado o stock insuficiente");
        return Map.of("producto", updated);
    }

    /**
     * Intenta decrementar stock de forma condicional y atómica.
     * Retorna el Producto actualizado si se pudo decrementar, o null si no había suficiente stock.
     */
    public Producto decreaseStockIfAvailable(String productoId, int qty) {
        if (qty <= 0) throw new IllegalArgumentException("qty debe ser > 0");
        Query q = Query.query(Criteria.where("_id").is(productoId).and("stock").gte(qty));
        Update u = new Update().inc("stock", -qty);
        Producto updated = mongoTemplate.findAndModify(q, u, FindAndModifyOptions.options().returnNew(true), Producto.class);
        return updated;
    }

    @PreAuthorize("hasRole('ADMIN') or @authorizationService.canManageProduct(authentication.name, #id)")
    public Map<String, Object> eliminarProducto(String id) {
        Optional<Producto> maybe = productoRepository.findById(id);
        if (maybe.isEmpty()) return Map.of("error", "Producto no encontrado");
        Producto p = maybe.get();
        // Intentar eliminar los recursos en Cloudinary (si existen publicId registrados)
        try {
            if (p.getListaMedios() != null) {
                for (var m : p.getListaMedios()) {
                    Object publicId = m.get("publicId");
                    if (publicId instanceof String) {
                        try {
                            cloudinaryService.destroy((String) publicId);
                        } catch (Exception e) {
                            // No interrumpir la eliminación del documento si falla la limpieza remota
                            // Loguear la excepción para revisión (usar slf4j si disponible)
                            System.err.println("Warning: fallo al eliminar recurso Cloudinary publicId=" + publicId + " -> " + e.getMessage());
                        }
                    }
                }
            }
        } catch (Exception ex) {
            // proteger: cualquier excepción inesperada no debe impedir la eliminación del documento
            System.err.println("Warning: error al intentar limpiar recursos Cloudinary: " + ex.getMessage());
        }
        productoRepository.deleteById(id);
        return Map.of("deleted", true);
    }

    // Listar por nombre con paginación
    public java.util.Map<String, Object> productosPorNombrePaginado(String q, int page, int size) {
        if (page < 0) page = 0;
        if (size <= 0) size = 20;
        org.springframework.data.domain.Pageable pg = org.springframework.data.domain.PageRequest.of(page, size);
        org.springframework.data.domain.Page<Producto> ppage = productoRepository.findByNombreContainingIgnoreCase(q == null ? "" : q, pg);
        java.util.List<ProductoResponse> items = ppage.getContent().stream().map(this::toResponse).toList();
        return Map.of("productos", items, "total", ppage.getTotalElements(), "page", page, "size", size);
    }

    // Obtener varios productos por lista de ids y devolver un mapa id->ProductoResponse
    public java.util.Map<String, ProductoResponse> findResponsesByIds(java.util.List<String> ids) {
        if (ids == null || ids.isEmpty()) return java.util.Map.of();
        java.util.List<Producto> list = productoRepository.findAllById(ids);
        java.util.Map<String, ProductoResponse> map = new java.util.HashMap<>();
        for (Producto p : list) {
            ProductoResponse pr = toResponse(p);
            map.put(p.getId(), pr);
        }
        return map;
    }
}
