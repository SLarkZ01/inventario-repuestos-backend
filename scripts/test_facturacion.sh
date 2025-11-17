#!/bin/bash
# Script de prueba para el sistema de facturación con IVA

BASE_URL="http://localhost:8080"
TOKEN="YOUR_JWT_TOKEN_HERE"

echo "=== PRUEBAS DE FACTURACIÓN CON IVA ==="
echo ""

# Test 1: Crear producto con IVA
echo "Test 1: Crear producto con IVA 19%"
curl -X POST "$BASE_URL/api/productos" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "nombre": "Filtro de Aceite",
    "precio": 50000,
    "tasaIva": 19.0,
    "stock": 100,
    "categoriaId": "cat123"
  }' | jq '.'

echo -e "\n\n"

# Test 2: Crear factura (descuenta stock automáticamente)
echo "Test 2: Crear factura EMITIDA"
FACTURA=$(curl -X POST "$BASE_URL/api/facturas" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "clienteId": "user123",
    "items": [
      {
        "productoId": "PRODUCTO_ID_AQUI",
        "cantidad": 5
      }
    ]
  }')

echo "$FACTURA" | jq '.'
FACTURA_ID=$(echo "$FACTURA" | jq -r '.factura.id')

echo -e "\n\n"

# Test 3: Verificar cálculo de IVA
echo "Test 3: Verificar desglose tributario"
echo "$FACTURA" | jq '.factura | {
  subtotal,
  totalIva,
  total,
  items: .items[] | {
    nombreProducto,
    cantidad,
    precioUnitario,
    tasaIva,
    valorIva,
    totalItem
  }
}'

echo -e "\n\n"

# Test 4: Descargar PDF
echo "Test 4: Generar PDF"
curl -X GET "$BASE_URL/api/facturas/$FACTURA_ID/pdf" \
  -H "Authorization: Bearer $TOKEN" \
  -o "factura_$FACTURA_ID.pdf"

if [ -f "factura_$FACTURA_ID.pdf" ]; then
  echo "✅ PDF generado: factura_$FACTURA_ID.pdf"
  ls -lh "factura_$FACTURA_ID.pdf"
else
  echo "❌ Error al generar PDF"
fi

echo -e "\n\n"

# Test 5: Crear borrador (no descuenta stock)
echo "Test 5: Crear factura BORRADOR"
BORRADOR=$(curl -X POST "$BASE_URL/api/facturas/borrador" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "clienteId": "user123",
    "items": [
      {
        "productoId": "PRODUCTO_ID_AQUI",
        "cantidad": 3
      }
    ]
  }')

echo "$BORRADOR" | jq '.'
BORRADOR_ID=$(echo "$BORRADOR" | jq -r '.factura.id')

echo -e "\n\n"

# Test 6: Emitir borrador (ahora sí descuenta stock)
echo "Test 6: Emitir borrador"
curl -X POST "$BASE_URL/api/facturas/$BORRADOR_ID/emitir" \
  -H "Authorization: Bearer $TOKEN" | jq '.'

echo -e "\n\n"

# Test 7: Intentar crear factura sin stock suficiente (debe fallar con 409)
echo "Test 7: Validar error de stock insuficiente"
curl -X POST "$BASE_URL/api/facturas" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "clienteId": "user123",
    "items": [
      {
        "productoId": "PRODUCTO_ID_AQUI",
        "cantidad": 999999
      }
    ]
  }' | jq '.'

echo -e "\n\n"

# Test 8: Anular factura
echo "Test 8: Anular factura"
curl -X POST "$BASE_URL/api/facturas/$FACTURA_ID/anular" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "motivo": "Prueba de anulación"
  }' | jq '.'

echo -e "\n\nTodas las pruebas completadas ✅"

