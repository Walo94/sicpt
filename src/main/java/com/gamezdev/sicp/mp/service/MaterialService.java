package com.gamezdev.sicp.mp.service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gamezdev.sicp.diseno.entity.UnidadMedida;
import com.gamezdev.sicp.diseno.repository.UnidadRepository;
import com.gamezdev.sicp.mp.entity.Material;
import com.gamezdev.sicp.mp.repository.MaterialRepository;
import com.gamezdev.sicp.security.service.AuditoriaService;
import com.gamezdev.sicp.diseno.entity.Talla;
import com.gamezdev.sicp.diseno.service.TallaService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MaterialService {
    public static final int PAGE_SIZE = 10;
    private static final Set<String> CAMPOS_ORDEN = Set.of("id", "estatus", "codigo", "descripcion", "tipoMaterial");

    private final MaterialRepository repository;
    private final UnidadRepository unidadRepository;
    private final AuditoriaService auditoria;
    private final TallaService tallaService;

    public static class DuplicadoException extends RuntimeException {
        public DuplicadoException(String msg) {
            super(msg);
        }
    }

    public static class UnidadInvalidaException extends RuntimeException {
        public UnidadInvalidaException(String msg) {
            super(msg);
        }
    }

    public static class TallaInvalidaException extends RuntimeException {
        public TallaInvalidaException(String msg) {
            super(msg);
        }
    }

    private Talla resolverTalla(boolean manejaTalla, Integer tallaId) {
        if (!manejaTalla) {
            return null; // si no maneja talla, no se asigna ninguna
        }
        if (tallaId == null) {
            throw new TallaInvalidaException("Debes seleccionar una talla.");
        }
        Talla t = tallaService.obtener(tallaId)
                .orElseThrow(() -> new TallaInvalidaException("La talla seleccionada no existe."));
        if (!t.isActivo()) {
            throw new TallaInvalidaException("La talla seleccionada está inactiva.");
        }
        return t;
    }

    @Transactional(readOnly = true)
    public Page<Material> buscar(String q, int page, String sort, String dir) {
        String campo = CAMPOS_ORDEN.contains(sort) ? sort : "id";
        Sort.Direction d = "desc".equalsIgnoreCase(dir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        String filtro = q == null ? "" : q.trim();
        return repository.buscar(filtro,
                PageRequest.of(Math.max(page, 0), PAGE_SIZE, Sort.by(d, campo)));
    }

    @Transactional(readOnly = true)
    public Optional<Material> obtener(Integer id) {
        return repository.findById(id);
    }

    @Transactional
    public Material crear(String codigo, String descripcion,
            Integer unidadCompraId, Integer unidadConsumoId,
            BigDecimal factorConversion, String tipoMaterial, boolean manejaTalla, Integer tallaId) {

        String cod = codigo.trim().toUpperCase();
        String desc = descripcion.trim();
        String tipo = tipoMaterial.trim().toUpperCase();
        BigDecimal factor = validarFactor(factorConversion);

        if (repository.existsByCodigoIgnoreCase(cod)) {
            throw new DuplicadoException("Ya existe un material con este código.");
        }
        UnidadMedida compra = obtenerUnidadActiva(unidadCompraId, "compra");
        UnidadMedida consumo = obtenerUnidadActiva(unidadConsumoId, "consumo");
        Talla talla = resolverTalla(manejaTalla, tallaId);

        Material m = new Material();
        m.setCodigo(cod);
        m.setDescripcion(desc);
        m.setUnidadCompra(compra);
        m.setUnidadConsumo(consumo);
        m.setFactorConversion(factor);
        m.setTipoMaterial(tipo);
        m.setManejaTalla(talla != null ? 1 : 0);
        m.setTalla(talla);
        m.setEstatus(1);
        m = repository.save(m);
        auditoria.registrar("CREAR", "MP", "materiales", m.getId(),
                "Código: " + cod + " - " + desc
                        + " (1 " + compra.getCodigo() + " = " + factor + " " + consumo.getCodigo() + ")"
                        + (talla != null ? " - Talla: " + talla.getDescripcion() : ""));
        return m;
    }

    @Transactional
    public Material actualizar(Integer id, String codigo, String descripcion,
            Integer unidadCompraId, Integer unidadConsumoId,
            BigDecimal factorConversion, String tipoMaterial, boolean manejaTalla, Integer tallaId) {

        String cod = codigo.trim().toUpperCase();
        String desc = descripcion.trim();
        String tipo = tipoMaterial.trim().toUpperCase();
        BigDecimal factor = validarFactor(factorConversion);

        Material m = repository.findById(id).orElseThrow();
        if (repository.existsByCodigoIgnoreCaseAndIdNot(cod, id)) {
            throw new DuplicadoException("Ya existe un material con este código.");
        }
        UnidadMedida compra = obtenerUnidadActiva(unidadCompraId, "compra");
        UnidadMedida consumo = obtenerUnidadActiva(unidadConsumoId, "consumo");
        Talla talla = resolverTalla(manejaTalla, tallaId);

        String anterior = m.getCodigo() + " - " + m.getDescripcion();
        m.setCodigo(cod);
        m.setDescripcion(desc);
        m.setUnidadCompra(compra);
        m.setUnidadConsumo(consumo);
        m.setFactorConversion(factor);
        m.setTipoMaterial(tipo);
        m.setManejaTalla(talla != null ? 1 : 0);
        m.setTalla(talla);
        auditoria.registrar("EDITAR", "MP", "materiales", id,
                anterior + " -> " + cod + " - " + desc
                        + " (1 " + compra.getCodigo() + " = " + factor + " " + consumo.getCodigo() + ")"
                        + (talla != null ? " - Talla: " + talla.getDescripcion() : ""));
        return m;
    }

    @Transactional
    public Material cambiarEstatus(Integer id) {
        Material m = repository.findById(id).orElseThrow();
        m.setEstatus(m.isActivo() ? 0 : 1);
        auditoria.registrar("ESTATUS", "MP", "materiales", id,
                m.getCodigo() + ": " + (m.isActivo() ? "alta" : "baja"));
        return m;
    }

    private BigDecimal validarFactor(BigDecimal factor) {
        if (factor == null || factor.signum() <= 0) {
            throw new UnidadInvalidaException("El factor de conversión debe ser mayor a cero.");
        }
        return factor;
    }

    private UnidadMedida obtenerUnidadActiva(Integer id, String tipo) {
        if (id == null) {
            throw new UnidadInvalidaException("Debes seleccionar una unidad de " + tipo + ".");
        }
        UnidadMedida u = unidadRepository.findById(id)
                .orElseThrow(() -> new UnidadInvalidaException(
                        "La unidad de " + tipo + " seleccionada no existe."));
        if (!u.isActivo()) {
            throw new UnidadInvalidaException(
                    "La unidad de " + tipo + " seleccionada está inactiva.");
        }
        return u;
    }

    @Transactional(readOnly = true)
    public java.util.List<com.gamezdev.sicp.common.dto.OpcionDto> buscarActivasParaCombo(String q) {
        String filtro = q == null ? "" : q.trim();
        var pageable = org.springframework.data.domain.PageRequest.of(
                0, 20, org.springframework.data.domain.Sort.by("descripcion"));
        return repository.buscar(filtro, pageable)
                .stream()
                .filter(Material::isActivo)
                .map(m -> new com.gamezdev.sicp.common.dto.OpcionDto(
                        m.getId(),
                        m.getCodigo() + " - " + m.getDescripcion()))
                .toList();
    }
}