package com.repobackend.api.tools;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;

@SpringBootTest
public class ApplyOptionARemoveIconoRecursoValidatorTest {
    private static final Logger log = LoggerFactory.getLogger(ApplyOptionARemoveIconoRecursoValidatorTest.class);

    @Autowired
    private MongoTemplate mongoTemplate;

    @Test
    public void applyCollMod_RemoveIconoRecursoFromRequired() {
        var db = mongoTemplate.getDb();

        //listCollections to fetch current validator for 'categorias'
        Document listCmd = new Document("listCollections", 1).append("filter", new Document("name", "categorias"));
        Document listRes = db.runCommand(listCmd);
        Document cursor = (Document) listRes.get("cursor");
        if (cursor == null) {
            log.info("No cursor returned by listCollections; skipping migration.");
            assertTrue(true);
            return;
        }
        @SuppressWarnings("unchecked")
        List<Document> firstBatch = (List<Document>) cursor.get("firstBatch");
        if (firstBatch == null || firstBatch.isEmpty()) {
            log.info("Collection 'categorias' not found; skipping migration.");
            assertTrue(true);
            return;
        }
        Document colInfo = firstBatch.get(0);
        Document options = (Document) colInfo.get("options");
        Document validator = options != null ? (Document) options.get("validator") : null;
        if (validator == null) {
            log.info("No validator found; nothing to change.");
            assertTrue(true);
            return;
        }
        @SuppressWarnings("unchecked")
        Document jsonSchema = (Document) validator.get("$jsonSchema");
        if (jsonSchema == null) {
            log.info("No $jsonSchema in validator; nothing to change.");
            assertTrue(true);
            return;
        }
        @SuppressWarnings("unchecked")
        List<String> required = (List<String>) jsonSchema.get("required");
        if (required == null) {
            log.info("No required array; nothing to change.");
            assertTrue(true);
            return;
        }
        if (!required.contains("iconoRecurso")) {
            log.info("'iconoRecurso' not present in required; nothing to change.");
            assertTrue(true);
            return;
        }

        List<String> newRequired = new ArrayList<>(required);
        newRequired.removeIf(r -> "iconoRecurso".equals(r));
        jsonSchema.put("required", newRequired);

        Document collMod = new Document("collMod", "categorias").append("validator", new Document("$jsonSchema", jsonSchema));
        Document result = db.runCommand(collMod);
        log.info("collMod result: {}", result.toJson());

        Object ok = result.get("ok");
        // ok == 1.0 indicates success
        boolean success = (ok instanceof Number) && ((Number) ok).doubleValue() == 1.0;
        assertTrue(success, "collMod did not return ok:1.0");
    }
}

