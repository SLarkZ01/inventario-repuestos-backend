param(
    [string]$mongoUri = 'mongodb://localhost:27017/facturacion-inventario',
    [string]$jwtSecret = 'change_this_in_prod_to_a_long_random_value'
)

Write-Host "Setting environment variables and running Spring Boot..."

$env:MONGODB_URI = $mongoUri
$env:APP_JWT_SECRET = $jwtSecret

Write-Host "MONGODB_URI = $env:MONGODB_URI"
Write-Host "APP_JWT_SECRET = [HIDDEN]"

mvn spring-boot:run
