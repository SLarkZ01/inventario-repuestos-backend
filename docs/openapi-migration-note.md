## Nota de migración: OpenAPI y SDKs

Resumen corto
- He extraído esquemas inline del `docs/openapi.yaml` hacia `components/schemas` y añadido `operationId` en las operaciones principales.
- Regeneré los SDKs para Android (Kotlin) y Next.js (TypeScript) en `docs/generated/android` y `docs/generated/nextjs`.

¿Que cambió para los clientes?
- Modelos con nombres más estables:
  - Ahora encontrarás modelos como `CrearTallerRequest`, `CrearAlmacenRequest`, `CrearInvitacionRequest`, `AcceptInvitationRequest`, `OAuthGoogleRequest`, `OAuthFacebookRequest`, `RefreshRequest`, `RegisterRequest`, `LoginRequest`, `AuthResponse`, `ErrorResponse` en los SDKs.
  - Antes, algunos modelos se generaban con nombres automáticos por ser "inline" (por ejemplo `_api_talleres_post_request`); eso debería reducirse ahora.
- Nombres de métodos:
  - Añadí `operationId` para las rutas más importantes. Los clientes generados usarán esos `operationId` como nombres de método cuando sea posible.
  - Aun quedan algunas rutas donde el generador añadió automáticamente `apiAuthRegisterPost` etc. Si quieres nombres distintos, podemos personalizar `operationId` con los nombres deseados.

Dónde buscar los cambios
- Android (Kotlin): `docs/generated/android/src/main/kotlin/org/openapitools/client/models` y `.../apis`.
- Next.js (TypeScript/Axios): `docs/generated/nextjs` (revisa `api.ts`, `index.ts` y `configuration.ts`).

Cómo regenerar los SDKs localmente
1. Asegúrate de tener Maven instalado y usa la rama `main` actualizada.
2. Ejecuta:

```pwsh
mvn -DskipTests generate-sources
```

3. Los SDKs se escribirán a `docs/generated/android` y `docs/generated/nextjs`.

Siguientes pasos recomendados
- Revisar los `operationId` actuales y decidir nombres finales (p.ej. `auth.register`, `auth.login`, `talleres.create`, etc.). Yo puedo aplicar esos cambios si me das la lista.
- Añadir `title` a esquemas en `docs/openapi.yaml` para asegurar nombres concretos en el generador.
- Añadir pruebas de integración para validar contratos API y evitar roturas en futuras regeneraciones.

Contacto
- Si quieres que cambie nombres específicos de `operationId` o `title` en los esquemas, dime la lista y lo hago.
