# 📄 GUÍA COMPLETA: Sistema de Facturación - Backend

**Fecha:** 2025-11-17  
**Versión:** 2.0 (Robusto con IVA y descuento de stock)

---

## 🎯 RESUMEN EJECUTIVO

El sistema de facturación está **completamente funcional** y cumple con:
- ✅ Cálculo automático de IVA por producto
- ✅ Descuento automático de stock desde almacenes
- ✅ Soporte para borradores y facturas emitidas
- ✅ Generación de PDF
- ✅ Preparado para integración DIAN (campos incluidos)
- ✅ Validación de stock antes de facturar
- ✅ Checkout desde carrito

---

## 📊 MODELO DE DATOS

### 1. Factura (Modelo Principal)

```java
@Document(collection = "facturas")
public class Factura {
    // Identificación
    private String id;                    // MongoDB ObjectId
    private String numeroFactura;         // Consecutivo (ej: "1", "2", "3")
    private String prefijo;               // Prefijo DIAN (ej: "FV")
    private String resolucionDian;        // Número resolución DIAN
    private Date fechaResolucion;         // Fecha resolución DIAN
    private String rangoAutorizado;       // Ej: "del 1 al 5000"
    
    // Cliente
    private ClienteEmbebido cliente;      // Snapshot: nombre, documento, dirección
    private String clienteId;             // Referencia al User (opcional)
    
    // Items y cálculos
    private List<FacturaItem> items;      // Lista de productos facturados
    
    // Totales (calculados automáticamente)
    private Double subtotal;              // Suma de (cantidad × precio) de todos los items
    private Double totalDescuentos;       // Suma de descuentos aplicados
    private Double baseImponible;         // subtotal - descuentos
    private Double totalIva;              // Suma del IVA de todos los items
    private Double total;                 // baseImponible + totalIva (TOTAL A PAGAR)
    
    // Auditoría
    private ObjectId realizadoPor;        // Usuario que creó la factura
    private String estado;                // BORRADOR, EMITIDA, ANULADA
    private Date creadoEn;                // Fecha de creación
    private Date emitidaEn;               // Fecha de emisión oficial
    
    // DIAN (futuro - campos preparados)
    private String cufe;                  // Código Único de Factura Electrónica
    private String qrCode;                // Código QR en base64
    private String xmlUrl;                // URL del XML oficial
    private String pdfUrl;                // URL del PDF oficial
    private String dianResponse;          // Respuesta del servicio DIAN
}
```

### 2. FacturaItem (Item Individual)

```java
public class FacturaItem {
    // Identificación del producto
    private String productoId;            // ID del producto
    private String nombreProducto;        // Snapshot del nombre (histórico)
    private String codigoProducto;        // Código/SKU del producto
    
    // Cantidades y precios
    private Integer cantidad;             // Cantidad vendida
    private Double precioUnitario;        // Precio por unidad (del producto)
    private Double descuento;             // Descuento aplicado (valor absoluto)
    
    // Cálculos tributarios
    private Double baseImponible;         // (cantidad × precio) - descuento
    private Double tasaIva;               // Tasa de IVA en % (ej: 19.0)
    private Double valorIva;              // IVA calculado: baseImponible × (tasaIva/100)
    private Double subtotal;              // (cantidad × precio) - descuento
    private Double totalItem;             // subtotal + valorIva
}
```

---

## 🔄 FLUJOS DE FACTURACIÓN

### FLUJO 1: Crear Factura Directa (Admin/Vendedor)

```
┌─────────────────────────────────────────────────────────────┐
│ POST /api/facturas                                          │
│ (Crear factura EMITIDA - descuenta stock)                  │
└─────────────────────────────────────────────────────────────┘
                          │
                          ▼
         ┌────────────────────────────────┐
         │ 1. Validar items del request   │
         └────────────────────────────────┘
                          │
                          ▼
         ┌────────────────────────────────┐
         │ 2. Buscar productos en BD      │
         │    (obtener precio e IVA)      │
         └────────────────────────────────┘
                          │
                          ▼
         ┌────────────────────────────────┐
         │ 3. Construir FacturaItems      │
         │    USANDO PRECIOS DEL PRODUCTO │
         │    (ignora precios del cliente)│
         └────────────────────────────────┘
                          │
                          ▼
         ┌────────────────────────────────┐
         │ 4. Calcular totales e IVA      │
         │    - subtotal                  │
         │    - baseImponible             │
         │    - totalIva                  │
         │    - total                     │
         └────────────────────────────────┘
                          │
                          ▼
         ┌────────────────────────────────┐
         │ 5. VALIDAR STOCK disponible    │
         │    en almacenes                │
         └────────────────────────────────┘
                          │
                          ▼
         ┌────────────────────────────────┐
         │ 6. DESCONTAR STOCK             │
         │    - Por almacén               │
         │    - Atómico                   │
         │    - Rollback si falla         │
         └────────────────────────────────┘
                          │
                          ▼
         ┌────────────────────────────────┐
         │ 7. Generar número consecutivo  │
         │    (usando sequence)           │
         └────────────────────────────────┘
                          │
                          ▼
         ┌────────────────────────────────┐
         │ 8. Guardar factura en BD       │
         │    Estado: EMITIDA             │
         └────────────────────────────────┘
                          │
                          ▼
                    ✅ FACTURA CREADA
```

**Endpoint:** `POST /api/facturas`

**Request:**
```json
{
  "items": [
    {
      "productoId": "507f1f77bcf86cd799439011",
      "cantidad": 2
    },
    {
      "productoId": "507f1f77bcf86cd799439012",
      "cantidad": 1
    }
  ],
  "cliente": {
    "nombre": "Juan Pérez",
    "documento": "1234567890",
    "direccion": "Calle 123 #45-67"
  }
}
```

**Response:**
```json
{
  "factura": {
    "id": "674a1234567890abcdef1234",
    "numeroFactura": "1",
    "estado": "EMITIDA",
    "cliente": {
      "nombre": "Juan Pérez",
      "documento": "1234567890",
      "direccion": "Calle 123 #45-67"
    },
    "items": [
      {
        "productoId": "507f1f77bcf86cd799439011",
        "nombreProducto": "Filtro de Aceite",
        "cantidad": 2,
        "precioUnitario": 25000.0,
        "tasaIva": 19.0,
        "valorIva": 9500.0,
        "subtotal": 50000.0,
        "totalItem": 59500.0
      },
      {
        "productoId": "507f1f77bcf86cd799439012",
        "nombreProducto": "Bujía NGK",
        "cantidad": 1,
        "precioUnitario": 15000.0,
        "tasaIva": 19.0,
        "valorIva": 2850.0,
        "subtotal": 15000.0,
        "totalItem": 17850.0
      }
    ],
    "subtotal": 65000.0,
    "totalDescuentos": 0.0,
    "baseImponible": 65000.0,
    "totalIva": 12350.0,
    "total": 77350.0,
    "creadoEn": "2025-11-17T10:30:00.000Z",
    "emitidaEn": "2025-11-17T10:30:00.000Z"
  }
}
```

---

### FLUJO 2: Crear Borrador (Sin Descontar Stock)

```
┌─────────────────────────────────────────────────────────────┐
│ POST /api/facturas/borrador                                 │
│ (Útil para cotizaciones - NO descuenta stock)              │
└─────────────────────────────────────────────────────────────┘
                          │
                          ▼
         ┌────────────────────────────────┐
         │ 1-4. Igual que flujo normal    │
         │     (validar, construir, calc) │
         └────────────────────────────────┘
                          │
                          ▼
         ┌────────────────────────────────┐
         │ 5. ⚠️ NO VALIDA STOCK          │
         └────────────────────────────────┘
                          │
                          ▼
         ┌────────────────────────────────┐
         │ 6. ⚠️ NO DESCUENTA STOCK       │
         └────────────────────────────────┘
                          │
                          ▼
         ┌────────────────────────────────┐
         │ 7. Guardar con estado:         │
         │    BORRADOR                    │
         └────────────────────────────────┘
                          │
                          ▼
                   ✅ BORRADOR CREADO
                          │
                          │  Luego...
                          ▼
         ┌────────────────────────────────┐
         │ POST /facturas/{id}/emitir     │
         │ (Descuenta stock y emite)      │
         └────────────────────────────────┘
```

**Endpoint:** `POST /api/facturas/borrador`

**Uso:** Ideal para cotizaciones o facturas pendientes de aprobación.

---

### FLUJO 3: Checkout desde Carrito (App Android / E-commerce)

```
┌─────────────────────────────────────────────────────────────┐
│ POST /api/facturas/checkout                                 │
│ (Crea factura desde carrito del usuario)                   │
└─────────────────────────────────────────────────────────────┘
                          │
                          ▼
         ┌────────────────────────────────┐
         │ 1. Obtener carrito del usuario │
         └────────────────────────────────┘
                          │
                          ▼
         ┌────────────────────────────────┐
         │ 2. Agrupar items por producto  │
         │    (sumar cantidades duplicadas│
         └────────────────────────────────┘
                          │
                          ▼
         ┌────────────────────────────────┐
         │ 3. Buscar datos de productos   │
         │    (precio, IVA actualizado)   │
         └────────────────────────────────┘
                          │
                          ▼
         ┌────────────────────────────────┐
         │ 4. Calcular totales e IVA      │
         └────────────────────────────────┘
                          │
                          ▼
         ┌────────────────────────────────┐
         │ 5. VALIDAR Y DESCONTAR STOCK   │
         └────────────────────────────────┘
                          │
                          ▼
         ┌────────────────────────────────┐
         │ 6. Crear factura EMITIDA       │
         └────────────────────────────────┘
                          │
                          ▼
         ┌────────────────────────────────┐
         │ 7. VACIAR CARRITO             │
         └────────────────────────────────┘
                          │
                          ▼
                    ✅ COMPRA COMPLETADA
```

**Endpoint:** `POST /api/facturas/checkout`

**Request:**
```json
{
  "carritoId": "507f1f77bcf86cd799439999"
}
```

---

## 🧮 CÁLCULO DE IVA Y TOTALES

### Fórmulas Aplicadas (FacturaCalculoService)

```javascript
// Por cada item:
1. subtotalBruto = cantidad × precioUnitario
2. subtotal = subtotalBruto - descuento
3. baseImponible = subtotal
4. valorIva = baseImponible × (tasaIva / 100)
5. totalItem = baseImponible + valorIva

// Para la factura completa:
1. subtotal = Σ(subtotales de items)
2. totalDescuentos = Σ(descuentos de items)
3. baseImponible = subtotal - totalDescuentos
4. totalIva = Σ(valorIva de items)
5. total = baseImponible + totalIva  ← TOTAL A PAGAR
```

### Ejemplo de Cálculo:

```
Producto: Filtro de Aceite
- Cantidad: 2
- Precio unitario: $25,000
- Tasa IVA: 19%

Cálculos:
1. subtotalBruto = 2 × 25,000 = $50,000
2. descuento = $0
3. subtotal = $50,000 - $0 = $50,000
4. baseImponible = $50,000
5. valorIva = $50,000 × 0.19 = $9,500
6. totalItem = $50,000 + $9,500 = $59,500 ✅
```

---

## 📦 DESCUENTO DE STOCK

### Proceso Automático:

```
1. Agrupar cantidades por producto
   └─ Si hay líneas duplicadas en la factura, se suman

2. Para cada producto:
   └─ Obtener stock disponible en TODOS los almacenes
   └─ Ordenar por almacén (normalmente por ID)
   
3. Descontar de forma secuencial:
   ┌──────────────────────────────────────────────┐
   │ Almacén A: 10 unidades disponibles           │
   │ Necesito: 15 unidades                        │
   │ ─────────────────────────────────────────    │
   │ Tomo: 10 de Almacén A (queda 0)              │
   │ Faltan: 5 unidades                           │
   ├──────────────────────────────────────────────┤
   │ Almacén B: 8 unidades disponibles            │
   │ Necesito: 5 unidades                         │
   │ ─────────────────────────────────────────────│
   │ Tomo: 5 de Almacén B (quedan 3)              │
   │ ✅ Completado                                │
   └──────────────────────────────────────────────┘

4. Si no alcanza:
   └─ ❌ Lanza excepción: "Stock insuficiente"
   └─ ❌ Rollback: NO se crea factura
   └─ ❌ NO se descuenta nada
```

### Seguridad del Descuento:

- ✅ **Atómico**: Si falla, se hace rollback completo
- ✅ **Validado**: Verifica stock ANTES de descontar
- ✅ **Por almacén**: Descuenta de múltiples almacenes si es necesario
- ✅ **Transaccional**: Usa `@Transactional` de Spring

---

## 📋 ENDPOINTS DISPONIBLES

### 1. `POST /api/facturas` - Crear Factura Emitida

**Rol:** ADMIN, VENDEDOR  
**Descripción:** Crea factura y descuenta stock inmediatamente

**Request:**
```json
{
  "items": [
    {
      "productoId": "507f...",
      "cantidad": 2
    }
  ],
  "cliente": {
    "nombre": "Juan Pérez",
    "documento": "123456",
    "direccion": "Calle 123"
  }
}
```

**Características:**
- ✅ Descuenta stock
- ✅ Calcula IVA automáticamente
- ✅ Usa precios del producto (NO del request)
- ✅ Genera número consecutivo
- ✅ Estado: EMITIDA

---

### 2. `POST /api/facturas/borrador` - Crear Borrador

**Rol:** ADMIN, VENDEDOR  
**Descripción:** Crea cotización sin descontar stock

**Request:** Igual que `/api/facturas`

**Características:**
- ❌ NO descuenta stock
- ✅ Calcula IVA automáticamente
- ✅ Usa precios del producto
- ✅ Estado: BORRADOR
- 🔄 Luego se puede emitir con `POST /facturas/{id}/emitir`

---

### 3. `POST /api/facturas/{id}/emitir` - Emitir Borrador

**Rol:** ADMIN, VENDEDOR  
**Descripción:** Emite un borrador (lo convierte en factura oficial)

**Response:**
```json
{
  "factura": {
    "id": "...",
    "estado": "EMITIDA",
    "emitidaEn": "2025-11-17T10:30:00.000Z",
    ...
  }
}
```

**Características:**
- ✅ Descuenta stock al emitir
- ✅ Cambia estado de BORRADOR → EMITIDA
- ✅ Registra fecha de emisión

---

### 4. `POST /api/facturas/{id}/anular` - Anular Factura

**Rol:** ADMIN  
**Descripción:** Anula una factura emitida

**Request:**
```json
{
  "motivo": "Error en facturación"
}
```

**Características:**
- ✅ Cambia estado a ANULADA
- ⚠️ **NO devuelve stock automáticamente** (requiere ajuste manual)

---

### 5. `POST /api/facturas/checkout` - Checkout Carrito

**Rol:** Cualquier usuario autenticado  
**Descripción:** Crea factura desde el carrito del usuario

**Request:**
```json
{
  "carritoId": "507f1f77bcf86cd799439999"
}
```

**Características:**
- ✅ Descuenta stock
- ✅ Calcula IVA automáticamente
- ✅ Usa precios actuales del producto
- ✅ Vacía el carrito
- ✅ Estado: EMITIDA

---

### 6. `GET /api/facturas/{id}` - Obtener Factura

**Rol:** ADMIN, VENDEDOR, o dueño de la factura  
**Descripción:** Obtiene detalles de una factura

---

### 7. `GET /api/facturas/{id}/pdf` - Descargar PDF

**Rol:** ADMIN, VENDEDOR, o dueño de la factura  
**Descripción:** Genera y descarga PDF de la factura

**Response:** PDF con:
- Datos de la empresa
- Datos del cliente
- Listado de productos
- Subtotales, IVA y total
- Número de factura
- Fecha de emisión

---

### 8. `GET /api/facturas?userId={id}` - Listar por Usuario

**Rol:** ADMIN o dueño  
**Descripción:** Lista facturas de un usuario específico

---

## ⚠️ REGLAS IMPERATIVAS DEL SISTEMA

### 1. Precios SIEMPRE desde el Producto
```java
// ❌ NUNCA usar precio del cliente
item.setPrecioUnitario(request.getPrecioUnitario()); // MAL

// ✅ SIEMPRE usar precio del producto
Producto prod = productoService.getById(productoId);
item.setPrecioUnitario(prod.getPrecio()); // BIEN
```

### 2. IVA SIEMPRE desde el Producto
```java
// ✅ El IVA viene del campo tasaIva del producto
item.setTasaIva(producto.getTasaIva()); // 19.0, 5.0, 0.0, etc.
```

### 3. Stock SIEMPRE se Descuenta (Facturas Emitidas)
```java
// ✅ No hay opción de "omitir descuento de stock"
// Si es EMITIDA, se descuenta. Si es BORRADOR, no.
if (estado == "EMITIDA") {
    descontarStock(); // OBLIGATORIO
}
```

### 4. Totales SIEMPRE se Calculan en Servidor
```java
// ❌ NUNCA confiar en total del cliente
factura.setTotal(request.getTotal()); // MAL

// ✅ SIEMPRE recalcular en servidor
calculoService.calcularTotales(factura); // BIEN
```

### 5. Validación de Stock ANTES de Facturar
```java
// ✅ Si no hay stock suficiente, lanza excepción
// NO se crea la factura si falta stock
if (stockInsuficiente) {
    throw new IllegalStateException("Stock insuficiente");
}
```

---

## 🎨 ESTADOS DE LA FACTURA

```
┌──────────────┐
│   BORRADOR   │ ◄── Creada pero NO oficial
└──────┬───────┘     (No descuenta stock)
       │
       │ POST /facturas/{id}/emitir
       ▼
┌──────────────┐
│   EMITIDA    │ ◄── Factura oficial
└──────┬───────┘     (Stock descontado)
       │
       │ POST /facturas/{id}/anular
       ▼
┌──────────────┐
│   ANULADA    │ ◄── Factura cancelada
└──────────────┘     (Stock NO se devuelve auto)
```

---

## 🔮 CAMPOS PREPARADOS PARA DIAN

```java
// Ya incluidos en el modelo Factura:
private String cufe;           // Código Único de Factura Electrónica
private String qrCode;         // Código QR (base64 o URL)
private String xmlUrl;         // URL del XML firmado
private String pdfUrl;         // URL del PDF representación gráfica
private String dianResponse;   // Respuesta del web service DIAN

// También en ConfiguracionGlobal:
private String resolucionDian;        // Número de resolución
private LocalDateTime fechaResolucionDian;
private Long rangoFacturaInicio;      // Rango autorizado inicio
private Long rangoFacturaFin;         // Rango autorizado fin
private Long proximoNumeroFactura;    // Próximo a asignar
```

**Cuando implementes DIAN:**
1. Al emitir factura → Generar XML con firma digital
2. Enviar XML a web service de DIAN
3. Recibir CUFE y respuesta
4. Guardar CUFE, QR, XML URL en factura
5. Actualizar pdfUrl con PDF oficial

---

## 📊 EJEMPLO COMPLETO DE USO EN FRONTEND

### Crear Factura desde Formulario (Next.js)

```typescript
// components/FacturaForm.tsx
import { useState } from 'react';

interface FacturaItem {
  productoId: string;
  cantidad: number;
}

interface FacturaData {
  items: FacturaItem[];
  cliente: {
    nombre: string;
    documento: string;
    direccion: string;
  };
}

export function FacturaForm() {
  const [items, setItems] = useState<FacturaItem[]>([]);
  const [cliente, setCliente] = useState({
    nombre: '',
    documento: '',
    direccion: ''
  });

  const agregarItem = (productoId: string, cantidad: number) => {
    setItems([...items, { productoId, cantidad }]);
  };

  const crearFactura = async () => {
    const facturaData: FacturaData = {
      items,
      cliente
    };

    try {
      const response = await fetch('/api/facturas', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify(facturaData)
      });

      const result = await response.json();

      if (response.ok) {
        const factura = result.factura;
        console.log('Factura creada:', factura);
        console.log('Total:', factura.total);
        console.log('IVA:', factura.totalIva);
        
        // Descargar PDF
        window.open(`/api/facturas/${factura.id}/pdf`, '_blank');
        
        toast.success('Factura creada exitosamente');
      } else {
        toast.error(result.error);
      }
    } catch (error) {
      toast.error('Error creando factura');
    }
  };

  return (
    <form onSubmit={(e) => { e.preventDefault(); crearFactura(); }}>
      {/* Formulario de cliente */}
      <div>
        <input
          value={cliente.nombre}
          onChange={(e) => setCliente({...cliente, nombre: e.target.value})}
          placeholder="Nombre del cliente"
        />
        <input
          value={cliente.documento}
          onChange={(e) => setCliente({...cliente, documento: e.target.value})}
          placeholder="Documento"
        />
        <input
          value={cliente.direccion}
          onChange={(e) => setCliente({...cliente, direccion: e.target.value})}
          placeholder="Dirección"
        />
      </div>

      {/* Selector de productos */}
      <ProductoSelector onAdd={agregarItem} />

      {/* Lista de items */}
      <div>
        {items.map((item, i) => (
          <div key={i}>
            Producto: {item.productoId} - Cant: {item.cantidad}
          </div>
        ))}
      </div>

      <button type="submit">Crear Factura</button>
    </form>
  );
}
```

### Checkout desde Carrito (App Android / Web)

```typescript
// Checkout simple
const checkout = async (carritoId: string) => {
  try {
    const response = await fetch('/api/facturas/checkout', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify({ carritoId })
    });

    const result = await response.json();

    if (response.ok) {
      const factura = result.factura;
      
      // Mostrar resumen
      alert(`
        Compra exitosa!
        Factura: ${factura.numeroFactura}
        Total: $${factura.total.toLocaleString()}
        IVA incluido: $${factura.totalIva.toLocaleString()}
      `);
      
      // Descargar PDF
      window.open(`/api/facturas/${factura.id}/pdf`, '_blank');
      
      // Limpiar carrito en UI
      clearCart();
    } else {
      if (result.error.includes('Stock insuficiente')) {
        alert('Lo sentimos, no hay suficiente stock');
      } else {
        alert('Error: ' + result.error);
      }
    }
  } catch (error) {
    alert('Error procesando compra');
  }
};
```

---

## ✅ CHECKLIST DE VALIDACIONES

### Al Crear Factura:
- ✅ Items no vacíos
- ✅ Cantidades > 0
- ✅ Productos existen en BD
- ✅ Stock suficiente en almacenes
- ✅ Cliente tiene datos mínimos (nombre)

### Cálculos Verificados:
- ✅ Precio desde producto (no request)
- ✅ IVA desde producto
- ✅ Subtotales correctos
- ✅ Total = baseImponible + IVA

### Descuento de Stock:
- ✅ Valida antes de descontar
- ✅ Descuenta de múltiples almacenes si es necesario
- ✅ Atómico (rollback si falla)
- ✅ No permite sobreventa

---

## 🚀 PRÓXIMOS PASOS (Roadmap DIAN)

### Fase 1: Preparación (ACTUAL - ✅ COMPLETADA)
- ✅ Modelo de factura con campos DIAN
- ✅ Cálculo de IVA correcto
- ✅ Descuento de stock
- ✅ Generación de PDF básico
- ✅ Numeración consecutiva

### Fase 2: Integración DIAN (Futuro)
- ⏳ Generación de XML según estándar DIAN
- ⏳ Firma digital del XML
- ⏳ Envío a web service DIAN
- ⏳ Recepción de CUFE
- ⏳ Generación de código QR
- ⏳ PDF con representación gráfica oficial

### Fase 3: Facturación Electrónica Completa
- ⏳ Notas crédito
- ⏳ Notas débito
- ⏳ Eventos de factura (aceptación/rechazo)
- ⏳ Consulta de validez en DIAN

---

## 📞 CONTACTO Y SOPORTE

**Documentación generada por:** GitHub Copilot  
**Fecha:** 2025-11-17  
**Versión del Sistema:** 2.0

---

✅ **SISTEMA COMPLETAMENTE FUNCIONAL Y LISTO PARA INTEGRAR EN FRONTEND**

