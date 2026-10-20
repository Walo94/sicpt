package com.gamezdev.sicp.common.web;

import com.gamezdev.sicp.security.model.UsuarioDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/** Expone el usuario logueado a todas las vistas como ${usuarioActual}. */
@ControllerAdvice
public class UsuarioActualAdvice {
    @ModelAttribute("usuarioActual")
    public UsuarioDetails usuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UsuarioDetails ud) {
            return ud;
        }
        return null;
    }
}
