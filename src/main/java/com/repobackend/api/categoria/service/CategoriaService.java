package com.repobackend.api.categoria.service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.repobackend.api.auth.service.AuthorizationService;
import com.repobackend.api.categoria.dto.CategoriaRequest;
import com.repobackend.api.categoria.dto.CategoriaResponse;
import com.repobackend.api.categoria.model.Categoria;
import com.repobackend.api.categoria.repository.CategoriaRepository;

@Service
public class CategoriaService {
    private final CategoriaRepository categoriaRepository;
    private final AuthorizationService authorizationService;

    public CategoriaService(CategoriaRepository categoriaRepository, AuthorizationService authorizationService) {
        this.categoriaRepository = categoriaRepository;
        this.authorizationService = authorizationService;
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    @CacheEvict(value = "categoriasGlobales", allEntries = true)
    public Map<String, Object> crearCategoria(Map<String, Object> body) {
        Categoria c = new Categoria();
        // Generate idString if not provided
        String idString = (String) body.get("id");
        if (idString == null || idString.trim().isEmpty()) {
            idString = java.util.UUID.randomUUID().toString();
        }
        c.setIdString(idString);
        c.setNombre((String) body.get("nombre"));
        c.setDescripcion((String) body.getOrDefault("descripcion", null));
        Number icono = (Number) body.getOrDefault("iconoRecurso", null);
        if (icono != null) c.setIconoRecurso(icono.intValue());
        // optional tallerId for local categories
        if (body.containsKey("tallerId")) c.setTallerId((String) body.get("tallerId"));
        if (body.containsKey("mappedGlobalCategoryId")) c.setMappedGlobalCategoryId((String) body.get("mappedGlobalCategoryId"));
        c.setCreadoEn(new Date());
        // Authorization: if local category (has tallerId) require membership; if global (no tallerId) require admin
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String caller = auth == null ? null : auth.getName();
        String tId = c.getTallerId();
        if (tId == null) {
            // Allow platform admin or global vendedor to create global categories
            if (!authorizationService.isPlatformAdmin(caller) && !authorizationService.isGlobalVendedor(caller))
                return Map.of("error", "Solo ADMIN o VENDEDOR global pueden crear categorías globales");
        } else {
            if (!authorizationService.isPlatformAdmin(caller) && !authorizationService.isMemberWithAnyRole(caller, tId, java.util.List.of("ADMIN","VENDEDOR"))) {
                return Map.of("error", "Permisos insuficientes para crear categoría local en este taller");
            }
        }
        Categoria saved = categoriaRepository.save(c);
        return Map.of("categoria", saved);
    }

    // DTO-based creation
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    @CacheEvict(value = "categoriasGlobales", allEntries = true)
    public Map<String, Object> crearCategoria(CategoriaRequest req) {
        Categoria c = toEntity(req);
        // Generate idString if not provided
        if (c.getIdString() == null || c.getIdString().trim().isEmpty()) {
            c.setIdString(java.util.UUID.randomUUID().toString());
        }
        c.setCreadoEn(new Date());
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String caller = auth == null ? null : auth.getName();
        String tId = c.getTallerId();
        if (tId == null) {
            if (!authorizationService.isPlatformAdmin(caller) && !authorizationService.isGlobalVendedor(caller))
                return Map.of("error", "Solo ADMIN o VENDEDOR global pueden crear categorías globales");
        } else {
            if (!authorizationService.isPlatformAdmin(caller) && !authorizationService.isMemberWithAnyRole(caller, tId, java.util.List.of("ADMIN","VENDEDOR"))) {
                return Map.of("error", "Permisos insuficientes para crear categoría local en este taller");
            }
        }
        Categoria saved = categoriaRepository.save(c);
        return Map.of("categoria", toResponse(saved));
    }

    public Categoria toEntity(CategoriaRequest req) {
        Categoria c = new Categoria();
        c.setIdString(req.getIdString());
        c.setNombre(req.getNombre());
        c.setDescripcion(req.getDescripcion());
        c.setIconoRecurso(req.getIconoRecurso());
        c.setTallerId(req.getTallerId());
        c.setMappedGlobalCategoryId(req.getMappedGlobalCategoryId());
        return c;
    }

    public CategoriaResponse toResponse(Categoria c) {
        CategoriaResponse r = new CategoriaResponse();
        r.setId(c.getId());
        r.setIdString(c.getIdString());
        r.setNombre(c.getNombre());
        r.setDescripcion(c.getDescripcion());
        r.setIconoRecurso(c.getIconoRecurso());
        r.setTallerId(c.getTallerId());
        r.setMappedGlobalCategoryId(c.getMappedGlobalCategoryId());
        r.setCreadoEn(c.getCreadoEn());
        return r;
    }

    // Listar categorías globales (tallerId == null)
    @Cacheable("categoriasGlobales")
    public Map<String,Object> listarCategoriasGlobales(int page, int size) {
        if (page < 0) page = 0;
        if (size <= 0) size = 20;
        org.springframework.data.domain.Pageable pg = org.springframework.data.domain.PageRequest.of(page, size);
        org.springframework.data.domain.Page<Categoria> p = categoriaRepository.findByTallerIdIsNull(pg);
        java.util.List<CategoriaResponse> items = p.getContent().stream().map(this::toResponse).toList();
        return Map.of("categorias", items, "total", p.getTotalElements(), "page", page, "size", size);
    }
    
    // Listar TODAS las categorías (globales + talleres)
    public Map<String,Object> listarTodasLasCategorias(int page, int size) {
        if (page < 0) page = 0;
        if (size <= 0) size = 20;
        org.springframework.data.domain.Pageable pg = org.springframework.data.domain.PageRequest.of(page, size);
        org.springframework.data.domain.Page<Categoria> p = categoriaRepository.findAll(pg);
        java.util.List<CategoriaResponse> items = p.getContent().stream().map(this::toResponse).toList();
        return Map.of("categorias", items, "total", p.getTotalElements(), "page", page, "size", size);
    }
    
    // Listar categorías de un taller
    public Map<String,Object> listarCategoriasPorTaller(String tallerId, int page, int size) {
        if (tallerId == null) return Map.of("categorias", java.util.List.of());
        if (page < 0) page = 0;
        if (size <= 0) size = 20;
        org.springframework.data.domain.Pageable pg = org.springframework.data.domain.PageRequest.of(page, size);
        org.springframework.data.domain.Page<Categoria> p = categoriaRepository.findByTallerId(tallerId, pg);
        java.util.List<CategoriaResponse> items = p.getContent().stream().map(this::toResponse).toList();
        return Map.of("categorias", items, "total", p.getTotalElements(), "page", page, "size", size);
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

    public Map<String, Object> buscarPorNombrePaginado(String q, int page, int size) {
        if (page < 0) page = 0;
        if (size <= 0) size = 20;
        org.springframework.data.domain.Pageable pg = org.springframework.data.domain.PageRequest.of(page, size);
        org.springframework.data.domain.Page<Categoria> p = categoriaRepository.findByNombreContainingIgnoreCase(q == null ? "" : q, pg);
        return Map.of("categorias", p.getContent(), "total", p.getTotalElements(), "page", page, "size", size);
    }

    @PreAuthorize("hasRole('ADMIN') or @authorizationService.canManageCategory(authentication.name)")
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
    @PreAuthorize("hasRole('ADMIN') or @authorizationService.canManageCategory(authentication.name)")
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

    @PreAuthorize("hasRole('ADMIN') or @authorizationService.canManageCategory(authentication.name)")
    public Map<String, Object> eliminarCategoria(String id) {
        Optional<Categoria> maybe = categoriaRepository.findById(id);
        if (maybe.isEmpty()) return Map.of("error", "Categoria no encontrada");
        categoriaRepository.deleteById(id);
        return Map.of("deleted", true);
    }
}
