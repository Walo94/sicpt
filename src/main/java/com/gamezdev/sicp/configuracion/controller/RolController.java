package com.gamezdev.sicp.configuracion.controller;

import com.gamezdev.sicp.common.exception.ReglaNegocioException;
import com.gamezdev.sicp.common.util.HtmxResponse;
import com.gamezdev.sicp.common.util.ListadoHelper;
import com.gamezdev.sicp.configuracion.dto.RolForm;
import com.gamezdev.sicp.configuracion.service.RolService;
import com.gamezdev.sicp.security.entity.Permiso;
import com.gamezdev.sicp.security.entity.Rol;
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
@RequestMapping(RolController.URL)
@RequiredArgsConstructor
public class RolController {

    static final String URL = "/configuracion/roles";
    private static final String VISTA = "configuracion/roles";
    private static final String TABLA = "configuracion/roles-tabla :: tabla";
    private static final String FORM = "configuracion/rol-form :: formulario";

    private final RolService service;

    @GetMapping
    @PreAuthorize("hasAuthority('ROLES_VER')")
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
    @PreAuthorize("hasAuthority('ROLES_CREAR')")
    public String nuevo(Model model) {
        model.addAttribute("form", new RolForm());
        prepararForm(model, false);
        return FORM;
    }

    @SuppressWarnings("null")
    @GetMapping("/{id}/editar")
    @PreAuthorize("hasAuthority('ROLES_EDITAR')")
    public String editar(@PathVariable Integer id, Model model) {
        Rol r = obtener(id);
        RolForm f = new RolForm();
        f.setId(r.getId());
        f.setCodigo(r.getCodigo());
        f.setNombre(r.getNombre());
        f.setDescripcion(r.getDescripcion());
        f.setPermisoIds(r.getPermisos().stream().map(Permiso::getId).toList());
        model.addAttribute("form", f);
        prepararForm(model, service.esProtegido(r));
        return FORM;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLES_CREAR')")
    public String crear(@Valid @ModelAttribute("form") RolForm form, BindingResult br,
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
            prepararForm(model, false);
            return FORM;
        }
        HtmxResponse.exito(res, "#tablaContainer", "Rol creado correctamente");
        cargarListado(model, q, page, sort, dir);
        return TABLA;
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLES_EDITAR')")
    public String actualizar(@PathVariable Integer id,
            @Valid @ModelAttribute("form") RolForm form, BindingResult br,
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String dir,
            Model model, HttpServletResponse res) {
        Rol r = obtener(id);
        form.setId(id);
        form.setCodigo(r.getCodigo()); // el código no se puede cambiar
        if (!br.hasErrors()) {
            try {
                service.actualizar(id, form);
            } catch (ReglaNegocioException e) {
                e.aplicarA(br);
            }
        }
        if (br.hasErrors()) {
            prepararForm(model, service.esProtegido(r));
            return FORM;
        }
        HtmxResponse.exito(res, "#tablaContainer", "Rol actualizado correctamente");
        cargarListado(model, q, page, sort, dir);
        return TABLA;
    }

    @PostMapping("/{id}/estatus")
    @PreAuthorize("hasAuthority('ROLES_ESTATUS')")
    public String estatus(@PathVariable Integer id,
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String dir,
            Model model, HttpServletResponse res) {
        obtener(id);
        try {
            Rol r = service.cambiarEstatus(id);
            HtmxResponse.toast(res, "Rol " + (r.isActivo() ? "activado" : "desactivado") + " correctamente");
        } catch (ReglaNegocioException e) {
            HtmxResponse.error(res, e.getMessage());
        }
        cargarListado(model, q, page, sort, dir);
        return TABLA;
    }

    // ---------------- helpers ----------------
    private Rol obtener(Integer id) {
        return service.obtener(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private void prepararForm(Model model, boolean protegido) {
        model.addAttribute("catalogo", service.catalogoPermisos());
        model.addAttribute("protegido", protegido);
    }

    private void cargarListado(Model model, String q, int page, String sort, String dir) {
        Page<Rol> pagina = ListadoHelper.ajustar(service.buscar(q, page, sort, dir), page,
                p -> service.buscar(q, p, sort, dir));
        ListadoHelper.cargar(model, pagina, URL, q);
    }
}
