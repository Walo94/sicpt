package com.gamezdev.sicp.diseno.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gamezdev.sicp.common.dto.OpcionDto;
import com.gamezdev.sicp.diseno.entity.Horma;
import com.gamezdev.sicp.diseno.entity.Linea;
import com.gamezdev.sicp.diseno.entity.Marca;
import com.gamezdev.sicp.diseno.repository.HormaRepository;
import com.gamezdev.sicp.diseno.repository.LineaRepository;
import com.gamezdev.sicp.diseno.repository.MarcaRepository;
import com.gamezdev.sicp.diseno.entity.Modelo;
import com.gamezdev.sicp.diseno.repository.ModeloRepository;
import com.gamezdev.sicp.security.service.AuditoriaService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ModeloService {
    public static final int PAGE_SIZE = 10;
    private static final Set<String> CAMPOS_ORDEN = Set.of("id", "estatus", "codigoModelo", "descripcion",
            "fechaCreacion");

    private final ModeloRepository repository;
    private final MarcaRepository marcaRepository;
    private final LineaRepository lineaRepository;
    private final HormaRepository hormaRepository;
    private final AuditoriaService auditoria;

    public static class DuplicadoException extends RuntimeException {
        public DuplicadoException(String msg) {
            super(msg);
        }
    }

    public static class ReferenciaInvalidaException extends RuntimeException {
        public ReferenciaInvalidaException(String msg) {
            super(msg);
        }
    }

    @Transactional(readOnly = true)
    public Page<Modelo> buscar(String q, int page, String sort, String dir) {
        String campo = CAMPOS_ORDEN.contains(sort) ? sort : "id";
        Sort.Direction d = "desc".equalsIgnoreCase(dir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        String filtro = q == null ? "" : q.trim();
        return repository.buscar(filtro,
                PageRequest.of(Math.max(page, 0), PAGE_SIZE, Sort.by(d, campo)));
    }

    @Transactional(readOnly = true)
    public Optional<Modelo> obtener(Integer id) {
        return repository.findById(id);
    }

    @Transactional
    public Modelo crear(String codigoModelo, String descripcion,
            Integer marcaId, Integer lineaId, Integer hormaId) {

        String cod = codigoModelo.trim().toUpperCase();
        String desc = descripcion.trim();

        if (repository.existsByCodigoModeloIgnoreCase(cod)) {
            throw new DuplicadoException("Ya existe un modelo con este código.");
        }

        Marca marca = marcaRepository.findById(marcaId)
                .filter(Marca::isActivo)
                .orElseThrow(() -> new ReferenciaInvalidaException("La marca seleccionada no existe o está inactiva."));
        Linea linea = lineaRepository.findById(lineaId)
                .filter(Linea::isActivo)
                .orElseThrow(() -> new ReferenciaInvalidaException("La línea seleccionada no existe o está inactiva."));
        Horma horma = hormaRepository.findById(hormaId)
                .filter(Horma::isActivo)
                .orElseThrow(() -> new ReferenciaInvalidaException("La horma seleccionada no existe o está inactiva."));

        Modelo m = new Modelo();
        m.setCodigoModelo(cod);
        m.setDescripcion(desc);
        m.setMarca(marca);
        m.setLinea(linea);
        m.setHorma(horma);
        m.setEstatus(1);
        m = repository.save(m);
        auditoria.registrar("CREAR", "DISENO", "modelos", m.getId(),
                "Código: " + cod + " - " + desc);
        return m;
    }

    @Transactional
    public Modelo actualizar(Integer id, String codigoModelo, String descripcion,
            Integer marcaId, Integer lineaId, Integer hormaId) {

        String cod = codigoModelo.trim().toUpperCase();
        String desc = descripcion.trim();

        Modelo m = repository.findById(id).orElseThrow();
        if (repository.existsByCodigoModeloIgnoreCaseAndIdNot(cod, id)) {
            throw new DuplicadoException("Ya existe un modelo con este código.");
        }

        Marca marca = marcaRepository.findById(marcaId)
                .filter(Marca::isActivo)
                .orElseThrow(() -> new ReferenciaInvalidaException("La marca seleccionada no existe o está inactiva."));
        Linea linea = lineaRepository.findById(lineaId)
                .filter(Linea::isActivo)
                .orElseThrow(() -> new ReferenciaInvalidaException("La línea seleccionada no existe o está inactiva."));
        Horma horma = hormaRepository.findById(hormaId)
                .filter(Horma::isActivo)
                .orElseThrow(() -> new ReferenciaInvalidaException("La horma seleccionada no existe o está inactiva."));

        String anterior = m.getCodigoModelo() + " - " + m.getDescripcion();
        m.setCodigoModelo(cod);
        m.setDescripcion(desc);
        m.setMarca(marca);
        m.setLinea(linea);
        m.setHorma(horma);
        auditoria.registrar("EDITAR", "DISENO", "modelos", id,
                anterior + " -> " + cod + " - " + desc);
        return m;
    }

    @Transactional
    public Modelo cambiarEstatus(Integer id) {
        Modelo m = repository.findById(id).orElseThrow();
        m.setEstatus(m.isActivo() ? 0 : 1);
        auditoria.registrar("ESTATUS", "DISENO", "modelos", id,
                m.getCodigoModelo() + ": " + (m.isActivo() ? "alta" : "baja"));
        return m;
    }

    @Transactional(readOnly = true)
    public List<OpcionDto> buscarActivasParaCombo(String q) {
        var pageable = PageRequest.of(0, 20, Sort.by("codigoModelo"));
        return repository.buscar(q == null ? "" : q.trim(), pageable)
                .stream().filter(Modelo::isActivo)
                .map(m -> new OpcionDto(m.getId(), m.getCodigoModelo() + " - " + m.getDescripcion())).toList();
    }
}