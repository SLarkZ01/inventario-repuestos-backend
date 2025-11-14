package com.repobackend.api.taller.service;

import java.util.Map;
import java.util.Optional;

import com.repobackend.api.taller.model.Almacen;
import com.repobackend.api.taller.model.Taller;

public interface ITallerService {
    Map<String, Object> crearTaller(String userId, String nombre);
    java.util.List<Taller> getTalleresByOwner(String ownerId);
    Map<String, Object> crearAlmacen(String userId, String tallerId, String nombre, String ubicacion);
    Map<String, Object> crearInvitacionCodigo(String fromUserId, String tallerId, String role, int daysValid);
    Map<String, Object> acceptInvitationByCode(String userId, String code);
    Map<String, Object> promoteMember(String callerId, String tallerId, String memberUserId);
    Map<String, Object> demoteMember(String callerId, String tallerId, String memberUserId);
    Map<String, Object> removeMember(String callerId, String tallerId, String memberUserId);
    Optional<Almacen> findAlmacenById(String almacenId);
    boolean isUserMemberWithAnyRole(String userId, String tallerId, java.util.List<String> allowedRoles);

    // Nuevos
    Optional<Taller> getTallerById(String tallerId);
    Map<String, Object> actualizarTaller(String callerId, String tallerId, String nuevoNombre);
    Map<String, Object> deleteTaller(String callerId, String tallerId);
    Map<String, Object> listAlmacenesByTaller(String tallerId);
    Map<String, Object> updateAlmacen(String callerId, String tallerId, String almacenId, String nombre, String ubicacion);
    Map<String, Object> deleteAlmacen(String callerId, String tallerId, String almacenId);
    Map<String, Object> listMembers(String tallerId);
    boolean isUserMember(String userId, String tallerId);

    // Nuevo: listar talleres donde el usuario es owner o miembro
    java.util.List<com.repobackend.api.taller.model.Taller> getTalleresForUser(String userId);
}