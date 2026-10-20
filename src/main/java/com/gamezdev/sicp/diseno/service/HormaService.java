package com.gamezdev.sicp.diseno.service;

import com.gamezdev.sicp.catalogos.entity.Talla;
import com.gamezdev.sicp.catalogos.repository.TallaRepository;
import com.gamezdev.sicp.diseno.entity.Horma;
import com.gamezdev.sicp.diseno.repository.HormaRepository;
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
public class HormaService {
    public static final int PAGE_SIZE = 10;
    private static final Set<String> CAMPOS_ORDEN = Set.of("id", "estatus", "claveHorma", "descripcion", "puntoBase",
            "alturaTaconMm", "tallaEscala");

    private final HormaRepository repository;
    private final TallaRepository tallaRepository;
    private final AuditoriaService auditoria;

    public static class DuplicadoException extends RuntimeException {
        public DuplicadoException(String msg) {
            super(msg);
        }
    }

    public static class TallaInvalidaException extends RuntimeException {
        public TallaInvalidaException(String msg) {
            super(msg);
        }
    }

    @Transactional(readOnly = true)
    public Page<Horma> buscar(String q, int page, String sort, String dir) {
        String campo = CAMPOS_ORDEN.contains(sort) ? sort : "id";
        Sort.Direction d = "desc".equalsIgnoreCase(dir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        String filtro = q == null ? "" : q.trim();
        return repository.buscar(filtro,
                PageRequest.of(Math.max(page, 0), PAGE_SIZE, Sort.by(d, campo)));
    }

    @Transactional(readOnly = true)
    public Optional<Horma> obtener(Integer id) {
        return repository.findById(id);
    }

    @Transactional
    public Horma crear(String claveHorma, String descripcion, Integer tallaEscalaId,
            BigDecimal puntoBase, Integer alturaTaconMm) {
        String clave = claveHorma.trim().toUpperCase();
        String desc = descripcion.trim();

        if (repository.existsByClaveHormaIgnoreCase(clave)) {
            throw new DuplicadoException("Ya existe una horma con esta clave.");
        }
        Talla talla = obtenerTallaActiva(tallaEscalaId);

        Horma h = new Horma();
        h.setClaveHorma(clave);
        h.setDescripcion(desc);
        h.setTallaEscala(talla);
        h.setPuntoBase(puntoBase);
        h.setAlturaTaconMm(alturaTaconMm == null ? 0 : alturaTaconMm);
        h.setEstatus(1);
        h = repository.save(h);
        auditoria.registrar("CREAR", "CATALOGOS", "hormas", h.getId(),
                "Clave: " + clave + " - " + desc);
        return h;
    }

    @Transactional
    public Horma actualizar(Integer id, String claveHorma, String descripcion,
            Integer tallaEscalaId, BigDecimal puntoBase, Integer alturaTaconMm) {
        String clave = claveHorma.trim().toUpperCase();
        String desc = descripcion.trim();

        Horma h = repository.findById(id).orElseThrow();
        if (repository.existsByClaveHormaIgnoreCaseAndIdNot(clave, id)) {
            throw new DuplicadoException("Ya existe una horma con esta clave.");
        }
        Talla talla = obtenerTallaActiva(tallaEscalaId);

        String anterior = h.getClaveHorma() + " - " + h.getDescripcion();
        h.setClaveHorma(clave);
        h.setDescripcion(desc);
        h.setTallaEscala(talla);
        h.setPuntoBase(puntoBase);
        h.setAlturaTaconMm(alturaTaconMm == null ? 0 : alturaTaconMm);
        auditoria.registrar("EDITAR", "CATALOGOS", "hormas", id,
                anterior + " -> " + clave + " - " + desc);
        return h;
    }

    @Transactional
    public Horma cambiarEstatus(Integer id) {
        Horma h = repository.findById(id).orElseThrow();
        h.setEstatus(h.isActivo() ? 0 : 1);
        auditoria.registrar("ESTATUS", "CATALOGOS", "hormas", id,
                h.getClaveHorma() + ": " + (h.isActivo() ? "alta" : "baja"));
        return h;
    }

    private Talla obtenerTallaActiva(Integer id) {
        if (id == null)
            throw new TallaInvalidaException("Debes seleccionar una talla.");
        Talla t = tallaRepository.findById(id)
                .orElseThrow(() -> new TallaInvalidaException("La talla seleccionada no existe."));
        if (!t.isActivo()) {
            throw new TallaInvalidaException("La talla seleccionada está inactiva.");
        }
        return t;
    }
}
