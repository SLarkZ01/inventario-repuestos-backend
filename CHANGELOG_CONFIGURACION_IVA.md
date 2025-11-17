# 🎯 CHANGELOG - Sistema de Configuración Global de IVA

**Fecha:** 2025-11-17  
**Versión:** 2.1  
**Estado:** ✅ IMPLEMENTADO Y DOCUMENTADO

---

## 📋 RESUMEN EJECUTIVO

Se ha implementado un **sistema de configuración global** que permite cambiar la **tasa de IVA por defecto** desde el frontend (panel de administración), eliminando el hardcoding del 19% y permitiendo adaptarse a cambios en la legislación colombiana.

---

## ✨ NUEVAS FUNCIONALIDADES

### 1. **Configuración Global del Sistema** ✅

Se creó un nuevo módulo completo para gestionar configuraciones a nivel de sistema:

#### Modelo: `ConfiguracionGlobal`
- **Colección MongoDB:** `configuracion_global`
- **Singleton:** Solo existe UN documento con `clave = "GLOBAL"`
- **Inicialización automática:** Si no existe, se crea con valores por defecto

**Campos implementados:**

| Campo | Tipo | Descripción |
|-------|------|-------------|
| `tasaIvaPorDefecto` | Double | IVA por defecto (%) para nuevos productos |
| `nombreEmpresa` | String | Razón social de la empresa |
| `nit` | String | NIT sin dígito de verificación |
| `digitoVerificacion` | String | Dígito de verificación del NIT |
| `direccion` | String | Dirección de la empresa |
| `telefono` | String | Teléfono de contacto |
| `email` | String | Email de contacto |
| `ciudad` | String | Ciudad de la empresa |
| `departamento` | String | Departamento de la empresa |
| `prefijoFactura` | String | Prefijo para facturas (ej: "FV") |
| `resolucionDian` | String | Número de resolución DIAN |
| `fechaResolucionDian` | LocalDateTime | Fecha de la resolución |
| `rangoFacturaInicio` | Long | Rango inicial autorizado |
| `rangoFacturaFin` | Long | Rango final autorizado |
| `proximoNumeroFactura` | Long | Próximo número a asignar |
| `actualizadoEn` | LocalDateTime | Última actualización |

### 2. **Nuevos Endpoints REST** ✅

#### `GET /api/configuracion`
**Rol requerido:** `ADMIN`

Obtiene la configuración completa del sistema.

**Respuesta:**
```json
{
  "id": "507f191e810c19729de860ea",
  "tasaIvaPorDefecto": 19.0,
  "nombreEmpresa": "Repuestos ABC S.A.S",
  "nit": "900123456",
  "digitoVerificacion": "7",
  "direccion": "Calle 123 #45-67",
  "telefono": "3001234567",
  "email": "contacto@repuestos.com",
  "ciudad": "Bogotá",
  "departamento": "Cundinamarca",
  "prefijoFactura": "FV",
  "resolucionDian": "18764123456789",
  "rangoFacturaInicio": 1,
  "rangoFacturaFin": 5000,
  "proximoNumeroFactura": 1,
  "actualizadoEn": "2025-01-17T10:30:00"
}
```

#### `PUT /api/configuracion`
**Rol requerido:** `ADMIN`

Actualiza la configuración (actualización parcial - solo campos enviados).

**Request (ejemplo: cambiar IVA):**
```json
{
  "tasaIvaPorDefecto": 21.0
}
```

**Request (configuración completa):**
```json
{
  "tasaIvaPorDefecto": 19.0,
  "nombreEmpresa": "Repuestos ABC S.A.S",
  "nit": "900123456",
  "digitoVerificacion": "7",
  "direccion": "Calle 123 #45-67",
  "telefono": "3001234567",
  "email": "contacto@repuestos.com",
  "ciudad": "Bogotá",
  "departamento": "Cundinamarca",
  "prefijoFactura": "FV",
  "resolucionDian": "18764123456789",
  "rangoFacturaInicio": 1,
  "rangoFacturaFin": 5000
}
```

#### `GET /api/configuracion/iva-defecto`
**Rol requerido:** `ADMIN` o `VENDEDOR`

Obtiene únicamente la tasa de IVA por defecto (útil para formularios).

**Respuesta:**
```json
{
  "tasaIvaPorDefecto": 19.0
}
```

### 3. **Integración con Productos** ✅

- El servicio `ProductoService` ahora **consume automáticamente** el IVA desde la configuración
- Al crear un producto sin especificar `tasaIva`, se usa el valor configurado globalmente
- **NO afecta productos existentes**, solo nuevos productos

**Antes:**
```java
// Hardcodeado
p.setTasaIva(req.getTasaIva() != null ? req.getTasaIva() : 19.0);
```

**Ahora:**
```java
// Dinámico desde configuración
p.setTasaIva(req.getTasaIva() != null ? req.getTasaIva() : 
    configuracionGlobalService.obtenerTasaIvaPorDefecto());
```

### 4. **Limpieza de Categorías** ✅

- ✅ **Eliminado:** Campo `iconoRecurso` del modelo `Categoria`
- ✅ **Motivo:** No se estaba usando y causaba errores
- ✅ **Afectación:** Ninguna (DTOs nunca lo tuvieron)

---

## 📂 ARCHIVOS CREADOS

### Nuevos Archivos (7)

```
src/main/java/com/repobackend/api/configuracion/
├── model/
│   └── ConfiguracionGlobal.java          ✅ Modelo de datos
├── repository/
│   └── ConfiguracionGlobalRepository.java ✅ Repositorio MongoDB
├── service/
│   └── ConfiguracionGlobalService.java   ✅ Lógica de negocio
├── controller/
│   └── ConfiguracionGlobalController.java ✅ API REST
└── dto/
    ├── ConfiguracionGlobalRequest.java   ✅ DTO de entrada
    └── ConfiguracionGlobalResponse.java  ✅ DTO de salida
```

---

## 🔄 ARCHIVOS MODIFICADOS

### 1. `ProductoService.java`
- Agregado: Import de `ConfiguracionGlobalService`
- Agregado: Inyección de dependencia en constructor
- Modificado: Método `toEntity()` usa IVA desde configuración

### 2. `Categoria.java`
- Eliminado: Campo `iconoRecurso`
- Eliminado: Getters/Setters de `iconoRecurso`

### 3. `docs/openapi.yaml` & `docs/api.json`
- ✅ **Regenerada:** Documentación completa con nuevos endpoints
- ✅ **Tag nueva:** "Configuración Global"
- ✅ **Ejemplos:** Incluye ejemplos de uso de configuración

---

## 🚀 CÓMO USAR (FRONTEND Next.js)

### 1. Pantalla de Configuración (Admin)

```typescript
// Obtener configuración actual
const getConfig = async () => {
  const response = await fetch('/api/configuracion', {
    headers: { 
      'Authorization': `Bearer ${token}` 
    }
  });
  const config = await response.json();
  return config;
};

// Actualizar solo el IVA
const updateIVA = async (nuevoIVA: number) => {
  await fetch('/api/configuracion', {
    method: 'PUT',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      tasaIvaPorDefecto: nuevoIVA
    })
  });
};

// Componente de ejemplo
function ConfiguracionPanel() {
  const [iva, setIva] = useState(19.0);

  const handleSave = () => {
    updateIVA(iva);
    toast.success('IVA actualizado correctamente');
  };

  return (
    <div>
      <h2>Configuración de IVA</h2>
      <input 
        type="number" 
        value={iva} 
        onChange={(e) => setIva(parseFloat(e.target.value))}
        step="0.01"
      />
      <button onClick={handleSave}>Guardar</button>
      <p className="text-sm text-gray-500">
        Cambios solo afectan productos nuevos
      </p>
    </div>
  );
}
```

### 2. Formulario de Producto (Pre-cargar IVA)

```typescript
// Obtener IVA por defecto para prellenar formulario
const getIVADefecto = async () => {
  const response = await fetch('/api/configuracion/iva-defecto', {
    headers: { 'Authorization': `Bearer ${token}` }
  });
  const { tasaIvaPorDefecto } = await response.json();
  return tasaIvaPorDefecto;
};

// Al abrir formulario de nuevo producto
useEffect(() => {
  getIVADefecto().then(iva => {
    setFormData(prev => ({ ...prev, tasaIva: iva }));
  });
}, []);
```

---

## ⚠️ IMPORTANTE - COMPATIBILIDAD

### Backend → Frontend

**✅ NO hay breaking changes:**
- Productos existentes mantienen su IVA actual
- Endpoint de productos (`/api/productos`) sigue funcionando igual
- El campo `tasaIva` ya existía desde v2.0

### Backend → App Android

**✅ NO hay breaking changes:**
- Endpoints públicos (`/api/public/productos`) sin cambios
- El campo `tasaIva` ya estaba disponible
- App Android solo lee, no escribe configuración

---

## 🔒 SEGURIDAD Y PERMISOS

| Endpoint | Rol Mínimo | Notas |
|----------|-----------|-------|
| `GET /api/configuracion` | `ADMIN` | Solo admins ven configuración completa |
| `PUT /api/configuracion` | `ADMIN` | Solo admins pueden cambiar IVA |
| `GET /api/configuracion/iva-defecto` | `ADMIN`, `VENDEDOR` | Vendedores pueden consultar para formularios |

---

## 📊 CASOS DE USO

### Caso 1: Cambio de IVA en Colombia
**Escenario:** El gobierno cambia el IVA de 19% a 21%

**Pasos:**
1. Admin ingresa a "Configuración" en Next.js
2. Cambia `tasaIvaPorDefecto` de 19 a 21
3. Hace clic en "Guardar"
4. Nuevos productos creados tendrán IVA 21%
5. Productos existentes mantienen su IVA (19%)

### Caso 2: Configurar Datos de Empresa
**Escenario:** Primera configuración del sistema

**Pasos:**
1. Admin ingresa a "Configuración"
2. Completa todos los campos (NIT, dirección, etc.)
3. Guarda
4. Datos quedan listos para facturación DIAN

### Caso 3: Configurar Resolución DIAN
**Escenario:** Se obtiene autorización de facturación electrónica

**Pasos:**
1. Admin ingresa resolución DIAN en configuración
2. Establece rango de facturas autorizado (1 - 5000)
3. Sistema usará estos datos en facturas futuras

---

## 🧪 TESTING

### Test Manual 1: Obtener Configuración
```bash
curl -H "Authorization: Bearer TOKEN_ADMIN" \
  http://localhost:8080/api/configuracion
```

**Esperado:** JSON con configuración completa

### Test Manual 2: Cambiar IVA
```bash
curl -X PUT \
  -H "Authorization: Bearer TOKEN_ADMIN" \
  -H "Content-Type: application/json" \
  -d '{"tasaIvaPorDefecto": 21.0}' \
  http://localhost:8080/api/configuracion
```

**Esperado:** Configuración actualizada con IVA 21%

### Test Manual 3: Crear Producto sin IVA
```bash
curl -X POST \
  -H "Authorization: Bearer TOKEN_ADMIN" \
  -H "Content-Type: application/json" \
  -d '{"nombre": "Producto Test", "precio": 50000}' \
  http://localhost:8080/api/productos
```

**Esperado:** Producto creado con IVA = configurado (21%)

### Test Manual 4: Crear Producto con IVA Custom
```bash
curl -X POST \
  -H "Authorization: Bearer TOKEN_ADMIN" \
  -H "Content-Type: application/json" \
  -d '{"nombre": "Producto Test", "precio": 50000, "tasaIva": 5.0}' \
  http://localhost:8080/api/productos
```

**Esperado:** Producto creado con IVA = 5% (override)

---

## 📝 DOCUMENTACIÓN OPENAPI

### Tag Nueva: "Configuración Global"
- 3 endpoints documentados
- Ejemplos completos de request/response
- Descripción de cada campo
- Casos de uso explicados

### Actualizado: "Productos"
- Documentación del campo `tasaIva` mejorada
- Explicación de IVA por defecto
- Ejemplos con IVA

---

## ✅ CHECKLIST DE IMPLEMENTACIÓN

- ✅ Modelo `ConfiguracionGlobal` creado
- ✅ Repository MongoDB creado
- ✅ Service con lógica de negocio
- ✅ Controller REST con 3 endpoints
- ✅ DTOs (Request/Response)
- ✅ Integración con `ProductoService`
- ✅ Documentación OpenAPI regenerada
- ✅ Eliminado campo `iconoRecurso` de categorías
- ✅ Compilación exitosa (BUILD SUCCESS)
- ✅ Documentación completa (este archivo)

---

## 🔮 PRÓXIMOS PASOS RECOMENDADOS

### Frontend (Next.js)

1. **Crear página de Configuración** (`/admin/configuracion`)
   - Form para cambiar IVA
   - Form para datos de empresa
   - Form para resolución DIAN

2. **Actualizar formulario de productos**
   - Pre-cargar IVA desde `/api/configuracion/iva-defecto`
   - Permitir override si es necesario

3. **Dashboard de facturación**
   - Mostrar datos de empresa configurados
   - Mostrar rango de facturas disponibles

### Backend (Futuro)

1. **Usar `proximoNumeroFactura`** en `FacturaService`
   - Incrementar automáticamente
   - Validar rango DIAN

2. **Validaciones adicionales**
   - Validar NIT con dígito de verificación
   - Validar rango de facturas no excedido

3. **Historial de cambios**
   - Auditoría de cambios de IVA
   - Log de configuraciones anteriores

---

## 📊 ESTADO DEL PROYECTO

| Componente | Estado | Comentario |
|------------|--------|------------|
| **Modelo ConfiguracionGlobal** | ✅ 100% | Completo con todos los campos DIAN |
| **API REST Configuración** | ✅ 100% | 3 endpoints documentados |
| **Integración Productos** | ✅ 100% | IVA dinámico desde config |
| **Limpieza Categorías** | ✅ 100% | iconoRecurso eliminado |
| **Documentación OpenAPI** | ✅ 100% | Regenerada y actualizada |
| **Frontend Next.js** | ⏳ 0% | Pendiente crear pantalla config |
| **App Android** | ✅ N/A | No requiere cambios |

---

## 🎯 CONCLUSIÓN

El sistema ahora permite **configurar el IVA por defecto desde el frontend** sin necesidad de modificar código. Esto cumple con el requisito de adaptarse a cambios frecuentes en la legislación tributaria colombiana.

**Beneficios:**
- ✅ No más hardcoding de tasas de IVA
- ✅ Configuración centralizada
- ✅ Preparado para integración DIAN
- ✅ Datos de empresa persistidos
- ✅ Documentación completa

**Impacto:**
- ✅ Sin breaking changes
- ✅ Productos existentes no afectados
- ✅ Compatible con frontend y app Android actuales

---

**Compilación:** ✅ BUILD SUCCESS  
**Documentación:** ✅ COMPLETA  
**Estado:** ✅ LISTO PARA USO

---

**Autor:** GitHub Copilot  
**Fecha:** 2025-11-17  
**Versión Backend:** 2.1

