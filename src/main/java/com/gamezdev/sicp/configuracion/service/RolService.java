package com.gamezdev.sicp.configuracion.service;

import com.gamezdev.sicp.common.exception.ReglaNegocioException;
import com.gamezdev.sicp.common.util.SecurityUtils;
import com.gamezdev.sicp.configuracion.dto.RolForm;
import com.gamezdev.sicp.security.entity.Permiso;
import com.gamezdev.sicp.security.entity.Rol;
import com.gamezdev.sicp.security.repository.PermisoRepository;
import com.gamezdev.sicp.security.repository.RolRepository;
import com.gamezdev.sicp.security.service.AuditoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RolService {

    public static final int PAGE_SIZE = 10;
    public static final String ROL_ADMIN = "ADMINISTRADOR";
    private static final Set<String> CAMPOS_ORDEN = Set.of("id", "estatus", "codigo", "nombre");

    /**
     * Estructura para pintar la matriz de permisos: módulo -> submódulo ->
     * acciones.
     */
    public record SubmoduloPermisos(String submodulo, List<Permiso> permisos) {
    }

    public record ModuloPermisos(String modulo, List<SubmoduloPermisos> submodulos) {
    }

    private final RolRepository repository;
    private final PermisoRepository permisoRepository;
    private final AuditoriaService auditoria;

    // ---------------- consultas ----------------
    @Transactional(readOnly = true)
    public Page<Rol> buscar(String q, int page, String sort, String dir) {
        String campo = CAMPOS_ORDEN.contains(sort) ? sort : "id";
        Sort.Direction d = "desc".equalsIgnoreCase(dir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return repository.buscar(q == null ? "" : q.trim(),
                PageRequest.of(Math.max(page, 0), PAGE_SIZE, Sort.by(d, campo)));
    }

    @Transactional(readOnly = true)
    public Optional<Rol> obtener(Integer id) {
        return repository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<ModuloPermisos> catalogoPermisos() {
        Map<String, Map<String, List<Permiso>>> agrupado = new LinkedHashMap<>();
        for (Permiso p : permisoRepository.findAll(Sort.by("id"))) {
            agrupado.computeIfAbsent(p.getModulo(), k -> new LinkedHashMap<>())
                    .computeIfAbsent(p.getSubmodulo(), k -> new ArrayList<>())
                    .add(p);
        }
        List<ModuloPermisos> resultado = new ArrayList<>();
        agrupado.forEach((modulo, subs) -> {
            List<SubmoduloPermisos> lista = new ArrayList<>();
            subs.forEach((sub, ps) -> lista.add(new SubmoduloPermisos(sub, ps)));
            resultado.add(new ModuloPermisos(modulo, lista));
        });
        return resultado;
    }

    // ---------------- comandos ----------------
    @Transactional
    public Rol crear(RolForm f) {
        String codigo = f.getCodigo().trim().toUpperCase();
        if (repository.existsByCodigoIgnoreCase(codigo)) {
            throw new ReglaNegocioException("codigo", "Ya existe un rol con ese código.");
        }
        Set<Permiso> permisos = resolverPermisos(f.getPermisoIds(), Set.of());

        Rol r = new Rol();
        r.setCodigo(codigo);
        r.setNombre(f.getNombre().trim());
        r.setDescripcion(normalizar(f.getDescripcion()));
        r.setEstatus(1);
        r.setPermisos(permisos);
        r = repository.save(r);

        auditoria.registrar("CREAR", "SEGURIDAD", "roles", r.getId(),
                "Rol: " + codigo + " | Permisos: " + permisos.size());
        return r;
    }

    @Transactional
    public Rol actualizar(Integer id, RolForm f) {
        Rol r = repository.findById(id).orElseThrow();
        r.setNombre(f.getNombre().trim());
        r.setDescripcion(normalizar(f.getDescripcion()));

        String detalle = "Rol: " + r.getCodigo();
        if (!esProtegido(r)) { // el rol Administrador conserva siempre sus permisos
            Set<Permiso> nuevos = resolverPermisos(f.getPermisoIds(), r.getPermisos());
            detalle += " | Permisos: " + r.getPermisos().size() + " -> " + nuevos.size();
            r.setPermisos(nuevos);
        }
        auditoria.registrar("EDITAR", "SEGURIDAD", "roles", id, detalle);
        return r;
    }

    @Transactional
    public Rol cambiarEstatus(Integer id) {
        Rol r = repository.findById(id).orElseThrow();
        if (esProtegido(r)) {
            throw new ReglaNegocioException("El rol Administrador no se puede dar de baja.");
        }
        r.setEstatus(r.isActivo() ? 0 : 1);
        auditoria.registrar("ESTATUS", "SEGURIDAD", "roles", id,
                r.getCodigo() + ": " + (r.isActivo() ? "alta" : "baja"));
        return r;
    }

    public boolean esProtegido(Rol r) {
        return ROL_ADMIN.equals(r.getCodigo());
    }

    // ---------------- reglas ----------------
    /**
     * Solo se pueden OTORGAR permisos que el usuario que edita también posee (salvo
     * administradores).
     */
    @SuppressWarnings("null")
    private Set<Permiso> resolverPermisos(List<Integer> ids, Set<Permiso> actuales) {
        if (ids == null || ids.isEmpty())
            return new HashSet<>();
        List<Permiso> permisos = permisoRepository.findAllById(ids);
        if (permisos.size() != new HashSet<>(ids).size()) {
            throw new ReglaNegocioException("permisoIds", "Algún permiso seleccionado no existe.");
        }
        Set<Integer> yaTenia = actuales.stream().map(Permiso::getId).collect(Collectors.toSet());
        if (!SecurityUtils.esAdministrador()) {
            for (Permiso p : permisos) {
                if (!yaTenia.contains(p.getId()) && !SecurityUtils.tiene(p.getCodigo())) {
                    throw new ReglaNegocioException("permisoIds",
                            "No puedes otorgar el permiso " + p.getCodigo() + " porque tú no lo tienes.");
                }
            }
        }
        return new HashSet<>(permisos);
    }

    private String normalizar(String s) {
        return (s == null || s.trim().isEmpty()) ? null : s.trim();
    }
}
