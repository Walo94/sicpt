package com.gamezdev.sicp.diseno.service;

import com.gamezdev.sicp.common.dto.OpcionDto;
import com.gamezdev.sicp.diseno.entity.UnidadMedida;
import com.gamezdev.sicp.diseno.repository.UnidadRepository;
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
public class UnidadService {
    public static final int PAGE_SIZE = 10;
    private static final Set<String> CAMPOS_ORDEN = Set.of("id", "estatus", "codigo", "descripcion", "claveSat");

    private final UnidadRepository repository;
    private final AuditoriaService auditoria;

    public static class DuplicadoException extends RuntimeException {
        public DuplicadoException(String msg) {
            super(msg);
        }
    }

    @Transactional(readOnly = true)
    public Page<UnidadMedida> buscar(String q, int page, String sort, String dir) {
        String campo = CAMPOS_ORDEN.contains(sort) ? sort : "id";
        Sort.Direction d = "desc".equalsIgnoreCase(dir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        String filtro = q == null ? "" : q.trim();
        return repository.findByCodigoContainingIgnoreCaseOrDescripcionContainingIgnoreCase(
                filtro, filtro,
                PageRequest.of(Math.max(page, 0), PAGE_SIZE, Sort.by(d, campo)));
    }

    @Transactional(readOnly = true)
    public Optional<UnidadMedida> obtener(Integer id) {
        return repository.findById(id);
    }

    @Transactional
    public UnidadMedida crear(String codigo, String descripcion, String claveSat) {
        String cod = normalizarCodigo(codigo);
        String desc = descripcion.trim();
        String sat = normalizarOpcional(claveSat);

        if (repository.existsByCodigoIgnoreCase(cod)) {
            throw new DuplicadoException("Ya existe una unidad con este código.");
        }
        UnidadMedida u = new UnidadMedida();
        u.setCodigo(cod);
        u.setDescripcion(desc);
        u.setClaveSat(sat);
        u.setEstatus(1);
        u = repository.save(u);
        auditoria.registrar("CREAR", "CATALOGOS", "unidades_medida", u.getId(),
                "Código: " + cod + " - " + desc);
        return u;
    }

    @Transactional
    public UnidadMedida actualizar(Integer id, String codigo, String descripcion, String claveSat) {
        String cod = normalizarCodigo(codigo);
        String desc = descripcion.trim();
        String sat = normalizarOpcional(claveSat);

        UnidadMedida u = repository.findById(id).orElseThrow();
        if (repository.existsByCodigoIgnoreCaseAndIdNot(cod, id)) {
            throw new DuplicadoException("Ya existe una unidad con este código.");
        }
        String anterior = u.getCodigo() + " - " + u.getDescripcion();
        u.setCodigo(cod);
        u.setDescripcion(desc);
        u.setClaveSat(sat);
        auditoria.registrar("EDITAR", "CATALOGOS", "unidades_medida", id,
                anterior + " -> " + cod + " - " + desc);
        return u;
    }

    @Transactional
    public UnidadMedida cambiarEstatus(Integer id) {
        UnidadMedida u = repository.findById(id).orElseThrow();
        u.setEstatus(u.isActivo() ? 0 : 1);
        auditoria.registrar("ESTATUS", "CATALOGOS", "unidades_medida", id,
                u.getCodigo() + ": " + (u.isActivo() ? "alta" : "baja"));
        return u;
    }

    // ---------- helpers ----------
    private String normalizarCodigo(String codigo) {
        return codigo == null ? "" : codigo.trim().toUpperCase();
    }

    private String normalizarOpcional(String valor) {
        if (valor == null)
            return null;
        String v = valor.trim();
        return v.isEmpty() ? null : v.toUpperCase();
    }

    // Dentro de UnidadService, agregar:

    @SuppressWarnings("null")
    @Transactional(readOnly = true)
    public java.util.List<OpcionDto> buscarActivasParaCombo(String q) {
        String filtro = q == null ? "" : q.trim();
        var pageable = org.springframework.data.domain.PageRequest.of(
                0, 20, org.springframework.data.domain.Sort.by("descripcion"));
        return repository
                .findByDescripcionContainingIgnoreCase(filtro, pageable)
                .stream()
                .filter(UnidadMedida::isActivo)
                .map(m -> new OpcionDto(m.getId(), m.getDescripcion()))
                .toList();
    }
}