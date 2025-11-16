package com.repobackend.api.categoria.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.repobackend.api.categoria.dto.CategoriaResponse;
import com.repobackend.api.categoria.service.CategoriaService;

@RestController
@RequestMapping("/api/public")
public class PublicCategoriasController {
    private final CategoriaService categoriaService;

    public PublicCategoriasController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping("/categorias")
    public ResponseEntity<?> listarPublico(@RequestParam(required = false, defaultValue = "0") int page,
                                           @RequestParam(required = false, defaultValue = "20") int size) {
        Map<String, Object> all = categoriaService.listarTodasLasCategorias(page, size);
        List<?> items = (List<?>) all.getOrDefault("categorias", List.of());
        // Transform to simplified DTO: id, nombre, descripcion, imageUrl
        List<Map<String, Object>> simple = items.stream().map(o -> {
            try {
                String id = null;
                String nombre = "";
                String descripcion = "";
                String imageUrl = null;

                if (o instanceof Map) {
                    Map<String, Object> cat = (Map<String, Object>) o;
                    Object idObj = cat.getOrDefault("idString", cat.get("id"));
                    if (idObj != null) id = idObj.toString();
                    Object nombreObj = cat.get("nombre");
                    if (nombreObj != null) nombre = nombreObj.toString();
                    Object descripcionObj = cat.get("descripcion");
                    if (descripcionObj != null) descripcion = descripcionObj.toString();
                    Object lm = cat.get("listaMedios");
                    if (lm instanceof List) {
                        List<?> list = (List<?>) lm;
                        if (!list.isEmpty()) {
                            Object first = list.get(0);
                            if (first instanceof Map) {
                                Map<?,?> m = (Map<?,?>) first;
                                Object url = m.get("url");
                                if (url == null) url = m.get("secure_url");
                                if (url != null) imageUrl = url.toString();
                            }
                        }
                    }
                } else if (o instanceof CategoriaResponse) {
                    CategoriaResponse cat = (CategoriaResponse) o;
                    id = cat.getIdString() != null ? cat.getIdString() : cat.getId();
                    nombre = cat.getNombre() != null ? cat.getNombre() : "";
                    descripcion = cat.getDescripcion() != null ? cat.getDescripcion() : "";
                    List<Map<String,Object>> lm = cat.getListaMedios();
                    if (lm != null && !lm.isEmpty()) {
                        Map<String,Object> first = lm.get(0);
                        Object url = first.get("url");
                        if (url == null) url = first.get("secure_url");
                        if (url != null) imageUrl = url.toString();
                    }
                } else {
                    // fallback: try toString values
                    nombre = o.toString();
                }

                Map<String, Object> map = new HashMap<>();
                map.put("id", id);
                map.put("nombre", nombre);
                map.put("descripcion", descripcion);
                map.put("imageUrl", imageUrl);
                return map;
            } catch (Exception ex) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", null);
                map.put("nombre", "");
                map.put("descripcion", "");
                map.put("imageUrl", null);
                return map;
            }
        }).collect(Collectors.toList());
        Map<String,Object> resp = new HashMap<>();
        resp.put("categorias", simple);
        resp.put("total", all.getOrDefault("total", 0));
        resp.put("page", page);
        resp.put("size", size);
        return ResponseEntity.ok(resp);
    }
}
