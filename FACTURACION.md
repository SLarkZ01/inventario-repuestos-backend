# Sistema de Facturación Robusto con IVA

## 📋 Resumen de Cambios

Se ha reforzado completamente el sistema de facturación para garantizar:

✅ **Descuento de stock OBLIGATORIO** - No hay forma de crear facturas sin descontar stock  
✅ **Cálculo automático de IVA** - Toma tasas de IVA desde productos  
✅ **Precios desde backend** - Ignora precios del cliente, usa siempre precios de productos  
✅ **Validación de stock** - Valida suficiencia ANTES de crear factura  
✅ **Estados de factura** - BORRADOR, EMITIDA, ANULADA  
✅ **Listo para DIAN** - Campos preparados para integración futura  

## 🔧 Arquitectura

### Nuevos Servicios

1. **FacturaCalculoService** - Calcula totales, IVA, descuentos
2. **FacturaServiceV2** - Lógica robusta de facturación
3. **FacturaPdfService** - Genera PDF con desglose tributario

### Modelos Ampliados

**Factura** ahora incluye:
- Campos tributarios: subtotal, baseImponible, totalIva, totalDescuentos
- Resolución DIAN: prefijo, resolucionDian, fechaResolucion, rangoAutorizado
- Estados: BORRADOR, EMITIDA, ACEPTADA, RECHAZADA, ANULADA
- Campos DIAN: cufe, qrCode, xmlUrl, pdfUrl

**FacturaItem** ahora incluye:
- nombreProducto, codigoProducto (snapshots)
- descuento, baseImponible, tasaIva, valorIva
- subtotal, totalItem (calculados)

**Producto** ahora incluye:
- tasaIva (0, 5, 19, etc.)

## 🚀 Endpoints

### Crear Factura EMITIDA (uso principal)

```http
POST /api/facturas
Content-Type: application/json
Authorization: Bearer <token>

{
  "clienteId": "507f1f77bcf86cd799439011",
  "items": [
    {
      "productoId": "507f191e810c19729de860ea",
      "cantidad": 5
    }
  ]
}
```

**Comportamiento:**
- ✅ Toma precio y tasa IVA del producto automáticamente
- ✅ Valida stock suficiente
- ✅ Descuenta stock por almacén
- ✅ Calcula subtotal, IVA, total
- ✅ Crea movimientos de stock
- ✅ Estado: EMITIDA

### Crear Borrador (cotizaciones)

```http
POST /api/facturas/borrador
```

No descuenta stock. Útil para cotizaciones o pre-facturas.

### Emitir Borrador

```http
POST /api/facturas/{id}/emitir
```

Convierte borrador a EMITIDA y descuenta stock.

### Checkout de Carrito

```http
POST /api/facturas/checkout
{
  "carritoId": "507f1f77bcf86cd799439999"
}
```

Convierte carrito en factura EMITIDA, descuenta stock, vacía carrito.

### Anular Factura

```http
POST /api/facturas/{id}/anular
{
  "motivo": "Error en datos del cliente"
}
```

**IMPORTANTE:** NO devuelve stock automáticamente. Requiere ajuste manual.

### Descargar PDF

```http
GET /api/facturas/{id}/pdf
```

Genera PDF con desglose completo de IVA.

## 🔒 Garantías del Sistema

### 1. Stock SIEMPRE se descuenta

```java
// ✅ CORRECTO - No hay forma de omitir
POST /api/facturas  // Descuenta stock

// ❌ NO EXISTE - No hay endpoint sin descuento
// El único caso que no descuenta es BORRADOR
```

### 2. Precios desde Productos

```java
// Cliente envía:
{
  "items": [{
    "productoId": "abc123",
    "cantidad": 5,
    "precioUnitario": 99999  // ❌ IGNORADO
  }]
}

// Sistema usa:
Producto.getPrecio()  // ✅ Precio real del producto
Producto.getTasaIva() // ✅ Tasa IVA del producto (ej: 19%)
```

### 3. Validación de Stock

```java
// Si producto tiene 10 unidades y pides 15:
throw new IllegalStateException(
  "Stock insuficiente para producto abc123 (faltan 5 unidades)"
); // HTTP 409
```

### 4. Cálculo Automático

```java
// Ejemplo: Producto $100, IVA 19%, Cantidad 5

item.setSubtotal(500.00);         // 5 × $100
item.setTasaIva(19.0);            // Del producto
item.setValorIva(95.00);          // $500 × 19%
item.setTotalItem(595.00);        // $500 + $95

factura.setSubtotal(500.00);
factura.setBaseImponible(500.00);
factura.setTotalIva(95.00);
factura.setTotal(595.00);
```

## 📊 Estados de Factura

| Estado | Descuenta Stock | Editable | Puede Emitirse | Puede Anularse |
|--------|----------------|----------|----------------|----------------|
| **BORRADOR** | ❌ No | ✅ Sí | ✅ Sí | ❌ No |
| **EMITIDA** | ✅ Sí | ❌ No | - | ✅ Sí |
| **ANULADA** | - | ❌ No | ❌ No | - |

## 🏗️ Preparación para DIAN

### Campos Ya Listos

```java
factura.setPrefijo("SETT");
factura.setResolucionDian("18764005714521");
factura.setFechaResolucion(new Date("2024-01-01"));
factura.setRangoAutorizado("del 1 al 5000");
```

### Pendientes (integración OFE)

1. Generación de UBL 2.1 XML
2. Firma digital (XAdES)
3. Cálculo de CUFE
4. Generación de QR
5. Transmisión a DIAN
6. Almacenamiento de XML/PDF oficiales

### Recomendación

Integrar un **OFE** (Operador de Facturación Electrónica) como:
- The Factory HKA
- Carvajal
- Siigo
- Alegra

El backend envía la factura estructurada → OFE devuelve CUFE, XML, PDF oficial.

## 🔄 Migración desde Sistema Antiguo

Si tenías el servicio anterior (`FacturaService`), ahora usa `FacturaServiceV2`.

### Cambios Breaking

| Antes | Ahora |
|-------|-------|
| `POST /api/facturas/dto?descontarStock=true` | `POST /api/facturas` (siempre descuenta) |
| Acepta `precioUnitario` del cliente | Ignora y usa precio del producto |
| Calcula total simple | Calcula subtotal + IVA |
| No valida stock | Valida ANTES de crear |

### Cómo Migrar Frontend

```typescript
// ❌ Antes (Next.js)
await fetch('/api/facturas/dto?descontarStock=true', {
  method: 'POST',
  body: JSON.stringify({
    items: [{
      productoId: 'abc',
      cantidad: 5,
      precioUnitario: 100 // ❌ Ya no se usa
    }]
  })
});

// ✅ Ahora
await fetch('/api/facturas', {
  method: 'POST',
  body: JSON.stringify({
    clienteId: '507f...',
    items: [{
      productoId: 'abc',
      cantidad: 5
      // ✅ Precio e IVA se toman automáticamente
    }]
  })
});

// Respuesta incluye desglose completo
{
  "factura": {
    "numeroFactura": "1",
    "estado": "EMITIDA",
    "subtotal": 500.00,
    "totalIva": 95.00,
    "total": 595.00,
    "items": [{
      "nombreProducto": "Filtro de aceite",
      "cantidad": 5,
      "precioUnitario": 100.00,
      "tasaIva": 19.0,
      "valorIva": 95.00,
      "totalItem": 595.00
    }]
  }
}
```

## 🧪 Testing

### Caso 1: Factura Simple

```bash
curl -X POST http://localhost:8080/api/facturas \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "clienteId": "507f1f77bcf86cd799439011",
    "items": [
      {"productoId": "507f191e810c19729de860ea", "cantidad": 2}
    ]
  }'
```

### Caso 2: Stock Insuficiente (409)

```bash
# Si producto tiene stock = 5
curl -X POST http://localhost:8080/api/facturas \
  -d '{"items": [{"productoId": "abc", "cantidad": 10}]}'
  
# Response: 409 Conflict
{
  "error": "Stock insuficiente para producto abc (faltan 5 unidades)"
}
```

### Caso 3: Borrador → Emitir

```bash
# 1. Crear borrador
curl -X POST http://localhost:8080/api/facturas/borrador \
  -d '{"items": [{"productoId": "abc", "cantidad": 2}]}'
  
# Response: {"factura": {"id": "xyz", "estado": "BORRADOR"}}

# 2. Emitir
curl -X POST http://localhost:8080/api/facturas/xyz/emitir

# Response: {"factura": {"id": "xyz", "estado": "EMITIDA"}}
# ✅ Ahora SÍ se descontó stock
```

## 📝 Notas Importantes

1. **Stock por Almacén**: El sistema descuenta de forma inteligente por almacén (usa `StockService.adjustStock`)

2. **Transacciones**: Usa `@Transactional` para garantizar atomicidad (rollback si falla)

3. **Movimientos**: Se crean movimientos de stock tipo "salida" automáticamente

4. **Precios Históricos**: Se guardan snapshots de nombres y precios para histórico

5. **IVA Configurable**: Puedes tener productos con IVA 0%, 5%, 19% o exentos

## 🎯 Próximos Pasos

1. ✅ Configurar tasaIva en productos existentes (default 19%)
2. ✅ Migrar frontend para usar nuevo endpoint
3. ⏳ Agregar datos de emisor (empresa, NIT, dirección)
4. ⏳ Configurar resolución DIAN y rango autorizado
5. ⏳ Integrar OFE para facturación electrónica oficial
6. ⏳ Implementar devoluciones/notas crédito

## 🆘 Soporte

Si encuentras algún problema:

1. Verifica que productos tengan `precio` y `tasaIva` configurados
2. Verifica que haya stock suficiente en almacenes
3. Revisa logs para stack trace completo
4. Errores comunes:
   - `409 Conflict` = Stock insuficiente
   - `400 Bad Request` = Datos inválidos (producto no existe, cantidad <= 0)
   - `404 Not Found` = Factura o producto no encontrado

---

**Versión:** 2.0  
**Fecha:** 2025-01-16  
**Estado:** ✅ Producción Ready (excepto integración DIAN)

