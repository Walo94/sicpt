package com.gamezdev.sicp.diseno.service;

import com.gamezdev.sicp.common.dto.OpcionDto;
import com.gamezdev.sicp.diseno.entity.Linea;
import com.gamezdev.sicp.diseno.repository.LineaRepository;
import com.gamezdev.sicp.security.service.AuditoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class LineaService {
    public static final int PAGE_SIZE = 10;
    private static final Set<String> CAMPOS_ORDEN = Set.of("id", "estatus", "descripcion");

    private final LineaRepository repository;
    private final AuditoriaService auditoria;

    /** Excepción de regla de negocio (se muestra como error en el campo). */
    public static class DuplicadoException extends RuntimeException {
        public DuplicadoException(String msg) {
            super(msg);
        }
    }

    @Transactional(readOnly = true)
    public Page<Linea> buscar(String q, int page, String sort, String dir) {
        String campo = CAMPOS_ORDEN.contains(sort) ? sort : "id";
        Sort.Direction d = "desc".equalsIgnoreCase(dir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        String filtro = q == null ? "" : q.trim();
        return repository.findByDescripcionContainingIgnoreCase(filtro,
                PageRequest.of(Math.max(page, 0), PAGE_SIZE, Sort.by(d, campo)));
    }

    @Transactional(readOnly = true)
    public Optional<Linea> obtener(Integer id) {
        return repository.findById(id);
    }

    @Transactional
    public Linea crear(String descripcion) {
        String desc = descripcion.trim();
        if (repository.existsByDescripcionIgnoreCase(desc)) {
            throw new DuplicadoException("Ya existe una línea con esta descripción.");
        }
        Linea d = new Linea();
        d.setDescripcion(desc);
        d.setEstatus(1); // siempre nace Activo
        d = repository.save(d);
        auditoria.registrar("CREAR", "CATALOGOS", "líneas", d.getId(), "Descripción: " + desc);
        return d;
    }

    @Transactional
    public Linea actualizar(Integer id, String descripcion) {
        String desc = descripcion.trim();
        Linea d = repository.findById(id).orElseThrow();
        if (repository.existsByDescripcionIgnoreCaseAndIdNot(desc, id)) {
            throw new DuplicadoException("Ya existe una línea con esta descripción.");
        }
        String anterior = d.getDescripcion();
        d.setDescripcion(desc); // el estatus NO cambia al editar
        auditoria.registrar("EDITAR", "CATALOGOS", "líneas", id, anterior + " -> " + desc);
        return d;
    }

    @Transactional
    public Linea cambiarEstatus(Integer id) {
        Linea d = repository.findById(id).orElseThrow();
        d.setEstatus(d.isActivo() ? 0 : 1);
        auditoria.registrar("ESTATUS", "CATALOGOS", "líneas", id,
                d.getDescripcion() + ": " + (d.isActivo() ? "alta" : "baja"));
        return d;
    }

    @SuppressWarnings("null")
    @Transactional(readOnly = true)
    public List<OpcionDto> buscarActivasParaCombo(String q) {
        var pageable = PageRequest.of(0, 20, Sort.by("descripcion"));
        return repository.findByDescripcionContainingIgnoreCase(q == null ? "" : q.trim(), pageable)
                .stream().filter(Linea::isActivo)
                .map(l -> new OpcionDto(l.getId(), l.getDescripcion())).toList();
    }
}
