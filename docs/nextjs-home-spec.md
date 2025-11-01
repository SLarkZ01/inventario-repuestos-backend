# Especificación de la página principal (Home) de la app Next.js

Objetivo
- Presentar un panel inicial (dashboard) claro y accionable para usuarios autenticados antes de comenzar a usar la app.
- Mostrar el estado del inventario, accesos rápidos a tareas frecuentes y actividad reciente (movimientos y ventas).
- Guiar el onboarding cuando el sistema está vacío (primer uso).

Perfiles y permisos (resumen)
- La app trabaja con talleres y almacenes. Los roles mencionados por el backend incluyen: OWNER, ADMIN, VENDEDOR.
- Operaciones de stock por almacén permiten VENDEDOR/ADMIN (según StockService). Otras operaciones pueden requerir OWNER/ADMIN.
- La Home debe respetar los permisos del usuario actual (ocultar/inhabilitar acciones no permitidas).

Estructura general de la Home
1) Encabezado (Header)
   - Logo/branding.
   - Selector de Taller (si el usuario pertenece a más de uno).
   - Selector de Almacén (opcional, filtra los widgets que lo soporten).
   - Buscador global de productos (por nombre; usa `GET /api/productos?q=...`).
   - Acceso al perfil y logout.

2) KPIs (tarjetas resumen)
   - Productos totales: cantidad de productos (desde `GET /api/productos` paginado; mostrar total). 
   - Stock total (sumatorio): usar los campos enriquecidos del producto (`totalStock`) agregando la página inicial de productos o listar todas las páginas de forma lazy. Alternativa mínima: sumar `stock` de cada producto en la primera página y mostrar "estimado".
   - Movimientos hoy: contar movimientos de hoy (`GET /api/movimientos` y filtrar por fecha en cliente; si se requieren filtros por fecha en servidor, considerar endpoint futuro). 
   - Ventas recientes: cantidad de facturas creadas hoy/esta semana (`GET /api/facturas?userId={me}`) y filtrar por fecha en cliente.

3) Acciones rápidas
   - Crear producto (link a flujo de creación).
   - Ajustar stock por almacén (link a vista que use `POST /api/stock/adjust`).
   - Crear factura (link a flujo de checkout desde carrito o creación directa de factura si aplica).
   - Registrar ingreso de mercancía (si se usa flujo global: `POST /api/movimientos` tipo INGRESO, sin almacénId).

4) Secciones de actividad
   - Movimientos recientes (auditoría): `GET /api/movimientos` (mostrar últimos N). Mostrar: tipo, producto, cantidad, referencia, fecha, realizadoPor.
     - Nota: Para ajustes por almacén, los movimientos se generan automáticamente vía evento del backend. No se debe crear movimiento manual con almacenId; la Home debe enlazar al flujo de Stock cuando corresponde.
   - Facturas recientes: `GET /api/facturas?userId={me}` (listar últimas N). Mostrar: número, total, fecha, cantidad de items.
   - Alertas de stock bajo: con datos de `GET /api/productos` -> campo `totalStock` (enriquecido desde backend). Definir umbral (ej. < 5 unidades). 

5) Sugerencias / Onboarding (primer uso)
   - Si no hay talleres: "Crea tu primer taller" (link a crear taller).
   - Si no hay almacenes: "Crea un almacén en tu taller".
   - Si no hay productos: "Crea tu primer producto".
   - Si no hay stock: "Ajusta stock inicial" (redirigir a flujo de `POST /api/stock/adjust`).
   - Invitar miembros: link al flujo para invitar administradores o vendedores.

Datos a consumir (endpoints backend)
- Usuario actual: `GET /api/auth/me` (para mostrar nombre, rol y controlar permisos en la Home).
- Productos:
  - Listado/paginación/búsqueda: `GET /api/productos?q=&categoriaId=&page=&size=` (ProductoResponse incluye `totalStock` y `stockByAlmacen` si disponible).
  - Obtener uno: `GET /api/productos/{id}`.
- Stock por almacén:
  - Obtener desglose por producto: `GET /api/stock?productoId={id}` (devuelve lista por almacén y total). 
  - Ajustar stock (delta): `POST /api/stock/adjust` con `{ productoId, almacenId, delta }` (positivo/negativo). Publica evento y crea movimiento automáticamente.
  - Setear stock absoluto: `PUT /api/stock/set` con `{ productoId, almacenId, cantidad }` (publica evento con diferencia).
- Movimientos (auditoría):
  - Listar filtrados: `GET /api/movimientos?productoId=&tipo=`.
  - Crear movimiento global: `POST /api/movimientos` (sin `almacenId`). Usa para ingresos globales/ajustes históricos. Para ajustes por almacén usar `POST /api/stock/adjust`.
- Facturas:
  - Checkout desde carrito: `POST /api/facturas/checkout` con `{ carritoId }`.
  - Listar por usuario: `GET /api/facturas?userId={me}`.
  - Obtener por id/número: `GET /api/facturas/{id}` / `GET /api/facturas/numero/{numero}`.
- Carritos: listar por usuario (mostrar conteo o acceso rápido a continuar compra).
- Talleres/Almacenes: usar endpoints de Taller para listar talleres propios y sus almacenes (mostrar en selectores).

Diseño de componentes (sugerencia)
- HeaderBar: selector de taller/almacén, buscador, perfil.
- KpiCards: tarjetas con KPIs mencionados (cálculos ligeros en server component).
- QuickActions: botones a flujos principales.
- RecentMovements: tabla con últimos movimientos (paginación simple en cliente o SSR con page=0).
- RecentInvoices: lista/tabla con últimas facturas del usuario.
- LowStockList: productos con `totalStock` por debajo del umbral.
- EmptyState: bloque de onboarding si faltan entidades clave.

Estados y reglas de UI
- Autenticación obligatoria:
  - Si `GET /api/auth/me` retorna 401 -> redirigir a /login.
  - Tokens: preferir cookies HttpOnly para refresh y Authorization Bearer para accesos.
- Permisos:
  - Mostrar/ocultar acciones según rol (p. ej. botón "Ajustar stock" solo para VENDEDOR/ADMIN del taller actual).
- Errores:
  - Mostrar toasts/banners con mensajes de backend (400, 409, 500).
  - En 409 (stock insuficiente) durante checkout, mostrar detalle del producto afectado.
- Cargas: skeleton loaders y estados vacíos amigables.

Estrategia de datos (Next.js App Router)
- Server Components para SSR de la Home (datos personalizados del usuario = no cachear o `cache: 'no-store'`).
- Fetch server-side con encabezado Authorization tomado de cookies (access token) y refresh silencioso si es necesario:

```ts
// ejemplo simplificado (server action / server component)
async function apiFetch(path: string, init: RequestInit = {}) {
  const access = cookies().get('accessToken')?.value;
  const headers = { 'Content-Type': 'application/json', ...(init.headers || {}) } as any;
  if (access) headers['Authorization'] = `Bearer ${access}`;
  const res = await fetch(process.env.BACKEND_URL + path, { ...init, headers, cache: 'no-store' });
  if (res.status === 401) {
    // opcional: intentar refresh vía API route /auth/refresh y reintentar
  }
  return res;
}
```

- Llamadas mínimas para Home (SSR):
  - `GET /api/auth/me` (usuario/roles).
  - `GET /api/productos?page=0&size=20` (derivar productos totales y primeros para low stock).
  - `GET /api/movimientos` (últimos movimientos, limitar en cliente a N elementos).
  - `GET /api/facturas?userId={me}` (últimas facturas del usuario).
  - (Opcional) Endpoints de talleres/almacenes para los selectores.

Buenas prácticas específicas del backend (a considerar en Home)
- No crear movimientos con `almacenId` desde UI: para cambios por almacén, usar `/api/stock/adjust`. El backend publicará el evento y creará el movimiento automáticamente.
- Los movimientos son inmutables (no hay PUT). Para correcciones, crear un movimiento compensatorio.
- Ajustes atómicos: `StockService` y `ProductoService` usan operaciones atómicas; aún así, preferir `adjustStock` por almacén para coherencia total.

Empty states (detalles)
- Sin talleres: bloque con CTA "Crear taller".
- Sin almacenes en el taller seleccionado: CTA "Crear almacén".
- Sin productos: CTA "Crear producto".
- Sin stock: CTA "Ajustar stock inicial".
- Sin movimientos ni facturas: texto explicativo y enlaces a acciones rápidas.

Accesibilidad y performance
- Respetar WCAG (foco visible, contraste, navegación con teclado, labels). 
- Hidratar solo lo necesario; preferir server components en la Home.
- Paginación o lazy-load para listas (movimientos/facturas) para no bloquear el TTFB.

Seguridad
- Guard de rutas protegidas en Next.js (middleware o layout) que valide sesión.
- Refresh Token en cookie HttpOnly; Access Token en memoria/encabezado.
- No exponer tokens en JavaScript del cliente si no es necesario.

Diseño responsive (breakpoints)
- Desktop: KPIs en grilla (3-4 columnas), tablas de movimientos/facturas lado a lado.
- Tablet: KPIs 2 columnas, listas apiladas.
- Móvil: 1 columna, tarjetas compactas y acciones agrupadas.

Roadmap (mejoras futuras útiles para la Home)
- Endpoint de agregados: ventas del día/semana, top productos vendidos, movimientos por tipo, etc.
- Filtros por fecha para `GET /api/movimientos` y `GET /api/facturas`.
- Widget de favoritos (atajos a productos frecuentes).
- Notificaciones: alertas push por stock crítico.

Apéndice: llamadas de ejemplo
- Listar productos (página 0, 20 por página):
```bash
curl "${BACKEND_URL}/api/productos?page=0&size=20" -H "Authorization: Bearer <ACCESS>"
```
- Movimientos recientes:
```bash
curl "${BACKEND_URL}/api/movimientos" -H "Authorization: Bearer <ACCESS>"
```
- Ajustar stock por almacén (delta negativo):
```bash
curl -X POST "${BACKEND_URL}/api/stock/adjust" \
  -H "Content-Type: application/json" -H "Authorization: Bearer <ACCESS>" \
  -d '{"productoId":"<prodId>","almacenId":"<almId>","delta":-5}'
```
- Crear movimiento global (ingreso de mercancía):
```bash
curl -X POST "${BACKEND_URL}/api/movimientos" \
  -H "Content-Type: application/json" -H "Authorization: Bearer <ACCESS>" \
  -d '{"tipo":"INGRESO","productoId":"<prodId>","cantidad":50,"referencia":"OC-2025-001"}'
```
