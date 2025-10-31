package com.repobackend.api.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "RepoBackend API",
        version = "0.0.1",
        description = "API para gestión de inventario y ventas (productos, stock, carritos, facturas, talleres, favoritos)",
        contact = @Contact(name = "Equipo RepoBackend", email = "devs@example.com"),
        license = @License(name = "Proprietary")
    ),
    servers = {
        @Server(url = "/", description = "Servidor local / producción (configurable)")
    },
    security = { @SecurityRequirement(name = "bearerAuth") }
)
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    description = "Autenticación con JWT Bearer. Añada el header: Authorization: Bearer {token}"
)
public class OpenApiConfig {
    // Clase vacía usada solo para definir meta-información OpenAPI y esquema de seguridad
}

