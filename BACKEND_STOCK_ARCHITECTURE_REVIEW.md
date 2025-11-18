# 🏗️ Revisión de Arquitectura de Stock - Backend

## 📋 Contexto

Durante la implementación del módulo de facturas en el frontend, hemos detectado una **ambigüedad crítica** en cómo se maneja el stock de productos. Existen **dos sistemas diferentes** en la API generada y necesitamos claridad sobre cuál es el enfoque correcto.

---

## 🔍 Sistemas de Stock Detectados en la API

### **Sistema 1: Stock Simple (Campo en Producto)**

**Modelo**: `ProductoRequest`
```typescript
interface ProductoRequest {
  nombre: string;
  descripcion?: string;
  precio?: number;
  tasaIva?: number;
  stock?: number;  // ⚠️ Campo opcional directo en el producto
  categoriaId?: string;
  tallerId?: string;
  // ... otros campos
}
```

**Características**:
- Stock como un **valor único** en el producto
- No considera múltiples ubicaciones/almacenes
- Simple de implementar y entender

---

### **Sistema 2: Stock por Almacén (API Dedicada)**

**API**: `StockApi` con endpoints separados

**Endpoints disponibles**:
```
GET    /api/stock?productoId=X           → Obtener stock desglosado por almacén
POST   /api/stock/adjust                 → Ajustar stock (delta +/-)
PUT    /api/stock/set                    → Establecer stock absoluto
DELETE /api/stock?productoId=X&almacenId=Y → Eliminar registro
```

**Modelo de datos** (inferido de la API):
```typescript
{
  productoId: string;
  almacenId: string;
  cantidad: number;
  delta?: number;      // Para ajustes incrementales
  motivo?: string;     // Razón del cambio
}
```

**Relación jerárquica**:
```
Taller (TallerRequest)
  └── Almacenes (AlmacenRequest: nombre, ubicacion)
       └── Stock por Producto
            └── cantidad: number
```

**Características**:
- Stock distribuido en múltiples almacenes
- Permite trazabilidad por ubicación
- Mayor complejidad pero más robusto

---

### **Sistema 3: Movimientos de Stock (Trazabilidad)**

**Modelo**: `MovimientoRequest`
```typescript
interface MovimientoRequest {
  tipo: string;           // ENTRADA, SALIDA, AJUSTE, FACTURA, etc.
  productoId: string;
  cantidad: number;
  referencia?: string;
  notas?: string;
  realizadoPor?: string;
  almacenId?: string;     // ⚠️ Opcional - puede o no ligarse a almacén
}
```

**Propósito aparente**: Historial de cambios de stock para auditoría

---

## 🚨 Problemas Críticos Detectados

### **1. Ambigüedad en Facturas**

Cuando se emite una factura (`POST /api/facturas`):

```typescript
// Request del frontend
{
  items: [
    { productoId: "691b481b8864e10726345b81", cantidad: 1 }
  ]
}
```

**Preguntas sin respuesta**:
- ❓ ¿De dónde se descuenta el stock? ¿De `producto.stock` o de un almacén?
- ❓ Si es de un almacén, ¿cuál? ¿Hay uno por defecto?
- ❓ ¿El frontend debería enviar el `almacenId` en cada item?
- ❓ ¿Se crea un `MovimientoRequest` automáticamente?

### **2. Sincronización entre Sistemas**

Si ambos sistemas coexisten:
- ❓ ¿`producto.stock` es la **suma** de todos los almacenes?
- ❓ ¿O son valores independientes?
- ❓ Si creo un producto con `stock: 100`, ¿se asigna automáticamente a algún almacén?
- ❓ Si ajusto stock en un almacén, ¿actualiza `producto.stock`?

### **3. Validación en Frontend**

Actualmente validamos así:
```typescript
if (producto.stock !== undefined && cantidad > producto.stock) {
  alert(`Stock insuficiente. Disponible: ${producto.stock}`);
}
```

**Problema**: Si el backend usa almacenes, esta validación puede ser **incorrecta** o **incompleta**.

### **4. Creación de Productos**

Al crear/editar un producto enviamos:
```typescript
{
  nombre: "Producto X",
  precio: 50000,
  stock: 10,  // ⚠️ ¿Esto va a producto.stock o se debe asignar a un almacén?
  tallerId: "..."
}
```

**Pregunta**: ¿Es válido enviar `stock` en el request? ¿O debe gestionarse solo vía StockApi?

---

## 🎯 Solicitud de Aclaración al Backend

### **Preguntas Prioritarias**

#### **A. Arquitectura General**

1. **¿Cuál es el sistema de stock oficial actualmente?**
   - [ ] Solo `producto.stock` (Sistema 1)
   - [ ] Solo Stock por Almacén (Sistema 2)
   - [ ] Híbrido: ambos coexisten con sincronización
   - [ ] Híbrido: ambos coexisten pero son independientes

2. **¿El campo `producto.stock` está obsoleto?**
   - [ ] Sí, se mantiene por retrocompatibilidad pero no se usa
   - [ ] No, es el sistema principal
   - [ ] Es calculado automáticamente (suma de almacenes)
   - [ ] Es independiente del stock por almacén

#### **B. Comportamiento en Facturas**

3. **Al emitir una factura (`EMITIDA`), ¿de dónde se descuenta stock?**
   - [ ] Del campo `producto.stock`
   - [ ] Del almacén asociado al taller
   - [ ] Del almacén con mayor stock disponible
   - [ ] No se descuenta (debe hacerse manualmente)
   - [ ] Otro (especificar): __________

4. **¿Se debe enviar `almacenId` en el request de facturas?**
   ```typescript
   // ¿Debería ser así?
   items: [
     { productoId: "...", cantidad: 1, almacenId: "..." }
   ]
   ```
   - [ ] Sí, es obligatorio
   - [ ] Opcional, si no se envía usa uno por defecto
   - [ ] No, el backend lo determina automáticamente
   - [ ] No aplica, se usa producto.stock

5. **¿Se crean `MovimientoRequest` automáticamente al emitir facturas?**
   - [ ] Sí, con tipo "FACTURA" o "VENTA"
   - [ ] No, debe crearse manualmente
   - [ ] Solo si hay almacenId especificado

#### **C. Gestión de Stock**

6. **¿Cómo se debe crear un producto con stock inicial?**
   - [ ] Enviar `stock` en `ProductoRequest` directamente
   - [ ] Crear producto sin stock, luego usar `POST /api/stock/set`
   - [ ] Ambas formas son válidas
   - [ ] Otro (especificar): __________

7. **Si un taller tiene múltiples almacenes, ¿cómo se distribuye el stock?**
   - [ ] Se asigna todo a un almacén "principal" automáticamente
   - [ ] Debe asignarse manualmente a cada almacén vía StockApi
   - [ ] Se divide equitativamente
   - [ ] No aplica (solo un almacén por taller)

8. **¿Existe sincronización automática?**
   - [ ] `producto.stock` = suma automática de todos sus almacenes
   - [ ] `producto.stock` se actualiza cuando se modifica stock por almacén
   - [ ] Son independientes (no hay sincronización)
   - [ ] `producto.stock` no se usa si hay almacenes

#### **D. API de Stock por Almacén**

9. **¿La StockApi está activa y en uso?**
   - [ ] Sí, es el sistema principal
   - [ ] Sí, pero es opcional/experimental
   - [ ] No, está deprecada
   - [ ] Sí, pero solo para casos específicos

10. **¿Qué devuelve `GET /api/stock?productoId=X`?**
    ```typescript
    // ¿Estructura de respuesta?
    {
      productoId: string;
      stockTotal?: number;  // ¿Existe este campo?
      almacenes: [          // ¿Array de almacenes?
        { almacenId: string; almacenNombre: string; cantidad: number }
      ]
    }
    ```

---

## 💡 Propuestas de Solución (para Backend)

### **Opción A: Stock Simple Único** ✅ **MÁS SIMPLE**

**Decisión**: Usar **solo** `producto.stock`, deprecar StockApi

**Ventajas**:
- Simple de implementar y mantener
- Menos complejidad en frontend y backend
- Adecuado para talleres con una ubicación

**Implementación**:
1. Facturas descuentan directamente de `producto.stock`
2. Crear endpoint para ajustes manuales con motivo
3. `MovimientoRequest` registra todos los cambios
4. StockApi se marca como `@deprecated` en OpenAPI

**Frontend necesitará**:
- Solo validar `producto.stock`
- Módulo simple de ajustes de inventario
- Vista de historial de movimientos

---

### **Opción B: Stock por Almacén Completo** ⭐ **MÁS ROBUSTO**

**Decisión**: Usar **solo** StockApi con almacenes, deprecar `producto.stock`

**Ventajas**:
- Soporte multi-ubicación
- Trazabilidad completa por almacén
- Escalable para talleres grandes

**Implementación**:
1. `producto.stock` se vuelve **readonly** (calculado como suma de almacenes)
2. `FacturaItemRequest` incluye `almacenId` obligatorio u opcional
3. Si no se especifica almacenId, usar almacén "principal" del taller
4. Todos los movimientos se registran con almacenId

**Cambios en OpenAPI**:
```yaml
FacturaItemRequest:
  properties:
    productoId: string
    cantidad: number
    almacenId: string  # ⬅️ NUEVO (opcional u obligatorio según decisión)
```

**Frontend necesitará**:
- Selector de almacén en facturas
- Módulo completo de gestión de stock:
  - Vista por almacén
  - Transferencias entre almacenes
  - Ajustes con motivo
- Dashboard de stock consolidado

---

### **Opción C: Híbrido con Sincronización** ⚖️ **BALANCE**

**Decisión**: Ambos sistemas coexisten con sincronización automática

**Reglas**:
1. `producto.stock` es **calculado automáticamente** (readonly)
   - Suma de stock en todos los almacenes del producto
2. StockApi es la única forma de **modificar** stock
3. `ProductoRequest.stock` se ignora en create/update (o retorna error)

**Ventajas**:
- Compatibilidad hacia atrás
- Validación simple en frontend (usar `producto.stock`)
- Poder de almacenes cuando se necesite

**Implementación**:
1. Al crear producto, NO aceptar `stock` en request
2. Stock inicial se asigna vía `POST /api/stock/set` a almacén default
3. `GET /api/productos` retorna `stock` calculado
4. Facturas pueden especificar `almacenId` (opcional, usa default si no se envía)

**Frontend necesitará**:
- Validar con `producto.stock` (simplificado)
- Opción avanzada de gestión por almacén
- UI adaptativa según necesidades del taller

---

## 🎨 Propuesta de Modelo Unificado (Opción Recomendada: B o C)

### **Modelo de Datos Propuesto**

```typescript
// PRODUCTO (Response)
interface ProductoResponse {
  id: string;
  nombre: string;
  precio: number;
  tasaIva: number;
  stock: number;        // ⬅️ READONLY - Calculado como suma de almacenes
  stockDetalle?: {      // ⬅️ NUEVO - Detalle opcional
    almacenes: Array<{
      almacenId: string;
      almacenNombre: string;
      cantidad: number;
    }>;
    total: number;
  };
  tallerId: string;
  // ... otros campos
}

// PRODUCTO (Request - Create/Update)
interface ProductoRequest {
  nombre: string;
  precio?: number;
  tasaIva?: number;
  // stock: REMOVED ⛔ - Ya no se acepta aquí
  tallerId?: string;
  // ... otros campos
}

// FACTURA ITEM (Request)
interface FacturaItemRequest {
  productoId: string;
  cantidad: number;
  almacenId?: string;   // ⬅️ NUEVO - Opcional, usa default si no se envía
}

// STOCK (Set/Adjust)
interface StockRequest {
  productoId: string;
  almacenId: string;    // ⬅️ OBLIGATORIO
  cantidad: number;     // Para SET: valor absoluto, para ADJUST: delta
  motivo?: string;
}
```

### **Flujo Recomendado**

#### **1. Crear Producto con Stock Inicial**
```typescript
// Paso 1: Crear producto (sin stock)
POST /api/productos
{
  nombre: "Producto X",
  precio: 50000,
  tallerId: "taller123"
}

// Paso 2: Asignar stock al almacén principal
POST /api/stock/set
{
  productoId: "prod456",
  almacenId: "almacen789",  // Almacén principal del taller
  cantidad: 100,
  motivo: "Stock inicial"
}

// Paso 3: GET /api/productos/prod456 retorna:
{
  id: "prod456",
  nombre: "Producto X",
  stock: 100,  // ⬅️ Calculado automáticamente
  stockDetalle: {
    almacenes: [
      { almacenId: "almacen789", almacenNombre: "Almacén Principal", cantidad: 100 }
    ],
    total: 100
  }
}
```

#### **2. Crear Factura (Descuenta Stock)**
```typescript
// Frontend envía:
POST /api/facturas
{
  items: [
    {
      productoId: "prod456",
      cantidad: 5,
      almacenId: "almacen789"  // ⬅️ Opcional, usa default si no se envía
    }
  ]
}

// Backend automáticamente:
// 1. Valida stock disponible en ese almacén
// 2. Descuenta 5 unidades del almacenId especificado
// 3. Crea MovimientoRequest automático:
{
  tipo: "VENTA",
  productoId: "prod456",
  cantidad: -5,
  almacenId: "almacen789",
  referencia: "FACTURA-001",
  realizadoPor: "usuario123"
}
// 4. Recalcula producto.stock = 95
```

---

## 📊 Impacto en Frontend

### **Si eligen Opción A (Stock Simple)**
- ✅ Cambios mínimos en frontend
- ✅ Validación simple con `producto.stock`
- ⚠️ Necesita UI para ajustes manuales

### **Si eligen Opción B (Solo Almacenes)**
- 🔨 Refactor moderado en frontend
- 🔨 Agregar selector de almacén en facturas
- 🔨 Crear módulo completo de stock por almacén
- ✅ Sistema más robusto y escalable

### **Si eligen Opción C (Híbrido)**
- ✅ Mínimos cambios iniciales
- ✅ Validación simple sigue funcionando
- 🔨 Opción de expandir a gestión avanzada después

---

## 🚀 Solicitud de Acción

Por favor, **respondan a este documento** con:

1. ✅ **Aclaración de las 10 preguntas** de la sección "Preguntas Prioritarias"

2. ✅ **Decisión sobre la opción a seguir**:
   - Opción A: Stock Simple
   - Opción B: Solo Almacenes
   - Opción C: Híbrido
   - Otra propuesta

3. ✅ **Actualización de la especificación OpenAPI** si hay cambios:
   - Deprecar campos obsoletos
   - Agregar campos nuevos (ej: `almacenId` en `FacturaItemRequest`)
   - Documentar comportamientos (descuento automático, cálculo de stock, etc.)

4. ✅ **Ejemplos de respuestas** de los endpoints clave:
   - `GET /api/productos/{id}` - ¿Cómo se ve `stock` y `stockDetalle`?
   - `GET /api/stock?productoId={id}` - Estructura completa de respuesta
   - `POST /api/facturas` - ¿Qué pasa con el stock al emitir?

---

## 📝 Contexto Adicional

- **Fecha**: 2025-11-17
- **Módulo en desarrollo**: Facturas (frontend)
- **Estado**: Bloqueado por ambigüedad de stock
- **Urgencia**: Alta - Afecta validaciones y flujo de facturas

---

**Generado desde**: `facturacion-inventario-frontend-nextjs`  
**Para**: Backend Spring Boot (inventario-repuestos-backend)  
**Objetivo**: Definir arquitectura clara de stock para continuar desarrollo del módulo de facturas
