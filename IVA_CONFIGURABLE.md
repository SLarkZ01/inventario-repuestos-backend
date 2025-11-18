# IVA CONFIGURABLE - Sistema de Facturación

**Fecha de implementación:** 2025-11-17

## 📋 Resumen

Se ha implementado un sistema de configuración global que permite modificar el valor del IVA por defecto desde el frontend. Esta configuración es global para todo el sistema y se aplica a todos los talleres.

## 🎯 Problema Resuelto

En Colombia, el IVA cambia constantemente por regulaciones gubernamentales. Anteriormente, el IVA estaba hardcodeado en el código, lo que requería:
- Modificar código fuente
- Recompilar la aplicación
- Redesplegar el sistema

Ahora, el IVA se puede cambiar desde el panel de administración sin necesidad de modificar código.

## 🏗️ Arquitectura Implementada

### 1. Modelo de Datos

**ConfiguracionGlobal.java**
```java
@Document(collection = "configuracion_global")
public class ConfiguracionGlobal {
    @Id
    private String id;
    
    @Field("iva_por_defecto")
    private Double ivaPorDefecto; // Ejemplo: 19.0 para 19%
    
    @Field("fecha_actualizacion")
    private Instant fechaActualizacion;
    
    @Field("actualizado_por")
    private String actualizadoPor; // ID del usuario que actualizó
}
```

**Almacenamiento:** MongoDB, colección `configuracion_global`
**Singleton:** Solo existe un documento de configuración global

### 2. Servicios

**ConfiguracionGlobalService.java**
- `obtenerConfiguracion()`: Obtiene la configuración global (crea una por defecto si no existe)
- `actualizarConfiguracion()`: Actualiza el IVA y registra quién y cuándo lo modificó
- `obtenerIvaPorDefecto()`: Devuelve el IVA actual (19% por defecto)

**Valor por defecto:** 19.0%

### 3. API REST

**Endpoint de Configuración Global**

```http
### Obtener configuración actual
GET /api/configuracion-global
Authorization: Bearer {token}

Response 200:
{
  "id": "673a4c3e9e8a3c4d5f6e7f8a",
  "ivaPorDefecto": 19.0,
  "fechaActualizacion": "2025-11-17T20:15:30.123Z",
  "actualizadoPor": "690d34252d7f961378d9f590"
}

### Actualizar IVA
POST /api/configuracion-global
Content-Type: application/json
Authorization: Bearer {token}

{
  "ivaPorDefecto": 21.0
}

Response 200:
{
  "id": "673a4c3e9e8a3c4d5f6e7f8a",
  "ivaPorDefecto": 21.0,
  "fechaActualizacion": "2025-11-17T20:30:45.789Z",
  "actualizadoPor": "690d34252d7f961378d9f590"
}
```

**Seguridad:** 
- Requiere autenticación JWT
- Solo usuarios con rol `ADMIN` pueden actualizar la configuración
- Los usuarios con rol `VENDEDOR` pueden consultar la configuración

### 4. Integración con Facturación

**FacturaService.java** - Actualizado para usar IVA dinámico:

```java
public Factura crearFactura(FacturaRequest request, Authentication auth) {
    // Obtiene el IVA actual de la configuración global
    Double ivaPorDefecto = configuracionGlobalService.obtenerIvaPorDefecto();
    
    // Aplica el IVA a cada item
    for (FacturaItem item : items) {
        Double tasaIva = item.getProducto().getTasaIva() != null 
            ? item.getProducto().getTasaIva() 
            : ivaPorDefecto;
            
        item.setPorcentajeIva(tasaIva);
        item.setValorIva(item.getSubtotal() * tasaIva / 100);
    }
}
```

**Prioridad:**
1. Si el producto tiene `tasaIva` específica → se usa esa
2. Si no → se usa el IVA global configurable

## 📊 Flujo de Datos

```
┌─────────────────────┐
│   Frontend Admin    │
│  (Configuración)    │
└──────────┬──────────┘
           │ POST /api/configuracion-global
           │ { ivaPorDefecto: 21.0 }
           ↓
┌─────────────────────────────────┐
│  ConfiguracionGlobalController  │
│  - Valida permisos ADMIN       │
│  - Valida datos (0.1 - 100)    │
└──────────┬──────────────────────┘
           │
           ↓
┌─────────────────────────────────┐
│  ConfiguracionGlobalService     │
│  - Actualiza MongoDB            │
│  - Registra usuario y fecha     │
└──────────┬──────────────────────┘
           │
           ↓
┌─────────────────────────────────┐
│        MongoDB                  │
│  Collection: configuracion_     │
│              global             │
│  {                              │
│    ivaPorDefecto: 21.0,         │
│    fechaActualizacion: ...      │
│  }                              │
└─────────────────────────────────┘

Cuando se crea una factura:
┌─────────────────────┐
│   Frontend Ventas   │
│  (Nueva Factura)    │
└──────────┬──────────┘
           │ POST /api/facturas
           ↓
┌─────────────────────────────────┐
│     FacturaService              │
│  1. obtenerIvaPorDefecto()      │
│  2. Aplica IVA a productos      │
│  3. Calcula totales             │
└─────────────────────────────────┘
```

## 🎨 UI - Panel de Configuración (Frontend)

Se debe implementar en el frontend (Next.js):

**Ruta:** `/admin/configuracion`

**Componente sugerido:**
```tsx
// Pantalla de configuración
- Input numérico para IVA (0.1 - 100)
- Validación en tiempo real
- Botón "Guardar"
- Muestra última actualización y quién la hizo
- Solo accesible para ADMIN
```

**Ejemplo de UI:**
```
┌────────────────────────────────────────┐
│  ⚙️  Configuración Global               │
├────────────────────────────────────────┤
│                                        │
│  IVA por Defecto (%)                   │
│  ┌──────────┐                          │
│  │  19.0    │  %                       │
│  └──────────┘                          │
│                                        │
│  ℹ️  Este IVA se aplicará a todos los  │
│     productos que no tengan un IVA     │
│     específico configurado.            │
│                                        │
│  📅 Última actualización:               │
│     2025-11-17 15:30                   │
│     por: admin@sistema.com             │
│                                        │
│  [ Cancelar ]  [ Guardar Cambios ]     │
└────────────────────────────────────────┘
```

## 🔒 Permisos y Seguridad

| Rol      | GET Configuración | POST/PUT Configuración |
|----------|-------------------|------------------------|
| ADMIN    | ✅ Permitido      | ✅ Permitido           |
| VENDEDOR | ✅ Permitido      | ❌ Denegado            |
| CLIENTE  | ❌ Denegado       | ❌ Denegado            |

**Validaciones:**
- IVA debe estar entre 0.1 y 100
- Solo números válidos
- Auditoría de cambios (quién y cuándo)

## 📝 Ejemplos de Uso

### 1. Obtener IVA Actual desde Frontend

```typescript
// En el frontend (Next.js)
const obtenerConfiguracion = async () => {
  const response = await fetch('http://localhost:8080/api/configuracion-global', {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
  const config = await response.json();
  console.log(`IVA actual: ${config.ivaPorDefecto}%`);
};
```

### 2. Actualizar IVA

```typescript
const actualizarIVA = async (nuevoIVA: number) => {
  const response = await fetch('http://localhost:8080/api/configuracion-global', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify({
      ivaPorDefecto: nuevoIVA
    })
  });
  
  if (response.ok) {
    alert('IVA actualizado correctamente');
  }
};
```

### 3. Crear Factura con IVA Dinámico

```typescript
// El IVA se aplica automáticamente en el backend
const crearFactura = async (datosFactura) => {
  const response = await fetch('http://localhost:8080/api/facturas', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify({
      cliente: {
        nombre: "Juan Pérez",
        documento: "123456789",
        direccion: "Calle 123"
      },
      items: [
        {
          productoId: "691a725aaba13b365dff6b93",
          cantidad: 2
        }
      ]
    })
  });
  
  // El backend aplicará el IVA configurado automáticamente
  const factura = await response.json();
};
```

## 🔄 Actualización de Open API

Se ha actualizado la documentación de Open API para reflejar:

✅ **ConfiguracionGlobalController**
- GET `/api/configuracion-global` - Obtener configuración
- POST `/api/configuracion-global` - Actualizar configuración

✅ **FacturasController**  
- Documentación actualizada indicando que usa IVA dinámico

✅ **ProductosController**
- Campo `tasaIva` opcional en productos
- Si no se especifica, usa IVA global

## 🧪 Testing

### Probar desde Postman/Curl

```bash
# 1. Obtener configuración actual
curl -X GET http://localhost:8080/api/configuracion-global \
  -H "Authorization: Bearer YOUR_TOKEN"

# 2. Actualizar IVA a 21%
curl -X POST http://localhost:8080/api/configuracion-global \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{"ivaPorDefecto": 21.0}'

# 3. Crear factura (usará el nuevo IVA)
curl -X POST http://localhost:8080/api/facturas \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "cliente": {
      "nombre": "Test Cliente",
      "documento": "123456",
      "direccion": "Test"
    },
    "items": [{
      "productoId": "PRODUCT_ID",
      "cantidad": 1
    }]
  }'
```

## 📚 Documentación OpenAPI

Acceder a: `http://localhost:8080/docs/openapi.yaml`

Los cambios se reflejan automáticamente gracias a las anotaciones:

```java
@Tag(name = "Configuración Global", description = "Gestión de configuración global del sistema")
@Operation(summary = "Obtener configuración global")
@SecurityRequirement(name = "bearerAuth")
```

## 🚀 Próximos Pasos para Frontend

1. **Crear pantalla de configuración:**
   - Ruta: `/admin/configuracion`
   - Solo accesible para ADMIN
   - Input para modificar IVA
   - Mostrar última actualización

2. **Actualizar formulario de productos:**
   - Campo opcional `tasaIva` para IVA específico
   - Si no se llena, indica que usará IVA global

3. **Panel de facturas:**
   - Mostrar IVA aplicado en cada item
   - Indicar si es IVA global o específico del producto

## ⚠️ Consideraciones Importantes

1. **Cambio de IVA no es retroactivo:**
   - Las facturas ya emitidas conservan el IVA con el que fueron generadas
   - Solo afecta a nuevas facturas

2. **IVA por Producto vs Global:**
   - Productos pueden tener `tasaIva` específica (ej: productos exentos = 0%)
   - Si no tienen `tasaIva`, usan el IVA global
   - Prioridad: IVA producto > IVA global

3. **Auditoría:**
   - Cada cambio de IVA queda registrado
   - Se guarda quién y cuándo cambió el IVA
   - Útil para reportes y compliance

4. **Validaciones:**
   - IVA debe ser número decimal
   - Rango: 0.1% - 100%
   - No puede ser null o vacío

## 📖 Referencias

- **DIAN Colombia:** [Normatividad IVA](https://www.dian.gov.co)
- **Spring Data MongoDB:** Configuración de documentos únicos
- **OpenAPI 3.0:** Documentación automática de APIs

---

**Desarrollado:** 2025-11-17  
**Estado:** ✅ Implementado y funcional  
**Última actualización:** 2025-11-17

