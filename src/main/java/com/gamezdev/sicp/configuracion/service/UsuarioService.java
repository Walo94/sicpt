package com.gamezdev.sicp.configuracion.service;

import com.gamezdev.sicp.common.exception.ReglaNegocioException;
import com.gamezdev.sicp.common.util.SecurityUtils;
import com.gamezdev.sicp.configuracion.dto.UsuarioForm;
import com.gamezdev.sicp.security.entity.Rol;
import com.gamezdev.sicp.security.entity.Usuario;
import com.gamezdev.sicp.security.model.UsuarioDetails;
import com.gamezdev.sicp.security.repository.RolRepository;
import com.gamezdev.sicp.security.repository.UsuarioRepository;
import com.gamezdev.sicp.security.service.AuditoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioService {
    public static final int PAGE_SIZE = 10;
    private static final int PASSWORD_MIN = 8;
    private static final Set<String> CAMPOS_ORDEN = Set.of("id", "estatus", "username", "nombre", "ultimoAcceso");

    private final UsuarioRepository repository;
    private final RolRepository rolRepository;
    private final PasswordEncoder encoder;
    private final AuditoriaService auditoria;

    // ---------------- consultas ----------------
    @Transactional(readOnly = true)
    public Page<Usuario> buscar(String q, int page, String sort, String dir) {
        String campo = CAMPOS_ORDEN.contains(sort) ? sort : "id";
        Sort.Direction d = "desc".equalsIgnoreCase(dir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return repository.buscar(q == null ? "" : q.trim(),
                PageRequest.of(Math.max(page, 0), PAGE_SIZE, Sort.by(d, campo)));
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> obtener(Integer id) {
        return repository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Rol> rolesActivos() {
        return rolRepository.findByEstatusOrderByNombreAsc(1);
    }

    // ---------------- comandos ----------------
    @Transactional
    public Usuario crear(UsuarioForm f) {
        String username = f.getUsername().trim();
        if (repository.existsByUsernameIgnoreCase(username)) {
            throw new ReglaNegocioException("username", "Ya existe un usuario con ese nombre de usuario.");
        }
        String email = normalizar(f.getEmail());
        if (email != null && repository.existsByEmailIgnoreCase(email)) {
            throw new ReglaNegocioException("email", "Ya existe un usuario con ese correo.");
        }
        validarPassword(f, true);
        Set<Rol> roles = resolverRoles(f.getRolIds(), Set.of());

        Usuario u = new Usuario();
        u.setUsername(username);
        u.setNombre(f.getNombre().trim());
        u.setApellidos(normalizar(f.getApellidos()));
        u.setEmail(email);
        u.setPasswordHash(encoder.encode(f.getPassword()));
        u.setEstatus(1);
        u.setRoles(roles);
        u = repository.save(u);

        auditoria.registrar("CREAR", "SEGURIDAD", "usuarios", u.getId(),
                "Usuario: " + username + " | Roles: " + nombres(roles));
        return u;
    }

    @Transactional
    public Usuario actualizar(Integer id, UsuarioForm f) {
        Usuario u = repository.findById(id).orElseThrow();
        UsuarioDetails actual = SecurityUtils.usuario();

        String email = normalizar(f.getEmail());
        if (email != null && repository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new ReglaNegocioException("email", "Ya existe un usuario con ese correo.");
        }
        validarPassword(f, false);

        @SuppressWarnings("null")
        Set<Integer> actualesIds = u.getRoles().stream().map(Rol::getId).collect(Collectors.toSet());
        Set<Integer> nuevosIds = new HashSet<>(f.getRolIds() == null ? List.of() : f.getRolIds());
        if (actual != null && actual.getId().equals(id) && !actualesIds.equals(nuevosIds)) {
            throw new ReglaNegocioException("rolIds", "No puedes modificar tus propios roles.");
        }
        Set<Rol> roles = resolverRoles(f.getRolIds(), u.getRoles());

        u.setNombre(f.getNombre().trim()); // el username no se puede cambiar
        u.setApellidos(normalizar(f.getApellidos()));
        u.setEmail(email);
        u.setRoles(roles);
        boolean cambioPassword = f.getPassword() != null && !f.getPassword().isEmpty();
        if (cambioPassword)
            u.setPasswordHash(encoder.encode(f.getPassword()));

        auditoria.registrar("EDITAR", "SEGURIDAD", "usuarios", id,
                "Usuario: " + u.getUsername() + " | Roles: " + nombres(roles)
                        + (cambioPassword ? " | Contraseña restablecida" : ""));
        return u;
    }

    @Transactional
    public Usuario cambiarEstatus(Integer id) {
        Usuario u = repository.findById(id).orElseThrow();
        UsuarioDetails actual = SecurityUtils.usuario();
        if (actual != null && actual.getId().equals(id)) {
            throw new ReglaNegocioException("No puedes darte de baja a ti mismo.");
        }
        u.setEstatus(u.isActivo() ? 0 : 1);
        auditoria.registrar("ESTATUS", "SEGURIDAD", "usuarios", id,
                u.getUsername() + ": " + (u.isActivo() ? "alta" : "baja"));
        return u;
    }

    // ---------------- reglas ----------------
    private void validarPassword(UsuarioForm f, boolean obligatoria) {
        String p = f.getPassword();
        boolean vacia = p == null || p.isEmpty();
        if (vacia) {
            if (obligatoria)
                throw new ReglaNegocioException("password", "La contraseña es obligatoria.");
            return;
        }
        if (p.length() < PASSWORD_MIN) {
            throw new ReglaNegocioException("password", "Mínimo " + PASSWORD_MIN + " caracteres.");
        }
        if (p.length() > 72) { // límite de BCrypt
            throw new ReglaNegocioException("password", "Máximo 72 caracteres.");
        }
        if (!p.equals(f.getConfirmar())) {
            throw new ReglaNegocioException("confirmar", "Las contraseñas no coinciden.");
        }
    }

    @SuppressWarnings("null")
    private Set<Rol> resolverRoles(List<Integer> ids, Set<Rol> actuales) {
        if (ids == null || ids.isEmpty()) {
            throw new ReglaNegocioException("rolIds", "Selecciona al menos un rol.");
        }
        List<Rol> roles = rolRepository.findAllById(ids);
        if (roles.size() != new HashSet<>(ids).size()) {
            throw new ReglaNegocioException("rolIds", "Algún rol seleccionado ya no existe.");
        }
        Set<Integer> yaTenia = actuales.stream().map(Rol::getId).collect(Collectors.toSet());
        for (Rol r : roles) {
            boolean nuevo = !yaTenia.contains(r.getId());
            if (nuevo && !r.isActivo()) {
                throw new ReglaNegocioException("rolIds", "El rol \"" + r.getNombre() + "\" está dado de baja.");
            }
            // Evita escalar privilegios: solo un administrador puede otorgar el rol
            // Administrador
            if (nuevo && "ADMINISTRADOR".equals(r.getCodigo()) && !SecurityUtils.esAdministrador()) {
                throw new ReglaNegocioException("rolIds", "Solo un administrador puede asignar el rol Administrador.");
            }
        }
        return new HashSet<>(roles);
    }

    private String normalizar(String s) {
        return (s == null || s.trim().isEmpty()) ? null : s.trim();
    }

    @SuppressWarnings("null")
    private String nombres(Set<Rol> roles) {
        return roles.stream().map(Rol::getCodigo).sorted().collect(Collectors.joining(", "));
    }

}
