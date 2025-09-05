# CrediActiva Desktop

Aplicación de escritorio para gestión financiera desarrollada con JavaFX y MySQL.

## Descripción

CrediActiva Desktop es una aplicación de escritorio diseñada para el sector financiero que permite gestionar:
- Usuarios y roles (Administrador, Asesor, Cliente)
- Solicitudes de préstamo
- Préstamos y cronogramas de pago
- Reportes de comisiones y cobros
- Control de acceso basado en roles (RBAC)

## Tecnologías

- **Java 21 LTS**
- **JavaFX 21** para la interfaz gráfica
- **MySQL 8+** como base de datos
- **HikariCP** para pool de conexiones
- **SLF4J/Logback** para logging
- **BCrypt** para encriptación de contraseñas
- **JUnit 5** para pruebas unitarias
- **Maven** como gestor de dependencias

## Requisitos del Sistema

- Java 21 LTS
- MySQL Server 8.0+
- Maven 3.9+
- Mínimo 512MB RAM
- Sistema operativo: Windows 10+, macOS 10.15+, Linux Ubuntu 20.04+

## Instalación y Configuración

### 1. Clonar el repositorio
```bash
git clone <repository-url>
cd CrediActiva_Desktop
```

### 2. Configurar Base de Datos

#### Crear la base de datos:
```bash
mysql -u root -p < scripts/00_create_schema.sql
mysql -u root -p crediactiva < scripts/01_seed_data.sql
mysql -u root -p crediactiva < scripts/02_views_procs.sql
```

#### Configurar conexión:
Editar `src/main/resources/application.properties` o usar variables de entorno:

```properties
# Database Configuration
db.url=jdbc:mysql://localhost:3306/crediactiva?serverTimezone=America/Lima&useSSL=false&allowPublicKeyRetrieval=true
db.username=crediactiva_user
db.password=crediactiva_pass
db.driver=com.mysql.cj.jdbc.Driver

# HikariCP Configuration
db.pool.maximum-pool-size=10
db.pool.minimum-idle=2
db.pool.connection-timeout=20000
```

### 3. Compilar y Ejecutar

#### Compilar:
```bash
mvn clean compile
```

#### Ejecutar pruebas:
```bash
mvn test
```

#### Ejecutar aplicación:
```bash
mvn javafx:run
```

#### O usar los scripts de ejecución:
```bash
# Windows
run\run-dev.bat

# Linux/macOS
./run/run-dev.sh
```

### 4. Empaquetar aplicación:
```bash
mvn clean package
java -jar target/crediactiva-desktop-1.0.0.jar
```

## Usuarios por Defecto

Después de ejecutar los scripts de datos iniciales:

| Usuario | Contraseña | Rol |
|---------|------------|-----|
| admin | admin123 | Administrador |
| asesor1 | asesor123 | Asesor |
| cliente1 | cliente123 | Cliente |
| cliente2 | cliente123 | Cliente |

## Estructura del Proyecto

```
CrediActiva_Desktop/
├── pom.xml                     # Configuración Maven
├── README.md                   # Este archivo
├── .gitignore                  # Archivos ignorados por Git
├── scripts/                    # Scripts SQL
│   ├── 00_create_schema.sql    # Creación de BD y tablas
│   ├── 01_seed_data.sql        # Datos iniciales
│   └── 02_views_procs.sql      # Vistas y procedimientos
├── src/main/java/pe/crediactiva/
│   ├── app/                    # Aplicación principal
│   ├── config/                 # Configuraciones
│   ├── security/               # Seguridad y autenticación
│   ├── dao/                    # Acceso a datos
│   ├── model/                  # Modelos/Entidades
│   ├── service/                # Lógica de negocio
│   ├── controller/             # Controladores JavaFX
│   ├── view/                   # Gestión de vistas
│   ├── util/                   # Utilidades
│   └── report/                 # Generación de reportes
├── src/main/resources/
│   ├── application.properties  # Configuración de la app
│   ├── logback.xml            # Configuración de logs
│   └── fxml/                  # Archivos FXML
└── src/test/java/             # Pruebas unitarias
```

## Funcionalidades por Rol

### Administrador
- ✅ CRUD de usuarios y asignación de roles
- ✅ Revisar y gestionar solicitudes de préstamo
- ✅ Aprobar/rechazar solicitudes (genera préstamo y cronograma automático)
- ✅ Visualizar y filtrar préstamos por estado, asesor o cliente
- ✅ Ver deuda actual de cada préstamo
- ✅ Generar reportes de comisiones de asesores

### Asesor
- ✅ Crear solicitudes de préstamo
- ✅ Ver préstamos a su cargo y cronogramas
- ✅ Ver deuda actual de préstamos
- ✅ Generar reporte personal de cobros

### Cliente
- ✅ Acceso a sus propios préstamos y cronogramas
- ✅ Solicitar nuevo préstamo
- ✅ Ver historial de pagos

## Reglas de Negocio

1. **Cronograma de Pagos**: Las cuotas excluyen domingos. Si una fecha cae domingo, se mueve al lunes siguiente.

2. **Estado de Cuotas** (calculado dinámicamente):
   - `PUNTUAL`: hoy ≤ vencimiento y cuota no pagada o pagada puntualmente
   - `ATRASADO`: 1-7 días después del vencimiento sin pago completo
   - `MUY_ATRASADO`: >7 días después del vencimiento sin pago completo

3. **Deuda Actual**: Monto total del préstamo - suma de pagos registrados

4. **Comisión del Asesor**: Suma de montos de cuotas pagadas × porcentaje de comisión del asesor

## Variables de Entorno

Puedes usar variables de entorno para configurar la conexión a la base de datos:

```bash
export DB_URL="jdbc:mysql://localhost:3306/crediactiva?serverTimezone=America/Lima"
export DB_USERNAME="crediactiva_user"
export DB_PASSWORD="crediactiva_pass"
```

## Logs

Los logs se generan en:
- Consola (nivel INFO y superior)
- Archivo `logs/crediactiva.log` (nivel DEBUG y superior)

## Soporte

Para reportar problemas o solicitar nuevas funcionalidades, crear un issue en el repositorio del proyecto.

## Licencia

Propiedad de CrediActiva - Todos los derechos reservados.


