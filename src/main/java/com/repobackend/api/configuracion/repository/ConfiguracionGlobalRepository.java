package com.repobackend.api.configuracion.repository;

import com.repobackend.api.configuracion.model.ConfiguracionGlobal;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConfiguracionGlobalRepository extends MongoRepository<ConfiguracionGlobal, ObjectId> {

    /**
     * Busca la configuración global por su clave única.
     * En producción, siempre debería existir una configuración con clave "GLOBAL".
     */
    Optional<ConfiguracionGlobal> findByClave(String clave);
}

