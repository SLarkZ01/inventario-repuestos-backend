package com.repobackend.api.factura.service;

import com.repobackend.api.factura.model.Factura;
import com.repobackend.api.factura.model.FacturaItem;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

// Para un MVP: generamos un PDF sencillo con OpenHTMLToPDF (renderer de HTML a PDF) y, si falla, usamos PDFBox por reflexión como fallback.
@Service
public class FacturaPdfService {
    private final SpringTemplateEngine templateEngine;

    public FacturaPdfService(SpringTemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public byte[] renderFacturaPdf(Factura f) {
        try {
            // 1) Construir el modelo Thymeleaf
            Context ctx = new Context();
            ctx.setVariables(Map.ofEntries(
                Map.entry("empresaNombre", "Hermotos"),
                Map.entry("empresaNit", "NIT 900.000.000-1"),
                Map.entry("empresaDireccion", "Timbio - Colombia"),
                Map.entry("numero", f.getNumeroFactura() == null ? f.getId() : f.getNumeroFactura()),
                Map.entry("fecha", f.getCreadoEn()),
                Map.entry("estado", f.getEstado() == null ? "EMITIDA" : String.valueOf(f.getEstado())),
                Map.entry("cliente", mapCliente(f)),
                Map.entry("subtotal", fmt(f.getSubtotal())),
                Map.entry("descuentos", fmt(f.getTotalDescuentos())),
                Map.entry("base", fmt(f.getBaseImponible())),
                Map.entry("iva", fmt(f.getTotalIva())),
                Map.entry("total", fmt(f.getTotal())),
                Map.entry("items", mapItems(f))
            ));

            String html = templateEngine.process("factura", ctx);

            // 2) Convertir HTML -> PDF con OpenHTMLToPDF
            try {
                Class<?> builderClass = Class.forName("com.openhtmltopdf.pdfboxout.PdfRendererBuilder");
                Object builder = builderClass.getDeclaredConstructor().newInstance();
                Method withHtmlContent = builderClass.getMethod("withHtmlContent", String.class, String.class);
                Method toStream = builderClass.getMethod("toStream", OutputStream.class);
                Method useFastMode = null;
                try { useFastMode = builderClass.getMethod("useFastMode"); } catch (NoSuchMethodException ignore) {}
                if (useFastMode != null) useFastMode.invoke(builder);
                Method run = builderClass.getMethod("run");
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                withHtmlContent.invoke(builder, html, null);
                toStream.invoke(builder, baos);
                run.invoke(builder);
                byte[] out = baos.toByteArray();
                if (isPdf(out)) return out;
                return renderFallbackPdfBox(f);
            } catch (ClassNotFoundException noLib) {
                return renderFallbackPdfBox(f);
            }
        } catch (Exception ex) {
            try { return renderFallbackPdfBox(f); } catch (Exception ignore) {}
            return minimalPdfHeader();
        }
    }

    private List<Map<String, Object>> mapItems(Factura f) {
        java.util.ArrayList<Map<String, Object>> list = new java.util.ArrayList<>();
        if (f.getItems() != null) {
            for (FacturaItem it : f.getItems()) {
                String nombre = it.getNombreProducto() != null ? it.getNombreProducto() : it.getProductoId();
                list.add(Map.of(
                    "nombre", safe(nombre),
                    "cantidad", it.getCantidad() == null ? 0 : it.getCantidad(),
                    "precio", fmt(it.getPrecioUnitario()),
                    "subtotal", fmt(it.getSubtotal()),
                    "tasaIva", fmt(it.getTasaIva()),
                    "valorIva", fmt(it.getValorIva()),
                    "total", fmt(it.getTotalItem())
                ));
            }
        }
        return list;
    }

    private Map<String, Object> mapCliente(Factura f) {
        if (f.getCliente() == null) {
            // Cliente vacío con valores por defecto
            return Map.of(
                "id", "",
                "username", "",
                "email", "",
                "nombre", "Cliente General",
                "apellido", "",
                "fechaCreacion", null
            );
        }
        var c = f.getCliente();
        return Map.of(
            "id", safe(c.getId()),
            "username", safe(c.getUsername()),
            "email", safe(c.getEmail()),
            "nombre", safe(c.getNombre()),
            "apellido", safe(c.getApellido()),
            "fechaCreacion", c.getFechaCreacion()
        );
    }

    private boolean isPdf(byte[] bytes) {
        return bytes != null && bytes.length > 4 && bytes[0] == 0x25 && bytes[1] == 0x50 && bytes[2] == 0x44 && bytes[3] == 0x46;
    }

    private byte[] renderFallbackPdfBox(Factura f) throws Exception {
        Class<?> docClazz = Class.forName("org.apache.pdfbox.pdmodel.PDDocument");
        Class<?> pageClazz = Class.forName("org.apache.pdfbox.pdmodel.PDPage");
        Class<?> contentClazz = Class.forName("org.apache.pdfbox.pdmodel.PDPageContentStream");
        Class<?> fontClazz = Class.forName("org.apache.pdfbox.pdmodel.font.PDType1Font");

        Object document = docClazz.getDeclaredConstructor().newInstance();
        Object page = pageClazz.getDeclaredConstructor().newInstance();
        docClazz.getMethod("addPage", pageClazz).invoke(document, page);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Object cs = contentClazz.getDeclaredConstructor(docClazz, pageClazz).newInstance(document, page);
        Method beginText = contentClazz.getMethod("beginText");
        Method setFont = contentClazz.getMethod("setFont", Class.forName("org.apache.pdfbox.pdmodel.font.PDFont"), float.class);
        Method newLineAtOffset = contentClazz.getMethod("newLineAtOffset", float.class, float.class);
        Method showText = contentClazz.getMethod("showText", String.class);
        Method endText = contentClazz.getMethod("endText");
        Method close = contentClazz.getMethod("close");

        beginText.invoke(cs);
        Object helveticaBold = fontClazz.getField("HELVETICA_BOLD").get(null);
        setFont.invoke(cs, helveticaBold, 14f);
        newLineAtOffset.invoke(cs, 50f, 750f);
        showText.invoke(cs, "Factura " + (f.getNumeroFactura() == null ? f.getId() : f.getNumeroFactura()));
        endText.invoke(cs);
        close.invoke(cs);

        docClazz.getMethod("save", OutputStream.class).invoke(document, baos);
        docClazz.getMethod("close").invoke(document);
        return baos.toByteArray();
    }

    private String safe(String s) { return s == null ? "" : s; }
    private String fmt(Double d) { return d == null ? "0.00" : String.format(java.util.Locale.US, "%.2f", d); }
    private byte[] minimalPdfHeader() { return new byte[]{0x25,0x50,0x44,0x46,0x2D,0x31,0x2E,0x34}; }
}
