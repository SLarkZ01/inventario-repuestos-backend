# 🚀 Inventario Repuestos - Backend (Spring Boot)

> Backend en Java (Spring Boot) para la aplicación de inventario y facturación.

Este repositorio contiene el backend que expone la API usada por la app Android (Kotlin) para iniciar sesión, registrarse y gestionar inventarios.

Repositorio Android relacionado: https://github.com/SLarkZ01/facturacion-inventario-android-kotlin 📱

---

**Contenido**
- **Descripción**: Qué hace el proyecto
- **Requisitos**: Herramientas necesarias
- **Descarga & Ejecución**: Cómo clonar y ejecutar localmente
- **Configuración**: Variables y ajustes importantes
- **Colaboración**: Flujo para contribuir al proyecto
- **Contacto**: Información de contacto

---

**Descripción**
- Backend REST creado con Spring Boot que gestiona usuarios, autenticación (JWT), refresh tokens y recursos de inventario.
- Se integra con la app Android Kotlin (ver repositorio arriba) que ya implementa el login y registro.

**Requisitos**
- Java 17+ (recomendado Java 21) ☕
- Maven 3.6+ (o usar el wrapper incluido) 🧰
- Git 🪄
- IDE opcional: IntelliJ IDEA / VS Code

**Descargar el proyecto**
1. Clonar el repositorio:

```bash
git clone https://github.com/SLarkZ01/inventario-repuestos-backend.git
cd inventario-repuestos-backend
```

2. Alternativa usando SSH:

```bash
git clone git@github.com:SLarkZ01/inventario-repuestos-backend.git
cd inventario-repuestos-backend
```

**Ejecutar en local**

- Usando Maven wrapper (incluido):

```bash
# En Windows PowerShell
./mvnw.cmd spring-boot:run

# O con Maven instalado
mvn spring-boot:run
```

- También puedes construir el artefacto y ejecutarlo:

```bash
./mvnw.cmd -DskipTests package
java -jar target/*.jar
```

Una vez levantado, por defecto la app lee `application.properties` en `src/main/resources` y estará disponible en `http://localhost:8080` ⚙️

**Variables de configuración importantes**
- `src/main/resources/application.properties`: revisa valores como puerto, datasource (si usas base de datos externa), y secretos JWT.
- Si vas a usar una base de datos local, ajusta la `spring.datasource.*` y crea las tablas necesarias (o usa `spring.jpa.hibernate.ddl-auto=update` en desarrollo).

Ejemplo mínimo de variables a revisar:

```properties
# Puerto
server.port=8080

# JWT secret (cambiar en producción)
security.jwt.secret=TU_SECRETO_MUY_SEGURA_AQUI

# Datasource (ejemplo H2 en memoria para pruebas)
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=update
```

⚠️ No comites secretos ni credenciales en el repositorio.

**Integración con la app Android (Kotlin)**
- La app Android consume los endpoints de autenticación (login/register) expuestos por este backend.
- Asegúrate de que la URL base en la app Android apunte a tu instancia local/servidor (por ejemplo `http://10.0.2.2:8080` para emulador Android).

**Pruebas**
- Ejecutar tests unitarios:

```bash
./mvnw.cmd test
```

**Flujo de colaboración (Git)**

- Fork & Branching:
  - Fork del repositorio si colaboras externamente.
  - Crear ramas por feature: `feature/nombre-descriptivo`.

- Commits:
  - Mensajes cortos y claros. Ejemplo: `git commit -m "feat(auth): add refresh token endpoint"`

- Pull Requests:
  - Abrir PR contra `main` con descripción del cambio y pasos para probar.
  - Revisión de código y aprobación antes de merge.

- Estrategia de ramas sugerida:
  - `main`: versión estable
  - `develop`: integración de features (opcional)
  - `feature/*`, `fix/*`, `chore/*` según convención

**Cómo colaborar localmente**
1. Crear y cambiar a una rama nueva:

```bash
git checkout -b feature/mi-mejora
```

2. Hacer cambios, ejecutar tests y commitear:

```bash
git add .
git commit -m "describe tu cambio aquí"
git push origin feature/mi-mejora
```

3. Abrir un Pull Request en GitHub y solicitar revisión ✅

**Buenas prácticas**
- No subir secretos al repositorio.
- Agregar documentación para nuevos endpoints (por ejemplo, usar Swagger/OpenAPI).
- Mantener los tests verdes en CI antes de mergear.

**Contacto / Equipo**
- Repo Android: https://github.com/SLarkZ01/facturacion-inventario-android-kotlin 📱
- Si necesitas ayuda con la configuración, asigna un issue en este repo y etiqueta a los responsables.

---
