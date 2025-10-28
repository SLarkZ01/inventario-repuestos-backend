package com.repobackend.api.taller.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import com.repobackend.api.taller.model.Almacen;
import com.repobackend.api.taller.model.Invitation;
import com.repobackend.api.taller.model.Taller;
import com.repobackend.api.taller.repository.AlmacenRepository;
import com.repobackend.api.taller.repository.InvitationRepository;
import com.repobackend.api.taller.repository.TallerRepository;

@Service
public class TallerService {
    private final TallerRepository tallerRepository;
    private final AlmacenRepository almacenRepository;
    private final InvitationRepository invitationRepository;

    public TallerService(TallerRepository tallerRepository, AlmacenRepository almacenRepository,
            InvitationRepository invitationRepository) {
        this.tallerRepository = tallerRepository;
        this.almacenRepository = almacenRepository;
        this.invitationRepository = invitationRepository;
    }

    public Map<String, Object> crearTaller(String userId, String nombre) {
        // limit: max 3 talleres per user
        var existing = tallerRepository.findByOwnerId(userId);
        if (existing.size() >= 3) {
            return Map.of("error", "Maximo de 3 talleres alcanzado");
        }
        Taller t = new Taller();
        t.setOwnerId(userId);
        t.setNombre(nombre);
        // add owner as miembro admin
        var miembro = new HashMap<String, Object>();
        miembro.put("userId", userId);
        miembro.put("roles", java.util.List.of("ADMIN"));
        miembro.put("joinedAt", new Date());
        t.setMiembros(java.util.List.of(miembro));
        Taller saved = tallerRepository.save(t);
        return Map.of("taller", saved);
    }

    public java.util.List<Taller> getTalleresByOwner(String ownerId) {
        return tallerRepository.findByOwnerId(ownerId);
    }

    public Map<String, Object> crearAlmacen(String userId, String tallerId, String nombre, String ubicacion) {
        // check taller exists and user is admin
        Optional<Taller> maybe = tallerRepository.findById(tallerId);
        if (maybe.isEmpty()) return Map.of("error", "Taller no encontrado");
        Taller t = maybe.get();
        @SuppressWarnings("unchecked")
        boolean isAdmin = t.getMiembros().stream().anyMatch(m -> userId.equals(String.valueOf(m.get("userId"))) && ((java.util.List<String>) m.get("roles")).contains("ADMIN"));
        if (!isAdmin && !t.getOwnerId().equals(userId)) return Map.of("error", "Permisos insuficientes");
        long c = almacenRepository.countByTallerId(tallerId);
        if (c >= 5) return Map.of("error", "Maximo de 5 almacenes alcanzado para este taller");
        Almacen a = new Almacen();
        a.setTallerId(tallerId);
        a.setNombre(nombre);
        a.setUbicacion(ubicacion);
        Almacen saved = almacenRepository.save(a);
        // update taller. add almacen id to list
        var almacenes = t.getAlmacenes();
        if (almacenes == null) almacenes = new java.util.ArrayList<>();
        almacenes.add(saved.getId());
        t.setAlmacenes(almacenes);
        tallerRepository.save(t);
        return Map.of("almacen", saved);
    }

    private String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] h = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(h);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public Map<String, Object> crearInvitacionCodigo(String fromUserId, String tallerId, String role, int daysValid) {
        Optional<Taller> maybe = tallerRepository.findById(tallerId);
        if (maybe.isEmpty()) return Map.of("error", "Taller no encontrado");
        Taller t = maybe.get();
        @SuppressWarnings("unchecked")
        boolean isAdmin = t.getMiembros().stream().anyMatch(m -> fromUserId.equals(String.valueOf(m.get("userId"))) && ((java.util.List<String>) m.get("roles")).contains("ADMIN"));
        if (!isAdmin && !t.getOwnerId().equals(fromUserId)) return Map.of("error", "Permisos insuficientes");

        // generate short code: 6 alphanumeric chars
        String raw = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 8).toUpperCase();
        String code = raw.substring(0, 6);
        Invitation inv = new Invitation();
        inv.setTallerId(tallerId);
        inv.setFromUserId(fromUserId);
        inv.setRole(role == null ? "VENDEDOR" : role);
        inv.setCodeHash(sha256(code));
        inv.setExpiresAt(new Date(System.currentTimeMillis() + (long)daysValid * 24 * 3600 * 1000));
        invitationRepository.save(inv);
        // return code to caller (owner) — caller should communicate code to invitee
        return Map.of("code", code, "expiresAt", inv.getExpiresAt());
    }

    public Map<String, Object> acceptInvitationByCode(String userId, String code) {
        String h = sha256(code);
        Optional<Invitation> maybe = invitationRepository.findByCodeHash(h);
        if (maybe.isEmpty()) return Map.of("error", "Invitacion inválida");
        Invitation inv = maybe.get();
        // Check if invitation is blocked due to too many failed attempts
        if (inv.isBlocked()) return Map.of("error", "Invitacion bloqueada por demasiados intentos");
        // mark attempt
        inv.setAttempts(inv.getAttempts() + 1);
        inv.setLastAttemptAt(new Date());
        if (inv.getAttempts() > inv.getMaxAttempts()) {
            inv.setBlocked(true);
            invitationRepository.save(inv);
            return Map.of("error", "Invitacion bloqueada por demasiados intentos");
        }
        if (inv.isRedeemed()) return Map.of("error", "Invitacion ya redimida");
        if (inv.getExpiresAt() != null && inv.getExpiresAt().before(new Date())) return Map.of("error", "Invitacion expirada");
        Optional<Taller> tMaybe = tallerRepository.findById(inv.getTallerId());
        if (tMaybe.isEmpty()) return Map.of("error", "Taller no encontrado");
        Taller t = tMaybe.get();
        // add user as miembro with role
        var miembro = new HashMap<String, Object>();
        miembro.put("userId", userId);
        miembro.put("roles", java.util.List.of(inv.getRole()));
        miembro.put("joinedAt", new Date());
        var miembros = t.getMiembros();
        if (miembros == null) miembros = new java.util.ArrayList<>();
        miembros.add(miembro);
        t.setMiembros(miembros);
        tallerRepository.save(t);
        inv.setRedeemed(true);
        inv.setRedeemedByUserId(userId);
        invitationRepository.save(inv);
        return Map.of("taller", t);
    }

    /**
     * Promueve a un miembro de un taller a rol ADMIN.
     * Solo el owner del taller o un miembro con rol ADMIN puede promover.
     */
    public Map<String, Object> promoteMember(String callerId, String tallerId, String memberUserId) {
        Optional<Taller> maybe = tallerRepository.findById(tallerId);
        if (maybe.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Taller no encontrado");
        Taller t = maybe.get();
        @SuppressWarnings("unchecked")
        boolean callerIsAdmin = t.getMiembros() != null && t.getMiembros().stream().anyMatch(m -> callerId.equals(String.valueOf(m.get("userId"))) && ((java.util.List<String>) m.get("roles")).contains("ADMIN"));
        if (!callerIsAdmin && !t.getOwnerId().equals(callerId)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Permisos insuficientes");

        var miembros = t.getMiembros();
        if (miembros == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Miembro no encontrado");
        java.util.Map<String, Object> target = null;
        for (var m : miembros) {
            if (memberUserId.equals(String.valueOf(m.get("userId")))) { target = m; break; }
        }
        if (target == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Miembro no encontrado");
        @SuppressWarnings("unchecked")
        java.util.List<String> roles = (java.util.List<String>) target.get("roles");
        if (roles == null) roles = new java.util.ArrayList<>();
        if (!roles.contains("ADMIN")) roles.add("ADMIN");
        target.put("roles", roles);
        t.setMiembros(miembros);
        tallerRepository.save(t);
        return Map.of("taller", t);
    }

    /**
     * Demueve a un miembro removiendo el rol ADMIN.
     * No se puede demover al owner.
     */
    public Map<String, Object> demoteMember(String callerId, String tallerId, String memberUserId) {
        Optional<Taller> maybe = tallerRepository.findById(tallerId);
        if (maybe.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Taller no encontrado");
        Taller t = maybe.get();
        if (t.getOwnerId().equals(memberUserId)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se puede demover al owner del taller");
        @SuppressWarnings("unchecked")
        boolean callerIsAdmin = t.getMiembros() != null && t.getMiembros().stream().anyMatch(m -> callerId.equals(String.valueOf(m.get("userId"))) && ((java.util.List<String>) m.get("roles")).contains("ADMIN"));
        if (!callerIsAdmin && !t.getOwnerId().equals(callerId)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Permisos insuficientes");

        var miembros = t.getMiembros();
        if (miembros == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Miembro no encontrado");
        java.util.Map<String, Object> target = null;
        for (var m : miembros) {
            if (memberUserId.equals(String.valueOf(m.get("userId")))) { target = m; break; }
        }
        if (target == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Miembro no encontrado");
        @SuppressWarnings("unchecked")
        java.util.List<String> roles = (java.util.List<String>) target.get("roles");
        if (roles != null && roles.contains("ADMIN")) {
            roles.removeIf(r -> r.equals("ADMIN"));
            target.put("roles", roles);
        }
        t.setMiembros(miembros);
        tallerRepository.save(t);
        return Map.of("taller", t);
    }

    /**
     * Remueve un miembro del taller. No se puede remover al owner.
     */
    public Map<String, Object> removeMember(String callerId, String tallerId, String memberUserId) {
        Optional<Taller> maybe = tallerRepository.findById(tallerId);
        if (maybe.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Taller no encontrado");
        Taller t = maybe.get();
        if (t.getOwnerId().equals(memberUserId)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se puede remover al owner del taller");
        @SuppressWarnings("unchecked")
        boolean callerIsAdmin = t.getMiembros() != null && t.getMiembros().stream().anyMatch(m -> callerId.equals(String.valueOf(m.get("userId"))) && ((java.util.List<String>) m.get("roles")).contains("ADMIN"));
        if (!callerIsAdmin && !t.getOwnerId().equals(callerId)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Permisos insuficientes");

        var miembros = t.getMiembros();
        if (miembros == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Miembro no encontrado");
        miembros.removeIf(m -> memberUserId.equals(String.valueOf(m.get("userId"))));
        t.setMiembros(miembros);
        tallerRepository.save(t);
        return Map.of("taller", t);
    }

    // Nuevo helper: obtener almacen por id
    public Optional<Almacen> findAlmacenById(String almacenId) {
        return almacenRepository.findById(almacenId);
    }

    // Nuevo helper: verificar si un usuario es owner o miembro con alguno de los roles dados
    @SuppressWarnings("unchecked")
    public boolean isUserMemberWithAnyRole(String userId, String tallerId, java.util.List<String> allowedRoles) {
        if (userId == null || tallerId == null) return false;
        Optional<Taller> maybe = tallerRepository.findById(tallerId);
        if (maybe.isEmpty()) return false;
        Taller t = maybe.get();
        if (t.getOwnerId() != null && t.getOwnerId().equals(userId)) return true;
        if (t.getMiembros() == null) return false;
        return t.getMiembros().stream().anyMatch(m -> userId.equals(String.valueOf(m.get("userId"))) && ((java.util.List<String>) m.get("roles")).stream().anyMatch(allowedRoles::contains));
    }
}
