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
import org.springframework.stereotype.Service;

import com.repobackend.api.producto.dto.ProductoRequest;
import com.repobackend.api.producto.dto.ProductoResponse;
import com.repobackend.api.producto.model.Producto;
import com.repobackend.api.producto.repository.ProductoRepository;
import com.repobackend.api.stock.service.StockService;

@Service
public class ProductoService {
    private final ProductoRepository productoRepository;
    private final MongoTemplate mongoTemplate;
    private final StockService stockService;

    public ProductoService(ProductoRepository productoRepository, MongoTemplate mongoTemplate, StockService stockService) {
        this.productoRepository = productoRepository;
        this.mongoTemplate = mongoTemplate;
        this.stockService = stockService;
    }

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
        Number imagen = (Number) body.getOrDefault("imagenRecurso", null);
        if (imagen != null) p.setImagenRecurso(imagen.intValue());
        Object lm = body.get("listaMedios");
        if (lm instanceof List) p.setListaMedios((List<java.util.Map<String, Object>>) lm);
        p.setCreadoEn(new Date());
        Producto saved = productoRepository.save(p);
        return Map.of("producto", saved);
    }

    // DTO-based creation
    public Map<String, Object> crearProducto(ProductoRequest req) {
        Producto p = toEntity(req);
        if (p.getIdString() == null || p.getIdString().isBlank()) {
            p.setIdString(new ObjectId().toHexString());
        }
        p.setCreadoEn(new Date());
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
        p.setListaMedios(req.getListaMedios());
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
            if (lm instanceof List) p.setListaMedios((List<java.util.Map<String, Object>>) lm);
        }
        Producto saved = productoRepository.save(p);
        return Map.of("producto", saved);
    }

    // DTO-based update
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
        if (req.getListaMedios() != null) p.setListaMedios(req.getListaMedios());
        if (req.getSpecs() != null) p.setSpecs(req.getSpecs());
        Producto saved = productoRepository.save(p);
        return Map.of("producto", toResponse(saved));
    }

    public Map<String, Object> ajustarStock(String id, int delta) {
        Optional<Producto> maybe = productoRepository.findById(id);
        if (maybe.isEmpty()) return Map.of("error", "Producto no encontrado");
        Producto p = maybe.get();
    Integer currentObj = p.getStock();
    int current = currentObj == null ? 0 : currentObj;
        p.setStock(Math.max(0, current + delta));
        Producto saved = productoRepository.save(p);
        return Map.of("producto", saved);
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

    public Map<String, Object> eliminarProducto(String id) {
        Optional<Producto> maybe = productoRepository.findById(id);
        if (maybe.isEmpty()) return Map.of("error", "Producto no encontrado");
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
