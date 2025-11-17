# ✅ RESUMEN - Cambios que Afectan Frontend (Next.js y Android)

## 🎯 Cambios Implementados

Se implementó un **sistema completo de facturación con IVA** que afecta tanto al frontend Next.js (admin) como a la app Android (clientes).

---

## 📱 IMPACTO EN APP ANDROID

### ✅ Cambios en API Pública (Productos)

#### Nuevo campo en todas las respuestas de productos:

```json
{
  "producto": {
    "id": "...",
    "nombre": "Filtro de Aceite",
    "precio": 25000,
    "tasaIva": 19.0,  // ⬅️ NUEVO CAMPO
    "stock": 50
  }
}
```

#### Endpoints afectados:
- ✅ `GET /api/public/productos` - Lista de productos
- ✅ `GET /api/public/productos/{id}` - Detalle de producto

#### ¿Qué hacer en Android?

1. **Actualizar modelo Kotlin:**
```kotlin
data class Producto(
    val id: String,
    val nombre: String,
    val precio: Double,
    val tasaIva: Double = 19.0,  // ⬅️ AGREGAR
    val stock: Int
)
```

2. **Regenerar cliente API** (si usas OpenAPI Generator):
```bash
# En Android Studio
./gradlew openApiGenerate
```

3. **(Opcional) Mostrar precio con IVA:**
```kotlin
fun precioConIva(precio: Double, tasaIva: Double): Double {
    return precio * (1 + tasaIva / 100)
}

// Uso en UI
Text("Precio: $${producto.precio}")
Text("IVA ${producto.tasaIva}%")
Text("Total: $${precioConIva(producto.precio, producto.tasaIva)}")
```

#### ⚠️ ¿Es urgente actualizar?

**NO es urgente** porque:
- El campo `tasaIva` viene con valor por defecto (19%)
- La app seguirá parseando JSON correctamente si ignoras el campo
- El checkout NO requiere que envíes IVA (lo calcula el backend)

**Actualiza cuando:** quieras mostrar el desglose de IVA en la UI de productos.

---

## 🌐 IMPACTO EN NEXT.JS (ADMIN/VENDEDOR)

### ✅ Cambios en API de Productos

#### 1. Crear/Editar Productos - NUEVO campo `tasaIva`

**Request:**
```json
{
  "nombre": "Filtro",
  "precio": 25000,
  "tasaIva": 19.0,  // ⬅️ NUEVO (opcional, default: 19%)
  "stock": 100
}
```

**Response:**
```json
{
  "producto": {
    "id": "...",
    "precio": 25000,
    "tasaIva": 19.0  // ⬅️ NUEVO
  }
}
```

#### 2. Crear Facturas - **CAMBIA COMPLETAMENTE**

**❌ ANTES (obsoleto):**
```typescript
// Ya NO funciona así
fetch('/api/facturas/dto?descontarStock=true', {
  body: JSON.stringify({
    items: [{
      productoId: 'abc',
      cantidad: 5,
      precioUnitario: 100  // ❌ Ya no se acepta
    }]
  })
})
```

**✅ AHORA (v2.0):**
```typescript
// NUEVO endpoint y estructura
fetch('/api/facturas', {
  method: 'POST',
  body: JSON.stringify({
    clienteId: 'user123',
    items: [{
      productoId: 'abc',
      cantidad: 5
      // ✅ NO enviar precio ni IVA - se toman del producto
    }]
  })
})

// Respuesta con desglose completo
{
  "factura": {
    "numeroFactura": "1",
    "estado": "EMITIDA",
    "subtotal": 125000,      // ⬅️ NUEVO
    "totalIva": 23750,       // ⬅️ NUEVO
    "total": 148750,
    "items": [{
      "nombreProducto": "Filtro",
      "cantidad": 5,
      "precioUnitario": 25000,
      "tasaIva": 19.0,       // ⬅️ NUEVO
      "valorIva": 23750,     // ⬅️ NUEVO
      "totalItem": 148750    // ⬅️ NUEVO
    }]
  }
}
```

#### ¿Qué hacer en Next.js?

1. **Actualizar tipos TypeScript:**

```typescript
// types/producto.ts
interface Producto {
  id: string;
  nombre: string;
  precio: number;
  tasaIva?: number;  // ⬅️ AGREGAR
  stock: number;
}

// types/factura.ts
interface Factura {
  numeroFactura: string;
  estado: 'BORRADOR' | 'EMITIDA' | 'ANULADA';
  subtotal: number;       // ⬅️ AGREGAR
  totalIva: number;       // ⬅️ AGREGAR
  total: number;
  items: FacturaItem[];
}

interface FacturaItem {
  nombreProducto: string;
  cantidad: number;
  precioUnitario: number;
  tasaIva: number;        // ⬅️ AGREGAR
  valorIva: number;       // ⬅️ AGREGAR
  subtotal: number;       // ⬅️ AGREGAR
  totalItem: number;      // ⬅️ AGREGAR
}
```

2. **Actualizar formulario de producto:**

```tsx
// components/ProductoForm.tsx
<select name="tasaIva">
  <option value={0}>0% - Exento</option>
  <option value={5}>5% - Canasta básica</option>
  <option value={19}>19% - Estándar</option>
</select>
```

3. **Actualizar creación de facturas:**

```typescript
// ❌ ELIMINAR código viejo
// const factura = await fetch('/api/facturas/dto?descontarStock=true', ...)

// ✅ USAR nuevo endpoint
const response = await fetch('/api/facturas', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    clienteId: clienteId,
    items: items.map(item => ({
      productoId: item.productoId,
      cantidad: item.cantidad
      // NO enviar precio ni IVA
    }))
  })
});
```

4. **Regenerar cliente API:**
```bash
# Si usas OpenAPI Generator
npm run generate-api
# O manualmente desde Swagger
# http://localhost:8080/v3/api-docs
```

---

## 🔄 NUEVOS ENDPOINTS (Next.js)

### Facturas

```typescript
// 1. Crear factura EMITIDA (descuenta stock)
POST /api/facturas
{
  "clienteId": "user123",
  "items": [{ "productoId": "abc", "cantidad": 5 }]
}

// 2. Crear BORRADOR (NO descuenta stock - para cotizaciones)
POST /api/facturas/borrador
{
  "clienteId": "user123",
  "items": [{ "productoId": "abc", "cantidad": 5 }]
}

// 3. Emitir borrador (descuenta stock)
POST /api/facturas/{id}/emitir

// 4. Anular factura (NO devuelve stock automáticamente)
POST /api/facturas/{id}/anular
{ "motivo": "Error en datos" }

// 5. Descargar PDF con IVA
GET /api/facturas/{id}/pdf
```

---

## 📊 COMPARATIVA ANTES/DESPUÉS

### Crear Factura

| Aspecto | Antes | Ahora (v2.0) |
|---------|-------|--------------|
| **Endpoint** | `/api/facturas/dto?descontarStock=true` | `/api/facturas` |
| **Precio** | Cliente envía `precioUnitario` | Backend toma de producto |
| **IVA** | No existía | Calculado automáticamente |
| **Stock** | Opcional descontar | **SIEMPRE** se descuenta |
| **Validación** | Débil | Stock validado ANTES |
| **Estados** | Solo CREADA | BORRADOR/EMITIDA/ANULADA |

### Response de Factura

| Campo | Antes | Ahora |
|-------|-------|-------|
| `total` | ✅ | ✅ |
| `subtotal` | ❌ | ✅ NUEVO |
| `totalIva` | ❌ | ✅ NUEVO |
| `baseImponible` | ❌ | ✅ NUEVO |
| `items[].tasaIva` | ❌ | ✅ NUEVO |
| `items[].valorIva` | ❌ | ✅ NUEVO |
| `estado` | Simple | BORRADOR/EMITIDA/ANULADA |

---

## ⚠️ BREAKING CHANGES

### Para Next.js

1. **Endpoint de crear factura cambió:**
   - ❌ Viejo: `POST /api/facturas/dto?descontarStock=true`
   - ✅ Nuevo: `POST /api/facturas`

2. **No enviar precios:**
   - ❌ Antes: `{ precioUnitario: 100 }`
   - ✅ Ahora: Solo `{ productoId, cantidad }`

3. **Response ampliada:**
   - Ahora incluye `subtotal`, `totalIva`, `baseImponible`
   - Items incluyen `tasaIva`, `valorIva`, `totalItem`

### Para Android

**NO hay breaking changes** - Solo nuevos campos en respuestas que puedes ignorar si quieres.

---

## 📚 Documentación Actualizada

### Swagger UI
1. Iniciar backend: `./mvnw spring-boot:run`
2. Abrir: http://localhost:8080/swagger-ui/index.html
3. Ver endpoints actualizados con ejemplos de `tasaIva`

### Archivos de Documentación
- `FACTURACION.md` - Guía completa del sistema de facturación
- `MIGRACION_TASA_IVA.md` - Guía detallada de migración
- `RESUMEN_FACTURACION.md` - Resumen ejecutivo de cambios

---

## ✅ CHECKLIST DE ACTUALIZACIÓN

### App Android (Kotlin)
- [ ] Actualizar modelo `Producto` con campo `tasaIva`
- [ ] Regenerar cliente API desde OpenAPI
- [ ] (Opcional) Implementar cálculo de precio con IVA en UI
- [ ] Testing: verificar parseo correcto del nuevo campo

### Frontend Next.js (TypeScript)
- [ ] Actualizar interface `Producto` con `tasaIva`
- [ ] Actualizar interface `Factura` con campos tributarios
- [ ] Regenerar cliente API desde OpenAPI
- [ ] **IMPORTANTE:** Actualizar endpoint de crear factura
- [ ] Agregar selector de IVA en formulario de producto
- [ ] Actualizar vista de facturas para mostrar IVA desglosado
- [ ] Testing: crear factura y verificar cálculo de IVA

### Base de Datos
- [ ] Ejecutar script: `mongo < scripts/actualizar_iva_productos.js`
- [ ] Verificar productos tienen `tasaIva: 19.0`

---

## 🧪 TESTING RÁPIDO

### 1. Producto con IVA
```bash
curl -X POST http://localhost:8080/api/productos \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Test",
    "precio": 10000,
    "tasaIva": 19,
    "stock": 50
  }'
```

### 2. Factura con IVA
```bash
curl -X POST http://localhost:8080/api/facturas \
  -H "Content-Type: application/json" \
  -d '{
    "clienteId": "user123",
    "items": [{ "productoId": "PRODUCTO_ID", "cantidad": 2 }]
  }'

# Esperado:
# {
#   "factura": {
#     "subtotal": 20000,
#     "totalIva": 3800,    ← 20000 * 19%
#     "total": 23800
#   }
# }
```

---

## 🆘 SOPORTE

### FAQ

**P: ¿Debo actualizar los frontends YA?**
- Android: No urgente (compatible hacia atrás)
- Next.js: Sí, para crear facturas correctamente

**P: ¿Los productos existentes tienen IVA?**
- Ejecuta el script de migración para asignarles 19%

**P: ¿Puedo seguir usando el endpoint viejo de facturas?**
- No, fue reemplazado por `/api/facturas` que es más robusto

**P: ¿El IVA se muestra en el PDF?**
- Sí, el PDF ahora incluye desglose completo de IVA

---

**Versión:** 2.0  
**Fecha:** 2025-01-16  
**Documentación OpenAPI:** ✅ Actualizada  
**Compilación:** ✅ SUCCESS  
**Breaking Changes:** ⚠️ Sí (solo Next.js - endpoint de facturas)

