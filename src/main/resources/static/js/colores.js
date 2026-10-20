// ============================================
// CATÁLOGO DE COLORES: editor de paleta
// ============================================
(function () {
  let colores = []; // [{nombre, hex}]

  document.addEventListener("DOMContentLoaded", inicializar);
  // También cuando HTMX inyecta el modal
  document.addEventListener("htmx:afterSwap", (e) => {
    if (e.detail.target?.id === "modalContent") inicializar();
  });

  function inicializar() {
    const form = document.getElementById("formColor");
    if (!form || form.dataset.init === "1") return;
    form.dataset.init = "1";

    const hidden = document.getElementById("colorJson");
    colores = parseJsonSeguro(hidden?.value);
    renderLista();

    const btn = document.getElementById("btnAgregarColor");
    const nombre = document.getElementById("colorNombre");
    const hexColor = document.getElementById("colorHex");
    const hexText = document.getElementById("colorHexText");

    // Sincronizar input color <-> input text hex
    hexColor.addEventListener(
      "input",
      () => (hexText.value = hexColor.value.toUpperCase()),
    );
    hexText.addEventListener("input", () => {
      const v = hexText.value.trim();
      if (/^#([0-9a-fA-F]{6}|[0-9a-fA-F]{3})$/.test(v))
        hexColor.value = normalizarHex(v);
    });

    btn.addEventListener("click", () => {
      const n = (nombre.value || "").trim();
      const h = (hexText.value || "").trim();
      if (!n) return mostrarToast("Escribe el nombre del color.", "danger");
      if (!/^#([0-9a-fA-F]{6}|[0-9a-fA-F]{3})$/.test(h))
        return mostrarToast("El código hexadecimal no es válido.", "danger");
      if (colores.length >= 5)
        return mostrarToast("Máximo 5 colores.", "danger");
      colores.push({ nombre: n, hex: h.toUpperCase() });
      nombre.value = "";
      renderLista();
      nombre.focus();
    });

    // Enter en los inputs agrega
    [nombre, hexText].forEach((el) =>
      el.addEventListener("keydown", (e) => {
        if (e.key === "Enter") {
          e.preventDefault();
          btn.click();
        }
      }),
    );

    // Al enviar el form, serializar el JSON
    form.addEventListener("submit", () => {
      hidden.value = colores.length ? JSON.stringify({ colores }) : "";
    });
  }

  function renderLista() {
    const cont = document.getElementById("listaColores");
    if (!cont) return;
    cont.innerHTML = "";
    if (!colores.length) {
      cont.innerHTML =
        '<span class="text-muted small">Sin colores configurados.</span>';
      return;
    }
    colores.forEach((c, idx) => {
      const chip = document.createElement("div");
      chip.className = "color-chip";
      chip.innerHTML = `
        <span class="color-swatch-big" style="background:${c.hex}"></span>
        <span class="color-chip-nombre"></span>
        <span class="color-chip-hex"></span>
        <button type="button" class="btn-close btn-close-sm ms-2" aria-label="Quitar"></button>
      `;
      chip.querySelector(".color-chip-nombre").textContent = c.nombre;
      chip.querySelector(".color-chip-hex").textContent = c.hex;
      chip.querySelector("button").addEventListener("click", () => {
        colores.splice(idx, 1);
        renderLista();
      });
      cont.appendChild(chip);
    });
  }

  function parseJsonSeguro(str) {
    if (!str) return [];
    try {
      const o = JSON.parse(str);
      return Array.isArray(o.colores) ? o.colores : [];
    } catch {
      return [];
    }
  }

  function normalizarHex(h) {
    h = h.toUpperCase();
    if (h.length === 4) {
      // #abc -> #AABBCC
      return "#" + h[1] + h[1] + h[2] + h[2] + h[3] + h[3];
    }
    return h;
  }
})();
