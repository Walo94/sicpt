package com.gamezdev.sicp.catalogos.service;

import com.gamezdev.sicp.catalogos.entity.Marca;
import com.gamezdev.sicp.catalogos.repository.MarcaRepository;
import com.gamezdev.sicp.security.service.AuditoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MarcaService {

    public static final int PAGE_SIZE = 10;
    private static final Set<String> CAMPOS_ORDEN = Set.of("id", "estatus", "descripcion");

    private final MarcaRepository repository;
    private final AuditoriaService auditoria;

    /** Excepción de regla de negocio (se muestra como error en el campo). */
    public static class DuplicadoException extends RuntimeException {
        public DuplicadoException(String msg) {
            super(msg);
        }
    }

    @Transactional(readOnly = true)
    public Page<Marca> buscar(String q, int page, String sort, String dir) {
        String campo = CAMPOS_ORDEN.contains(sort) ? sort : "id";
        Sort.Direction d = "desc".equalsIgnoreCase(dir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        String filtro = q == null ? "" : q.trim();
        return repository.findByDescripcionContainingIgnoreCase(filtro,
                PageRequest.of(Math.max(page, 0), PAGE_SIZE, Sort.by(d, campo)));
    }

    @Transactional(readOnly = true)
    public Optional<Marca> obtener(Integer id) {
        return repository.findById(id);
    }

    @Transactional
    public Marca crear(String descripcion) {
        String desc = descripcion.trim();
        if (repository.existsByDescripcionIgnoreCase(desc)) {
            throw new DuplicadoException("Ya existe un departamento con esta descripción.");
        }
        Marca d = new Marca();
        d.setDescripcion(desc);
        d.setEstatus(1); // siempre nace Activo
        d = repository.save(d);
        auditoria.registrar("CREAR", "CATALOGOS", "marcas", d.getId(), "Descripción: " + desc);
        return d;
    }

    @Transactional
    public Marca actualizar(Integer id, String descripcion) {
        String desc = descripcion.trim();
        Marca d = repository.findById(id).orElseThrow();
        if (repository.existsByDescripcionIgnoreCaseAndIdNot(desc, id)) {
            throw new DuplicadoException("Ya existe un departamento con esta descripción.");
        }
        String anterior = d.getDescripcion();
        d.setDescripcion(desc); // el estatus NO cambia al editar
        auditoria.registrar("EDITAR", "CATALOGOS", "marcas", id, anterior + " -> " + desc);
        return d;
    }

    @Transactional
    public Marca cambiarEstatus(Integer id) {
        Marca d = repository.findById(id).orElseThrow();
        d.setEstatus(d.isActivo() ? 0 : 1);
        auditoria.registrar("ESTATUS", "CATALOGOS", "marcas", id,
                d.getDescripcion() + ": " + (d.isActivo() ? "alta" : "baja"));
        return d;
    }

}
