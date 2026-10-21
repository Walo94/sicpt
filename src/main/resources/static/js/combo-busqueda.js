// ============================================
// COMBO CON BÚSQUEDA GENÉRICO
// Uso: <div class="combo-busqueda" id="unidadCombo"
//        data-url="/catalogos/unidades/opciones">
// Comportamiento: solo busca cuando hay texto escrito.
// ============================================
(function () {
  document.addEventListener("DOMContentLoaded", inicializar);
  document.addEventListener("htmx:afterSwap", (e) => {
    if (e.detail.target?.id === "modalContent") inicializar();
  });

  function inicializar() {
    document.querySelectorAll(".combo-busqueda").forEach(setupCombo);
  }

  function setupCombo(combo) {
    if (combo.dataset.init === "1") return;
    combo.dataset.init = "1";

    const inputBuscar = combo.querySelector("input[type=text]");
    const hiddenId = combo.querySelector("input[type=hidden]");
    const dropdown = combo.querySelector(".combo-busqueda-dropdown");
    const urlBase = combo.dataset.url;
    if (!inputBuscar || !hiddenId || !dropdown || !urlBase) return;

    let debounceTimer = null;

    inputBuscar.addEventListener("input", () => {
      clearTimeout(debounceTimer);
      const q = inputBuscar.value.trim();

      // Al borrar todo, limpiamos selección y ocultamos dropdown
      if (!q) {
        hiddenId.value = "";
        ocultarDropdown();
        return;
      }
      debounceTimer = setTimeout(() => buscar(q), 250);
    });

    // Ya NO abrimos con lista completa al enfocar.
    // Solo reabrimos si ya hay texto y resultados previos.
    inputBuscar.addEventListener("focus", () => {
      const q = inputBuscar.value.trim();
      if (q) {
        buscar(q);
      }
    });

    // Cerrar al hacer clic fuera
    document.addEventListener("click", (e) => {
      if (!combo.contains(e.target)) ocultarDropdown();
    });

    // Cerrar con Escape
    inputBuscar.addEventListener("keydown", (e) => {
      if (e.key === "Escape") ocultarDropdown();
    });

    function buscar(q) {
      const url = `${urlBase}?q=${encodeURIComponent(q)}`;
      fetch(url, { headers: { Accept: "application/json" } })
        .then((r) => (r.ok ? r.json() : []))
        .then((items) => renderDropdown(items, q))
        .catch(() => renderDropdown([], q));
    }

    function renderDropdown(items, q) {
      dropdown.innerHTML = "";
      if (!items.length) {
        // "Sin resultados" solo si el usuario escribió algo
        if (q) {
          dropdown.innerHTML =
            '<div class="combo-opcion-empty">Sin resultados</div>';
          dropdown.classList.add("show");
        } else {
          ocultarDropdown();
        }
        return;
      }
      items.forEach((t) => {
        const div = document.createElement("div");
        div.className = "combo-opcion";
        div.textContent = t.texto;
        div.addEventListener("click", () => {
          inputBuscar.value = t.texto;
          hiddenId.value = t.id;
          ocultarDropdown();
        });
        dropdown.appendChild(div);
      });
      dropdown.classList.add("show");
    }

    function ocultarDropdown() {
      dropdown.classList.remove("show");
    }
  }
})();