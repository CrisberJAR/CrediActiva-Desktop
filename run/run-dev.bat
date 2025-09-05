@echo off
REM Script para ejecutar CrediActiva Desktop en modo desarrollo (Windows)

echo ========================================
echo    CrediActiva Desktop - Desarrollo
echo ========================================

REM Verificar Java 21
echo Verificando Java 21...
java -version 2>&1 | findstr "21" >nul
if %errorlevel% neq 0 (
    echo ERROR: Se requiere Java 21 LTS
    echo Por favor instale Java 21 y configure JAVA_HOME
    pause
    exit /b 1
)

REM Verificar Maven
echo Verificando Maven...
mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Maven no está instalado o no está en el PATH
    echo Por favor instale Maven 3.9+ y agregue al PATH
    pause
    exit /b 1
)

REM Crear directorio de logs si no existe
if not exist "logs" mkdir logs

REM Configurar variables de entorno si no están definidas
if "%DB_URL%"=="" set DB_URL=jdbc:mysql://localhost:3306/crediactiva?serverTimezone=America/Lima^&useSSL=false^&allowPublicKeyRetrieval=true
if "%DB_USERNAME%"=="" set DB_USERNAME=crediactiva_user
if "%DB_PASSWORD%"=="" set DB_PASSWORD=crediactiva_pass

echo Configuración de base de datos:
echo URL: %DB_URL%
echo Usuario: %DB_USERNAME%
echo.

REM Compilar si es necesario
echo Compilando aplicación...
call mvn clean compile -q
if %errorlevel% neq 0 (
    echo ERROR: Falló la compilación
    pause
    exit /b 1
)

echo Compilación exitosa!
echo.

REM Ejecutar aplicación
echo Iniciando CrediActiva Desktop...
echo Para detener la aplicación, cierre la ventana o presione Ctrl+C
echo.

call mvn javafx:run

if %errorlevel% neq 0 (
    echo.
    echo ERROR: La aplicación terminó con errores
    echo Revise los logs en la carpeta 'logs' para más detalles
    pause
    exit /b 1
)

echo.
echo Aplicación terminada correctamente
pause


