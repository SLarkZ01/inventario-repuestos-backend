package com.repobackend.api.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.repobackend.api.dto.CarritoItemRequest;
import com.repobackend.api.dto.CarritoItemResponse;
import com.repobackend.api.dto.CarritoRequest;
import com.repobackend.api.dto.CarritoResponse;
import com.repobackend.api.model.Carrito;
import com.repobackend.api.model.CarritoItem;
import com.repobackend.api.repository.CarritoRepository;

@Service
public class CarritoService {
    private static final Logger logger = LoggerFactory.getLogger(CarritoService.class);

    private final CarritoRepository carritoRepository;
    private final ObjectMapper objectMapper;

    public CarritoService(CarritoRepository carritoRepository, ObjectMapper objectMapper) {
        this.carritoRepository = carritoRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Create a new carrito. Expects usuarioId as string in body (required) and optional items list.
     */
    public Map<String, Object> crearCarrito(Map<String, Object> body) {
        // use ObjectMapper to safely convert map to DTO avoiding unchecked casts
        try {
            CarritoRequest req = objectMapper.convertValue(body, CarritoRequest.class);
            Carrito savedEntity = carritoRepository.save(toEntity(req));
            return Map.of("carrito", toResponse(savedEntity));
        } catch (IllegalArgumentException iae) {
            // convertValue throws IllegalArgumentException if mapping fails
            return Map.of("error", "payload inválido: " + iae.getMessage());
        } catch (Exception ex) {
            logger.error("Error creando carrito DTO: {}", ex.getMessage(), ex);
            return Map.of("error", "Error creando carrito: " + ex.getMessage());
        }
    }

    // New typed creation
    public CarritoResponse crearCarrito(CarritoRequest req) {
        Carrito c = toEntity(req);
        Carrito saved = carritoRepository.save(c);
        return toResponse(saved);
    }

    public Optional<Carrito> getById(String id) {
        return carritoRepository.findById(id);
    }

    public List<Carrito> listarPorUsuario(String usuarioId) {
        return carritoRepository.findByUsuarioId(usuarioId);
    }

    public Map<String, Object> addItem(String carritoId, CarritoItemRequest body) {
        Optional<Carrito> maybe = carritoRepository.findById(carritoId);
        if (maybe.isEmpty()) return Map.of("error", "Carrito no encontrado");
        Carrito c = maybe.get();
        String productoId = body.getProductoId();
        if (productoId == null) return Map.of("error", "productoId es requerido");
        int cantidad = body.getCantidad() == null ? 1 : body.getCantidad();
        List<CarritoItem> items = c.getItems();
        if (items == null) items = new ArrayList<>();
        boolean found = false;
        for (CarritoItem it : items) {
            if (it.getProductoId() != null && it.getProductoId().equals(productoId)) {
                Integer cur = it.getCantidad();
                it.setCantidad((cur == null ? 0 : cur) + cantidad);
                found = true;
                break;
            }
        }
        if (!found) {
            CarritoItem it = new CarritoItem();
            it.setProductoId(productoId);
            it.setCantidad(cantidad);
            items.add(it);
        }
        c.setItems(items);
        Carrito saved = carritoRepository.save(c);
        return Map.of("carrito", toResponse(saved));
    }

    public Map<String, Object> removeItem(String carritoId, String productoId) {
        Optional<Carrito> maybe = carritoRepository.findById(carritoId);
        if (maybe.isEmpty()) return Map.of("error", "Carrito no encontrado");
        Carrito c = maybe.get();
        List<CarritoItem> items = c.getItems();
        if (items == null || items.isEmpty()) return Map.of("carrito", c);
        items.removeIf(it -> productoId.equals(it.getProductoId()));
        c.setItems(items);
        Carrito saved = carritoRepository.save(c);
        return Map.of("carrito", toResponse(saved));
    }

    public Map<String, Object> clear(String carritoId) {
        Optional<Carrito> maybe = carritoRepository.findById(carritoId);
        if (maybe.isEmpty()) return Map.of("error", "Carrito no encontrado");
        Carrito c = maybe.get();
        c.setItems(new ArrayList<>());
        Carrito saved = carritoRepository.save(c);
        return Map.of("carrito", toResponse(saved));
    }

    public Map<String, Object> delete(String carritoId) {
        Optional<Carrito> maybe = carritoRepository.findById(carritoId);
        if (maybe.isEmpty()) return Map.of("error", "Carrito no encontrado");
        carritoRepository.deleteById(carritoId);
        return Map.of("deleted", true);
    }

    // Mapping helpers
    public Carrito toEntity(CarritoRequest req) {
        if (req.getUsuarioId() == null) throw new IllegalArgumentException("usuarioId es requerido");
        Carrito c = new Carrito();
        try {
            c.setUsuarioId(new ObjectId(req.getUsuarioId()));
        } catch (IllegalArgumentException iae) {
            throw new IllegalArgumentException("usuarioId inválido: " + req.getUsuarioId());
        }
        List<CarritoItem> items = new ArrayList<>();
        if (req.getItems() != null) {
            for (CarritoItemRequest ir : req.getItems()) {
                CarritoItem it = new CarritoItem();
                it.setProductoId(ir.getProductoId());
                it.setCantidad(ir.getCantidad() == null ? 0 : ir.getCantidad());
                items.add(it);
            }
        }
        c.setItems(items);
        if (req.getRealizadoPor() != null) {
            try {
                c.setRealizadoPor(new ObjectId(req.getRealizadoPor()));
            } catch (IllegalArgumentException iae) {
                throw new IllegalArgumentException("realizadoPor inválido: " + req.getRealizadoPor());
            }
        }
        c.setCreadoEn(new Date());
        return c;
    }

    public CarritoResponse toResponse(Carrito c) {
        CarritoResponse r = new CarritoResponse();
        r.setId(c.getId());
        r.setUsuarioId(c.getUsuarioId() == null ? null : c.getUsuarioId().toHexString());
        List<CarritoItemResponse> items = new ArrayList<>();
        if (c.getItems() != null) {
            for (CarritoItem it : c.getItems()) {
                CarritoItemResponse ir = new CarritoItemResponse();
                ir.setProductoId(it.getProductoId());
                ir.setCantidad(it.getCantidad());
                items.add(ir);
            }
        }
        r.setItems(items);
        r.setRealizadoPor(c.getRealizadoPor() == null ? null : c.getRealizadoPor().toHexString());
        r.setCreadoEn(c.getCreadoEn());
        return r;
    }
}
