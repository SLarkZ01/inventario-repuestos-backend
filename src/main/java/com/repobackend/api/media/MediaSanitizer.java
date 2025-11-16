package com.repobackend.api.media;

import java.util.List;
import java.util.Map;

/**
 * Utilidad para sanitizar listas de medios (imagen/video) recibidas desde clientes.
 * Mantiene la misma lógica que la usada antes en ProductoService.
 */
public final class MediaSanitizer {
    private MediaSanitizer() {}

    public static List<java.util.Map<String, Object>> sanitize(List<java.util.Map<String, Object>> raw) {
        if (raw == null) return null;
        List<java.util.Map<String, Object>> out = new java.util.ArrayList<>();
        for (java.util.Map<String, Object> item : raw) {
            if (item == null) continue;
            java.util.Map<String, Object> m = new java.util.HashMap<>();
            Object type = item.get("type");
            if (type instanceof String) m.put("type", ((String) type).trim());
            Object publicId = item.get("publicId");
            if (publicId instanceof String) m.put("publicId", ((String) publicId).trim());
            Object url = item.get("url");
            if (url instanceof String) m.put("url", ((String) url).trim());
            Object secure = item.get("secure_url");
            if (secure instanceof String) m.put("secure_url", ((String) secure).trim());
            Object order = item.get("order");
            if (order instanceof Number) m.put("order", ((Number) order).intValue());
            else if (order instanceof String) {
                try { m.put("order", Integer.parseInt(((String) order).trim())); } catch (Exception e) { /* ignore parse errors */ }
            }
            Object format = item.get("format");
            if (format instanceof String) m.put("format", ((String) format).trim());
            Object width = item.get("width");
            if (width instanceof Number) m.put("width", ((Number) width).intValue());
            Object height = item.get("height");
            if (height instanceof Number) m.put("height", ((Number) height).intValue());
            if (!m.isEmpty()) out.add(m);
        }
        return out;
    }
}

