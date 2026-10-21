package com.gamezdev.sicp.diseno.service;

import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gamezdev.sicp.diseno.entity.Color;
import com.gamezdev.sicp.diseno.entity.Talla;
import com.gamezdev.sicp.diseno.entity.UnidadMedida;
import com.gamezdev.sicp.diseno.repository.ColorRepository;
import com.gamezdev.sicp.diseno.repository.TallaRepository;
import com.gamezdev.sicp.diseno.repository.UnidadRepository;
import com.gamezdev.sicp.diseno.entity.Combinacion;
import com.gamezdev.sicp.diseno.entity.Modelo;
import com.gamezdev.sicp.diseno.repository.CombinacionRepository;
import com.gamezdev.sicp.diseno.repository.ModeloRepository;
import com.gamezdev.sicp.security.service.AuditoriaService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CombinacionService {

    public static final int PAGE_SIZE = 10;
    private static final Set<String> CAMPOS_ORDEN =
            Set.of("id", "estatus", "codigoCombinacion", "precioVentaBase");

    private final CombinacionRepository repository;
    private final ModeloRepository modeloRepository;
    private final ColorRepository colorRepository;
    private final TallaRepository tallaRepository;
    private final UnidadRepository unidadRepository;
    private final AuditoriaService auditoria;

    public static class DuplicadoException extends RuntimeException {
        public DuplicadoException(String msg) { super(msg); }
    }

    public static class ReferenciaInvalidaException extends RuntimeException {
        public ReferenciaInvalidaException(String msg) { super(msg); }
    }

    @Transactional(readOnly = true)
    public Page<Combinacion> buscar(String q, int page, String sort, String dir) {
        String campo = CAMPOS_ORDEN.contains(sort) ? sort : "id";
        Sort.Direction d = "desc".equalsIgnoreCase(dir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        String filtro = q == null ? "" : q.trim();
        return repository.buscar(filtro,
                PageRequest.of(Math.max(page, 0), PAGE_SIZE, Sort.by(d, campo)));
    }

    @Transactional(readOnly = true)
    public Optional<Combinacion> obtener(Integer id) {
        return repository.findById(id);
    }

    @Transactional
    public Combinacion crear(String codigo, Integer modeloId, Integer colorId, Integer tallaId,
            Integer unidadVentaId, String claveSat, java.math.BigDecimal precio) {

        String cod = codigo.trim().toUpperCase();
        if (repository.existsByCodigoCombinacionIgnoreCase(cod)) {
            throw new DuplicadoException("Ya existe una combinación con este código.");
        }

        Modelo modelo = modeloRepository.findById(modeloId).filter(Modelo::isActivo)
                .orElseThrow(() -> new ReferenciaInvalidaException("El modelo no existe o está inactivo."));

        Color color = colorRepository.findById(colorId).filter(Color::isActivo)
                .orElseThrow(() -> new ReferenciaInvalidaException("El color no existe o está inactivo."));

        Talla talla = tallaRepository.findById(tallaId).filter(Talla::isActivo)
                .orElseThrow(() -> new ReferenciaInvalidaException("La talla no existe o está inactiva."));

        UnidadMedida unidad = unidadRepository.findById(unidadVentaId).filter(UnidadMedida::isActivo)
                .orElseThrow(() -> new ReferenciaInvalidaException("La unidad no existe o está inactiva."));

        Combinacion c = new Combinacion();
        c.setCodigoCombinacion(cod);
        c.setModelo(modelo);
        c.setColor(color);
        c.setTalla(talla);
        c.setUnidadVenta(unidad);
        c.setClaveSatProducto(claveSat.trim());
        c.setPrecioVentaBase(precio);
        c.setEstatus(1);

        c = repository.save(c);
        auditoria.registrar("CREAR", "DISENO", "productos_combinaciones", c.getId(),
                "Código: " + cod + " - " + modelo.getCodigoModelo());
        return c;
    }

    @Transactional
    public Combinacion actualizar(Integer id, String codigo, Integer modeloId, Integer colorId,
            Integer tallaId, Integer unidadVentaId, String claveSat, java.math.BigDecimal precio) {

        String cod = codigo.trim().toUpperCase();
        Combinacion c = repository.findById(id).orElseThrow();

        if (repository.existsByCodigoCombinacionIgnoreCaseAndIdNot(cod, id)) {
            throw new DuplicadoException("Ya existe una combinación con este código.");
        }

        Modelo modelo = modeloRepository.findById(modeloId).filter(Modelo::isActivo)
                .orElseThrow(() -> new ReferenciaInvalidaException("El modelo no existe o está inactivo."));

        Color color = colorRepository.findById(colorId).filter(Color::isActivo)
                .orElseThrow(() -> new ReferenciaInvalidaException("El color no existe o está inactivo."));

        Talla talla = tallaRepository.findById(tallaId).filter(Talla::isActivo)
                .orElseThrow(() -> new ReferenciaInvalidaException("La talla no existe o está inactiva."));

        UnidadMedida unidad = unidadRepository.findById(unidadVentaId).filter(UnidadMedida::isActivo)
                .orElseThrow(() -> new ReferenciaInvalidaException("La unidad no existe o está inactiva."));

        String anterior = c.getCodigoCombinacion();
        c.setCodigoCombinacion(cod);
        c.setModelo(modelo);
        c.setColor(color);
        c.setTalla(talla);
        c.setUnidadVenta(unidad);
        c.setClaveSatProducto(claveSat.trim());
        c.setPrecioVentaBase(precio);

        auditoria.registrar("EDITAR", "DISENO", "productos_combinaciones", id, anterior + " -> " + cod);
        return c;
    }

    @Transactional
    public Combinacion cambiarEstatus(Integer id) {
        Combinacion c = repository.findById(id).orElseThrow();
        c.setEstatus(c.isActivo() ? 0 : 1);
        auditoria.registrar("ESTATUS", "DISENO", "productos_combinaciones", id,
                c.getCodigoCombinacion() + ": " + (c.isActivo() ? "alta" : "baja"));
        return c;
    }
}