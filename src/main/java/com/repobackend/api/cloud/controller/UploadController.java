package com.repobackend.api.cloud.controller;

import java.util.Map;

import com.repobackend.api.cloud.service.CloudinaryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controlador para endpoints relacionados con uploads a Cloudinary.
 * - Generación de firma para uploads firmados
 * - (Opcional) endpoint proxy para subir desde el backend
 */
@RestController
@RequestMapping("/api/uploads")
@Tag(name = "Uploads", description = "Operaciones relacionadas con uploads a Cloudinary")
public class UploadController {
    private final CloudinaryService cloudinaryService;

    public UploadController(CloudinaryService cloudinaryService) {
        this.cloudinaryService = cloudinaryService;
    }

    @Operation(summary = "Generar firma para Cloudinary",
        description = "Genera apiKey, timestamp y signature para permitir uploads firmados desde el cliente. El body opcional puede incluir parámetros a firmar como 'folder' o 'public_id'.")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    @PostMapping("/cloudinary-sign")
    public ResponseEntity<?> sign(@RequestBody(required = false) Map<String, String> body) {
        Map<String, Object> sig = cloudinaryService.generateSignature(body);
        return ResponseEntity.ok(sig);
    }

    // Nota: No implementamos proxy upload por defecto para evitar carga en backend.
    // Si se necesita, implementar un endpoint que reciba MultipartFile y use CloudinaryService.cloudinary.uploader().upload(...)
}

