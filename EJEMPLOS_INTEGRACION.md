# 🔧 Ejemplos de Integración - Frontend

## 📱 ANDROID (Kotlin/Jetpack Compose)

### 1. Modelo de Datos Actualizado

```kotlin
// models/Producto.kt
data class Producto(
    val id: String,
    val nombre: String,
    val descripcion: String? = null,
    val precio: Double,
    val tasaIva: Double = 19.0,  // ⬅️ NUEVO con default
    val stock: Int = 0,
    val categoriaId: String? = null,
    val thumbnailUrl: String? = null
) {
    // Helper para calcular precio con IVA incluido
    fun precioConIva(): Double {
        return precio * (1 + tasaIva / 100.0)
    }
    
    // Helper para calcular solo el valor del IVA
    fun valorIva(): Double {
        return precio * (tasaIva / 100.0)
    }
}
```

### 2. Servicio API (Retrofit)

```kotlin
// api/ProductoService.kt
interface ProductoService {
    @GET("public/productos")
    suspend fun getProductos(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
        @Query("q") query: String? = null,
        @Query("categoriaId") categoriaId: String? = null
    ): ProductosResponse
    
    @GET("public/productos/{id}")
    suspend fun getProducto(@Path("id") id: String): ProductoResponse
}

data class ProductosResponse(
    val productos: List<Producto>,
    val total: Long,
    val page: Int,
    val size: Int
)

data class ProductoResponse(
    val producto: Producto
)
```

### 3. UI - Card de Producto

```kotlin
// ui/components/ProductoCard.kt
@Composable
fun ProductoCard(
    producto: Producto,
    onAddToCart: (Producto) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Nombre
            Text(
                text = producto.nombre,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Precio base
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Precio base:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                Text(
                    text = formatoPrecio(producto.precio),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            // IVA
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "IVA (${producto.tasaIva}%):",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Text(
                    text = formatoPrecio(producto.valorIva()),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Total con IVA
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = formatoPrecio(producto.precioConIva()),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            // Stock
            if (producto.stock > 0) {
                Text(
                    text = "${producto.stock} disponibles",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Green,
                    modifier = Modifier.padding(top = 4.dp)
                )
            } else {
                Text(
                    text = "Sin stock",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Red,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            // Botón agregar al carrito
            Button(
                onClick = { onAddToCart(producto) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                enabled = producto.stock > 0
            ) {
                Icon(Icons.Default.ShoppingCart, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Agregar al carrito")
            }
        }
    }
}

// Helper para formatear precios
fun formatoPrecio(precio: Double): String {
    return NumberFormat.getCurrencyInstance(Locale("es", "CO")).format(precio)
}
```

### 4. ViewModel

```kotlin
// viewmodels/ProductosViewModel.kt
class ProductosViewModel(
    private val productoService: ProductoService
) : ViewModel() {
    
    private val _productos = MutableStateFlow<List<Producto>>(emptyList())
    val productos: StateFlow<List<Producto>> = _productos.asStateFlow()
    
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()
    
    fun cargarProductos(categoriaId: String? = null) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val response = productoService.getProductos(
                    categoriaId = categoriaId
                )
                _productos.value = response.productos
            } catch (e: Exception) {
                Log.e("ProductosVM", "Error cargando productos", e)
            } finally {
                _loading.value = false
            }
        }
    }
}
```

---

## 🌐 NEXT.JS (TypeScript/React)

### 1. Tipos TypeScript Actualizados

```typescript
// types/producto.ts
export interface Producto {
  id: string;
  nombre: string;
  descripcion?: string;
  precio: number;
  tasaIva?: number;  // ⬅️ NUEVO (opcional con default 19)
  stock: number;
  categoriaId?: string;
  thumbnailUrl?: string;
}

// types/factura.ts
export interface Factura {
  id: string;
  numeroFactura: string;
  estado: 'BORRADOR' | 'EMITIDA' | 'ANULADA';
  clienteId?: string;
  cliente?: Cliente;
  items: FacturaItem[];
  subtotal: number;        // ⬅️ NUEVO
  totalDescuentos: number; // ⬅️ NUEVO
  baseImponible: number;   // ⬅️ NUEVO
  totalIva: number;        // ⬅️ NUEVO
  total: number;
  creadoEn: string;
  emitidaEn?: string;
}

export interface FacturaItem {
  productoId: string;
  nombreProducto: string;  // ⬅️ NUEVO
  cantidad: number;
  precioUnitario: number;
  descuento?: number;      // ⬅️ NUEVO
  tasaIva: number;         // ⬅️ NUEVO
  valorIva: number;        // ⬅️ NUEVO
  subtotal: number;        // ⬅️ NUEVO
  totalItem: number;       // ⬅️ NUEVO
}

export interface Cliente {
  nombre: string;
  documento?: string;
  direccion?: string;
}
```

### 2. Cliente API

```typescript
// lib/api/productos.ts
export async function crearProducto(data: {
  nombre: string;
  descripcion?: string;
  precio: number;
  tasaIva?: number;  // ⬅️ NUEVO
  stock: number;
  categoriaId?: string;
}): Promise<{ producto: Producto }> {
  const response = await fetch('/api/productos', {
    method: 'POST',
    headers: { 
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${getToken()}`
    },
    body: JSON.stringify({
      ...data,
      tasaIva: data.tasaIva ?? 19  // Default 19% si no se especifica
    })
  });
  
  if (!response.ok) {
    throw new Error('Error al crear producto');
  }
  
  return response.json();
}

export async function actualizarProducto(
  id: string, 
  data: Partial<Producto>
): Promise<{ producto: Producto }> {
  const response = await fetch(`/api/productos/${id}`, {
    method: 'PATCH',
    headers: { 
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${getToken()}`
    },
    body: JSON.stringify(data)
  });
  
  if (!response.ok) {
    throw new Error('Error al actualizar producto');
  }
  
  return response.json();
}
```

```typescript
// lib/api/facturas.ts
export async function crearFactura(data: {
  clienteId?: string;
  cliente?: Cliente;
  items: Array<{
    productoId: string;
    cantidad: number;
  }>;
}): Promise<{ factura: Factura }> {
  const response = await fetch('/api/facturas', {
    method: 'POST',
    headers: { 
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${getToken()}`
    },
    body: JSON.stringify(data)
  });
  
  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.error || 'Error al crear factura');
  }
  
  return response.json();
}

export async function descargarPdfFactura(id: string): Promise<Blob> {
  const response = await fetch(`/api/facturas/${id}/pdf`, {
    headers: { 
      'Authorization': `Bearer ${getToken()}`
    }
  });
  
  if (!response.ok) {
    throw new Error('Error al descargar PDF');
  }
  
  return response.blob();
}
```

### 3. Formulario de Producto

```tsx
// components/ProductoForm.tsx
'use client';

import { useState } from 'react';
import { Producto } from '@/types/producto';
import { crearProducto, actualizarProducto } from '@/lib/api/productos';

interface Props {
  producto?: Producto;
  onSuccess?: () => void;
}

export default function ProductoForm({ producto, onSuccess }: Props) {
  const [formData, setFormData] = useState({
    nombre: producto?.nombre || '',
    descripcion: producto?.descripcion || '',
    precio: producto?.precio || 0,
    tasaIva: producto?.tasaIva || 19,  // ⬅️ NUEVO con default
    stock: producto?.stock || 0,
    categoriaId: producto?.categoriaId || '',
  });
  
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    
    try {
      if (producto) {
        await actualizarProducto(producto.id, formData);
      } else {
        await crearProducto(formData);
      }
      onSuccess?.();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Error desconocido');
    } finally {
      setLoading(false);
    }
  };
  
  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      {/* Nombre */}
      <div>
        <label className="block text-sm font-medium mb-1">
          Nombre *
        </label>
        <input
          type="text"
          value={formData.nombre}
          onChange={(e) => setFormData({...formData, nombre: e.target.value})}
          className="w-full border rounded px-3 py-2"
          required
        />
      </div>
      
      {/* Descripción */}
      <div>
        <label className="block text-sm font-medium mb-1">
          Descripción
        </label>
        <textarea
          value={formData.descripcion}
          onChange={(e) => setFormData({...formData, descripcion: e.target.value})}
          className="w-full border rounded px-3 py-2"
          rows={3}
        />
      </div>
      
      {/* Precio */}
      <div>
        <label className="block text-sm font-medium mb-1">
          Precio (sin IVA) *
        </label>
        <input
          type="number"
          value={formData.precio}
          onChange={(e) => setFormData({...formData, precio: Number(e.target.value)})}
          className="w-full border rounded px-3 py-2"
          min="0"
          step="100"
          required
        />
      </div>
      
      {/* IVA - NUEVO */}
      <div>
        <label className="block text-sm font-medium mb-1">
          IVA * (Colombia)
        </label>
        <select
          value={formData.tasaIva}
          onChange={(e) => setFormData({...formData, tasaIva: Number(e.target.value)})}
          className="w-full border rounded px-3 py-2"
        >
          <option value={0}>0% - Exento de IVA</option>
          <option value={5}>5% - Canasta básica</option>
          <option value={19}>19% - Estándar</option>
        </select>
        <p className="text-xs text-gray-500 mt-1">
          Precio final: ${(formData.precio * (1 + formData.tasaIva / 100)).toLocaleString()}
        </p>
      </div>
      
      {/* Stock */}
      <div>
        <label className="block text-sm font-medium mb-1">
          Stock inicial
        </label>
        <input
          type="number"
          value={formData.stock}
          onChange={(e) => setFormData({...formData, stock: Number(e.target.value)})}
          className="w-full border rounded px-3 py-2"
          min="0"
        />
      </div>
      
      {error && (
        <div className="bg-red-50 text-red-600 p-3 rounded">
          {error}
        </div>
      )}
      
      <button
        type="submit"
        disabled={loading}
        className="w-full bg-blue-600 text-white py-2 px-4 rounded hover:bg-blue-700 disabled:opacity-50"
      >
        {loading ? 'Guardando...' : producto ? 'Actualizar' : 'Crear'} Producto
      </button>
    </form>
  );
}
```

### 4. Tabla de Productos

```tsx
// components/ProductosTable.tsx
'use client';

import { Producto } from '@/types/producto';

interface Props {
  productos: Producto[];
  onEdit?: (producto: Producto) => void;
  onDelete?: (id: string) => void;
}

export default function ProductosTable({ productos, onEdit, onDelete }: Props) {
  return (
    <div className="overflow-x-auto">
      <table className="min-w-full divide-y divide-gray-200">
        <thead className="bg-gray-50">
          <tr>
            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
              Nombre
            </th>
            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
              Precio base
            </th>
            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
              IVA
            </th>
            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
              Precio + IVA
            </th>
            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
              Stock
            </th>
            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
              Acciones
            </th>
          </tr>
        </thead>
        <tbody className="bg-white divide-y divide-gray-200">
          {productos.map((producto) => {
            const tasaIva = producto.tasaIva || 19;
            const precioConIva = producto.precio * (1 + tasaIva / 100);
            
            return (
              <tr key={producto.id}>
                <td className="px-6 py-4 whitespace-nowrap">
                  {producto.nombre}
                </td>
                <td className="px-6 py-4 whitespace-nowrap">
                  ${producto.precio.toLocaleString()}
                </td>
                <td className="px-6 py-4 whitespace-nowrap">
                  {tasaIva}%
                </td>
                <td className="px-6 py-4 whitespace-nowrap font-semibold">
                  ${precioConIva.toLocaleString()}
                </td>
                <td className="px-6 py-4 whitespace-nowrap">
                  {producto.stock}
                </td>
                <td className="px-6 py-4 whitespace-nowrap">
                  <button
                    onClick={() => onEdit?.(producto)}
                    className="text-blue-600 hover:text-blue-800 mr-3"
                  >
                    Editar
                  </button>
                  <button
                    onClick={() => onDelete?.(producto.id)}
                    className="text-red-600 hover:text-red-800"
                  >
                    Eliminar
                  </button>
                </td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}
```

### 5. Vista de Factura con IVA

```tsx
// components/FacturaDetalle.tsx
'use client';

import { Factura } from '@/types/factura';
import { descargarPdfFactura } from '@/lib/api/facturas';

interface Props {
  factura: Factura;
}

export default function FacturaDetalle({ factura }: Props) {
  const handleDescargarPdf = async () => {
    try {
      const blob = await descargarPdfFactura(factura.id);
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `factura-${factura.numeroFactura}.pdf`;
      a.click();
      URL.revokeObjectURL(url);
    } catch (error) {
      alert('Error al descargar PDF');
    }
  };
  
  return (
    <div className="bg-white p-6 rounded-lg shadow">
      {/* Encabezado */}
      <div className="flex justify-between items-start mb-6">
        <div>
          <h2 className="text-2xl font-bold">
            Factura #{factura.numeroFactura}
          </h2>
          <p className="text-gray-600">
            Estado: <span className={`font-semibold ${
              factura.estado === 'EMITIDA' ? 'text-green-600' :
              factura.estado === 'BORRADOR' ? 'text-yellow-600' :
              'text-red-600'
            }`}>{factura.estado}</span>
          </p>
        </div>
        <button
          onClick={handleDescargarPdf}
          className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700"
        >
          📄 Descargar PDF
        </button>
      </div>
      
      {/* Items */}
      <table className="w-full mb-6">
        <thead className="bg-gray-50">
          <tr>
            <th className="px-4 py-2 text-left">Producto</th>
            <th className="px-4 py-2 text-right">Cant.</th>
            <th className="px-4 py-2 text-right">P. Unit</th>
            <th className="px-4 py-2 text-right">Subtotal</th>
            <th className="px-4 py-2 text-right">IVA</th>
            <th className="px-4 py-2 text-right">Total</th>
          </tr>
        </thead>
        <tbody>
          {factura.items.map((item, idx) => (
            <tr key={idx} className="border-t">
              <td className="px-4 py-2">{item.nombreProducto}</td>
              <td className="px-4 py-2 text-right">{item.cantidad}</td>
              <td className="px-4 py-2 text-right">
                ${item.precioUnitario.toLocaleString()}
              </td>
              <td className="px-4 py-2 text-right">
                ${item.subtotal.toLocaleString()}
              </td>
              <td className="px-4 py-2 text-right">
                {item.tasaIva}%<br/>
                <span className="text-sm text-gray-600">
                  ${item.valorIva.toLocaleString()}
                </span>
              </td>
              <td className="px-4 py-2 text-right font-semibold">
                ${item.totalItem.toLocaleString()}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
      
      {/* Totales */}
      <div className="border-t pt-4 space-y-2">
        <div className="flex justify-between text-gray-600">
          <span>Subtotal:</span>
          <span>${factura.subtotal.toLocaleString()}</span>
        </div>
        <div className="flex justify-between text-gray-600">
          <span>Total IVA:</span>
          <span>${factura.totalIva.toLocaleString()}</span>
        </div>
        <div className="flex justify-between text-xl font-bold">
          <span>TOTAL:</span>
          <span>${factura.total.toLocaleString()}</span>
        </div>
      </div>
    </div>
  );
}
```

---

## 📦 Regenerar Clientes API

### Android (OpenAPI Generator)

```bash
# En build.gradle.kts
openApiGenerate {
    generatorName.set("kotlin")
    inputSpec.set("http://localhost:8080/v3/api-docs")
    outputDir.set("$buildDir/generated")
    apiPackage.set("com.example.api")
    modelPackage.set("com.example.models")
}

# Ejecutar
./gradlew openApiGenerate
```

### Next.js (TypeScript)

```bash
# Instalar generador
npm install -D @openapitools/openapi-generator-cli

# Generar cliente
npx openapi-generator-cli generate \
  -i http://localhost:8080/v3/api-docs \
  -g typescript-axios \
  -o ./lib/api/generated

# O usar swagger-typescript-api
npx swagger-typescript-api \
  -p http://localhost:8080/v3/api-docs \
  -o ./lib/api \
  -n api.ts
```

---

**Versión:** 2.0  
**Última actualización:** 2025-01-16

