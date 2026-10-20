package com.gamezdev.sicp.security.service;

import com.gamezdev.sicp.security.entity.Auditoria;
import com.gamezdev.sicp.security.model.UsuarioDetails;
import com.gamezdev.sicp.security.repository.AuditoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.gamezdev.sicp.common.util.ClienteIP;

@Service
@RequiredArgsConstructor
public class AuditoriaService {

    private final AuditoriaRepository repository;

    /** Registra una acción del usuario actualmente autenticado. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrar(String accion, String modulo, String entidad, Object entidadId, String detalle) {
        Auditoria a = new Auditoria();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UsuarioDetails ud) {
            a.setUsuarioId(ud.getId());
            a.setUsername(ud.getUsername());
        }
        completar(a, accion, modulo, entidad, entidadId, detalle);
    }

    /**
     * Registra una acción de un usuario explícito (login, logout, intentos
     * fallidos).
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarComo(Integer usuarioId, String username, String accion, String detalle) {
        Auditoria a = new Auditoria();
        a.setUsuarioId(usuarioId);
        a.setUsername(recortar(username, 50));
        completar(a, accion, "SEGURIDAD", "usuarios", usuarioId, detalle);
    }

    private void completar(Auditoria a, String accion, String modulo, String entidad, Object entidadId,
            String detalle) {
        a.setAccion(accion);
        a.setModulo(modulo);
        a.setEntidad(entidad);
        a.setEntidadId(entidadId == null ? null : String.valueOf(entidadId));
        a.setDetalle(recortar(detalle, 1000));
        a.setIp(ipActual());
        repository.save(a);
    }

    private String ipActual() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes sra) {
            return ClienteIP.resolver(sra.getRequest());
        }
        return null;
    }

    private String recortar(String s, int max) {
        return (s != null && s.length() > max) ? s.substring(0, max) : s;
    }

}
