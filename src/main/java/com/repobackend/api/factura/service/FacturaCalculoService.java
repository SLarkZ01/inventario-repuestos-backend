package com.repobackend.api.factura.service;

import com.repobackend.api.factura.model.Factura;
import com.repobackend.api.factura.model.FacturaItem;
import com.repobackend.api.producto.model.Producto;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio para cálculos tributarios de facturas.
 * Centraliza toda la lógica de IVA, descuentos y totales para DIAN.
 */
@Service
public class FacturaCalculoService {

    /**
     * Calcula y establece todos los totales tributarios de una factura.
     * Recalcula desde los items para asegurar consistencia.
     */
    public void calcularTotales(Factura factura) {
        if (factura.getItems() == null || factura.getItems().isEmpty()) {
            factura.setSubtotal(0.0);
            factura.setTotalDescuentos(0.0);
            factura.setBaseImponible(0.0);
            factura.setTotalIva(0.0);
            factura.setTotal(0.0);
            return;
        }

        double subtotal = 0.0;
        double totalDescuentos = 0.0;
        double totalIva = 0.0;

        for (FacturaItem item : factura.getItems()) {
            calcularItem(item);
            subtotal += nvl(item.getSubtotal());
            totalDescuentos += nvl(item.getDescuento());
            totalIva += nvl(item.getValorIva());
        }

        double baseImponible = subtotal - totalDescuentos;
        double total = baseImponible + totalIva;

        factura.setSubtotal(subtotal);
        factura.setTotalDescuentos(totalDescuentos);
        factura.setBaseImponible(baseImponible);
        factura.setTotalIva(totalIva);
        factura.setTotal(total);
    }

    /**
     * Calcula los valores de un item individual (subtotal, IVA, total).
     */
    public void calcularItem(FacturaItem item) {
        int cantidad = nvl(item.getCantidad(), 0);
        double precioUnitario = nvl(item.getPrecioUnitario());
        double descuento = nvl(item.getDescuento());
        double tasaIva = nvl(item.getTasaIva());

        // Subtotal antes de descuento
        double subtotalBruto = cantidad * precioUnitario;

        // Aplicar descuento
        double subtotal = subtotalBruto - descuento;

        // Base imponible (igual al subtotal en este modelo simple)
        double baseImponible = subtotal;

        // Calcular IVA
        double valorIva = baseImponible * (tasaIva / 100.0);

        // Total del item
        double totalItem = baseImponible + valorIva;

        item.setSubtotal(subtotal);
        item.setBaseImponible(baseImponible);
        item.setValorIva(valorIva);
        item.setTotalItem(totalItem);
    }

    /**
     * Construye un FacturaItem desde un Producto con cantidad y descuento opcional.
     * Usa precio y tasa IVA del producto.
     */
    public FacturaItem construirItemDesdeProducto(Producto producto, int cantidad, Double descuentoOpcional) {
        if (producto == null) throw new IllegalArgumentException("Producto no puede ser null");
        if (cantidad <= 0) throw new IllegalArgumentException("Cantidad debe ser mayor a 0");

        FacturaItem item = new FacturaItem();
        item.setProductoId(producto.getId());
        item.setNombreProducto(producto.getNombre());
        item.setCodigoProducto(producto.getIdString()); // usar idString como código
        item.setCantidad(cantidad);
        item.setPrecioUnitario(nvl(producto.getPrecio()));
        item.setDescuento(nvl(descuentoOpcional));
        item.setTasaIva(nvl(producto.getTasaIva(), 19.0)); // por defecto 19% si no está definido

        calcularItem(item);
        return item;
    }

    /**
     * Valida que el total proporcionado coincida con el calculado (tolerancia de 0.01).
     */
    public void validarTotal(Factura factura, Double totalProporcionado) {
        if (totalProporcionado == null) return;
        calcularTotales(factura);
        double calculado = nvl(factura.getTotal());
        if (Math.abs(totalProporcionado - calculado) > 0.01) {
            throw new IllegalArgumentException(
                String.format("Total proporcionado (%.2f) no coincide con total calculado (%.2f)",
                    totalProporcionado, calculado)
            );
        }
    }

    private double nvl(Double val) {
        return val == null ? 0.0 : val;
    }

    private double nvl(Double val, double defaultVal) {
        return val == null ? defaultVal : val;
    }

    private int nvl(Integer val, int defaultVal) {
        return val == null ? defaultVal : val;
    }
}

