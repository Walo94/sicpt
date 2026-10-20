package com.gamezdev.sicp.security.handler;

import com.gamezdev.sicp.security.service.AuditoriaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthEventsListener {
    private final AuditoriaService auditoria;

    @EventListener
    public void onFallo(AbstractAuthenticationFailureEvent event) {
        try {
            auditoria.registrarComo(null, event.getAuthentication().getName(), "LOGIN_FALLIDO",
                    event.getException().getClass().getSimpleName());
        } catch (Exception e) {
            log.error("No se pudo registrar el intento fallido en auditoría", e);
        }
    }
}
