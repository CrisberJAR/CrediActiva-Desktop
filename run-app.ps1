# Script para ejecutar CrediActiva Desktop
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "    CrediActiva Desktop - Ejecutar" -ForegroundColor Cyan  
Write-Host "========================================" -ForegroundColor Cyan

# Compilar aplicación
Write-Host "Compilando aplicación..." -ForegroundColor Yellow
mvn clean compile -q

if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Falló la compilación" -ForegroundColor Red
    exit 1
}

Write-Host "Compilación exitosa!" -ForegroundColor Green

# Ejecutar aplicación
Write-Host "Iniciando CrediActiva Desktop..." -ForegroundColor Yellow
Write-Host "Para detener la aplicación, cierre la ventana" -ForegroundColor Gray

# Intentar diferentes métodos de ejecución
Write-Host "Intentando ejecutar con JavaFX plugin..." -ForegroundColor Cyan
mvn javafx:run 2>$null

if ($LASTEXITCODE -ne 0) {
    Write-Host "Intentando ejecutar con exec plugin..." -ForegroundColor Cyan
    mvn exec:java 2>$null
}

if ($LASTEXITCODE -ne 0) {
    Write-Host "Intentando ejecutar directamente con Java..." -ForegroundColor Cyan
    $classpath = "target/classes"
    
    # Obtener dependencias de Maven
    $dependencies = mvn dependency:build-classpath -Dmdep.outputFile=classpath.tmp -q 2>$null
    if (Test-Path "classpath.tmp") {
        $depClasspath = Get-Content "classpath.tmp"
        $classpath = "$classpath;$depClasspath"
        Remove-Item "classpath.tmp"
    }
    
    # Ejecutar con JavaFX desde Maven dependencies
    java -cp $classpath pe.crediactiva.app.CrediActivaApp
}

if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: No se pudo ejecutar la aplicación" -ForegroundColor Red
    Write-Host "Verifique que tiene Java 21 y JavaFX instalados" -ForegroundColor Yellow
    exit 1
}

Write-Host "Aplicación terminada" -ForegroundColor Green
