package com.gamezdev.sicp.diseno.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gamezdev.sicp.common.dto.OpcionDto;
import com.gamezdev.sicp.diseno.dto.ModeloForm;
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

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

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
    public Modelo crear(ModeloForm form) {
        String cod = form.getCodigoModelo().trim().toUpperCase();
        String desc = form.getDescripcion().trim();

        Modelo m;
        if (form.getId() == null) {
            if (repository.existsByCodigoModeloIgnoreCase(cod)) {
                throw new DuplicadoException("Ya existe un modelo con este código.");
            }
            m = new Modelo();
            m.setEstatus(1);
        } else {
            m = repository.findById(form.getId()).orElseThrow();
            if (repository.existsByCodigoModeloIgnoreCaseAndIdNot(cod, form.getId())) {
                throw new DuplicadoException("Ya existe un modelo con este código.");
            }
        }

        Marca marca = marcaRepository.findById(form.getMarcaId()).filter(Marca::isActivo)
                .orElseThrow(() -> new ReferenciaInvalidaException("La marca seleccionada no existe o está inactiva."));
        Linea linea = lineaRepository.findById(form.getLineaId()).filter(Linea::isActivo)
                .orElseThrow(() -> new ReferenciaInvalidaException("La línea seleccionada no existe o está inactiva."));
        Horma horma = hormaRepository.findById(form.getHormaId()).filter(Horma::isActivo)
                .orElseThrow(() -> new ReferenciaInvalidaException("La horma seleccionada no existe o está inactiva."));

        m.setCodigoModelo(cod);
        m.setDescripcion(desc);
        m.setMarca(marca);
        m.setLinea(linea);
        m.setHorma(horma);

        // Mapear nuevos campos
        m.setMaterialCorte(form.getMaterialCorte());
        m.setForro(form.getForro());
        m.setPlantilla(form.getPlantilla());
        m.setSuela(form.getSuela());
        m.setColorPrincipal(form.getColorPrincipal());
        m.setEstilo(form.getEstilo());
        m.setCordones(form.getCordones());
        m.setContrafuerte(form.getContrafuerte());
        m.setCuello(form.getCuello());
        m.setLengueta(form.getLengueta());
        m.setOjales(form.getOjales());
        m.setVidaUtil(form.getVidaUtil());
        m.setTallasDisponibles(form.getTallasDisponibles());
        m.setObservaciones(form.getObservaciones());
        m.setRecomendaciones(form.getRecomendaciones());
        m.setAlmacenamiento(form.getAlmacenamiento());

        // Manejo de Imagen
        if (form.getImagenFile() != null && !form.getImagenFile().isEmpty()) {
            try {
                // Creamos la carpeta si no existe
                Path uploadPath = Paths.get(uploadDir, "modelos");
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                // Nombre único para evitar sobreescribir: timestamp_nombreOriginal
                String nombreArchivo = System.currentTimeMillis() + "_"
                        + form.getImagenFile().getOriginalFilename().replaceAll("\\s+", "_");
                Path rutaDestino = uploadPath.resolve(nombreArchivo);

                Files.copy(form.getImagenFile().getInputStream(), rutaDestino, StandardCopyOption.REPLACE_EXISTING);

                // Guardamos la ruta relativa en la BD
                m.setImagenUrl("/uploads/modelos/" + nombreArchivo);

            } catch (IOException e) {
                throw new RuntimeException("Error al guardar la imagen: " + e.getMessage());
            }
        }

        m = repository.save(m);
        auditoria.registrar(form.getId() == null ? "CREAR" : "EDITAR", "DISENO", "modelos", m.getId(),
                "Código: " + cod);
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