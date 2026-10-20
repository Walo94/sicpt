package com.gamezdev.sicp.catalogos.service;

import com.gamezdev.sicp.catalogos.dto.TallaOpcionDto;
import com.gamezdev.sicp.catalogos.entity.Talla;
import com.gamezdev.sicp.catalogos.repository.TallaRepository;
import com.gamezdev.sicp.security.service.AuditoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TallaService {
    public static final int PAGE_SIZE = 10;
    private static final Set<String> CAMPOS_ORDEN = Set.of("id", "estatus", "descripcion", "puntoInicial",
            "puntoFinal");

    private final TallaRepository repository;
    private final AuditoriaService auditoria;

    public static class DuplicadoException extends RuntimeException {
        public DuplicadoException(String msg) {
            super(msg);
        }
    }

    public static class RangoInvalidoException extends RuntimeException {
        public RangoInvalidoException(String msg) {
            super(msg);
        }
    }

    @Transactional(readOnly = true)
    public Page<Talla> buscar(String q, int page, String sort, String dir) {
        String campo = CAMPOS_ORDEN.contains(sort) ? sort : "id";
        Sort.Direction d = "desc".equalsIgnoreCase(dir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        String filtro = q == null ? "" : q.trim();
        return repository.findByDescripcionContainingIgnoreCase(filtro,
                PageRequest.of(Math.max(page, 0), PAGE_SIZE, Sort.by(d, campo)));
    }

    @Transactional(readOnly = true)
    public Optional<Talla> obtener(Integer id) {
        return repository.findById(id);
    }

    @Transactional
    public Talla crear(String descripcion, BigDecimal puntoInicial, BigDecimal puntoFinal) {
        String desc = descripcion.trim();
        validarRango(puntoInicial, puntoFinal);
        if (repository.existsByDescripcionIgnoreCase(desc)) {
            throw new DuplicadoException("Ya existe una talla con esta descripción.");
        }
        Talla t = new Talla();
        t.setDescripcion(desc);
        t.setPuntoInicial(puntoInicial);
        t.setPuntoFinal(puntoFinal);
        t.setEstatus(1);
        t = repository.save(t);
        auditoria.registrar("CREAR", "CATALOGOS", "tallas", t.getId(),
                "Descripción: " + desc + " [" + puntoInicial + " - " + puntoFinal + "]");
        return t;
    }

    @Transactional
    public Talla actualizar(Integer id, String descripcion, BigDecimal puntoInicial, BigDecimal puntoFinal) {
        String desc = descripcion.trim();
        validarRango(puntoInicial, puntoFinal);
        Talla t = repository.findById(id).orElseThrow();
        if (repository.existsByDescripcionIgnoreCaseAndIdNot(desc, id)) {
            throw new DuplicadoException("Ya existe una talla con esta descripción.");
        }
        String anterior = t.getDescripcion() + " [" + t.getPuntoInicial() + " - " + t.getPuntoFinal() + "]";
        t.setDescripcion(desc);
        t.setPuntoInicial(puntoInicial);
        t.setPuntoFinal(puntoFinal);
        auditoria.registrar("EDITAR", "CATALOGOS", "tallas", id,
                anterior + " -> " + desc + " [" + puntoInicial + " - " + puntoFinal + "]");
        return t;
    }

    @Transactional
    public Talla cambiarEstatus(Integer id) {
        Talla t = repository.findById(id).orElseThrow();
        t.setEstatus(t.isActivo() ? 0 : 1);
        auditoria.registrar("ESTATUS", "CATALOGOS", "tallas", id,
                t.getDescripcion() + ": " + (t.isActivo() ? "alta" : "baja"));
        return t;
    }

    private void validarRango(BigDecimal inicial, BigDecimal fin) {
        if (inicial == null || fin == null) {
            throw new RangoInvalidoException("Los puntos inicial y final son obligatorios.");
        }
        if (fin.compareTo(inicial) <= 0) {
            throw new RangoInvalidoException("El punto final debe ser mayor que el punto inicial.");
        }
    }

    // Dentro de TallaService, agregar:

    @SuppressWarnings("null")
    @Transactional(readOnly = true)
    public java.util.List<TallaOpcionDto> buscarActivasParaCombo(String q) {
        String filtro = q == null ? "" : q.trim();
        var pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "puntoInicial"));
        return repository
                .findByDescripcionContainingIgnoreCase(filtro, pageable)
                .stream()
                // Solo activas
                .filter(Talla::isActivo)
                .map(t -> new TallaOpcionDto(
                        t.getId(),
                        t.getDescripcion() + " (" + formatPunto(t.getPuntoInicial())
                                + " - " + formatPunto(t.getPuntoFinal()) + ")"))
                .toList();
    }

    private String formatPunto(java.math.BigDecimal v) {
        if (v == null)
            return "";
        java.math.BigDecimal n = v.stripTrailingZeros();
        return n.scale() <= 0 ? n.toPlainString() : n.toPlainString();
    }
}