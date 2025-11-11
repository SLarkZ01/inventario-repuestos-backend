# 🚀 Instrucciones de Reinicio del Backend

## 🔴 Situación Actual

**Estado del código:** ✅ CORRECTO  
**Estado del backend:** ❌ Usando versión anterior

El archivo `SecurityConfig.java` tiene la configuración correcta:
```java
.requestMatchers("/api/carritos/**").permitAll()  // ✅ Correcto
```

Pero el backend que está corriendo **NO tiene estos cambios aplicados**, por eso da error 401.

---

## ⚡ Solución Rápida (3 pasos)

### Paso 1: Detener el Backend Actual

1. Busca la terminal llamada: **"Run: InventarioRepuestosBackendApplication"**
2. Haz click en esa terminal
3. Presiona **Ctrl+C** para detener el servidor
4. Espera a que se detenga completamente

---

### Paso 2: Reiniciar con Clean

Ejecuta en la terminal:

```bash
./mvnw clean spring-boot:run
```

**¿Por qué `clean`?**
- Elimina archivos compilados antiguos
- Fuerza recompilación completa
- Asegura que los nuevos cambios se apliquen

**Espera a ver este mensaje:**
```
Started InventarioRepuestosBackendApplication in X.XXX seconds
```

⏱️ **Tiempo estimado:** 30-60 segundos

---

### Paso 3: Verificar que Funciona

Ejecuta el script de verificación:

```powershell
.\scripts\verificar_backend.ps1
```

**Resultado esperado:**
```
✅ Backend configurado correctamente
✅ Carritos anónimos funcionan
✅ Se pueden agregar items sin autenticación
✅ Se pueden eliminar items sin autenticación

📱 El backend está listo para la app Android
```

---

## 🧪 Verificación Manual (Alternativa)

Si prefieres verificar manualmente, ejecuta en PowerShell:

```powershell
# 1. Crear carrito
$headers = @{ "Content-Type" = "application/json" }
$carrito = Invoke-RestMethod -Uri "http://localhost:8080/api/carritos" -Method POST -Headers $headers -Body '{"items":[]}'
$carritoId = $carrito.carrito.id
Write-Host "✅ Carrito creado: $carritoId"

# 2. Agregar item (esto debe funcionar sin error 401)
$itemBody = '{"productoId":"690f7c95c989e80f1c0afc78","cantidad":2}'
Invoke-RestMethod -Uri "http://localhost:8080/api/carritos/$carritoId/items" -Method POST -Headers $headers -Body $itemBody
Write-Host "✅ Item agregado correctamente"
```

**Si ves error 401 en el paso 2:**
- El backend todavía no tiene los cambios
- Verifica que ejecutaste `clean` antes de `spring-boot:run`
- Asegúrate de esperar a que el backend inicie completamente

---

## 🆘 Solución de Problemas

### Problema: "mvnw: command not found"

**Solución:**
```bash
# En Windows PowerShell, usa:
./mvnw.cmd clean spring-boot:run

# O:
.\mvnw.cmd clean spring-boot:run
```

---

### Problema: "MongoDB connection refused"

**Causa:** MongoDB no está corriendo

**Solución:**
```bash
# En otra terminal, inicia MongoDB:
mongod --dbpath C:\data\db

# Luego reinicia el backend en la terminal original
```

---

### Problema: El backend se detiene inmediatamente

**Causa posible 1:** Puerto 8080 ya está en uso

```powershell
# Ver qué proceso usa el puerto 8080:
netstat -ano | findstr :8080

# Matar el proceso (reemplaza PID con el número que aparece):
taskkill /PID <PID> /F
```

**Causa posible 2:** Error en el código

- Revisa los logs en la terminal
- Busca líneas que empiecen con `ERROR`

---

### Problema: Sigue dando error 401 después de reiniciar

**Verifica que el archivo esté guardado:**

1. Abre `SecurityConfig.java`
2. Busca la línea con `/api/carritos`
3. Verifica que sea **EXACTAMENTE:**
   ```java
   .requestMatchers("/api/carritos/**").permitAll()
   ```
4. Si dice `/api/carritos` o `/api/carritos/*` (sin `/**`), cámbialo
5. Guarda (Ctrl+S)
6. Reinicia de nuevo con `clean`

---

## 📊 Checklist de Verificación

- [ ] Backend detenido (Ctrl+C)
- [ ] MongoDB corriendo
- [ ] Ejecutado: `./mvnw clean spring-boot:run`
- [ ] Visto mensaje: "Started InventarioRepuestosBackendApplication"
- [ ] Ejecutado: `.\scripts\verificar_backend.ps1`
- [ ] Todos los tests pasan ✅
- [ ] Listo para probar en la app 🎉

---

## 📱 Próximo Paso: App Android

Una vez que veas:
```
🎉 ¡TODO CORRECTO!
📱 El backend está listo para la app Android
```

**Puedes probar en la app:**

1. Abre la app Android en el emulador
2. Toca el ícono del carrito 🛒
3. Debería decir "Carrito vacío" (sin error 401)
4. Ve a cualquier producto
5. Toca "Agregar al carrito"
6. Vuelve al carrito
7. ¡El producto debería estar ahí! ✅

---

## 📄 Documentación Completa

Para más detalles, revisa:
- `SOLUCION_ERROR_401_CARRITOS.md` - Documentación completa del problema y solución
- `scripts/verificar_backend.ps1` - Script de verificación automática

---

**Última actualización:** 9 de noviembre de 2025
