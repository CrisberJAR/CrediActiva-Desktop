package pe.crediactiva.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.ZoneId;
import java.util.TimeZone;

/**
 * Configuración de la base de datos para CrediActiva.
 * Gestiona el pool de conexiones HikariCP y la configuración de MySQL.
 * 
 * @author CrediActiva Development Team
 * @version 1.0
 */
public class DatabaseConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);
    private static HikariDataSource dataSource;
    private static boolean initialized = false;
    
    /**
     * Inicializa la configuración de la base de datos.
     */
    public static synchronized void initialize() {
        if (initialized) {
            return;
        }
        
        try {
            // Configurar zona horaria
            setupTimezone();
            
            // Crear configuración de HikariCP
            HikariConfig config = createHikariConfig();
            
            // Crear el DataSource
            dataSource = new HikariDataSource(config);
            
            // Probar la conexión
            testConnection();
            
            initialized = true;
            logger.info("Base de datos inicializada correctamente");
            
        } catch (Exception e) {
            logger.error("Error al inicializar la base de datos", e);
            throw new RuntimeException("No se pudo inicializar la conexión a la base de datos", e);
        }
    }
    
    /**
     * Configura la zona horaria de la aplicación.
     */
    private static void setupTimezone() {
        String timezone = AppConfig.getTimezone();
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.of(timezone)));
        logger.info("Zona horaria configurada: {}", timezone);
    }
    
    /**
     * Crea la configuración de HikariCP.
     * 
     * @return configuración de HikariCP
     */
    private static HikariConfig createHikariConfig() {
        HikariConfig config = new HikariConfig();
        
        // Configuración básica de conexión
        config.setJdbcUrl(AppConfig.getProperty("db.url"));
        config.setUsername(AppConfig.getProperty("db.username"));
        config.setPassword(AppConfig.getProperty("db.password"));
        config.setDriverClassName(AppConfig.getProperty("db.driver", "com.mysql.cj.jdbc.Driver"));
        
        // Configuración del pool
        config.setMaximumPoolSize(AppConfig.getIntProperty("db.pool.maximum-pool-size", 10));
        config.setMinimumIdle(AppConfig.getIntProperty("db.pool.minimum-idle", 2));
        config.setConnectionTimeout(AppConfig.getIntProperty("db.pool.connection-timeout", 20000));
        config.setIdleTimeout(AppConfig.getIntProperty("db.pool.idle-timeout", 300000));
        config.setMaxLifetime(AppConfig.getIntProperty("db.pool.max-lifetime", 1200000));
        config.setLeakDetectionThreshold(AppConfig.getIntProperty("db.pool.leak-detection-threshold", 60000));
        
        // Configuración de pool específica
        config.setPoolName("CrediActivaPool");
        config.setAutoCommit(false); // Para manejo manual de transacciones
        
        // Configuración de validación de conexiones
        config.setConnectionTestQuery("SELECT 1");
        config.setValidationTimeout(3000);
        
        // Propiedades específicas de MySQL
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("cacheServerConfiguration", "true");
        config.addDataSourceProperty("elideSetAutoCommits", "true");
        config.addDataSourceProperty("maintainTimeStats", "false");
        
        // Configuración de zona horaria en MySQL
        config.addDataSourceProperty("serverTimezone", AppConfig.getTimezone());
        
        logger.info("Configuración de HikariCP creada - URL: {}, Usuario: {}", 
                   config.getJdbcUrl(), config.getUsername());
        
        return config;
    }
    
    /**
     * Prueba la conexión a la base de datos.
     * 
     * @throws SQLException si no se puede conectar
     */
    private static void testConnection() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(5)) {
                logger.info("Conexión a la base de datos establecida correctamente");
            } else {
                throw new SQLException("La conexión no es válida");
            }
        }
    }
    
    /**
     * Obtiene una conexión de la base de datos.
     * 
     * @return conexión a la base de datos
     * @throws SQLException si no se puede obtener la conexión
     */
    public static Connection getConnection() throws SQLException {
        if (!initialized) {
            initialize();
        }
        
        Connection connection = dataSource.getConnection();
        logger.debug("Conexión obtenida del pool");
        return connection;
    }
    
    /**
     * Obtiene el DataSource de HikariCP.
     * 
     * @return el DataSource
     */
    public static HikariDataSource getDataSource() {
        if (!initialized) {
            initialize();
        }
        return dataSource;
    }
    
    /**
     * Verifica si la configuración está inicializada.
     * 
     * @return true si está inicializada
     */
    public static boolean isInitialized() {
        return initialized;
    }
    
    /**
     * Obtiene información del estado del pool de conexiones.
     * 
     * @return información del pool
     */
    public static String getPoolStatus() {
        if (!initialized || dataSource == null) {
            return "Pool no inicializado";
        }
        
        return String.format("Pool Status - Total: %d, Activas: %d, Inactivas: %d, Esperando: %d",
                dataSource.getHikariPoolMXBean().getTotalConnections(),
                dataSource.getHikariPoolMXBean().getActiveConnections(),
                dataSource.getHikariPoolMXBean().getIdleConnections(),
                dataSource.getHikariPoolMXBean().getThreadsAwaitingConnection());
    }
    
    /**
     * Ejecuta una operación en una transacción.
     * 
     * @param operation operación a ejecutar
     * @throws SQLException si ocurre un error en la base de datos
     */
    public static void executeInTransaction(DatabaseOperation operation) throws SQLException {
        Connection connection = null;
        try {
            connection = getConnection();
            connection.setAutoCommit(false);
            
            operation.execute(connection);
            
            connection.commit();
            logger.debug("Transacción completada exitosamente");
            
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                    logger.debug("Transacción revertida debido a error");
                } catch (SQLException rollbackEx) {
                    logger.error("Error al revertir la transacción", rollbackEx);
                }
            }
            throw e;
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException e) {
                    logger.error("Error al cerrar la conexión", e);
                }
            }
        }
    }
    
    /**
     * Cierra el pool de conexiones de manera segura.
     */
    public static synchronized void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("Pool de conexiones cerrado");
        }
        initialized = false;
    }
    
    /**
     * Interfaz funcional para operaciones de base de datos.
     */
    @FunctionalInterface
    public interface DatabaseOperation {
        void execute(Connection connection) throws SQLException;
    }
}


