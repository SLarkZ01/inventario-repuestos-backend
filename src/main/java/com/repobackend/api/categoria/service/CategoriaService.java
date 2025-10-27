package com.repobackend.api.categoria.service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.repobackend.api.categoria.dto.CategoriaRequest;
import com.repobackend.api.categoria.dto.CategoriaResponse;
import com.repobackend.api.categoria.model.Categoria;
import com.repobackend.api.categoria.repository.CategoriaRepository;

@Service
public class CategoriaService {
    private final CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    public Map<String, Object> crearCategoria(Map<String, Object> body) {
        Categoria c = new Categoria();
        c.setIdString((String) body.get("id"));
        c.setNombre((String) body.get("nombre"));
        c.setDescripcion((String) body.getOrDefault("descripcion", null));
        Number icono = (Number) body.getOrDefault("iconoRecurso", null);
        if (icono != null) c.setIconoRecurso(icono.intValue());
        c.setCreadoEn(new Date());
        Categoria saved = categoriaRepository.save(c);
        return Map.of("categoria", saved);
    }

    // DTO-based creation
    public Map<String, Object> crearCategoria(CategoriaRequest req) {
        Categoria c = toEntity(req);
        c.setCreadoEn(new Date());
        Categoria saved = categoriaRepository.save(c);
        return Map.of("categoria", toResponse(saved));
    }

    public Categoria toEntity(CategoriaRequest req) {
        Categoria c = new Categoria();
        c.setIdString(req.getIdString());
        c.setNombre(req.getNombre());
        c.setDescripcion(req.getDescripcion());
        c.setIconoRecurso(req.getIconoRecurso());
        return c;
    }

    public CategoriaResponse toResponse(Categoria c) {
        CategoriaResponse r = new CategoriaResponse();
        r.setId(c.getId());
        r.setIdString(c.getIdString());
        r.setNombre(c.getNombre());
        r.setDescripcion(c.getDescripcion());
        r.setIconoRecurso(c.getIconoRecurso());
        r.setCreadoEn(c.getCreadoEn());
        return r;
    }

    public Optional<Categoria> getById(String id) {
        return categoriaRepository.findById(id);
    }

    public Categoria findByIdString(String idString) {
        return categoriaRepository.findByIdString(idString);
    }

    public List<Categoria> buscarPorNombre(String q) {
        return categoriaRepository.findByNombreContainingIgnoreCase(q == null ? "" : q);
    }

    public Map<String, Object> actualizarCategoria(String id, Map<String, Object> body) {
        Optional<Categoria> maybe = categoriaRepository.findById(id);
        if (maybe.isEmpty()) return Map.of("error", "Categoria no encontrada");
        Categoria c = maybe.get();
        if (body.containsKey("nombre")) c.setNombre((String) body.get("nombre"));
        if (body.containsKey("descripcion")) c.setDescripcion((String) body.get("descripcion"));
        if (body.containsKey("iconoRecurso")) {
            Number n = (Number) body.get("iconoRecurso");
            c.setIconoRecurso(n == null ? null : n.intValue());
        }
        Categoria saved = categoriaRepository.save(c);
        return Map.of("categoria", saved);
    }

    // DTO-based update
    public Map<String, Object> actualizarCategoria(String id, CategoriaRequest req) {
        Optional<Categoria> maybe = categoriaRepository.findById(id);
        if (maybe.isEmpty()) return Map.of("error", "Categoria no encontrada");
        Categoria c = maybe.get();
        if (req.getNombre() != null) c.setNombre(req.getNombre());
        if (req.getDescripcion() != null) c.setDescripcion(req.getDescripcion());
        if (req.getIconoRecurso() != null) c.setIconoRecurso(req.getIconoRecurso());
        Categoria saved = categoriaRepository.save(c);
        return Map.of("categoria", toResponse(saved));
    }

    public Map<String, Object> eliminarCategoria(String id) {
        Optional<Categoria> maybe = categoriaRepository.findById(id);
        if (maybe.isEmpty()) return Map.of("error", "Categoria no encontrada");
        categoriaRepository.deleteById(id);
        return Map.of("deleted", true);
    }
}
