package com.gamezdev.sicp.catalogos.service;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import com.gamezdev.sicp.catalogos.dto.ColorItem;
import com.gamezdev.sicp.catalogos.dto.ColorJsonWrapper;
import com.gamezdev.sicp.catalogos.entity.Color;
import com.gamezdev.sicp.catalogos.repository.ColorRepository;
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
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class ColorService {
    public static final int PAGE_SIZE = 10;
    private static final Set<String> CAMPOS_ORDEN = Set.of("id", "estatus", "descripcion");
    private static final Pattern HEX_PATTERN = Pattern.compile("^#([0-9a-fA-F]{6}|[0-9a-fA-F]{3})$");

    private final ColorRepository repository;
    private final AuditoriaService auditoria;
    private final ObjectMapper objectMapper;

    public static class DuplicadoException extends RuntimeException {
        public DuplicadoException(String msg) {
            super(msg);
        }
    }

    public static class JsonInvalidoException extends RuntimeException {
        public JsonInvalidoException(String msg) {
            super(msg);
        }
    }

    @Transactional(readOnly = true)
    public Page<Color> buscar(String q, int page, String sort, String dir) {
        String campo = CAMPOS_ORDEN.contains(sort) ? sort : "id";
        Sort.Direction d = "desc".equalsIgnoreCase(dir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        String filtro = q == null ? "" : q.trim();
        return repository.findByDescripcionContainingIgnoreCase(filtro,
                PageRequest.of(Math.max(page, 0), PAGE_SIZE, Sort.by(d, campo)));
    }

    @Transactional(readOnly = true)
    public Optional<Color> obtener(Integer id) {
        return repository.findById(id);
    }

    /**
     * Parsea el JSON almacenado a lista de colores. Devuelve lista vacía si es
     * null/inválido.
     */
    public List<ColorItem> parseColores(String json) {
        if (json == null || json.isBlank())
            return List.of();
        try {
            ColorJsonWrapper w = objectMapper.readValue(json, ColorJsonWrapper.class);
            return w.colores() == null ? List.of() : w.colores();
        } catch (JacksonException e) {
            return List.of();
        }
    }

    @Transactional
    public Color crear(String descripcion, String colorJson) {
        String desc = descripcion.trim();
        String jsonNormalizado = normalizarYValidarJson(colorJson);
        if (repository.existsByDescripcionIgnoreCase(desc)) {
            throw new DuplicadoException("Ya existe un color con esta descripción.");
        }
        Color c = new Color();
        c.setDescripcion(desc);
        c.setColorJson(jsonNormalizado);
        c.setEstatus(1);
        c = repository.save(c);
        auditoria.registrar("CREAR", "CATALOGOS", "colores", c.getId(), "Descripción: " + desc);
        return c;
    }

    @Transactional
    public Color actualizar(Integer id, String descripcion, String colorJson) {
        String desc = descripcion.trim();
        String jsonNormalizado = normalizarYValidarJson(colorJson);
        Color c = repository.findById(id).orElseThrow();
        if (repository.existsByDescripcionIgnoreCaseAndIdNot(desc, id)) {
            throw new DuplicadoException("Ya existe un color con esta descripción.");
        }
        String anterior = c.getDescripcion();
        c.setDescripcion(desc);
        c.setColorJson(jsonNormalizado);
        auditoria.registrar("EDITAR", "CATALOGOS", "colores", id, anterior + " -> " + desc);
        return c;
    }

    @Transactional
    public Color cambiarEstatus(Integer id) {
        Color c = repository.findById(id).orElseThrow();
        c.setEstatus(c.isActivo() ? 0 : 1);
        auditoria.registrar("ESTATUS", "CATALOGOS", "colores", id,
                c.getDescripcion() + ": " + (c.isActivo() ? "alta" : "baja"));
        return c;
    }

    /**
     * Recibe el JSON del cliente, valida estructura y hex, y devuelve un JSON
     * normalizado.
     * Si viene vacío o null, devuelve null (campo opcional).
     */
    private String normalizarYValidarJson(String json) {
        if (json == null || json.isBlank())
            return null;
        ColorJsonWrapper w;
        try {
            w = objectMapper.readValue(json, ColorJsonWrapper.class);
        } catch (JacksonException e) {
            throw new JsonInvalidoException("El formato del color no es válido.");
        }
        if (w.colores() == null || w.colores().isEmpty())
            return null;
        for (ColorItem ci : w.colores()) {
            if (ci.hex() == null || !HEX_PATTERN.matcher(ci.hex()).matches()) {
                throw new JsonInvalidoException("El código hexadecimal '" + ci.hex() + "' no es válido.");
            }
            if (ci.nombre() == null || ci.nombre().isBlank()) {
                throw new JsonInvalidoException("Cada color debe tener un nombre.");
            }
        }
        try {
            return objectMapper.writeValueAsString(w);
        } catch (JacksonException e) {
            throw new JsonInvalidoException("No se pudo procesar el color.");
        }
    }
}