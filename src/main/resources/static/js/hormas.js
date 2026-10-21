// ============================================
// HORMAS: combo con búsqueda de tallas activas
// ============================================
(function () {
  let debounceTimer = null;
  let tallasCache = [];
  let seleccionActual = null;

  document.addEventListener("DOMContentLoaded", inicializar);
  document.addEventListener("htmx:afterSwap", (e) => {
    if (e.detail.target?.id === "modalContent") inicializar();
  });

  function inicializar() {
    const combo = document.getElementById("tallaCombo");
    if (!combo || combo.dataset.init === "1") return;
    combo.dataset.init = "1";

    const inputBuscar = document.getElementById("tallaBuscar");
    const hiddenId = document.getElementById("tallaEscalaId");
    const dropdown = document.getElementById("tallaDropdown");

    // Si venimos de edición, precargamos el texto ya viene desde Thymeleaf
    if (hiddenId.value) {
      seleccionActual = { id: hiddenId.value, texto: inputBuscar.value };
    }

    inputBuscar.addEventListener("input", () => {
      clearTimeout(debounceTimer);
      // Si el usuario borra el texto, limpiamos la selección
      if (!inputBuscar.value.trim()) {
        hiddenId.value = "";
        seleccionActual = null;
      }
      debounceTimer = setTimeout(() => buscar(inputBuscar.value.trim()), 250);
    });

    inputBuscar.addEventListener("focus", () => {
      buscar(inputBuscar.value.trim());
    });

    document.addEventListener("click", (e) => {
      if (!combo.contains(e.target)) ocultarDropdown();
    });

    function buscar(q) {
      const url = `/diseno/tallas/opciones?q=${encodeURIComponent(q)}`;
      fetch(url, { headers: { Accept: "application/json" } })
        .then((r) => (r.ok ? r.json() : []))
        .then((data) => {
          tallasCache = data || [];
          renderDropdown(tallasCache);
        })
        .catch(() => renderDropdown([]));
    }

    function renderDropdown(items) {
      dropdown.innerHTML = "";
      if (!items.length) {
        dropdown.innerHTML =
          '<div class="talla-opcion-empty">Sin resultados</div>';
        dropdown.classList.add("show");
        return;
      }
      items.forEach((t) => {
        const div = document.createElement("div");
        div.className = "talla-opcion";
        div.textContent = t.texto;
        div.addEventListener("click", () => {
          inputBuscar.value = t.texto;
          hiddenId.value = t.id;
          seleccionActual = t;
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