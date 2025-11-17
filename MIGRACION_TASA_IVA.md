
# 📱 Guía de Migración - Campo tasaIva en Productos

## 🎯 Cambio Implementado

Se ha agregado el campo **`tasaIva`** (tasa de IVA en porcentaje) al modelo `Producto`.

### Para qué sirve
- Permite calcular automáticamente el IVA al generar facturas
- El backend usa este valor para calcular `valorIva` y `total` en facturas
- Valores comunes en Colombia: **0%** (exento), **5%** (canasta básica), **19%** (estándar)

---

## 📊 Cambios en API

### Endpoint: `GET /api/public/productos` (Android)

**Antes:**
```json
{
  "productos": [{
    "id": "507f191e810c19729de860ea",
    "nombre": "Filtro de Aceite",
    "precio": 25000,
    "stock": 50
  }]
}
```

**Ahora (v2.0):**
```json
{
  "productos": [{
    "id": "507f191e810c19729de860ea",
    "nombre": "Filtro de Aceite",
    "precio": 25000,
    "tasaIva": 19.0,  // ⬅️ NUEVO
    "stock": 50
  }]
}
```

### Endpoint: `GET /api/public/productos/{id}` (Android)

**Ahora incluye:**
```json
{
  "producto": {
    "id": "507f191e810c19729de860ea",
    "nombre": "Filtro de Aceite",
    "precio": 25000,
    "tasaIva": 19.0,  // ⬅️ NUEVO
    "totalStock": 50
  }
}
```

### Endpoint: `POST /api/productos` (Next.js Admin)

**Request (crear producto):**
```json
{
  "nombre": "Filtro de Aceite",
  "precio": 25000,
  "tasaIva": 19.0,  // ⬅️ NUEVO (opcional, default: 19%)
  "stock": 100,
  "categoriaId": "cat123"
}
```

**Response:**
```json
{
  "producto": {
    "id": "507f191e810c19729de860ea",
    "nombre": "Filtro de Aceite",
    "precio": 25000,
    "tasaIva": 19.0,  // ⬅️ NUEVO
    "stock": 100
  }
}
```

---

## 📱 Migración App Android (Kotlin)

### 1. Actualizar modelo Producto

```kotlin
// Antes
data class Producto(
    val id: String,
    val nombre: String,
    val precio: Double,
    val stock: Int
)

// Ahora
data class Producto(
    val id: String,
    val nombre: String,
    val precio: Double,
    val tasaIva: Double = 19.0,  // ⬅️ NUEVO con default
    val stock: Int
)
```

### 2. Calcular precio con IVA (opcional, para mostrar)

```kotlin
fun Producto.precioConIva(): Double {
    return precio * (1 + tasaIva / 100.0)
}

// Ejemplo de uso en UI
val producto = Producto(
    id = "123",
    nombre = "Filtro",
    precio = 25000.0,
    tasaIva = 19.0,
    stock = 50
)

// Mostrar precio base
Text("Precio: $${producto.precio}")

// Mostrar precio con IVA
Text("Precio final (IVA incluido): $${producto.precioConIva()}")
// Resultado: "Precio final (IVA incluido): $29750.0"
```

### 3. Vista de producto

```kotlin
@Composable
fun ProductoCard(producto: Producto) {
    Card {
        Column {
            Text(producto.nombre, style = MaterialTheme.typography.h6)
            
            // Precio base
            Text("Precio base: $${producto.precio}", 
                 style = MaterialTheme.typography.body2)
            
            // IVA
            Text("IVA ${producto.tasaIva}%", 
                 color = Color.Gray)
            
            // Precio final
            Text("Total: $${producto.precioConIva()}", 
                 style = MaterialTheme.typography.h5,
                 fontWeight = FontWeight.Bold)
        }
    }
}
```

### 4. **NO es necesario** enviar tasaIva al checkout

El backend calcula automáticamente el IVA desde el producto:

```kotlin
// ✅ CORRECTO - No enviar tasaIva al checkout
val checkoutRequest = CheckoutRequest(
    carritoId = carritoId
)

// ❌ NO HACER - No intentar enviar tasaIva
// El backend lo toma del producto automáticamente
```

---

## 🌐 Migración Next.js (Frontend Admin)

### 1. Actualizar tipo TypeScript

```typescript
// types/producto.ts

// Antes
interface Producto {
  id: string;
  nombre: string;
  precio: number;
  stock: number;
}

// Ahora
interface Producto {
  id: string;
  nombre: string;
  precio: number;
  tasaIva?: number;  // ⬅️ NUEVO (opcional porque tiene default)
  stock: number;
}
```

### 2. Formulario de crear/editar producto

```typescript
// components/ProductoForm.tsx

import { useState } from 'react';

export default function ProductoForm() {
  const [formData, setFormData] = useState({
    nombre: '',
    precio: 0,
    tasaIva: 19,  // ⬅️ NUEVO (default 19%)
    stock: 0,
  });

  return (
    <form onSubmit={handleSubmit}>
      <input
        type="text"
        value={formData.nombre}
        onChange={(e) => setFormData({...formData, nombre: e.target.value})}
        placeholder="Nombre"
      />
      
      <input
        type="number"
        value={formData.precio}
        onChange={(e) => setFormData({...formData, precio: Number(e.target.value)})}
        placeholder="Precio"
      />
      
      {/* ⬅️ NUEVO Campo IVA */}
      <select
        value={formData.tasaIva}
        onChange={(e) => setFormData({...formData, tasaIva: Number(e.target.value)})}
      >
        <option value={0}>0% - Exento</option>
        <option value={5}>5% - Canasta básica</option>
        <option value={19}>19% - Estándar</option>
      </select>
      
      <button type="submit">Guardar</button>
    </form>
  );
}
```

### 3. Mostrar IVA en tabla de productos

```typescript
// components/ProductoTable.tsx

export default function ProductoTable({ productos }: { productos: Producto[] }) {
  return (
    <table>
      <thead>
        <tr>
          <th>Nombre</th>
          <th>Precio</th>
          <th>IVA</th>  {/* ⬅️ NUEVO */}
          <th>Precio + IVA</th>  {/* ⬅️ NUEVO */}
          <th>Stock</th>
        </tr>
      </thead>
      <tbody>
        {productos.map(p => (
          <tr key={p.id}>
            <td>{p.nombre}</td>
            <td>${p.precio.toLocaleString()}</td>
            <td>{p.tasaIva || 19}%</td>  {/* ⬅️ NUEVO */}
            <td>${((p.precio * (1 + (p.tasaIva || 19) / 100))).toLocaleString()}</td>
            <td>{p.stock}</td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}
```

### 4. Cliente API actualizado

```typescript
// lib/api/productos.ts

export async function crearProducto(data: {
  nombre: string;
  precio: number;
  tasaIva?: number;  // ⬅️ NUEVO (opcional)
  stock: number;
  categoriaId: string;
}) {
  const response = await fetch('/api/productos', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      ...data,
      tasaIva: data.tasaIva ?? 19  // Default 19% si no se especifica
    })
  });
  
  return response.json();
}
```

---

## ⚠️ Compatibilidad Hacia Atrás

### ✅ NO rompe compatibilidad

- Si el frontend **no envía** `tasaIva` al crear producto → el backend asigna **19% automáticamente**
- Si el frontend **no lee** `tasaIva` de la respuesta → simplemente ignora el campo
- El checkout **NO requiere** que el frontend envíe IVA (el backend lo calcula)

### 🔄 Productos existentes (sin tasaIva)

Ejecutar script de migración en MongoDB:

```bash
# Actualizar todos los productos sin tasaIva
mongo inventario_db < scripts/actualizar_iva_productos.js
```

O manualmente:
```javascript
db.productos.updateMany(
  { tasaIva: { $exists: false } },
  { $set: { tasaIva: 19.0 } }
)
```

---

## 🧪 Testing

### Test 1: Crear producto sin especificar IVA

```bash
curl -X POST http://localhost:8080/api/productos \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Producto Test",
    "precio": 10000,
    "stock": 50
  }'

# Respuesta esperada:
# { "producto": { "tasaIva": 19.0, ... } }  ← Default 19%
```

### Test 2: Crear producto con IVA 5%

```bash
curl -X POST http://localhost:8080/api/productos \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Producto Básico",
    "precio": 5000,
    "tasaIva": 5.0,
    "stock": 100
  }'

# Respuesta esperada:
# { "producto": { "tasaIva": 5.0, ... } }
```

### Test 3: Verificar en checkout

```bash
# 1. Agregar al carrito
curl -X POST http://localhost:8080/api/carritos/{id}/items \
  -d '{ "productoId": "...", "cantidad": 2 }'

# 2. Hacer checkout
curl -X POST http://localhost:8080/api/facturas/checkout \
  -d '{ "carritoId": "..." }'

# Respuesta esperada (fragmento):
# {
#   "factura": {
#     "items": [{
#       "nombreProducto": "Producto Test",
#       "precioUnitario": 10000,
#       "tasaIva": 19.0,        ← IVA del producto
#       "valorIva": 3800,       ← Calculado: 10000*2*19%
#       "totalItem": 23800      ← Con IVA incluido
#     }],
#     "subtotal": 20000,
#     "totalIva": 3800,
#     "total": 23800
#   }
# }
```

---

## 📚 Documentación OpenAPI Actualizada

La documentación en Swagger ahora incluye:

1. **POST /api/productos** - Muestra `tasaIva` como campo opcional con default 19%
2. **GET /api/productos/{id}** - Incluye `tasaIva` en respuesta de ejemplo
3. **GET /api/public/productos** - Incluye `tasaIva` para la app Android
4. **GET /api/public/productos/{id}** - Incluye `tasaIva` con ejemplo de cálculo

### Ver documentación actualizada:

1. Iniciar backend: `./mvnw spring-boot:run`
2. Abrir: http://localhost:8080/swagger-ui/index.html
3. Buscar endpoints de "Productos"
4. Ver ejemplos actualizados con `tasaIva`

---

## ✅ Checklist de Migración

### Android
- [ ] Actualizar modelo `Producto` con campo `tasaIva: Double = 19.0`
- [ ] Regenerar cliente API desde OpenAPI (`/api/public/productos`)
- [ ] (Opcional) Implementar `precioConIva()` para mostrar precio final
- [ ] Actualizar UI para mostrar IVA si es relevante
- [ ] Testing: verificar que parsea correctamente el nuevo campo

### Next.js
- [ ] Actualizar interface `Producto` con `tasaIva?: number`
- [ ] Regenerar cliente TypeScript desde OpenAPI (`/api/productos`)
- [ ] Actualizar formulario de producto con selector de IVA
- [ ] Actualizar tabla/lista de productos para mostrar IVA
- [ ] Testing: crear producto con IVA 5%, 19%, verificar en DB

### Backend
- [x] ✅ Campo `tasaIva` agregado a modelo `Producto`
- [x] ✅ DTOs actualizados (`ProductoRequest`, `ProductoResponse`)
- [x] ✅ Servicio asigna default 19% si no se envía
- [x] ✅ Documentación OpenAPI actualizada
- [x] ✅ Script de migración creado

### Base de Datos
- [ ] Ejecutar script de migración: `mongo < scripts/actualizar_iva_productos.js`
- [ ] Verificar que productos existentes tienen `tasaIva: 19.0`

---

## 🆘 Soporte

### Pregunta: ¿Qué pasa si no actualizo el frontend?

**Respuesta:** Sigue funcionando. El backend asigna 19% por defecto si no envías `tasaIva`.

### Pregunta: ¿Debo actualizar la app Android inmediatamente?

**Respuesta:** No es urgente. La app seguirá funcionando porque:
- El campo `tasaIva` viene con valor por defecto (19%)
- El checkout no requiere que envíes IVA
- Solo actualiza cuando quieras mostrar el IVA en la UI

### Pregunta: ¿Cómo cambio el IVA de productos existentes?

**Respuesta:** Usa el endpoint PATCH/PUT:
```bash
curl -X PATCH http://localhost:8080/api/productos/{id} \
  -d '{ "tasaIva": 5.0 }'
```

### Pregunta: ¿Los precios en la app deben incluir IVA?

**Respuesta:** Depende de tu país/regulación. En Colombia:
- **Opción 1:** Mostrar precio sin IVA + IVA aparte
- **Opción 2:** Mostrar precio con IVA incluido (más común)

---

**Fecha:** 2025-01-16  
**Versión API:** v2.0  
**Breaking Changes:** ❌ Ninguno (compatible hacia atrás)

