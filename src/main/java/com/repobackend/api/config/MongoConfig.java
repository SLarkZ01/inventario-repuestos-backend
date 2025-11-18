package com.repobackend.api.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

@Configuration
public class MongoConfig {
    private static final Logger log = LoggerFactory.getLogger(MongoConfig.class);

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    @Bean
    public MongoClient mongoClient() {
        // Forzar retryWrites(false) independientemente de la URI efectiva
        ConnectionString cs = new ConnectionString(mongoUri);
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(cs)
                .retryWrites(false)
                .build();

        // Log mínimo (sin credenciales) para depurar el origen del problema
        try {
            String sanitized = mongoUri
                .replaceAll("(?i)(:[^@/]+@)", ":***@") // ocultar password si existe
                .replaceAll("(?i)(retryWrites=)true", "$1false");
            log.info("Inicializando MongoClient con retryWrites=false. URI efectiva: {}", sanitized);
        } catch (Exception ignore) {}

        return MongoClients.create(settings);
    }
}

