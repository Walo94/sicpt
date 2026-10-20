package com.gamezdev.sicp.security.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Uso en Thymeleaf:
 * th:if="${@permisos.tiene('DEPARTAMENTOS_CREAR')}"
 * th:if="${@permisos.tieneAlguno('PEDIDOS_VER','LOTES_VER')}"
 */
@Component("permisos")
public class PermisosHelper {
    public boolean tiene(String authority) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated())
            return false;
        for (GrantedAuthority ga : auth.getAuthorities()) {
            if (ga.getAuthority().equals(authority))
                return true;
        }
        return false;
    }

    public boolean tieneAlguno(String... authorities) {
        return Arrays.stream(authorities).anyMatch(this::tiene);
    }
}
