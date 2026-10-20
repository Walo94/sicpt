package com.gamezdev.sicp.configuracion.controller;

import com.gamezdev.sicp.common.exception.ReglaNegocioException;
import com.gamezdev.sicp.common.util.HtmxResponse;
import com.gamezdev.sicp.common.util.ListadoHelper;
import com.gamezdev.sicp.configuracion.dto.UsuarioForm;
import com.gamezdev.sicp.configuracion.service.UsuarioService;
import com.gamezdev.sicp.security.entity.Rol;
import com.gamezdev.sicp.security.entity.Usuario;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping(UsuarioController.URL)
@RequiredArgsConstructor
public class UsuarioController {

    static final String URL = "/configuracion/usuarios";
    private static final String VISTA = "configuracion/usuarios";
    private static final String TABLA = "configuracion/usuarios-tabla :: tabla";
    private static final String FORM = "configuracion/usuario-form :: formulario";

    private final UsuarioService service;

    @GetMapping
    @PreAuthorize("hasAuthority('USUARIOS_VER')")
    public String listar(@RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String dir,
            @RequestHeader(value = "HX-Request", required = false) String hx,
            Model model) {
        cargarListado(model, q, page, sort, dir);
        return hx != null ? TABLA : VISTA;
    }

    @GetMapping("/nuevo")
    @PreAuthorize("hasAuthority('USUARIOS_CREAR')")
    public String nuevo(Model model) {
        model.addAttribute("form", new UsuarioForm());
        model.addAttribute("roles", service.rolesActivos());
        return FORM;
    }

    @SuppressWarnings("null")
    @GetMapping("/{id}/editar")
    @PreAuthorize("hasAuthority('USUARIOS_EDITAR')")
    public String editar(@PathVariable Integer id, Model model) {
        Usuario u = obtener(id);
        UsuarioForm f = new UsuarioForm();
        f.setId(u.getId());
        f.setUsername(u.getUsername());
        f.setNombre(u.getNombre());
        f.setApellidos(u.getApellidos());
        f.setEmail(u.getEmail());
        f.setRolIds(u.getRoles().stream().map(Rol::getId).toList());
        model.addAttribute("form", f);
        model.addAttribute("roles", rolesParaEditar(u));
        return FORM;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('USUARIOS_CREAR')")
    public String crear(@Valid @ModelAttribute("form") UsuarioForm form, BindingResult br,
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String dir,
            Model model, HttpServletResponse res) {
        form.setId(null);
        if (!br.hasErrors()) {
            try {
                service.crear(form);
            } catch (ReglaNegocioException e) {
                e.aplicarA(br);
            }
        }
        if (br.hasErrors()) {
            model.addAttribute("roles", service.rolesActivos());
            return FORM;
        }
        HtmxResponse.exito(res, "#tablaContainer", "Usuario creado correctamente");
        cargarListado(model, q, page, sort, dir);
        return TABLA;
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasAuthority('USUARIOS_EDITAR')")
    public String actualizar(@PathVariable Integer id,
            @Valid @ModelAttribute("form") UsuarioForm form, BindingResult br,
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String dir,
            Model model, HttpServletResponse res) {
        Usuario u = obtener(id);
        form.setId(id);
        form.setUsername(u.getUsername()); // no editable
        if (!br.hasErrors()) {
            try {
                service.actualizar(id, form);
            } catch (ReglaNegocioException e) {
                e.aplicarA(br);
            }
        }
        if (br.hasErrors()) {
            model.addAttribute("roles", rolesParaEditar(u));
            return FORM;
        }
        HtmxResponse.exito(res, "#tablaContainer", "Usuario actualizado correctamente");
        cargarListado(model, q, page, sort, dir);
        return TABLA;
    }

    @PostMapping("/{id}/estatus")
    @PreAuthorize("hasAuthority('USUARIOS_ESTATUS')")
    public String estatus(@PathVariable Integer id,
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String dir,
            Model model, HttpServletResponse res) {
        obtener(id);
        try {
            Usuario u = service.cambiarEstatus(id);
            HtmxResponse.toast(res, "Usuario " + (u.isActivo() ? "activado" : "desactivado") + " correctamente");
        } catch (ReglaNegocioException e) {
            HtmxResponse.error(res, e.getMessage());
        }
        cargarListado(model, q, page, sort, dir);
        return TABLA;
    }

    // ---------------- helpers ----------------
    private Usuario obtener(Integer id) {
        return service.obtener(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    /**
     * Roles activos + los que el usuario ya tiene (aunque estén de baja) para no
     * perderlos al guardar.
     */
    private java.util.List<Rol> rolesParaEditar(Usuario u) {
        java.util.List<Rol> lista = new java.util.ArrayList<>(service.rolesActivos());
        u.getRoles().stream().filter(r -> !lista.contains(r)).forEach(lista::add);
        return lista;
    }

    private void cargarListado(Model model, String q, int page, String sort, String dir) {
        Page<Usuario> pagina = ListadoHelper.ajustar(service.buscar(q, page, sort, dir), page,
                p -> service.buscar(q, p, sort, dir));
        ListadoHelper.cargar(model, pagina, URL, q);
    }
}
