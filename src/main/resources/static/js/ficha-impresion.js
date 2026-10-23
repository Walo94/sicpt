function imprimirContenido(html) {
  const iframe = document.createElement("iframe");
  iframe.style.cssText = "position:fixed;right:0;bottom:0;width:0;height:0;border:0;";
  document.body.appendChild(iframe);

  const doc = iframe.contentWindow.document;
  doc.open();
  doc.write(`
    <html>
      <head>
        <title>Ficha Técnica</title>
        <style>
          body { font-family: Arial, sans-serif; font-size: 12px; padding: 20px; -webkit-print-color-adjust: exact; print-color-adjust: exact; }
          .ficha-tabla { width: 100%; border-collapse: collapse; margin-bottom: 20px; }
          .ficha-tabla th, .ficha-tabla td { border: 1px solid #000; padding: 6px; vertical-align: top; }
          .ficha-tabla .bg-gray { background-color: #f8f9fa; font-weight: bold; }
          .text-center { text-align: center; }
          .text-end { text-align: right; }
          .text-primary { color: #2c5f8a; }
          .table { width: 100%; border-collapse: collapse; margin-bottom: 1rem; }
          .table th, .table td { padding: 0.5rem; border: 1px solid #dee2e6; }
          .table-light th { background-color: #f8f9fa; }
          .badge { padding: 3px 6px; border-radius: 4px; color: white; font-size: 10px; }
          .bg-success { background-color: #198754 !important; }
          .bg-warning { background-color: #ffc107 !important; color: black !important; }
          .alert-warning { background-color: #fff3cd !important; color: #664d03 !important; padding: 10px; border: 1px solid #ffecb5; }
          .ficha-page-break { page-break-after: always; }
          .ficha-page-break:last-child { page-break-after: auto; }
        </style>
      </head>
      <body>${html}</body>
    </html>
  `);
  doc.close();

  // Espera a que carguen las imágenes (en vez de un setTimeout fijo)
  const pendientes = Array.from(doc.images).map(img =>
    img.complete ? Promise.resolve()
                 : new Promise(r => { img.onload = img.onerror = r; })
  );

  Promise.all(pendientes).then(() => {
    iframe.contentWindow.onafterprint = () => iframe.remove();
    iframe.contentWindow.focus();
    iframe.contentWindow.print();
    setTimeout(() => iframe.remove(), 60000); // respaldo por si afterprint no se dispara
  });
}

// Cuando HTMX termina de poner la ficha en el contenedor oculto → imprimir
document.addEventListener("htmx:afterSwap", (e) => {
  const host = e.detail.target;
  if (host?.id !== "printHost") return;
  imprimirContenido(host.innerHTML);
  host.innerHTML = "";
});

document.addEventListener("htmx:responseError", (e) => {
  if (e.detail.target?.id === "printHost") {
    alert("No se pudo generar la ficha técnica.");
  }
});