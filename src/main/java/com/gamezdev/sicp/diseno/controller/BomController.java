package com.gamezdev.sicp.diseno.controller;

import com.gamezdev.sicp.common.util.HtmxResponse;
import com.gamezdev.sicp.diseno.dto.BomForm;
import com.gamezdev.sicp.diseno.dto.ConsumoItemForm;
import com.gamezdev.sicp.diseno.entity.ModeloBom;
import com.gamezdev.sicp.diseno.entity.ModeloConsumo;
import com.gamezdev.sicp.diseno.service.BomService;
import com.gamezdev.sicp.diseno.service.BomService.DuplicadoException;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

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
@RequestMapping("/diseno/bom")
@RequiredArgsConstructor
public class BomController {

    private static final String VISTA = "diseno/bom";
    private static final String TABLA = "diseno/bom-tabla :: tabla";
    private static final String FORM = "diseno/bom-form :: formulario";
    private static final String REPORTE = "diseno/bom-reporte :: reporte";

    private final BomService service;

    @GetMapping
    @PreAuthorize("hasAuthority('BOM_VER')")
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
    @PreAuthorize("hasAuthority('BOM_CREAR')")
    public String nuevo(Model model) {
        BomForm form = new BomForm();
        form.setVersion(1);
        model.addAttribute("form", form);
        return FORM;
    }

    @GetMapping("/{id}/editar")
    @PreAuthorize("hasAuthority('BOM_EDITAR')")
    public String editar(@PathVariable Integer id, Model model) {
        ModeloBom bom = obtener404(id);
        BomForm form = new BomForm();
        form.setId(bom.getId());
        form.setModeloId(bom.getModelo().getId());
        form.setModeloTexto(bom.getModelo().getCodigoModelo() + " - " + bom.getModelo().getDescripcion());
        form.setVersion(bom.getVersion());
        form.setDescripcion(bom.getDescripcion());

        for (ModeloConsumo mc : bom.getConsumos()) {
            ConsumoItemForm item = new ConsumoItemForm();
            item.setMaterialId(mc.getMaterial().getId());
            item.setMaterialTexto(mc.getMaterial().getCodigo() + " - " + mc.getMaterial().getDescripcion());
            item.setPiezaCalzado(mc.getPiezaCalzado());
            item.setCantidadConsumo(mc.getCantidadConsumo());
            form.getConsumos().add(item);
        }
        model.addAttribute("form", form);
        return FORM;
    }

    @GetMapping("/{id}/reporte")
    @PreAuthorize("hasAuthority('BOM_VER')")
    public String reporte(@PathVariable Integer id,
            @RequestParam(required = false) Integer versionId,
            Model model) {
        ModeloBom bomBase = obtener404(id);
        
        // Traemos todas las versiones de este modelo
        List<ModeloBom> versiones = service.obtenerVersiones(bomBase.getModelo().getId());
        
        // Seleccionamos la versión pedida o la actual
        ModeloBom bomMostrar = bomBase;
        if (versionId != null) {
            bomMostrar = versiones.stream()
                .filter(v -> v.getId().equals(versionId))
                .findFirst().orElse(bomBase);
        }

        model.addAttribute("bom", bomMostrar);
        model.addAttribute("versiones", versiones);
        return REPORTE;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('BOM_CREAR')")
    public String crear(@Valid @ModelAttribute("form") BomForm form, BindingResult br,
            @RequestParam(defaultValue = "") String q, @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "id") String sort, @RequestParam(defaultValue = "asc") String dir,
            Model model, HttpServletResponse res) {
        form.setId(null);
        return guardarInterno(form, br, q, page, sort, dir, model, res);
    }

     @PostMapping("/{id}")
    @PreAuthorize("hasAuthority('BOM_EDITAR')")
    public String actualizar(@PathVariable Integer id,
            @Valid @ModelAttribute("form") BomForm form, BindingResult br,
            @RequestParam(defaultValue = "") String q, @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "id") String sort, @RequestParam(defaultValue = "asc") String dir,
            Model model, HttpServletResponse res) {
        obtener404(id);
        form.setId(id);
        return guardarInterno(form, br, q, page, sort, dir, model, res);
    }


    @PostMapping("/{id}/estatus")
    @PreAuthorize("hasAuthority('BOM_ESTATUS')")
    public String estatus(@PathVariable Integer id,
            @RequestParam(defaultValue = "") String q, @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "id") String sort, @RequestParam(defaultValue = "asc") String dir,
            Model model, HttpServletResponse res) {
        obtener404(id);
        ModeloBom bom = service.cambiarEstatus(id);
        HtmxResponse.toast(res, "BOM " + (bom.isActivo() ? "activado" : "desactivado") + " correctamente");
        cargarListado(model, q, page, sort, dir);
        return TABLA;
    }

    // ---------------- helpers ----------------
     private String guardarInterno(BomForm form, BindingResult br, String q, int page, String sort, String dir, Model model, HttpServletResponse res) {
        if (!br.hasErrors()) {
            try {
                service.guardar(form);
            } catch (Exception e) {
                br.reject("global", "Error al guardar: " + e.getMessage());
            }
        }
        if (br.hasErrors()) {
            return FORM;
        }
        HtmxResponse.exito(res, "#tablaContainer", "BOM guardado correctamente");
        cargarListado(model, q, page, sort, dir);
        return TABLA;
    }


    private ModeloBom obtener404(Integer id) {
        return service.obtener(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private void cargarListado(Model model, String q, int page, String sort, String dir) {
        Page<ModeloBom> pagina = service.buscar(q, page, sort, dir);
        String campo = pagina.getSort().stream().findFirst().map(o -> o.getProperty()).orElse("id");
        String direccion = pagina.getSort().stream().findFirst().map(o -> o.isAscending() ? "asc" : "desc")
                .orElse("asc");

        String qsQ = UriComponentsBuilder.newInstance().queryParam("q", q).build().encode().getQuery();
        String qsSort = UriComponentsBuilder.newInstance().queryParam("q", q).queryParam("sort", campo)
                .queryParam("dir", direccion).build().encode().getQuery();

        model.addAttribute("pagina", pagina);
        model.addAttribute("q", q);
        model.addAttribute("sort", campo);
        model.addAttribute("dir", direccion);
        model.addAttribute("baseUrl", "/diseno/bom");
        model.addAttribute("qsQ", qsQ);
        model.addAttribute("qsSort", qsSort);
    }
}