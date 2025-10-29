# Documentación OpenAPI - Solución Implementada

## ✅ Problema Resuelto

El error `NoSuchMethodError` en `/v3/api-docs` se debía a una incompatibilidad entre:
- **Spring Boot 3.5.5** (Spring Framework 6.2.10)
- **springdoc-openapi 2.6.0** / **swagger-core 2.2.22**

### Solución Aplicada

Se bajó la versión de **Spring Boot a 3.3.5** (Spring Framework 6.1.14) que es totalmente compatible con `springdoc-openapi-starter-webmvc-ui 2.5.0`.

## 📋 Cambios Realizados

### 1. `pom.xml`
```xml
<!-- Cambiado de 3.5.5 a 3.3.5 -->
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.3.5</version>
</parent>

<!-- Dependencias añadidas -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.5.0</version>
</dependency>

<dependency>
    <groupId>com.fasterxml.jackson.dataformat</groupId>
    <artifactId>jackson-dataformat-yaml</artifactId>
</dependency>

<!-- Dependencias de swagger-core forzadas a 2.2.15 -->
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.swagger.core.v3</groupId>
            <artifactId>swagger-core-jakarta</artifactId>
            <version>2.2.15</version>
        </dependency>
        <!-- ... más artefactos swagger-core -->
    </dependencies>
</dependencyManagement>
```

### 2. Controlador Estático (fallback)
Creado `OpenApiStaticController.java` que sirve `docs/openapi.yaml` en `/v3/api-docs-static` como respaldo.

### 3. SecurityConfig
Permitido acceso público a:
- `/v3/api-docs/**` (endpoint dinámico de springdoc)
- `/v3/api-docs-static` (endpoint estático fallback)
- `/swagger-ui/**`, `/swagger-ui.html` (UI de Swagger)
- `/webjars/**` (recursos estáticos de swagger-ui)

## 🚀 Cómo Usar la Documentación

### Opción 1: Swagger UI (Recomendado)

1. **Arrancar la aplicación:**
   ```cmd
   mvn spring-boot:run
   ```
   O desde el JAR:
   ```cmd
   java -jar target\repobackend-api-0.0.1-SNAPSHOT.jar
   ```

2. **Abrir en el navegador:**
   - http://localhost:8080/swagger-ui/index.html
   - O simplemente: http://localhost:8080/swagger-ui.html

3. **Explorar y probar endpoints** directamente desde la interfaz web.

### Opción 2: Importar en Postman

#### Desde el archivo local:
1. En Postman: **Import** → **File**
2. Seleccionar `docs/openapi.yaml`
3. Click **Import**

#### Desde la app en ejecución:
1. En Postman: **Import** → **Link**
2. Pegar la URL: `http://localhost:8080/v3/api-docs`
3. Click **Import**

### Opción 3: Acceso programático

**Obtener el JSON de OpenAPI:**
```bash
curl http://localhost:8080/v3/api-docs > api-spec.json
```

**Obtener el YAML estático:**
```bash
curl http://localhost:8080/v3/api-docs-static > api-spec.yaml
```

## 📦 Endpoints de Documentación Disponibles

| Endpoint | Descripción | Formato |
|----------|-------------|---------|
| `/v3/api-docs` | Especificación OpenAPI generada dinámicamente | JSON |
| `/v3/api-docs-static` | Especificación desde `docs/openapi.yaml` | JSON |
| `/swagger-ui/index.html` | Interfaz web de Swagger UI | HTML |
| `/swagger-ui.html` | Redirección a `/swagger-ui/index.html` | HTML (redirect) |

## 🔧 Compilar y Arrancar

```cmd
# Limpiar y compilar
mvn clean package -DskipTests

# Arrancar la aplicación
java -jar target\repobackend-api-0.0.1-SNAPSHOT.jar

# O directamente con Maven
mvn spring-boot:run
```

## 📝 Notas Importantes

1. **Spring Boot 3.3.5** es la versión LTS actual más compatible con springdoc.
2. **springdoc 2.5.0** es estable con Spring Framework 6.1.x.
3. El archivo `docs/openapi.yaml` se mantiene manualmente y sirve como fuente de verdad para la generación de clientes.
4. La documentación dinámica (`/v3/api-docs`) se genera automáticamente desde los controladores Spring.

## 🎯 Verificación

Para verificar que todo funciona correctamente:

```bash
# Verificar endpoint dinámico
curl http://localhost:8080/v3/api-docs

# Verificar endpoint estático
curl http://localhost:8080/v3/api-docs-static

# Verificar UI (debe devolver HTML)
curl http://localhost:8080/swagger-ui/index.html
```

Todos los endpoints deben devolver **HTTP 200 OK**.

## 🐛 Troubleshooting

### Si ves error 500 en `/v3/api-docs`:
- Verifica que estás usando Spring Boot 3.3.5 (no 3.5.x)
- Ejecuta `mvn clean install -DskipTests`
- Reinicia la aplicación

### Si la UI no carga:
- Verifica que el puerto 8080 no esté en uso
- Revisa los logs en `logs/app.log`
- Confirma que aparece: "Init duration for springdoc-openapi is: XXX ms"

## 📚 Referencias

- [Spring Boot 3.3.5 Documentation](https://docs.spring.io/spring-boot/docs/3.3.5/reference/html/)
- [springdoc-openapi Documentation](https://springdoc.org/)
- [OpenAPI Specification](https://swagger.io/specification/)
