package pe.crediactiva.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Configuración general de la aplicación CrediActiva.
 * Gestiona las propiedades de configuración desde application.properties.
 * 
 * @author CrediActiva Development Team
 * @version 1.0
 */
public class AppConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(AppConfig.class);
    private static final Properties properties = new Properties();
    private static boolean initialized = false;
    
    // Valores por defecto
    private static final String DEFAULT_APP_NAME = "CrediActiva Desktop";
    private static final String DEFAULT_VERSION = "1.0.0";
    private static final String DEFAULT_TIMEZONE = "America/Lima";
    private static final String DEFAULT_CURRENCY = "PEN";
    private static final String DEFAULT_CURRENCY_SYMBOL = "S/";
    private static final String DEFAULT_LOCALE = "es_PE";
    
    static {
        initialize();
    }
    
    /**
     * Inicializa la configuración cargando el archivo de propiedades.
     */
    public static synchronized void initialize() {
        if (initialized) {
            return;
        }
        
        try (InputStream input = AppConfig.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            
            if (input != null) {
                properties.load(input);
                logger.info("Configuración cargada desde application.properties");
            } else {
                logger.warn("Archivo application.properties no encontrado, usando valores por defecto");
            }
            
            // Cargar variables de entorno que sobrescriben las propiedades
            loadEnvironmentVariables();
            
            initialized = true;
            logger.info("Configuración de la aplicación inicializada");
            
        } catch (IOException e) {
            logger.error("Error al cargar la configuración", e);
            throw new RuntimeException("No se pudo cargar la configuración de la aplicación", e);
        }
    }
    
    /**
     * Carga variables de entorno que pueden sobrescribir las propiedades.
     */
    private static void loadEnvironmentVariables() {
        // Variables de entorno para la base de datos
        String dbUrl = System.getenv("DB_URL");
        if (dbUrl != null && !dbUrl.isEmpty()) {
            properties.setProperty("db.url", dbUrl);
        }
        
        String dbUsername = System.getenv("DB_USERNAME");
        if (dbUsername != null && !dbUsername.isEmpty()) {
            properties.setProperty("db.username", dbUsername);
        }
        
        String dbPassword = System.getenv("DB_PASSWORD");
        if (dbPassword != null && !dbPassword.isEmpty()) {
            properties.setProperty("db.password", dbPassword);
        }
        
        logger.debug("Variables de entorno cargadas");
    }
    
    /**
     * Obtiene una propiedad como String.
     * 
     * @param key clave de la propiedad
     * @param defaultValue valor por defecto
     * @return valor de la propiedad
     */
    public static String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
    
    /**
     * Obtiene una propiedad como String.
     * 
     * @param key clave de la propiedad
     * @return valor de la propiedad o null si no existe
     */
    public static String getProperty(String key) {
        return properties.getProperty(key);
    }
    
    /**
     * Obtiene una propiedad como entero.
     * 
     * @param key clave de la propiedad
     * @param defaultValue valor por defecto
     * @return valor de la propiedad como entero
     */
    public static int getIntProperty(String key, int defaultValue) {
        try {
            String value = properties.getProperty(key);
            return value != null ? Integer.parseInt(value) : defaultValue;
        } catch (NumberFormatException e) {
            logger.warn("Error al parsear propiedad entero '{}', usando valor por defecto: {}", key, defaultValue);
            return defaultValue;
        }
    }
    
    /**
     * Obtiene una propiedad como boolean.
     * 
     * @param key clave de la propiedad
     * @param defaultValue valor por defecto
     * @return valor de la propiedad como boolean
     */
    public static boolean getBooleanProperty(String key, boolean defaultValue) {
        String value = properties.getProperty(key);
        return value != null ? Boolean.parseBoolean(value) : defaultValue;
    }
    
    // Métodos específicos para propiedades de la aplicación
    
    public static String getAppName() {
        return getProperty("app.name", DEFAULT_APP_NAME);
    }
    
    public static String getVersion() {
        return getProperty("app.version", DEFAULT_VERSION);
    }
    
    public static String getTimezone() {
        return getProperty("app.timezone", DEFAULT_TIMEZONE);
    }
    
    public static String getCurrency() {
        return getProperty("app.currency", DEFAULT_CURRENCY);
    }
    
    public static String getCurrencySymbol() {
        return getProperty("app.currency.symbol", DEFAULT_CURRENCY_SYMBOL);
    }
    
    public static String getLocale() {
        return getProperty("app.locale", DEFAULT_LOCALE);
    }
    
    // Propiedades de seguridad
    
    public static int getPasswordMinLength() {
        return getIntProperty("security.password.min.length", 8);
    }
    
    public static int getSessionTimeout() {
        return getIntProperty("security.session.timeout", 3600);
    }
    
    public static int getMaxLoginAttempts() {
        return getIntProperty("security.max.login.attempts", 3);
    }
    
    public static int getLockoutDuration() {
        return getIntProperty("security.lockout.duration", 300);
    }
    
    // Propiedades de JavaFX
    
    public static boolean isJavaFXPreloaderEnabled() {
        return getBooleanProperty("javafx.preloader.enabled", false);
    }
    
    public static int getJavaFXSceneWidth() {
        return getIntProperty("javafx.scene.width", 1200);
    }
    
    public static int getJavaFXSceneHeight() {
        return getIntProperty("javafx.scene.height", 800);
    }
    
    public static boolean isJavaFXSceneResizable() {
        return getBooleanProperty("javafx.scene.resizable", true);
    }
    
    // Propiedades de reportes
    
    public static String getReportOutputDirectory() {
        return getProperty("report.output.directory", "reports");
    }
    
    public static String getReportDateFormat() {
        return getProperty("report.date.format", "dd/MM/yyyy");
    }
    
    public static String getReportCurrencyFormat() {
        return getProperty("report.currency.format", "#,##0.00");
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
     * Obtiene todas las propiedades (para debugging).
     * 
     * @return copia de las propiedades
     */
    public static Properties getAllProperties() {
        Properties copy = new Properties();
        copy.putAll(properties);
        // Ocultar contraseñas
        if (copy.containsKey("db.password")) {
            copy.setProperty("db.password", "***");
        }
        return copy;
    }
    
    /**
     * Recarga la configuración desde el archivo.
     */
    public static synchronized void reload() {
        initialized = false;
        properties.clear();
        initialize();
    }
}


