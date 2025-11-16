<#
Set Cloudinary environment variables for the current PowerShell session or persist to user environment.

Usage (temporary for current session):
    .\set_cloudinary_env.ps1 -CloudName df7ggzasi -ApiKey 658776198171533 -ApiSecret 'G6AiYeUagieCYP4uZdh9db0z2WY'

Usage (persist to user env):
    .\set_cloudinary_env.ps1 -CloudName df7ggzasi -ApiKey 658776198171533 -ApiSecret '...' -Persist

This script DOES NOT write secrets to the repository. If you pass -Persist the variables will be saved to the current user's environment (you may need to restart shells).
#>
param(
    [Parameter(Mandatory=$true)][string]$CloudName,
    [Parameter(Mandatory=$true)][string]$ApiKey,
    [Parameter(Mandatory=$true)][string]$ApiSecret,
    [switch]$Persist
)

Write-Host "Setting Cloudinary environment variables for current session..." -ForegroundColor Cyan
$env:CLOUDINARY_CLOUD_NAME = $CloudName
$env:CLOUDINARY_API_KEY = $ApiKey
$env:CLOUDINARY_API_SECRET = $ApiSecret

Write-Host "CLOUDINARY_CLOUD_NAME=$env:CLOUDINARY_CLOUD_NAME" -ForegroundColor Green
Write-Host "CLOUDINARY_API_KEY=$env:CLOUDINARY_API_KEY" -ForegroundColor Green
Write-Host "CLOUDINARY_API_SECRET=(hidden)" -ForegroundColor Green

if ($Persist) {
    Write-Host "Persisting variables to user environment (requires shell restart to take effect)..." -ForegroundColor Yellow
    [System.Environment]::SetEnvironmentVariable("CLOUDINARY_CLOUD_NAME", $CloudName, [System.EnvironmentVariableTarget]::User)
    [System.Environment]::SetEnvironmentVariable("CLOUDINARY_API_KEY", $ApiKey, [System.EnvironmentVariableTarget]::User)
    [System.Environment]::SetEnvironmentVariable("CLOUDINARY_API_SECRET", $ApiSecret, [System.EnvironmentVariableTarget]::User)
    Write-Host "Persisted. Restart your shell (or log off / log on) for changes to be available in new processes." -ForegroundColor Yellow
}

Write-Host "Done." -ForegroundColor Cyan

