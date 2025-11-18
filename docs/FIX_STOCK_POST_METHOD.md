# Fix: Error "Request method 'POST' is not supported" en /api/stock/set

**Fecha:** 2025-11-17  
**Problema:** El frontend hacía `POST /api/stock/set` pero el endpoint solo aceptaba `PUT`  
**Solución:** Agregado `@PostMapping("/set")` al controlador para aceptar ambos métodos

---

## Descripción del Error

Al intentar establecer stock desde el frontend (página "Ajustar Stock"), el backend respondía:

```
org.springframework.web.HttpRequestMethodNotSupportedException: Request method 'POST' is not supported
```

**Causa raíz:** El endpoint `/api/stock/set` estaba definido solo con `@PutMapping` pero el frontend enviaba `POST`.

---

## Solución Aplicada

### Archivo modificado:
`src/main/java/com/repobackend/api/stock/controller/StockController.java`

### Cambio realizado:
```java
// ANTES:
@PutMapping("/set")
public ResponseEntity<?> set(@RequestBody Map<String, Object> body, Authentication authentication) {
    // ...
}

// DESPUÉS:
@PostMapping("/set")  // Acepta POST para compatibilidad con frontend
@PutMapping("/set")   // También acepta PUT (semánticamente correcto)
public ResponseEntity<?> set(@RequestBody Map<String, Object> body, Authentication authentication) {
    // ...
}
```

**Ventaja:** Ahora el endpoint acepta tanto POST como PUT, manteniendo compatibilidad con:
- Frontend actual (que usa POST)
- Clientes REST que prefieren usar PUT (semánticamente correcto para operaciones de reemplazo)

---

## Endpoints de Stock Disponibles

Después de este fix, los endpoints de stock son:

1. **GET /api/stock?productoId={id}&almacenId={id}**
   - Obtener stock por producto (opcional: filtrado por almacén)
   - Respuesta: `{ stockByAlmacen: [...], total: number, cantidadAlmacen: number }`

2. **GET /api/stock/{productoId}?almacenId={id}**
   - Variante con path param (mismo comportamiento)

3. **POST /api/stock/adjust**
   - Ajustar stock (aumentar o disminuir)
   - Body: `{ productoId, almacenId, delta }` (delta puede ser positivo o negativo)
   - Respuesta: `{ success: true, nuevaCantidad: number }`

4. **POST /api/stock/set** o **PUT /api/stock/set** ✨ (ambos aceptados)
   - Establecer cantidad exacta (reemplaza el valor anterior)
   - Body: `{ productoId, almacenId, cantidad }`
   - Respuesta: `{ success: true, cantidad: number }`

5. **DELETE /api/stock?productoId={id}&almacenId={id}**
   - Eliminar registro de stock
   - Respuesta: `{ success: true }`

---

## Verificación

Para verificar que funciona:

1. Reiniciar el backend (si está corriendo):
   ```powershell
   # Detener (Ctrl+C) y volver a ejecutar:
   ./mvnw.cmd spring-boot:run
   ```

2. Desde el frontend (página Ajustar Stock):
   - Seleccionar producto
   - Seleccionar almacén
   - Establecer cantidad (ej: 53)
   - Click en "Guardar"
   - **Resultado esperado:** El stock se actualiza sin error 500

3. Verificar en logs del backend:
   - Ya NO debe aparecer: `Request method 'POST' is not supported`
   - Debe aparecer: `200 OK` en la respuesta

---

## Impacto en Frontend

**No se requiere cambio en el frontend** — el endpoint ahora acepta POST como esperaba el cliente.

Si en el futuro se desea usar PUT (más correcto semánticamente):
```typescript
// Opción 1: POST (actual, funciona)
fetch('/api/stock/set', { method: 'POST', ... })

// Opción 2: PUT (también funciona ahora, más RESTful)
fetch('/api/stock/set', { method: 'PUT', ... })
```

---

## Estado

✅ **Resuelto** - Compilado y listo para testing  
📝 Documentación actualizada en: `docs/FRONTEND_COPILOT_FACTURACION.md`

---

*Fix aplicado por: backend-dev-team*  
*Próximos pasos: Regenerar OpenAPI para reflejar ambos métodos en la documentación*

