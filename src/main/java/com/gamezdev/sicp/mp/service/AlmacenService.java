package com.gamezdev.sicp.mp.service;

import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gamezdev.sicp.mp.entity.AlmacenMp;
import com.gamezdev.sicp.mp.repository.AlmacenRepository;
import com.gamezdev.sicp.security.service.AuditoriaService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AlmacenService {
    public static final int PAGE_SIZE = 10;
    private static final Set<String> CAMPOS_ORDEN = Set.of("id", "estatus", "codigo", "descripcion");

    private final AlmacenRepository repository;
    private final AuditoriaService auditoria;

    public static class DuplicadoException extends RuntimeException {
        public DuplicadoException(String msg) { super(msg); }
    }

    @Transactional(readOnly = true)
    public Page<AlmacenMp> buscar(String q, int page, String sort, String dir) {
        String campo = CAMPOS_ORDEN.contains(sort) ? sort : "id";
        Sort.Direction d = "desc".equalsIgnoreCase(dir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        String filtro = q == null ? "" : q.trim();
        return repository.buscar(filtro,
                PageRequest.of(Math.max(page, 0), PAGE_SIZE, Sort.by(d, campo)));
    }

    @Transactional(readOnly = true)
    public Optional<AlmacenMp> obtener(Integer id) {
        return repository.findById(id);
    }

    @Transactional
    public AlmacenMp crear(String codigo, String descripcion) {
        String cod = codigo.trim().toUpperCase();
        String desc = descripcion.trim();

        if (repository.existsByCodigoIgnoreCase(cod)) {
            throw new DuplicadoException("Ya existe un almacén con este código.");
        }

        AlmacenMp a = new AlmacenMp();
        a.setCodigo(cod);
        a.setDescripcion(desc);
        a.setEstatus(1);
        a = repository.save(a);
        
        auditoria.registrar("CREAR", "MP", "almacenes_mp", a.getId(), "Código: " + cod);
        return a;
    }

    @Transactional
    public AlmacenMp actualizar(Integer id, String codigo, String descripcion) {
        String cod = codigo.trim().toUpperCase();
        String desc = descripcion.trim();

        AlmacenMp a = repository.findById(id).orElseThrow();
        if (repository.existsByCodigoIgnoreCaseAndIdNot(cod, id)) {
            throw new DuplicadoException("Ya existe un almacén con este código.");
        }

        String anterior = a.getCodigo() + " - " + a.getDescripcion();
        a.setCodigo(cod);
        a.setDescripcion(desc);
        
        auditoria.registrar("EDITAR", "MP", "almacenes_mp", id, anterior + " -> " + cod + " - " + desc);
        return a;
    }

    @Transactional
    public AlmacenMp cambiarEstatus(Integer id) {
        AlmacenMp a = repository.findById(id).orElseThrow();
        a.setEstatus(a.isActivo() ? 0 : 1);
        auditoria.registrar("ESTATUS", "MP", "almacenes_mp", id,
                a.getCodigo() + ": " + (a.isActivo() ? "alta" : "baja"));
        return a;
    }
}