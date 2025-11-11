// Eliminar completamente la colección y recrearla sin validación estricta
use('facturacion_inventario');

// Eliminar colección si existe
db.carritos.drop();
print("✅ Colección carritos eliminada");

// Crear nueva colección sin validación estricta en usuarioId
db.createCollection("carritos");
print("✅ Colección carritos creada sin validador");

// Ahora agregar el validador correcto
db.runCommand({
  collMod: "carritos",
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["_id", "items", "creadoEn"],
      properties: {
        _id: {
          bsonType: "objectId"
        },
        usuarioId: {
          bsonType: "objectId"
        },
        items: {
          bsonType: "array",
          items: {
            bsonType: "object",
            required: ["productoId", "cantidad"],
            properties: {
              productoId: {
                bsonType: "string"
              },
              cantidad: {
                bsonType: "int"
              }
            }
          }
        },
        creadoEn: {
          bsonType: "date"
        },
        realizadoPor: {
          bsonType: "objectId"
        }
      }
    }
  },
  validationLevel: "moderate"
});
print("✅ Validador aplicado (usuarioId NO es requerido)");
