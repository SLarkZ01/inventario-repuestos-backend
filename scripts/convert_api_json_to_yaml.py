# Script para convertir docs/api.json a docs/openapi.yaml
# Usa PyYAML; si no está instalado intenta instalarlo automáticamente.
import json
import sys
import subprocess
from pathlib import Path

try:
    import yaml
except Exception:
    print('PyYAML no encontrado. Instalando PyYAML...')
    subprocess.check_call([sys.executable, '-m', 'pip', 'install', 'PyYAML'])
    import yaml

in_path = Path('docs') / 'api.json'
out_path = Path('docs') / 'openapi.yaml'

if not in_path.exists():
    print(f'ERROR: {in_path} no existe. Ejecuta la app y asegúrate de exponer /v3/api-docs')
    sys.exit(2)

with in_path.open('r', encoding='utf-8') as f:
    data = json.load(f)

# Dump YAML preserving order (PyYAML safe_dump with sort_keys=False)
with out_path.open('w', encoding='utf-8') as f:
    yaml.safe_dump(data, f, sort_keys=False, allow_unicode=True)

print(f'Generado: {out_path}')

