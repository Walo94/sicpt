package com.gamezdev.sicp.configuracion.service;

import com.gamezdev.sicp.security.entity.Auditoria;
import com.gamezdev.sicp.security.repository.AuditoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditoriaConsultaService {
    public static final int PAGE_SIZE = 15;
    private static final LocalDateTime MIN = LocalDateTime.of(2000, 1, 1, 0, 0);
    private static final LocalDateTime MAX = LocalDateTime.of(2999, 12, 31, 0, 0);

    private final AuditoriaRepository repository;

    @Transactional(readOnly = true)
    public Page<Auditoria> buscar(String q, String accion, LocalDate desde, LocalDate hasta, int page) {
        return repository.buscar(
                q == null ? "" : q.trim().toLowerCase(),
                accion == null ? "" : accion,
                desde == null ? MIN : desde.atStartOfDay(),
                hasta == null ? MAX : hasta.plusDays(1).atStartOfDay(),
                PageRequest.of(Math.max(page, 0), PAGE_SIZE,
                        Sort.by(Sort.Direction.DESC, "fecha").and(Sort.by(Sort.Direction.DESC, "id"))));
    }

    @Transactional(readOnly = true)
    public List<String> acciones() {
        return repository.accionesDistintas();
    }
}
