package com.gamezdev.sicp.common.util;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.ui.Model;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.function.IntFunction;

public class ListadoHelper {

    private ListadoHelper() {
    }

    /** Si la página pedida ya no existe (p. ej. tras filtrar), carga la última. */
    public static <T> Page<T> ajustar(Page<T> pagina, int page, IntFunction<Page<T>> loader) {
        if (pagina.getTotalPages() > 0 && page >= pagina.getTotalPages()) {
            return loader.apply(pagina.getTotalPages() - 1);
        }
        return pagina;
    }

    public static void cargar(Model model, Page<?> pagina, String baseUrl, String q) {
        Sort.Order o = pagina.getSort().stream().findFirst().orElse(Sort.Order.asc("id"));
        String campo = o.getProperty();
        String dir = o.isAscending() ? "asc" : "desc";

        model.addAttribute("pagina", pagina);
        model.addAttribute("q", q);
        model.addAttribute("sort", campo);
        model.addAttribute("dir", dir);
        model.addAttribute("baseUrl", baseUrl);
        model.addAttribute("qsQ", UriComponentsBuilder.newInstance()
                .queryParam("q", q).build().encode().getQuery());
        model.addAttribute("qsSort", UriComponentsBuilder.newInstance()
                .queryParam("q", q).queryParam("sort", campo).queryParam("dir", dir)
                .build().encode().getQuery());
    }
}
