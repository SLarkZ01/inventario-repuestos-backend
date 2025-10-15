# Movimientos — inmutabilidad y correcciones

En esta API los movimientos (entradas/egresos) se tratan como registros históricos. Por seguridad, consistencia y trazabilidad, las operaciones de edición directa (PUT) sobre movimientos no están permitidas.

## Cómo corregir un movimiento incorrecto

Si por ejemplo accidentalmente registraste un ingreso de 5 unidades y quieres revertirlo, crea un nuevo movimiento compensatorio de tipo `egreso` con la misma cantidad. Esto preserva el historial y ajusta el stock de forma clara.

### Ejemplo PowerShell (crear movimiento compensatorio)

```powershell
# $token debe contener el token JWT (solo el token, no incluye la palabra 'Bearer')
$token = 'TU_JWT_AQUI'
$body = '{
  "tipo":"egreso",
  "productoId":"ID_DEL_PRODUCTO",
  "cantidad":5,
  "referencia":"Corrección movimiento 123",
  "notas":"Movimiento compensatorio",
  "realizadoPor":"ID_USUARIO"
}'
Invoke-RestMethod -Method Post -Uri 'http://localhost:8080/api/movimientos' -Headers @{ 'Content-Type'='application/json'; 'Authorization'="Bearer $token" } -Body $body
```

### Ejemplo curl

```bash
curl -X POST "http://localhost:8080/api/movimientos" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer TU_JWT_AQUI" \
  -d '{"tipo":"egreso","productoId":"ID_DEL_PRODUCTO","cantidad":5,"referencia":"Corrección movimiento 123","notas":"Movimiento compensatorio","realizadoPor":"ID_USUARIO"}'
```

## Si necesitas editar movimientos

Si prefieres que el sistema permita editar movimientos (PUT) o cambiar ciertos campos, es posible implementarlo, pero con las siguientes precauciones:

- Calcular correctamente el delta de stock (nuevo efecto - efecto anterior).
- Registrar auditoría (quién modificó, cuándo, qué valores antes/después).
- Restringir el permiso a roles administrativos.

Por ahora la API responde 405 en `PUT /api/movimientos/{id}` y se recomienda usar movimientos compensatorios para correcciones.
