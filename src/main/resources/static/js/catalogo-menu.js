// ============================================
// MENÚ CONTEXTUAL GENÉRICO PARA CATÁLOGOS HTMX
// Requiere: #tablaContainer, #menuContextual[data-base], #buscador, #estadoTabla
// ============================================
(function () {
  const container = document.getElementById("tablaContainer");
  const menu = document.getElementById("menuContextual");
  const base = menu.dataset.base;
  let idSeleccionado = null;

  // Estado actual de la tabla (q, page, sort, dir) para conservarlo tras cada acción
  function estadoTabla() {
    const vals = { q: document.getElementById("buscador").value.trim() };
    document
      .querySelectorAll("#estadoTabla input")
      .forEach((i) => (vals[i.name] = i.value));
    return vals;
  }

  function cerrarMenu() {
    menu.classList.remove("show");
  }

  // Delegación: el contenido de #tablaContainer se reemplaza con cada petición
  container.addEventListener("contextmenu", (e) => {
    const tr = e.target.closest("tr[data-id]");
    if (!tr || !menu.querySelector(".menu-contextual-item")) return;
    e.preventDefault();

    container
      .querySelectorAll("tr.selected")
      .forEach((r) => r.classList.remove("selected"));
    tr.classList.add("selected");
    idSeleccionado = tr.dataset.id;

    const icon = document.getElementById("iconEstatus");
    const texto = document.getElementById("textoEstatus");
    if (icon && texto) {
      const activo = tr.dataset.estatus === "1";
      icon.className = activo
        ? "fas fa-toggle-off text-danger"
        : "fas fa-toggle-on text-success";
      texto.textContent = activo ? "Dar de baja" : "Dar de alta";
    }

    menu.style.left = e.pageX + "px";
    menu.style.top = e.pageY + "px";
    menu.classList.add("show");
  });

  menu.addEventListener("click", (e) => {
    e.preventDefault();
    const item = e.target.closest(".menu-contextual-item");
    if (!item || !idSeleccionado) return;

    if (item.dataset.action === "editar") {
      htmx.ajax("GET", `${base}/${idSeleccionado}/editar`, {
        target: "#modalContent",
        swap: "innerHTML",
      });
    } else if (item.dataset.action === "estatus") {
      htmx.ajax("POST", `${base}/${idSeleccionado}/estatus`, {
        target: "#tablaContainer",
        swap: "innerHTML",
        values: estadoTabla(),
      });
    }
    else if (item.dataset.action === 'reporte') {
    const baseUrl = menu.getAttribute("data-base");
    // Asumimos que la URL del reporte es /base/{id}/reporte
    htmx.ajax('GET', `${baseUrl}/${idSeleccionado}/reporte`, { target: '#modalContent', swap: 'innerHTML' });
}
    cerrarMenu();
  });

  document.addEventListener("click", (e) => {
    if (!menu.contains(e.target)) cerrarMenu();
  });
  document.addEventListener(
    "keydown",
    (e) => e.key === "Escape" && cerrarMenu(),
  );
  window.addEventListener("scroll", cerrarMenu, true);
})();
