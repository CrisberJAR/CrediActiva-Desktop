package pe.crediactiva.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pe.crediactiva.config.AppConfig;
import pe.crediactiva.config.DatabaseConfig;
import pe.crediactiva.security.SessionManager;

import java.io.IOException;
import java.io.InputStream;

/**
 * Aplicación principal de CrediActiva Desktop.
 * Punto de entrada de la aplicación JavaFX.
 * 
 * @author CrediActiva Development Team
 * @version 1.0
 */
public class CrediActivaApp extends Application {
    
    private static final Logger logger = LoggerFactory.getLogger(CrediActivaApp.class);
    private static Stage primaryStage;
    
    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        
        logger.info("Iniciando CrediActiva Desktop v{}", AppConfig.getVersion());
        
        try {
            // Inicializar configuraciones
            initializeApplication();
            
            // Configurar la ventana principal
            setupPrimaryStage(stage);
            
            // Cargar la pantalla de login
            loadLoginScene();
            
            // Mostrar la aplicación
            stage.show();
            
            logger.info("Aplicación iniciada correctamente");
            
        } catch (Exception e) {
            logger.error("Error al iniciar la aplicación", e);
            showErrorAlert("Error de Inicio", 
                          "No se pudo iniciar la aplicación", 
                          "Error: " + e.getMessage());
            System.exit(1);
        }
    }
    
    /**
     * Inicializa las configuraciones de la aplicación.
     */
    private void initializeApplication() {
        try {
            // Inicializar configuración de base de datos
            DatabaseConfig.initialize();
            logger.info("Base de datos inicializada correctamente");
            
            // Inicializar gestor de sesiones
            SessionManager.getInstance().initialize();
            logger.info("Gestor de sesiones inicializado");
            
        } catch (Exception e) {
            logger.error("Error al inicializar la aplicación", e);
            throw new RuntimeException("Error de inicialización", e);
        }
    }
    
    /**
     * Configura la ventana principal de la aplicación.
     * 
     * @param stage el stage principal
     */
    private void setupPrimaryStage(Stage stage) {
        stage.setTitle(AppConfig.getAppName() + " v" + AppConfig.getVersion());
        stage.setMinWidth(800);
        stage.setMinHeight(600);
        stage.setWidth(1200);
        stage.setHeight(800);
        stage.setResizable(true);
        
        // Configurar icono de la aplicación
        try (InputStream iconStream = getClass().getResourceAsStream("/icons/app-icon.png")) {
            if (iconStream != null) {
                stage.getIcons().add(new Image(iconStream));
            }
        } catch (IOException e) {
            logger.warn("No se pudo cargar el icono de la aplicación", e);
        }
        
        // Configurar el cierre de la aplicación
        stage.setOnCloseRequest(event -> {
            logger.info("Cerrando aplicación...");
            shutdown();
        });
    }
    
    /**
     * Carga la escena de login.
     */
    private void loadLoginScene() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            
            // Aplicar CSS si está disponible
            try (InputStream cssStream = getClass().getResourceAsStream("/css/styles.css")) {
                if (cssStream != null) {
                    scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
                }
            } catch (IOException e) {
                logger.warn("No se pudo cargar el archivo CSS", e);
            }
            
            primaryStage.setScene(scene);
            
        } catch (IOException e) {
            logger.error("Error al cargar la pantalla de login", e);
            throw new RuntimeException("No se pudo cargar la pantalla de login", e);
        }
    }
    
    /**
     * Cambia la escena actual.
     * 
     * @param fxmlPath ruta del archivo FXML
     * @param title título de la ventana
     */
    public static void changeScene(String fxmlPath, String title) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(CrediActivaApp.class.getResource(fxmlPath));
            Scene scene = new Scene(fxmlLoader.load());
            
            // Aplicar CSS
            try (InputStream cssStream = CrediActivaApp.class.getResourceAsStream("/css/styles.css")) {
                if (cssStream != null) {
                    scene.getStylesheets().add(CrediActivaApp.class.getResource("/css/styles.css").toExternalForm());
                }
            } catch (IOException e) {
                logger.warn("No se pudo cargar el archivo CSS para la nueva escena", e);
            }
            
            primaryStage.setScene(scene);
            if (title != null && !title.isEmpty()) {
                primaryStage.setTitle(AppConfig.getAppName() + " - " + title);
            }
            
            logger.debug("Escena cambiada a: {}", fxmlPath);
            
        } catch (IOException e) {
            logger.error("Error al cambiar la escena a: {}", fxmlPath, e);
            showErrorAlert("Error de Navegación", 
                          "No se pudo cargar la pantalla solicitada", 
                          "Error: " + e.getMessage());
        }
    }
    
    /**
     * Obtiene el stage principal de la aplicación.
     * 
     * @return el stage principal
     */
    public static Stage getPrimaryStage() {
        return primaryStage;
    }
    
    /**
     * Muestra un diálogo de error.
     * 
     * @param title título del diálogo
     * @param header encabezado del mensaje
     * @param content contenido del mensaje
     */
    public static void showErrorAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    /**
     * Muestra un diálogo de información.
     * 
     * @param title título del diálogo
     * @param header encabezado del mensaje
     * @param content contenido del mensaje
     */
    public static void showInfoAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    /**
     * Muestra un diálogo de confirmación.
     * 
     * @param title título del diálogo
     * @param header encabezado del mensaje
     * @param content contenido del mensaje
     * @return true si el usuario confirma
     */
    public static boolean showConfirmAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        return alert.showAndWait().orElse(null) == javafx.scene.control.ButtonType.OK;
    }
    
    /**
     * Cierra la aplicación de manera segura.
     */
    private void shutdown() {
        try {
            // Cerrar sesión si existe
            SessionManager.getInstance().logout();
            
            // Cerrar pool de conexiones
            DatabaseConfig.shutdown();
            
            logger.info("Aplicación cerrada correctamente");
            
        } catch (Exception e) {
            logger.error("Error durante el cierre de la aplicación", e);
        } finally {
            System.exit(0);
        }
    }
    
    @Override
    public void stop() throws Exception {
        shutdown();
        super.stop();
    }
    
    /**
     * Punto de entrada principal de la aplicación.
     * 
     * @param args argumentos de línea de comandos
     */
    public static void main(String[] args) {
        // Configurar propiedades del sistema para JavaFX
        System.setProperty("javafx.preloader", "false");
        System.setProperty("prism.lcdtext", "false");
        System.setProperty("prism.text", "t2k");
        
        logger.info("Iniciando CrediActiva Desktop...");
        
        try {
            launch(args);
        } catch (Exception e) {
            logger.error("Error crítico al ejecutar la aplicación", e);
            System.exit(1);
        }
    }
}


