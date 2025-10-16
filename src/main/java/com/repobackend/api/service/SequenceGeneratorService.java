package com.repobackend.api.service;

import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import com.repobackend.api.model.DatabaseSequence;

@Service
public class SequenceGeneratorService {
    private final MongoTemplate mongoTemplate;

    public SequenceGeneratorService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * Genera una secuencia atómica para el nombre dado (ej: "factura").
     */
    public long generateSequence(String seqName) {
        Query query = new Query(Criteria.where("_id").is(seqName));
        Update update = new Update().inc("secuencia", 1).inc("seq", 1); // compatibilidad si existe campo "secuencia" en documentos existentes
        // Preferimos devolver el documento nuevo y crear si no existe
        DatabaseSequence counter = mongoTemplate.findAndModify(
            query,
            update,
            FindAndModifyOptions.options().returnNew(true).upsert(true),
            DatabaseSequence.class);
        if (counter == null) {
            return 1L;
        }
        // si el documento tiene campo 'seq' lo usamos; de lo contrario intentar leer 'secuencia'
        long v = counter.getSeq();
        if (v <= 0) {
            try {
                // fallback: intentar leer campo 'secuencia' del documento crudo
                org.bson.Document raw = mongoTemplate.getCollection("data").find(new org.bson.Document("_id", seqName)).first();
                if (raw != null) {
                    Object s = raw.get("secuencia");
                    if (s instanceof Number) {
                        v = ((Number) s).longValue();
                    }
                }
            } catch (Exception ex) {
                // ignore fallback
            }
        }
        return v;
    }
}
