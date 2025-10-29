@echo off
REM Script para regenerar docs/api.json -> docs/openapi.yaml y preparar commit
powershell -Command "Invoke-RestMethod -Uri 'http://localhost:8080/v3/api-docs' -OutFile 'docs\\api.json' -ErrorAction Stop"
python scripts\convert_api_json_to_yaml.py
if %ERRORLEVEL% NEQ 0 (
    echo Error al generar openapi.yaml
    exit /b %ERRORLEVEL%
)

git add docs/api.json docs/openapi.yaml
git commit -m "chore: regenerar OpenAPI desde /v3/api-docs"

echo Hecho. Revisa el commit y haz push si todo está bien.

