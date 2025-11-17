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
import com.repobackend.api.media.MediaSanitizer;
import com.repobackend.api.cloud.service.CloudinaryService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class CategoriaService {
    private static final Logger logger = LoggerFactory.getLogger(CategoriaService.class);
     private final CategoriaRepository categoriaRepository;
     private final AuthorizationService authorizationService;
     private final CloudinaryService cloudinaryService;

    public CategoriaService(CategoriaRepository categoriaRepository, AuthorizationService authorizationService, CloudinaryService cloudinaryService) {
        this.categoriaRepository = categoriaRepository;
        this.authorizationService = authorizationService;
        this.cloudinaryService = cloudinaryService;
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
        // tallerId ahora obligatorio
        if (body.containsKey("tallerId")) c.setTallerId((String) body.get("tallerId"));
        else return Map.of("error", "tallerId es obligatorio para crear categoría");
        if (body.containsKey("mappedGlobalCategoryId")) c.setMappedGlobalCategoryId((String) body.get("mappedGlobalCategoryId"));
        // listaMedios opcional (aceptar si viene desde cliente) y sanitizar
        if (body.containsKey("listaMedios")) {
            Object lm = body.get("listaMedios");
            if (lm instanceof List) c.setListaMedios(MediaSanitizer.sanitize((List<java.util.Map<String, Object>>) lm));
        }
        c.setCreadoEn(new Date());
        // Authorization: platform admin o miembro del taller con rol ADMIN/VENDEDOR
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String caller = auth == null ? null : auth.getName();
        if (!authorizationService.isPlatformAdmin(caller) && !authorizationService.isMemberWithAnyRole(caller, c.getTallerId(), java.util.List.of("ADMIN","VENDEDOR"))) {
            return Map.of("error", "Permisos insuficientes para crear categoría en este taller");
        }
        Categoria saved = categoriaRepository.save(c);
        return Map.of("categoria", saved);
    }

    // DTO-based creation
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    @CacheEvict(value = "categoriasGlobales", allEntries = true)
    public Map<String, Object> crearCategoria(CategoriaRequest req) {
        Categoria c = toEntity(req);
        // Ahora las categorías siempre deben pertenecer a un taller => validar tallerId
        if (c.getTallerId() == null || c.getTallerId().isBlank()) {
            return Map.of("error", "tallerId es obligatorio para crear una categoría");
        }
        // Generate idString si no se provee
        if (c.getIdString() == null || c.getIdString().trim().isEmpty()) {
            c.setIdString(java.util.UUID.randomUUID().toString());
        }
        c.setCreadoEn(new Date());
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String caller = auth == null ? null : auth.getName();
        // Authorization: platform admin o miembro del taller con rol ADMIN/VENDEDOR
        if (!authorizationService.isPlatformAdmin(caller) && !authorizationService.isMemberWithAnyRole(caller, c.getTallerId(), java.util.List.of("ADMIN","VENDEDOR"))) {
            return Map.of("error", "Permisos insuficientes para crear categoría en este taller");
        }
        // Sanitizar listaMedios si existe
        if (c.getListaMedios() != null) c.setListaMedios(MediaSanitizer.sanitize(c.getListaMedios()));
        Categoria saved = categoriaRepository.save(c);
        return Map.of("categoria", toResponse(saved));
    }

    public Categoria toEntity(CategoriaRequest req) {
        Categoria c = new Categoria();
        c.setIdString(req.getIdString());
        c.setNombre(req.getNombre());
        c.setDescripcion(req.getDescripcion());
        c.setTallerId(req.getTallerId());
        c.setMappedGlobalCategoryId(req.getMappedGlobalCategoryId());
        // copiar y sanitizar lista de medios si se provee
        if (req.getListaMedios() != null) c.setListaMedios(MediaSanitizer.sanitize(req.getListaMedios()));
        return c;
    }

    public CategoriaResponse toResponse(Categoria c) {
        CategoriaResponse r = new CategoriaResponse();
        r.setId(c.getId());
        r.setIdString(c.getIdString());
        r.setNombre(c.getNombre());
        r.setDescripcion(c.getDescripcion());
        r.setTallerId(c.getTallerId());
        r.setMappedGlobalCategoryId(c.getMappedGlobalCategoryId());
        r.setCreadoEn(c.getCreadoEn());
        // exponer lista de medios
        r.setListaMedios(c.getListaMedios());
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

    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public Map<String, Object> actualizarCategoria(String id, Map<String, Object> body) {
        Optional<Categoria> maybe = categoriaRepository.findById(id);
        if (maybe.isEmpty()) return Map.of("error", "Categoria no encontrada");
        Categoria c = maybe.get();
        // autorización: platform admin o miembro del taller
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String caller = auth == null ? null : auth.getName();
        String tId = c.getTallerId();
        if (!authorizationService.isPlatformAdmin(caller) && !authorizationService.isMemberWithAnyRole(caller, tId, java.util.List.of("ADMIN","VENDEDOR"))) {
            return Map.of("error", "Permisos insuficientes para actualizar esta categoría");
        }
        if (body.containsKey("nombre")) c.setNombre((String) body.get("nombre"));
        if (body.containsKey("descripcion")) c.setDescripcion((String) body.get("descripcion"));
        if (body.containsKey("listaMedios")) {
            Object lm = body.get("listaMedios");
            if (lm instanceof List) c.setListaMedios(MediaSanitizer.sanitize((List<java.util.Map<String, Object>>) lm));
        }
        Categoria saved = categoriaRepository.save(c);
        return Map.of("categoria", saved);
    }

    // DTO-based update
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public Map<String, Object> actualizarCategoria(String id, CategoriaRequest req) {
        Optional<Categoria> maybe = categoriaRepository.findById(id);
        if (maybe.isEmpty()) return Map.of("error", "Categoria no encontrada");
        Categoria c = maybe.get();
        // autorización: platform admin o miembro del taller
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String caller = auth == null ? null : auth.getName();
        String tId = c.getTallerId();
        if (!authorizationService.isPlatformAdmin(caller) && !authorizationService.isMemberWithAnyRole(caller, tId, java.util.List.of("ADMIN","VENDEDOR"))) {
            return Map.of("error", "Permisos insuficientes para actualizar esta categoría");
        }
        if (req.getNombre() != null) c.setNombre(req.getNombre());
        if (req.getDescripcion() != null) c.setDescripcion(req.getDescripcion());
        if (req.getListaMedios() != null) c.setListaMedios(MediaSanitizer.sanitize(req.getListaMedios()));
        Categoria saved = categoriaRepository.save(c);
        // invalidar cache de categoriasGlobales por si acaso
        return Map.of("categoria", toResponse(saved));
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public Map<String, Object> eliminarCategoria(String id) {
        Optional<Categoria> maybe = categoriaRepository.findById(id);
        if (maybe.isEmpty()) return Map.of("error", "Categoria no encontrada");
        Categoria c = maybe.get();
        // autorización: platform admin o miembro del taller
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String caller = auth == null ? null : auth.getName();
        if (!authorizationService.isPlatformAdmin(caller) && !authorizationService.isMemberWithAnyRole(caller, c.getTallerId(), java.util.List.of("ADMIN","VENDEDOR"))) {
            return Map.of("error", "Permisos insuficientes para eliminar esta categoría");
        }
        // Intentar eliminar recursos asociados en Cloudinary (si hay listaMedios)
        try {
            if (c.getListaMedios() != null && !c.getListaMedios().isEmpty()) {
                logger.info("Intentando eliminar {} medios de Cloudinary para categoría {}", c.getListaMedios().size(), id);
                for (Map<String, Object> m : c.getListaMedios()) {
                    if (m == null) continue;
                    String publicId = null;
                    Object p = m.get("publicId");
                    if (p instanceof String && !((String)p).isBlank()) {
                        publicId = ((String)p).trim();
                        logger.info("PublicId encontrado directamente: {}", publicId);
                    } else {
                        // intentar extraer publicId desde secure_url o url
                        Object su = m.get("secure_url");
                        Object uo = m.get("url");
                        String u = su != null ? su.toString() : (uo != null ? uo.toString() : null);
                        logger.info("Extrayendo publicId de URL: {}", u);
                        if (u != null && !u.isBlank()) {
                            int pos = u.indexOf("/image/upload/");
                            if (pos >= 0) {
                                String after = u.substring(pos + "/image/upload/".length());
                                // quitar prefijo de versión si existe: v12345/
                                after = after.replaceFirst("^v\\d+/", "");
                                publicId = after;
                            } else {
                                pos = u.indexOf("/upload/");
                                if (pos >= 0) {
                                    String after = u.substring(pos + "/upload/".length());
                                    after = after.replaceFirst("^v\\d+/", "");
                                    publicId = after;
                                }
                            }
                        }
                    }
                    if (publicId != null && !publicId.isBlank()) {
                        logger.info("Intentando destruir recurso Cloudinary con publicId: {}", publicId);
                        try {
                            // Cloudinary a veces necesita el publicId sin extensión
                            String publicIdSinExt = publicId;
                            if (publicId.contains(".")) {
                                publicIdSinExt = publicId.substring(0, publicId.lastIndexOf('.'));
                            }
                            // Intentar primero sin extensión (formato preferido por Cloudinary)
                            try {
                                Map<String,Object> result = cloudinaryService.destroy(publicIdSinExt);
                                logger.info("Recurso Cloudinary '{}' eliminado exitosamente: {}", publicIdSinExt, result);
                            } catch (Exception ex1) {
                                // Si falla, intentar con extensión
                                logger.warn("Fallo al eliminar sin extensión '{}', intentando con extensión: {}", publicIdSinExt, ex1.getMessage());
                                Map<String,Object> result = cloudinaryService.destroy(publicId);
                                logger.info("Recurso Cloudinary '{}' eliminado exitosamente con extensión: {}", publicId, result);
                            }
                        } catch (Exception ex) {
                            logger.error("No se pudo destruir recurso Cloudinary '{}' al eliminar categoría {}: {}", publicId, id, ex.getMessage(), ex);
                        }
                    } else {
                        logger.warn("No se pudo extraer publicId del medio: {}", m);
                    }
                }
            } else {
                logger.info("No hay medios para eliminar en categoría {}", id);
            }
        } catch (Exception ex) {
            // registrar y continuar con eliminación en base de datos
            logger.error("Error al intentar eliminar recursos en Cloudinary para la categoría {}: {}", id, ex.getMessage(), ex);
        }

        categoriaRepository.deleteById(id);
        return Map.of("deleted", true);
    }
}