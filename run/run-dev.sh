#!/bin/bash
# Script para ejecutar CrediActiva Desktop en modo desarrollo (Linux/macOS)

echo "========================================"
echo "   CrediActiva Desktop - Desarrollo"
echo "========================================"

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Función para mostrar errores
error() {
    echo -e "${RED}ERROR: $1${NC}"
    exit 1
}

# Función para mostrar información
info() {
    echo -e "${GREEN}$1${NC}"
}

# Función para mostrar advertencias
warning() {
    echo -e "${YELLOW}$1${NC}"
}

# Verificar Java 21
echo "Verificando Java 21..."
if ! java -version 2>&1 | grep -q "21"; then
    error "Se requiere Java 21 LTS. Por favor instale Java 21 y configure JAVA_HOME"
fi
info "✓ Java 21 encontrado"

# Verificar Maven
echo "Verificando Maven..."
if ! command -v mvn &> /dev/null; then
    error "Maven no está instalado o no está en el PATH. Por favor instale Maven 3.9+"
fi
info "✓ Maven encontrado"

# Crear directorio de logs si no existe
mkdir -p logs

# Configurar variables de entorno si no están definidas
export DB_URL="${DB_URL:-jdbc:mysql://localhost:3306/crediactiva?serverTimezone=America/Lima&useSSL=false&allowPublicKeyRetrieval=true}"
export DB_USERNAME="${DB_USERNAME:-crediactiva_user}"
export DB_PASSWORD="${DB_PASSWORD:-crediactiva_pass}"

echo
echo "Configuración de base de datos:"
echo "URL: $DB_URL"
echo "Usuario: $DB_USERNAME"
echo

# Compilar si es necesario
info "Compilando aplicación..."
if ! mvn clean compile -q; then
    error "Falló la compilación"
fi
info "✓ Compilación exitosa!"
echo

# Ejecutar aplicación
info "Iniciando CrediActiva Desktop..."
echo "Para detener la aplicación, cierre la ventana o presione Ctrl+C"
echo

# Ejecutar con manejo de errores
if mvn javafx:run; then
    echo
    info "Aplicación terminada correctamente"
else
    echo
    error "La aplicación terminó con errores. Revise los logs en la carpeta 'logs' para más detalles"
fi


