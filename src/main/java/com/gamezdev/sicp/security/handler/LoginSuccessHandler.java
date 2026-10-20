package com.gamezdev.sicp.security.handler;

import com.gamezdev.sicp.security.model.UsuarioDetails;
import com.gamezdev.sicp.security.repository.UsuarioRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Actualiza la fecha del último acceso después de un login correcto.
 * No registra eventos en el módulo de auditoría.
 *
 * No se marca como @Transactional para evitar mezclar la actualización
 * del usuario con transacciones de auditoría.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {
    private final UsuarioRepository usuarioRepository;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        if (authentication.getPrincipal() instanceof UsuarioDetails ud) {
            try {
                usuarioRepository.actualizarUltimoAcceso(
                        ud.getId(),
                        LocalDateTime.now());
            } catch (Exception e) {
                // Un fallo al actualizar la fecha no debe impedir que el usuario entre.
                log.error(
                        "No se pudo actualizar la fecha del último acceso para el usuario ID {}",
                        ud.getId(),
                        e);
            }
        }

        // Conserva la redirección a la URL solicitada previamente;
        // "/" funciona como destino predeterminado.
        setDefaultTargetUrl("/");
        super.onAuthenticationSuccess(request, response, authentication);
    }
}
