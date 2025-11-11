// Script para crear/actualizar el esquema de validación de carritos
// Permite carritos anónimos (sin usuarioId requerido)

use('facturacion_inventario');

// Intentar crear la colección si no existe
try {
  db.createCollection("carritos", {
    validator: {
      $jsonSchema: {
        bsonType: "object",
        required: ["_id", "items", "creadoEn"],
        properties: {
          _id: {
            bsonType: "objectId",
            description: "Identificador único del carrito"
          },
          usuarioId: {
            bsonType: ["objectId", "null"],
            description: "ID del usuario dueño del carrito (opcional para carritos anónimos)"
          },
          items: {
            bsonType: "array",
            description: "Lista de items en el carrito",
            items: {
              bsonType: "object",
              required: ["productoId", "cantidad"],
              properties: {
                productoId: {
                  bsonType: "string",
                  description: "ID del producto"
                },
                cantidad: {
                  bsonType: "int",
                  minimum: 0,
                  description: "Cantidad del producto"
                }
              }
            }
          },
          creadoEn: {
            bsonType: "date",
            description: "Fecha de creación del carrito"
          },
          realizadoPor: {
            bsonType: ["objectId", "null"],
            description: "ID del usuario que realizó la acción (opcional)"
          }
        }
      }
    },
    validationLevel: "moderate",
    validationAction: "error"
  });
  print("✅ Colección carritos creada con el esquema correcto");
} catch (e) {
  // Si la colección ya existe, actualizar el validador
  if (e.codeName === 'NamespaceExists') {
    db.runCommand({
      collMod: "carritos",
      validator: {
        $jsonSchema: {
          bsonType: "object",
          required: ["_id", "items", "creadoEn"],
          properties: {
            _id: {
              bsonType: "objectId",
              description: "Identificador único del carrito"
            },
            usuarioId: {
              bsonType: ["objectId", "null"],
              description: "ID del usuario dueño del carrito (opcional para carritos anónimos)"
            },
            items: {
              bsonType: "array",
              description: "Lista de items en el carrito",
              items: {
                bsonType: "object",
                required: ["productoId", "cantidad"],
                properties: {
                  productoId: {
                    bsonType: "string",
                    description: "ID del producto"
                  },
                  cantidad: {
                    bsonType: "int",
                    minimum: 0,
                    description: "Cantidad del producto"
                  }
                }
              }
            },
            creadoEn: {
              bsonType: "date",
              description: "Fecha de creación del carrito"
            },
            realizadoPor: {
              bsonType: ["objectId", "null"],
              description: "ID del usuario que realizó la acción (opcional)"
            }
          }
        }
      },
      validationLevel: "moderate",
      validationAction: "error"
    });
    print("✅ Esquema de carritos actualizado: usuarioId ahora es opcional");
  } else {
    print("❌ Error: " + e.message);
  }
}
