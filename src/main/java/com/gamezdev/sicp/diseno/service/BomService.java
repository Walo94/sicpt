package com.gamezdev.sicp.diseno.service;

import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gamezdev.sicp.diseno.dto.BomForm;
import com.gamezdev.sicp.diseno.dto.ConsumoItemForm;
import com.gamezdev.sicp.diseno.entity.Modelo;
import com.gamezdev.sicp.diseno.entity.ModeloBom;
import com.gamezdev.sicp.diseno.entity.ModeloConsumo;
import com.gamezdev.sicp.diseno.repository.BomRepository;
import com.gamezdev.sicp.diseno.repository.ModeloRepository;
import com.gamezdev.sicp.mp.entity.Material;
import com.gamezdev.sicp.mp.repository.MaterialRepository;
import com.gamezdev.sicp.security.service.AuditoriaService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BomService {

    public static final int PAGE_SIZE = 10;
    private static final Set<String> CAMPOS_ORDEN = Set.of("id", "estatus", "version");

    private final BomRepository repository;
    private final ModeloRepository modeloRepository;
    private final MaterialRepository materialRepository;
    private final AuditoriaService auditoria;

    public static class DuplicadoException extends RuntimeException {
        public DuplicadoException(String msg) {
            super(msg);
        }
    }

    @Transactional(readOnly = true)
    public Page<ModeloBom> buscar(String q, int page, String sort, String dir) {
        String campo = CAMPOS_ORDEN.contains(sort) ? sort : "id";
        Sort.Direction d = "desc".equalsIgnoreCase(dir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return repository.buscarUltimasVersiones(q == null ? "" : q.trim(),
                PageRequest.of(Math.max(page, 0), PAGE_SIZE, Sort.by(d, campo)));
    }

    @Transactional(readOnly = true)
    public Optional<ModeloBom> obtener(Integer id) {
        // Se asume que EntityGraph o una transacción abierta cargan las líneas
        return repository.findById(id);
    }

    @Transactional(readOnly = true)
    public java.util.List<ModeloBom> obtenerVersiones(Integer modeloId) {
        // Retorna todas las versiones del modelo ordenadas de la más reciente a la más
        // antigua
        return repository.findByModeloIdOrderByVersionDesc(modeloId);
    }

    @Transactional
    public ModeloBom guardar(BomForm form) {
        ModeloBom bom;

        if (form.getId() == null) {
            // CREACIÓN: Nueva versión (si ya existe alguna, será la MAX + 1)
            bom = new ModeloBom();
            Modelo m = modeloRepository.findById(form.getModeloId()).orElseThrow();
            bom.setModelo(m);

            int nextVersion = repository.findFirstByModeloIdOrderByVersionDesc(form.getModeloId())
                    .map(b -> b.getVersion() + 1)
                    .orElse(1);
            bom.setVersion(nextVersion);

        } else {
            // EDICIÓN: No modificamos la versión vieja, creamos una NUEVA
            ModeloBom oldBom = repository.findById(form.getId()).orElseThrow();

            bom = new ModeloBom();
            bom.setModelo(oldBom.getModelo());
            bom.setVersion(oldBom.getVersion() + 1);
        }

        bom.setDescripcion(form.getDescripcion());
        bom.setEstatus(1);

        // Sincronización Maestro-Detalle
        for (ConsumoItemForm item : form.getConsumos()) {
            Material mat = materialRepository.findById(item.getMaterialId()).orElseThrow();
            ModeloConsumo mc = new ModeloConsumo();
            mc.setBom(bom);
            mc.setPiezaCalzado(item.getPiezaCalzado().trim().toUpperCase());
            mc.setMaterial(mat);
            mc.setCantidadConsumo(item.getCantidadConsumo());
            bom.getConsumos().add(mc);
        }

        bom = repository.save(bom);

        repository.flush();

        auditoria.registrar(form.getId() == null ? "CREAR" : "EDITAR (Nueva Versión)", "DISENO", "modelos_bom",
                bom.getId(),
                "BOM v" + bom.getVersion() + " - Modelo: " + bom.getModelo().getCodigoModelo());

        return bom;
    }

    @Transactional
    public ModeloBom cambiarEstatus(Integer id) {
        ModeloBom bom = repository.findById(id).orElseThrow();
        bom.setEstatus(bom.isActivo() ? 0 : 1);
        auditoria.registrar("ESTATUS", "DISENO", "modelos_bom", id,
                "BOM v" + bom.getVersion() + ": " + (bom.isActivo() ? "alta" : "baja"));
        return bom;
    }
}