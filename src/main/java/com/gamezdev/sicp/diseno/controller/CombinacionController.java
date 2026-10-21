package com.gamezdev.sicp.diseno.controller;

import com.gamezdev.sicp.common.util.HtmxResponse;
import com.gamezdev.sicp.diseno.dto.CombinacionForm;
import com.gamezdev.sicp.diseno.entity.Combinacion;
import com.gamezdev.sicp.diseno.service.CombinacionService;
import com.gamezdev.sicp.diseno.service.CombinacionService.DuplicadoException;
import com.gamezdev.sicp.diseno.service.CombinacionService.ReferenciaInvalidaException;
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
import org.springframework.web.util.UriComponentsBuilder;

@Controller
@RequestMapping("/diseno/combinaciones")
@RequiredArgsConstructor
public class CombinacionController {

    private static final String VISTA = "diseno/combinaciones";
    private static final String TABLA = "diseno/combinaciones-tabla :: tabla";
    private static final String FORM = "diseno/combinacion-form :: formulario";

    private final CombinacionService service;

    @GetMapping
    @PreAuthorize("hasAuthority('COMBINACIONES_VER')")
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
    @PreAuthorize("hasAuthority('COMBINACIONES_CREAR')")
    public String nuevo(Model model) {
        CombinacionForm form = new CombinacionForm();
        form.setClaveSatProducto("53111500"); // Valor por defecto
        model.addAttribute("form", form);
        return FORM;
    }

    @GetMapping("/{id}/editar")
    @PreAuthorize("hasAuthority('COMBINACIONES_EDITAR')")
    public String editar(@PathVariable Integer id, Model model) {
        Combinacion c = obtener404(id);
        CombinacionForm form = new CombinacionForm();
        form.setId(c.getId());
        form.setCodigoCombinacion(c.getCodigoCombinacion());
        form.setModeloId(c.getModelo().getId());
        form.setModeloTexto(c.getModelo().getCodigoModelo() + " - " + c.getModelo().getDescripcion());
        form.setColorId(c.getColor().getId());
        form.setColorTexto(c.getColor().getDescripcion());
        form.setTallaId(c.getTalla().getId());
        form.setTallaTexto(c.getTalla().getDescripcion());
        form.setUnidadVentaId(c.getUnidadVenta().getId());
        form.setUnidadVentaTexto(c.getUnidadVenta().getCodigo() + " - " + c.getUnidadVenta().getDescripcion());
        form.setClaveSatProducto(c.getClaveSatProducto());
        form.setPrecioVentaBase(c.getPrecioVentaBase());
        model.addAttribute("form", form);
        return FORM;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('COMBINACIONES_CREAR')")
    public String crear(@Valid @ModelAttribute("form") CombinacionForm form, BindingResult br,
            @RequestParam(defaultValue = "") String q, @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "id") String sort, @RequestParam(defaultValue = "asc") String dir,
            Model model, HttpServletResponse res) {
        form.setId(null);
        if (!br.hasErrors()) {
            try {
                service.crear(form.getCodigoCombinacion(), form.getModeloId(), form.getColorId(),
                        form.getTallaId(), form.getUnidadVentaId(), form.getClaveSatProducto(),
                        form.getPrecioVentaBase());
            } catch (DuplicadoException e) {
                br.rejectValue("codigoCombinacion", "duplicado", e.getMessage());
            } catch (ReferenciaInvalidaException e) {
                // Aquí podrías mapear el error al campo específico, pero usaremos un error
                // global
                br.rejectValue("modeloId", "error", e.getMessage());
            }
        }
        if (br.hasErrors())
            return FORM;

        HtmxResponse.exito(res, "#tablaContainer", "Combinación creada correctamente");
        cargarListado(model, q, page, sort, dir);
        return TABLA;
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasAuthority('COMBINACIONES_EDITAR')")
    public String actualizar(@PathVariable Integer id,
            @Valid @ModelAttribute("form") CombinacionForm form, BindingResult br,
            @RequestParam(defaultValue = "") String q, @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "id") String sort, @RequestParam(defaultValue = "asc") String dir,
            Model model, HttpServletResponse res) {
        Combinacion c = obtener404(id);
        form.setId(id);

        // FORZAR QUE NO CAMBIE EL CÓDIGO ORIGINAL
        form.setCodigoCombinacion(c.getCodigoCombinacion());

        if (!br.hasErrors()) {
            try {
                service.actualizar(id, form.getCodigoCombinacion(), form.getModeloId(), form.getColorId(),
                        form.getTallaId(), form.getUnidadVentaId(), form.getClaveSatProducto(),
                        form.getPrecioVentaBase());
            } catch (DuplicadoException e) {
                br.rejectValue("codigoCombinacion", "duplicado", e.getMessage());
            } catch (ReferenciaInvalidaException e) {
                br.rejectValue("modeloId", "error", e.getMessage());
            }
        }
        if (br.hasErrors())
            return FORM;

        HtmxResponse.exito(res, "#tablaContainer", "Combinación actualizada correctamente");
        cargarListado(model, q, page, sort, dir);
        return TABLA;
    }

    @PostMapping("/{id}/estatus")
    @PreAuthorize("hasAuthority('COMBINACIONES_ESTATUS')")
    public String estatus(@PathVariable Integer id,
            @RequestParam(defaultValue = "") String q, @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "id") String sort, @RequestParam(defaultValue = "asc") String dir,
            Model model, HttpServletResponse res) {
        obtener404(id);
        Combinacion c = service.cambiarEstatus(id);
        HtmxResponse.toast(res, "Combinación " + (c.isActivo() ? "activada" : "desactivada") + " correctamente");
        cargarListado(model, q, page, sort, dir);
        return TABLA;
    }

    private Combinacion obtener404(Integer id) {
        return service.obtener(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private void cargarListado(Model model, String q, int page, String sort, String dir) {
        Page<Combinacion> pagina = service.buscar(q, page, sort, dir);
        if (pagina.getTotalPages() > 0 && page >= pagina.getTotalPages()) {
            pagina = service.buscar(q, pagina.getTotalPages() - 1, sort, dir);
        }
        String campo = pagina.getSort().stream().findFirst().map(o -> o.getProperty()).orElse("id");
        String direccion = pagina.getSort().stream().findFirst().map(o -> o.isAscending() ? "asc" : "desc")
                .orElse("asc");

        String qsQ = UriComponentsBuilder.newInstance().queryParam("q", q).build().encode().getQuery();
        String qsSort = UriComponentsBuilder.newInstance()
                .queryParam("q", q).queryParam("sort", campo).queryParam("dir", direccion)
                .build().encode().getQuery();

        model.addAttribute("pagina", pagina);
        model.addAttribute("q", q);
        model.addAttribute("sort", campo);
        model.addAttribute("dir", direccion);
        model.addAttribute("baseUrl", "/diseno/combinaciones");
        model.addAttribute("qsQ", qsQ);
        model.addAttribute("qsSort", qsSort);
    }
}