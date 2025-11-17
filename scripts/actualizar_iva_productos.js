// Script MongoDB para configurar tasaIva en productos existentes
// Ejecutar en MongoDB shell o mediante código

// 1. Configurar IVA 19% (estándar Colombia) en todos los productos que no tengan tasaIva
db.productos.updateMany(
  { tasaIva: { $exists: false } },
  { $set: { tasaIva: 19.0 } }
);

// 2. Actualizar productos específicos con IVA 5% (ejemplo: productos de primera necesidad)
// db.productos.updateMany(
//   { categoriaId: "ID_CATEGORIA_NECESIDAD" },
//   { $set: { tasaIva: 5.0 } }
// );

// 3. Productos exentos de IVA (0%)
// db.productos.updateMany(
//   { categoriaId: "ID_CATEGORIA_EXENTA" },
//   { $set: { tasaIva: 0.0 } }
// );

// Verificar actualización
print("Productos actualizados con tasaIva:");
db.productos.aggregate([
  {
    $group: {
      _id: "$tasaIva",
      count: { $sum: 1 }
    }
  },
  {
    $sort: { _id: 1 }
  }
]);

