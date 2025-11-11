# Script de Verificación del Backend - Error 401 Carritos
# Este script verifica que el backend esté correctamente configurado

Write-Host "`n" -NoNewline
Write-Host "╔═══════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║     VERIFICACIÓN BACKEND - CARRITOS ANÓNIMOS                  ║" -ForegroundColor Cyan
Write-Host "╚═══════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""

# Función para verificar endpoint
function Test-Endpoint {
    param(
        [string]$Name,
        [string]$Url,
        [string]$Method,
        [string]$Body = $null
    )
    
    Write-Host "📍 $Name" -ForegroundColor Yellow
    Write-Host "   URL: $Url" -ForegroundColor Gray
    
    try {
        $headers = @{ "Content-Type" = "application/json" }
        
        if ($Method -eq "POST" -and $Body) {
            $response = Invoke-RestMethod -Uri $Url -Method $Method -Headers $headers -Body $Body -ErrorAction Stop
        } else {
            $response = Invoke-RestMethod -Uri $Url -Method $Method -ErrorAction Stop
        }
        
        Write-Host "   ✅ SUCCESS" -ForegroundColor Green
        return @{ Success = $true; Data = $response }
    }
    catch {
        $statusCode = $_.Exception.Response.StatusCode.value__
        Write-Host "   ❌ FAILED - Status: $statusCode" -ForegroundColor Red
        
        if ($statusCode -eq 401) {
            Write-Host "   ⚠️  Error 401: Autenticación requerida (configuración incorrecta)" -ForegroundColor Yellow
        }
        
        return @{ Success = $false; StatusCode = $statusCode }
    }
}

Write-Host "🔍 Verificando configuración del backend...`n" -ForegroundColor Cyan

# Test 1: Crear carrito
Write-Host "TEST 1: Crear Carrito Anónimo" -ForegroundColor Magenta
Write-Host "────────────────────────────────────────────────────────────────" -ForegroundColor DarkGray
$test1 = Test-Endpoint -Name "POST /api/carritos" -Url "http://localhost:8080/api/carritos" -Method "POST" -Body '{"items":[]}'
Write-Host ""

if (-not $test1.Success) {
    Write-Host "❌ FALLO CRÍTICO: No se puede crear carrito" -ForegroundColor Red
    Write-Host ""
    Write-Host "Causa posible:" -ForegroundColor Yellow
    Write-Host "  • Backend no está corriendo" -ForegroundColor White
    Write-Host "  • MongoDB no está corriendo" -ForegroundColor White
    Write-Host ""
    Write-Host "Solución:" -ForegroundColor Cyan
    Write-Host "  1. Inicia MongoDB: mongod --dbpath C:\data\db" -ForegroundColor White
    Write-Host "  2. Inicia backend: ./mvnw clean spring-boot:run" -ForegroundColor White
    Write-Host ""
    exit 1
}

$carritoId = $test1.Data.carrito.id
Write-Host "🎫 Carrito ID: $carritoId`n" -ForegroundColor Green

# Test 2: Agregar item (CRÍTICO - requiere /**)
Write-Host "TEST 2: Agregar Item al Carrito (Requiere /**)" -ForegroundColor Magenta
Write-Host "────────────────────────────────────────────────────────────────" -ForegroundColor DarkGray
$test2 = Test-Endpoint -Name "POST /api/carritos/$carritoId/items" -Url "http://localhost:8080/api/carritos/$carritoId/items" -Method "POST" -Body '{"productoId":"690f7c95c989e80f1c0afc78","cantidad":2}'
Write-Host ""

if (-not $test2.Success) {
    Write-Host "❌ ERROR CRÍTICO: No se puede agregar items al carrito" -ForegroundColor Red
    Write-Host ""
    
    if ($test2.StatusCode -eq 401) {
        Write-Host "🔴 CAUSA: SecurityConfig.java NO tiene el patrón correcto" -ForegroundColor Red
        Write-Host ""
        Write-Host "El backend está rechazando la petición con 401 Unauthorized." -ForegroundColor Yellow
        Write-Host "Esto significa que la configuración de seguridad NO permite este endpoint.`n" -ForegroundColor Yellow
        
        Write-Host "DIAGNÓSTICO:" -ForegroundColor Cyan
        Write-Host "  ✅ POST /api/carritos funciona (crear carrito)" -ForegroundColor Green
        Write-Host "  ❌ POST /api/carritos/{id}/items NO funciona (agregar item)" -ForegroundColor Red
        Write-Host ""
        Write-Host "CAUSA:" -ForegroundColor Yellow
        Write-Host "  El patrón en SecurityConfig.java probablemente es:" -ForegroundColor White
        Write-Host "    • .requestMatchers(`"/api/carritos`").permitAll()   ← Sin /**" -ForegroundColor Red
        Write-Host "  O:" -ForegroundColor White
        Write-Host "    • .requestMatchers(`"/api/carritos/*`").permitAll()  ← Solo un nivel" -ForegroundColor Red
        Write-Host ""
        Write-Host "DEBE SER:" -ForegroundColor Cyan
        Write-Host "    • .requestMatchers(`"/api/carritos/**`").permitAll() ← Con /**" -ForegroundColor Green
        Write-Host ""
        Write-Host "SOLUCIÓN:" -ForegroundColor Cyan
        Write-Host "  1. Abre: src\main\java\com\repobackend\api\auth\config\SecurityConfig.java" -ForegroundColor White
        Write-Host "  2. Busca la línea: .requestMatchers(`"/api/carritos" -ForegroundColor White
        Write-Host "  3. Asegúrate que sea: .requestMatchers(`"/api/carritos/**`").permitAll()" -ForegroundColor Green
        Write-Host "  4. Guarda el archivo (Ctrl+S)" -ForegroundColor White
        Write-Host "  5. REINICIA con clean:" -ForegroundColor Yellow
        Write-Host "       ./mvnw clean spring-boot:run" -ForegroundColor White
        Write-Host "  6. Espera 'Started InventarioRepuestosBackendApplication'" -ForegroundColor White
        Write-Host "  7. Ejecuta este script de nuevo para verificar" -ForegroundColor White
        Write-Host ""
    }
    
    exit 1
}

# Test 3: Verificar carrito
Write-Host "TEST 3: Obtener Carrito" -ForegroundColor Magenta
Write-Host "────────────────────────────────────────────────────────────────" -ForegroundColor DarkGray
$test3 = Test-Endpoint -Name "GET /api/carritos/$carritoId" -Url "http://localhost:8080/api/carritos/$carritoId" -Method "GET"
Write-Host ""

if ($test3.Success) {
    $itemCount = $test3.Data.carrito.items.Count
    Write-Host "📦 Items en carrito: $itemCount`n" -ForegroundColor Green
}

# Test 4: Eliminar item
Write-Host "TEST 4: Eliminar Item del Carrito" -ForegroundColor Magenta
Write-Host "────────────────────────────────────────────────────────────────" -ForegroundColor DarkGray
$test4 = Test-Endpoint -Name "DELETE /api/carritos/$carritoId/items/690f7c95c989e80f1c0afc78" -Url "http://localhost:8080/api/carritos/$carritoId/items/690f7c95c989e80f1c0afc78" -Method "DELETE"
Write-Host ""

# Resumen final
Write-Host "╔═══════════════════════════════════════════════════════════════╗" -ForegroundColor Green
Write-Host "║                    VERIFICACIÓN COMPLETA                      ║" -ForegroundColor Green
Write-Host "╚═══════════════════════════════════════════════════════════════╝" -ForegroundColor Green
Write-Host ""

if ($test1.Success -and $test2.Success -and $test3.Success -and $test4.Success) {
    Write-Host "🎉 ¡TODO CORRECTO!" -ForegroundColor Green
    Write-Host ""
    Write-Host "✅ Backend configurado correctamente" -ForegroundColor Green
    Write-Host "✅ Carritos anónimos funcionan" -ForegroundColor Green
    Write-Host "✅ Se pueden agregar items sin autenticación" -ForegroundColor Green
    Write-Host "✅ Se pueden eliminar items sin autenticación" -ForegroundColor Green
    Write-Host ""
    Write-Host "📱 El backend está listo para la app Android" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "PRÓXIMOS PASOS:" -ForegroundColor Yellow
    Write-Host "  1. Abre la app Android en el emulador" -ForegroundColor White
    Write-Host "  2. Navega al carrito (ícono 🛒)" -ForegroundColor White
    Write-Host "  3. Agrega productos desde cualquier categoría" -ForegroundColor White
    Write-Host "  4. Verifica que aparezcan en el carrito" -ForegroundColor White
    Write-Host ""
} else {
    Write-Host "❌ VERIFICACIÓN FALLIDA" -ForegroundColor Red
    Write-Host ""
    Write-Host "Revisa los errores arriba para más detalles." -ForegroundColor Yellow
    Write-Host ""
}
