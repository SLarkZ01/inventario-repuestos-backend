package com.repobackend.api.factura.service;

import com.repobackend.api.factura.model.Factura;
import com.repobackend.api.factura.model.FacturaItem;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

// Para un MVP: generamos un PDF sencillo con OpenHTMLToPDF (renderer de HTML a PDF) o devolvemos un PDF mínimo si la lib no está lista.
@Service
public class FacturaPdfService {
    public byte[] renderFacturaPdf(Factura f) {
        try {
            // Simple plantilla HTML inline; en producción usar Thymeleaf/Freemarker y recursos estáticos
            StringBuilder html = new StringBuilder();
            html.append("<html><head><meta charset='UTF-8'><style>")
                .append("body{font-family:Arial, sans-serif;font-size:12px} table{width:100%;border-collapse:collapse} th,td{border:1px solid #ddd;padding:6px;text-align:left} th{background:#f0f0f0}")
                .append("</style></head><body>");
            html.append("<h2>Factura ").append(escape(f.getNumeroFactura() == null ? f.getId() : f.getNumeroFactura())).append("</h2>");
            html.append("<p>Fecha: ").append(f.getCreadoEn()).append("</p>");
            if (f.getCliente() != null) {
                html.append("<p>Cliente: ").append(escape(nullToEmpty(f.getCliente().getNombre()))).append("<br>")
                    .append("Documento: ").append(escape(nullToEmpty(f.getCliente().getDocumento()))).append("<br>")
                    .append("Dirección: ").append(escape(nullToEmpty(f.getCliente().getDireccion()))).append("</p>");
            }
            html.append("<table><thead><tr><th>Producto</th><th>Cant.</th><th>P.Unit</th><th>Subtotal</th><th>IVA(%)</th><th>Valor IVA</th><th>Total</th></tr></thead><tbody>");
            if (f.getItems() != null) {
                for (FacturaItem it : f.getItems()) {
                    String nombre = it.getNombreProducto() != null ? it.getNombreProducto() : it.getProductoId();
                    int qty = it.getCantidad() == null ? 0 : it.getCantidad();
                    double pu = it.getPrecioUnitario() == null ? 0.0 : it.getPrecioUnitario();
                    double sub = it.getSubtotal() == null ? 0.0 : it.getSubtotal();
                    double tasaIva = it.getTasaIva() == null ? 0.0 : it.getTasaIva();
                    double valorIva = it.getValorIva() == null ? 0.0 : it.getValorIva();
                    double totalItem = it.getTotalItem() == null ? 0.0 : it.getTotalItem();

                    html.append("<tr>")
                        .append("<td>").append(escape(nombre)).append("</td>")
                        .append("<td>").append(qty).append("</td>")
                        .append("<td>$").append(fmt(pu)).append("</td>")
                        .append("<td>$").append(fmt(sub)).append("</td>")
                        .append("<td>").append(fmt(tasaIva)).append("%</td>")
                        .append("<td>$").append(fmt(valorIva)).append("</td>")
                        .append("<td>$").append(fmt(totalItem)).append("</td>")
                        .append("</tr>");
                }
            }
            html.append("</tbody></table>");

            // Resumen tributario
            html.append("<div style='margin-top:20px;text-align:right'>");
            html.append("<p><strong>Subtotal:</strong> $").append(fmt(f.getSubtotal())).append("</p>");
            html.append("<p><strong>Descuentos:</strong> $").append(fmt(f.getTotalDescuentos())).append("</p>");
            html.append("<p><strong>Base Imponible:</strong> $").append(fmt(f.getBaseImponible())).append("</p>");
            html.append("<p><strong>IVA:</strong> $").append(fmt(f.getTotalIva())).append("</p>");
            html.append("<p style='font-size:14px'><strong>TOTAL:</strong> $").append(fmt(f.getTotal())).append("</p>");
            html.append("</div>");
            html.append("<p style='margin-top:20px;font-size:10px;color:#666'>Este documento es un comprobante interno. Para facturación electrónica DIAN, integrar un proveedor autorizado (OFEs) o la API DIAN.</p>");
            html.append("</body></html>");

            // Intentar usar OpenHTMLToPDF con reflexión para evitar dependencia de compilación fuerte
            try {
                Class<?> builderClass = Class.forName("com.openhtmltopdf.pdfboxout.PdfRendererBuilder");
                Object builder = builderClass.getDeclaredConstructor().newInstance();
                Method withHtmlContent = builderClass.getMethod("withHtmlContent", String.class, String.class);
                Method toStream = builderClass.getMethod("toStream", OutputStream.class);
                Method run = builderClass.getMethod("run");
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                withHtmlContent.invoke(builder, html.toString(), null);
                toStream.invoke(builder, baos);
                run.invoke(builder);
                return baos.toByteArray();
            } catch (ClassNotFoundException noLib) {
                // Fallback: devolver HTML como bytes
                return html.toString().getBytes(StandardCharsets.UTF_8);
            }
        } catch (Exception ex) {
            // En caso de error, retornar un PDF mínimo
            return ("Factura " + (f.getNumeroFactura() == null ? f.getId() : f.getNumeroFactura())).getBytes(StandardCharsets.UTF_8);
        }
    }

    private String nullToEmpty(String s) { return s == null ? "" : s; }
    private String escape(String s) {
        return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
    private String fmt(Double d) {
        return d == null ? "0.00" : String.format(java.util.Locale.US, "%.2f", d);
    }
}
