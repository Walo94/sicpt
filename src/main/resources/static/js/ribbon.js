// ============================================
// CINTA DE OPCIONES - COMPORTAMIENTO GLOBAL
// ============================================

document.addEventListener("DOMContentLoaded", () => {
  // ====== TOGGLE SUBMENÚS ======
  const btnToggle = document.getElementById("toggleSubmenus");
  const ribbon = document.querySelector(".ribbon-container");

  if (btnToggle && ribbon) {
    btnToggle.addEventListener("click", () => {
      ribbon.classList.toggle("collapsed");

      const colapsado = ribbon.classList.contains("collapsed");
      btnToggle.title = colapsado ? "Mostrar submenús" : "Ocultar submenús";
    });
  }

  // ====== BOTÓN "SALIR" DE LA CINTA -> envía el formulario POST /logout ======
  document.querySelectorAll("[data-logout]").forEach((el) => {
    el.addEventListener("click", (e) => {
      e.preventDefault();
      document.getElementById("formLogout")?.requestSubmit();
    });
  });

  // ====== BOTONES SIN PANTALLA TODAVÍA ======
  document.querySelectorAll(".ribbon-btn-pendiente").forEach((el) => {
    el.addEventListener("click", (e) => e.preventDefault());
  });

  // ====== MARCAR PESTAÑA ACTIVA SEGÚN LA URL ======
  marcarPestanaActiva();
});

function marcarPestanaActiva() {
  const path = window.location.pathname;

  // Recorre las pestañas reales (#ribbonTabs .nav-link).
  // Para cada pestaña, revisa los botones de su panel (data-bs-target)
  // y verifica si alguno tiene un href que coincida con la URL actual.
  let mejorTab = null;
  let mejor = 0;

  document.querySelectorAll("#ribbonTabs .nav-link").forEach((tabBtn) => {
    const paneSelector = tabBtn.getAttribute("data-bs-target");
    if (!paneSelector) return;

    const pane = document.querySelector(paneSelector);
    if (!pane) return;

    pane.querySelectorAll(".ribbon-btn").forEach((btn) => {
      const href = btn.getAttribute("href");
      if (!href || href === "#" || href === "/") return;
      if (path.startsWith(href) && href.length > mejor) {
        mejor = href.length;
        mejorTab = tabBtn;
      }
    });
  });

  // Si encontramos coincidencia, activamos esa pestaña.
  // Si no, dejamos la pestaña 0 (Inicio) como activa por defecto.
  const tabBtn = mejorTab || document.getElementById("tab-0");
  if (tabBtn) new bootstrap.Tab(tabBtn).show();

  resaltarBotonActual(path);
}

function resaltarBotonActual(path) {
  document
    .querySelectorAll(".ribbon-btn")
    .forEach((b) => b.classList.remove("ribbon-btn-active"));

  document.querySelectorAll(".ribbon-btn").forEach((btn) => {
    const href = btn.getAttribute("href");
    if (href && href !== "#" && href !== "/" && path.startsWith(href)) {
      btn.classList.add("ribbon-btn-active");
    }
  });
}
