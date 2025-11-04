package com.repobackend.api.auth.service;

import java.util.List;

public interface AuthorizationService {
    boolean isPlatformAdmin(String userId);
    boolean isGlobalVendedor(String userId);
    boolean isMemberWithAnyRole(String userId, String tallerId, List<String> allowedRoles);
    boolean canManageProduct(String userId, String productId);
    boolean canManageCategory(String userId);
}

