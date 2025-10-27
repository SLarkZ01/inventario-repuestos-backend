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

@Service
public class ProductoService {
    private final ProductoRepository productoRepository;
    private final MongoTemplate mongoTemplate;

    public ProductoService(ProductoRepository productoRepository, MongoTemplate mongoTemplate) {
        this.productoRepository = productoRepository;
        this.mongoTemplate = mongoTemplate;
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
        r.setCreadoEn(p.getCreadoEn());
        return r;
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
}
