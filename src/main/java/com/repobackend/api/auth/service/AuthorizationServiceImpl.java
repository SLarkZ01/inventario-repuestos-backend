package com.repobackend.api.auth.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.repobackend.api.auth.model.User;
import com.repobackend.api.auth.repository.UserRepository;
import com.repobackend.api.producto.model.Producto;
import com.repobackend.api.producto.repository.ProductoRepository;
import com.repobackend.api.taller.model.Taller;
import com.repobackend.api.taller.repository.TallerRepository;

@Service("authorizationService")
public class AuthorizationServiceImpl implements AuthorizationService {
    private final UserRepository userRepository;
    private final TallerRepository tallerRepository;
    private final ProductoRepository productoRepository;

    public AuthorizationServiceImpl(UserRepository userRepository, TallerRepository tallerRepository, ProductoRepository productoRepository) {
        this.userRepository = userRepository;
        this.tallerRepository = tallerRepository;
        this.productoRepository = productoRepository;
    }

    @Override
    public boolean isPlatformAdmin(String userId) {
        if (userId == null) return false;
        Optional<User> maybe = userRepository.findById(userId);
        if (maybe.isEmpty()) return false;
        User u = maybe.get();
        return u.getRoles() != null && u.getRoles().contains("ADMIN");
    }

    @Override
    public boolean isGlobalVendedor(String userId) {
        if (userId == null) return false;
        Optional<User> maybe = userRepository.findById(userId);
        if (maybe.isEmpty()) return false;
        User u = maybe.get();
        return u.getRoles() != null && u.getRoles().contains("VENDEDOR");
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean isMemberWithAnyRole(String userId, String tallerId, List<String> allowedRoles) {
        if (userId == null || tallerId == null) return false;
        Optional<Taller> maybe = tallerRepository.findById(tallerId);
        if (maybe.isEmpty()) return false;
        Taller t = maybe.get();
        if (t.getOwnerId() != null && t.getOwnerId().equals(userId)) return true;
        if (t.getMiembros() == null) return false;
        return t.getMiembros().stream().anyMatch(m -> userId.equals(String.valueOf(m.get("userId"))) && ((java.util.List<String>) m.get("roles")).stream().anyMatch(allowedRoles::contains));
    }

    @Override
    public boolean canManageProduct(String userId, String productId) {
        if (userId == null) return false;
        // platform admin can manage any product
        if (isPlatformAdmin(userId)) return true;
        // otherwise check ownership by product ownerId and membership in product's taller
        Optional<Producto> maybe = productoRepository.findById(productId);
        if (maybe.isEmpty()) return false;
        Producto p = maybe.get();
        // owner check
        String owner = p.getOwnerId();
        if (owner != null && owner.equals(userId)) return true;
        // taller membership check
        String tallerId = p.getTallerId();
        if (tallerId != null && isMemberWithAnyRole(userId, tallerId, java.util.List.of("ADMIN","VENDEDOR"))) return true;
        return false;
    }

    @Override
    public boolean canManageCategory(String userId) {
        if (userId == null) return false;
        if (isPlatformAdmin(userId)) return true;
        // allow global vendedores (backwards compat) to manage categories
        if (isGlobalVendedor(userId)) return true;
        // further scoping rules could be added here (e.g. category owners)
        return false;
    }
}
