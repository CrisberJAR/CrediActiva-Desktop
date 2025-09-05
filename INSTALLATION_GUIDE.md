# Guía de Instalación y Configuración - CrediActiva Desktop

## Requisitos del Sistema

### Software Requerido
- **Java 21 LTS** - [Descargar desde Oracle](https://www.oracle.com/java/technologies/downloads/#java21) o [OpenJDK](https://adoptium.net/)
- **Apache Maven 3.9+** - [Descargar desde Apache Maven](https://maven.apache.org/download.cgi)
- **MySQL Server 8.0+** - [Descargar desde MySQL](https://dev.mysql.com/downloads/mysql/)
- **MySQL Workbench** (opcional pero recomendado) - [Descargar desde MySQL](https://dev.mysql.com/downloads/workbench/)

### Especificaciones Mínimas
- **RAM**: 512 MB mínimo, 2 GB recomendado
- **Espacio en disco**: 100 MB para la aplicación + espacio para base de datos
- **Sistema Operativo**: Windows 10+, macOS 10.15+, Linux Ubuntu 20.04+

## Instalación Paso a Paso

### 1. Instalar Java 21 LTS

#### Windows:
1. Descargar el instalador de Java 21 LTS
2. Ejecutar el instalador como administrador
3. Verificar instalación:
```cmd
java -version
javac -version
```

#### Linux:
```bash
# Ubuntu/Debian
sudo apt update
sudo apt install openjdk-21-jdk

# Verificar
java -version
```

#### macOS:
```bash
# Usando Homebrew
brew install openjdk@21

# Verificar
java -version
```

### 2. Instalar Apache Maven

#### Windows:
1. Descargar Maven desde el sitio oficial
2. Extraer en `C:\Program Files\Apache\Maven`
3. Agregar al PATH: `C:\Program Files\Apache\Maven\bin`
4. Verificar:
```cmd
mvn -version
```

#### Linux:
```bash
# Ubuntu/Debian
sudo apt install maven

# Verificar
mvn -version
```

#### macOS:
```bash
# Usando Homebrew
brew install maven

# Verificar
mvn -version
```

### 3. Instalar y Configurar MySQL

#### Instalación:
1. Descargar MySQL Server 8.0+
2. Instalar con configuración por defecto
3. **Recordar la contraseña del usuario root**
4. Instalar MySQL Workbench (opcional)

#### Configuración inicial:
```sql
-- Conectar como root
mysql -u root -p

-- Crear base de datos
CREATE DATABASE crediactiva CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Crear usuario específico
CREATE USER 'crediactiva_user'@'localhost' IDENTIFIED BY 'crediactiva_pass';
GRANT SELECT, INSERT, UPDATE, DELETE ON crediactiva.* TO 'crediactiva_user'@'localhost';
FLUSH PRIVILEGES;

-- Configurar zona horaria
SET time_zone = '-05:00'; -- America/Lima
```

### 4. Configurar el Proyecto CrediActiva

#### Clonar o descargar el proyecto:
```bash
# Si tienes Git instalado
git clone <repository-url>
cd CrediActiva_Desktop

# O descomprimir el archivo ZIP en la carpeta deseada
```

#### Configurar base de datos:
```bash
# Ejecutar scripts SQL en orden
mysql -u root -p crediactiva < scripts/00_create_schema.sql
mysql -u root -p crediactiva < scripts/01_seed_data.sql
mysql -u root -p crediactiva < scripts/02_views_procs.sql
```

#### Configurar conexión (opcional):
Editar `src/main/resources/application.properties` si necesitas cambiar la configuración:

```properties
# Configuración de base de datos
db.url=jdbc:mysql://localhost:3306/crediactiva?serverTimezone=America/Lima&useSSL=false&allowPublicKeyRetrieval=true
db.username=crediactiva_user
db.password=crediactiva_pass
```

O usar variables de entorno:
```bash
# Windows (PowerShell)
$env:DB_URL="jdbc:mysql://localhost:3306/crediactiva?serverTimezone=America/Lima&useSSL=false&allowPublicKeyRetrieval=true"
$env:DB_USERNAME="crediactiva_user"
$env:DB_PASSWORD="crediactiva_pass"

# Linux/macOS
export DB_URL="jdbc:mysql://localhost:3306/crediactiva?serverTimezone=America/Lima&useSSL=false&allowPublicKeyRetrieval=true"
export DB_USERNAME="crediactiva_user"
export DB_PASSWORD="crediactiva_pass"
```

### 5. Compilar y Ejecutar

#### Compilar el proyecto:
```bash
mvn clean compile
```

#### Ejecutar pruebas:
```bash
mvn test
```

#### Ejecutar la aplicación:
```bash
mvn javafx:run
```

#### O usar los scripts de ejecución:
```bash
# Windows
run\run-dev.bat

# Linux/macOS
chmod +x run/run-dev.sh
./run/run-dev.sh
```

## Verificación de la Instalación

### 1. Verificar Base de Datos
Conectar a MySQL y verificar que las tablas se crearon correctamente:
```sql
USE crediactiva;
SHOW TABLES;

-- Verificar datos iniciales
SELECT * FROM usuarios;
SELECT * FROM roles;
```

### 2. Verificar Aplicación
1. La aplicación debe iniciar mostrando la pantalla de login
2. Usar las credenciales de prueba:
   - **Admin**: `admin` / `admin123`
   - **Asesor**: `asesor1` / `asesor123`
   - **Cliente**: `cliente1` / `cliente123`

### 3. Verificar Logs
Los logs se generan en la carpeta `logs/`:
- `crediactiva.log` - Log general
- `crediactiva-error.log` - Solo errores
- `crediactiva-audit.log` - Log de auditoría

## Solución de Problemas Comunes

### Error: "Java not found"
**Solución**: Verificar que Java 21 esté instalado y en el PATH
```bash
java -version
echo $JAVA_HOME  # Linux/macOS
echo %JAVA_HOME% # Windows
```

### Error: "mvn command not found"
**Solución**: Instalar Maven y agregarlo al PATH
```bash
mvn -version
```

### Error: "Access denied for user"
**Solución**: Verificar credenciales de MySQL
```sql
-- Recrear usuario si es necesario
DROP USER IF EXISTS 'crediactiva_user'@'localhost';
CREATE USER 'crediactiva_user'@'localhost' IDENTIFIED BY 'crediactiva_pass';
GRANT ALL PRIVILEGES ON crediactiva.* TO 'crediactiva_user'@'localhost';
FLUSH PRIVILEGES;
```

### Error: "Connection refused"
**Solución**: Verificar que MySQL esté ejecutándose
```bash
# Windows
net start mysql80

# Linux
sudo systemctl start mysql
sudo systemctl status mysql

# macOS
brew services start mysql
```

### Error: JavaFX Runtime Components Missing
**Solución**: El proyecto incluye JavaFX como dependencia de Maven, debería resolverse automáticamente. Si persiste:
```bash
mvn clean install -U
```

### Error: "Port 3306 already in use"
**Solución**: Cambiar el puerto en `application.properties` o detener otros servicios MySQL:
```properties
db.url=jdbc:mysql://localhost:3307/crediactiva?serverTimezone=America/Lima&useSSL=false&allowPublicKeyRetrieval=true
```

## Configuración Avanzada

### Configurar SSL (Opcional)
Para producción, habilitar SSL en MySQL:
```properties
db.url=jdbc:mysql://localhost:3306/crediactiva?serverTimezone=America/Lima&useSSL=true&requireSSL=true
```

### Configurar Pool de Conexiones
Ajustar configuración de HikariCP en `application.properties`:
```properties
db.pool.maximum-pool-size=20
db.pool.minimum-idle=5
db.pool.connection-timeout=30000
```

### Configurar Logs
Ajustar nivel de logs en `src/main/resources/logback.xml`:
```xml
<root level="INFO">  <!-- Cambiar a DEBUG para más detalle -->
```

## Próximos Pasos

1. **Personalizar la aplicación** según las necesidades específicas
2. **Configurar backups** de la base de datos
3. **Implementar funcionalidades adicionales** como reportes avanzados
4. **Configurar entorno de producción** con configuraciones de seguridad adicionales

## Soporte

Para problemas o preguntas:
1. Revisar los logs en la carpeta `logs/`
2. Verificar la configuración de la base de datos
3. Consultar la documentación del proyecto en `README.md`

---

**Nota**: Esta guía asume una instalación en entorno de desarrollo. Para producción, considerar configuraciones adicionales de seguridad, backups y monitoreo.


