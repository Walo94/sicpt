package com.gamezdev.sicp.common.util;

import com.gamezdev.sicp.security.model.UsuarioDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    private SecurityUtils() {
    }

    /** Usuario autenticado o null. */
    public static UsuarioDetails usuario() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.getPrincipal() instanceof UsuarioDetails ud) ? ud : null;
    }

    public static boolean tiene(String authority) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null)
            return false;
        for (GrantedAuthority ga : auth.getAuthorities()) {
            if (ga.getAuthority().equals(authority))
                return true;
        }
        return false;
    }

    public static boolean esAdministrador() {
        return tiene("ROLE_ADMINISTRADOR");
    }
}
