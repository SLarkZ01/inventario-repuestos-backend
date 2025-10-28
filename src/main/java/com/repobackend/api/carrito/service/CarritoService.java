package com.repobackend.api.carrito.service;

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
import com.repobackend.api.carrito.dto.CarritoItemRequest;
import com.repobackend.api.carrito.dto.CarritoItemResponse;
import com.repobackend.api.carrito.dto.CarritoRequest;
import com.repobackend.api.carrito.dto.CarritoResponse;
import com.repobackend.api.carrito.model.Carrito;
import com.repobackend.api.carrito.model.CarritoItem;
import com.repobackend.api.carrito.repository.CarritoRepository;

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
     * Create a new carrito. Accepta usuarioId opcional para carritos anónimos.
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
        if (items == null || items.isEmpty()) return Map.of("carrito", toResponse(c));
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

    // Nuevo: merge de carrito anónimo al carrito del usuario autenticado.
    // Comportamiento:
    // - Si existe un carrito anónimo con id `anonCartId` y el usuario ya tiene un carrito, se suman cantidades y se elimina el carrito anónimo.
    // - Si existe un carrito anónimo y el usuario no tiene carrito, se asigna el usuarioId al carrito anónimo (se reutiliza).
    // - Si no se envía `anonCartId` pero se envían `items`, se mergean esos items en el carrito del usuario (crear si no existe).
    public Map<String, Object> mergeAnonymousCartIntoUser(String anonCartId, List<CarritoItemRequest> items, String userId) {
        if (userId == null) return Map.of("error", "Usuario no autenticado");
        try {
            // 1) buscar carrito del usuario existente (tomamos el primer carrito si hay varios)
            List<Carrito> userCarts = carritoRepository.findByUsuarioId(userId);
            Carrito target = null;
            if (!userCarts.isEmpty()) target = userCarts.get(0);

            // 2) si se proporcionó anonCartId intentar usarlo
            if (anonCartId != null && !anonCartId.isBlank()) {
                Optional<Carrito> maybeAnon = carritoRepository.findById(anonCartId);
                if (maybeAnon.isEmpty()) return Map.of("error", "Carrito anónimo no encontrado");
                Carrito anon = maybeAnon.get();
                // Si el carrito ya pertenece a un usuario y no es anónimo, evitar reasignarlo
                if (anon.getUsuarioId() != null) {
                    // si pertenece al mismo usuario, devolverlo
                    if (anon.getUsuarioId().toHexString().equals(userId)) return Map.of("carrito", toResponse(anon));
                    // si pertenece a otro usuario, evitar merge
                    return Map.of("error", "El carrito proporcionado no es anónimo");
                }
                if (target != null) {
                    // merge anon -> target y eliminar anon
                    List<CarritoItem> merged = mergeItems(target.getItems(), anon.getItems());
                    target.setItems(merged);
                    carritoRepository.save(target);
                    carritoRepository.deleteById(anon.getId());
                    return Map.of("carrito", toResponse(target));
                } else {
                    // asignar usuario al carrito anónimo y devolverlo
                    anon.setUsuarioId(new ObjectId(userId));
                    Carrito saved = carritoRepository.save(anon);
                    return Map.of("carrito", toResponse(saved));
                }
            }

            // 3) si no hay anonCartId pero sí items: merge crear/actualizar target
            if (items != null && !items.isEmpty()) {
                if (target != null) {
                    List<CarritoItem> merged = mergeItems(target.getItems(), toCarritoItemList(items));
                    target.setItems(merged);
                    carritoRepository.save(target);
                    return Map.of("carrito", toResponse(target));
                } else {
                    // crear nuevo carrito para usuario
                    CarritoRequest req = new CarritoRequest();
                    req.setUsuarioId(userId);
                    req.setItems(items);
                    Carrito c = toEntity(req);
                    Carrito saved = carritoRepository.save(c);
                    return Map.of("carrito", toResponse(saved));
                }
            }

            // 4) nada para merge; si existe target devolverlo, si no crear carrito vacío para usuario
            if (target != null) return Map.of("carrito", toResponse(target));
            CarritoRequest req = new CarritoRequest();
            req.setUsuarioId(userId);
            Carrito c = toEntity(req);
            Carrito saved = carritoRepository.save(c);
            return Map.of("carrito", toResponse(saved));
        } catch (Exception ex) {
            logger.error("Error al mergear carrito: {}", ex.getMessage(), ex);
            return Map.of("error", "No se pudo sincronizar el carrito: " + ex.getMessage());
        }
    }

    // Helper: mergear dos listas de CarritoItem (sumando cantidades)
    private List<CarritoItem> mergeItems(List<CarritoItem> base, List<CarritoItem> extra) {
        java.util.Map<String, CarritoItem> map = new java.util.HashMap<>();
        if (base != null) {
            for (CarritoItem it : base) {
                map.put(it.getProductoId(), newItemCopy(it));
            }
        }
        if (extra != null) {
            for (CarritoItem it : extra) {
                if (it == null || it.getProductoId() == null) continue;
                CarritoItem cur = map.get(it.getProductoId());
                if (cur == null) map.put(it.getProductoId(), newItemCopy(it));
                else {
                    Integer curQty = cur.getCantidad() == null ? 0 : cur.getCantidad();
                    Integer addQty = it.getCantidad() == null ? 0 : it.getCantidad();
                    cur.setCantidad(curQty + addQty);
                }
            }
        }
        return new ArrayList<>(map.values());
    }

    private List<CarritoItem> toCarritoItemList(List<CarritoItemRequest> items) {
        List<CarritoItem> out = new ArrayList<>();
        if (items == null) return out;
        for (CarritoItemRequest r : items) {
            CarritoItem it = new CarritoItem();
            it.setProductoId(r.getProductoId());
            it.setCantidad(r.getCantidad() == null ? 0 : r.getCantidad());
            out.add(it);
        }
        return out;
    }

    private CarritoItem newItemCopy(CarritoItem it) {
        CarritoItem c = new CarritoItem();
        c.setProductoId(it.getProductoId());
        c.setCantidad(it.getCantidad() == null ? 0 : it.getCantidad());
        return c;
    }

    // Mapping helpers
    public Carrito toEntity(CarritoRequest req) {
        Carrito c = new Carrito();
        if (req.getUsuarioId() != null) {
            try {
                c.setUsuarioId(new ObjectId(req.getUsuarioId()));
            } catch (IllegalArgumentException iae) {
                throw new IllegalArgumentException("usuarioId inválido: " + req.getUsuarioId());
            }
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
