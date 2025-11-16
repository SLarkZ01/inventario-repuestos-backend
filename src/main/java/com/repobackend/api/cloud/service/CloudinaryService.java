package com.repobackend.api.cloud.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Servicio que encapsula interacciones con Cloudinary.
 * - Lee credenciales desde properties / environment
 * - Provee método para generar firma para uploads firmados
 * - Provee helpers para destruir recursos y construir URLs con transformaciones (mínimo ejemplo)
 *
 * Comentarios en español y siguiendo buenas prácticas: responsabilidades claras y única.
 */
@Service
public class CloudinaryService {
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${cloudinary.cloud_name:}")
    private String cloudName;

    @Value("${cloudinary.api_key:}")
    private String apiKey;

    @Value("${cloudinary.api_secret:}")
    private String apiSecret;

    @Value("${cloudinary.default_folder:}")
    private String defaultFolder;

    public CloudinaryService(@Value("${cloudinary.cloud_name:}") String cloudName,
                              @Value("${cloudinary.api_key:}") String apiKey,
                              @Value("${cloudinary.api_secret:}") String apiSecret) {
        this.cloudName = cloudName;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
    }

    /**
     * Genera un mapa con apiKey, timestamp y signature para un upload firmado.
     * Acepta parámetros opcionales que quieras incluir en la firma (p.ej. folder, public_id)
     */
    public Map<String, Object> generateSignature(Map<String, String> params) {
        long timestamp = Instant.now().getEpochSecond();
        StringBuilder toSign = new StringBuilder();
        // Incluir params ordenados alfabéticamente según recomendación de Cloudinary
        java.util.TreeMap<String, String> sorted = new java.util.TreeMap<>();
        if (params != null) sorted.putAll(params);
        // Añadir timestamp
        sorted.put("timestamp", String.valueOf(timestamp));
        boolean first = true;
        for (Map.Entry<String, String> e : sorted.entrySet()) {
            if (!first) toSign.append("&");
            toSign.append(e.getKey()).append("=").append(e.getValue());
            first = false;
        }
        // signature = sha1(toSign + api_secret)
        String signature = sha1Hex(toSign.toString() + (apiSecret == null ? "" : apiSecret));
        Map<String, Object> resp = new HashMap<>();
        resp.put("apiKey", apiKey);
        resp.put("timestamp", timestamp);
        resp.put("signature", signature);
        resp.put("cloudName", cloudName);
        return resp;
    }

    /**
     * Calcula SHA-1 hex en minúsculas de una cadena utilizando la librería estándar de Java.
     */
    private static String sha1Hex(String input) {
        if (input == null) return null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-1 algorithm not available", e);
        }
    }

    /**
     * Destruye un recurso por su publicId. Retorna el mapa de respuesta de Cloudinary o lanza excepción si hay error.
     */
    public Map<String,Object> destroy(String publicId) throws Exception {
        if (publicId == null || publicId.isBlank()) throw new IllegalArgumentException("publicId es requerido");
        long timestamp = Instant.now().getEpochSecond();
        // params to sign: public_id=<publicId>&timestamp=<timestamp>
        String toSign = "public_id=" + publicId + "&timestamp=" + timestamp;
        String signature = sha1Hex(toSign + (apiSecret == null ? "" : apiSecret));
        // build form body
        String form = "public_id=" + URLEncoder.encode(publicId, StandardCharsets.UTF_8)
                + "&timestamp=" + timestamp
                + "&api_key=" + URLEncoder.encode(apiKey == null ? "" : apiKey, StandardCharsets.UTF_8)
                + "&signature=" + URLEncoder.encode(signature, StandardCharsets.UTF_8);
        String url = String.format("https://api.cloudinary.com/v1_1/%s/image/destroy", cloudName);
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();
        HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
            try {
                Map<String,Object> map = objectMapper.readValue(resp.body(), Map.class);
                return map;
            } catch (Exception ex) {
                return Map.of("status", resp.statusCode(), "body", resp.body());
            }
        }
        throw new RuntimeException("Cloudinary destroy failed: " + resp.statusCode() + " -> " + resp.body());
    }

    /**
     * Construye una URL pública transformada simple. Para personalizar más, expandir.
     */
    public String buildImageUrl(String publicId, Integer width, Integer height, Map<String, String> options) {
        if (publicId == null) return null;
        // Forma simple de construir URL con trasformaciones: w_{width},h_{height},q_auto,f_auto
        StringBuilder t = new StringBuilder();
        if (width != null) t.append("w_").append(width).append(",");
        if (height != null) t.append("h_").append(height).append(",");
        t.append("q_auto,f_auto");
        // Cloudinary base: https://res.cloudinary.com/{cloudName}/image/upload/{transformations}/{publicId}.{ext}
        String ext = "";
        // Si publicId ya incluye extensión, no añadir
        if (publicId.contains(".")) {
            return String.format("https://res.cloudinary.com/%s/image/upload/%s/%s", cloudName, t.toString(), publicId);
        }
        // Si no, dejar ext vacía y el cliente puede usar publicId con su ext
        return String.format("https://res.cloudinary.com/%s/image/upload/%s/%s", cloudName, t.toString(), publicId);
    }

    public String getDefaultFolder() { return defaultFolder; }
}
