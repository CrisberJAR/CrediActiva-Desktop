package pe.crediactiva.controller;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pe.crediactiva.app.CrediActivaApp;
import pe.crediactiva.model.Usuario;
import pe.crediactiva.security.SessionManager;
import pe.crediactiva.service.UsuarioService;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controlador para la pantalla de login de CrediActiva.
 * 
 * @author CrediActiva Development Team
 * @version 1.0
 */
public class LoginController implements Initializable {
    
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
    
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Button cancelButton;
    @FXML private Label errorLabel;
    @FXML private Label statusLabel;
    
    private int loginAttempts = 0;
    private static final int MAX_LOGIN_ATTEMPTS = 3;
    
    private UsuarioService usuarioService;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.debug("Inicializando LoginController");
        
        // Inicializar servicios
        usuarioService = new UsuarioService();
        
        // Configurar eventos
        setupEventHandlers();
        
        // Configurar estado inicial
        setupInitialState();
        
        // Focus inicial en el campo de usuario
        usernameField.requestFocus();
        
        logger.debug("LoginController inicializado correctamente");
    }
    
    /**
     * Configura los manejadores de eventos.
     */
    private void setupEventHandlers() {
        // Enter en el campo de usuario pasa al campo de contraseña
        usernameField.setOnAction(e -> passwordField.requestFocus());
        
        // Enter en el campo de contraseña ejecuta el login
        passwordField.setOnAction(e -> handleLogin());
        
        // Limpiar mensaje de error al escribir
        usernameField.textProperty().addListener((obs, oldText, newText) -> clearErrorMessage());
        passwordField.textProperty().addListener((obs, oldText, newText) -> clearErrorMessage());
    }
    
    /**
     * Configura el estado inicial de la interfaz.
     */
    private void setupInitialState() {
        errorLabel.setVisible(false);
        statusLabel.setText("Estado: Listo para iniciar sesión");
        
        // Configurar tooltips
        usernameField.setTooltip(new Tooltip("Ingrese su nombre de usuario"));
        passwordField.setTooltip(new Tooltip("Ingrese su contraseña"));
        loginButton.setTooltip(new Tooltip("Hacer clic para iniciar sesión"));
    }
    
    /**
     * Maneja el evento de login.
     */
    @FXML
    private void handleLogin() {
        logger.debug("Iniciando proceso de login");
        
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        
        // Validar campos
        if (!validateInput(username, password)) {
            return;
        }
        
        // Verificar intentos de login
        if (loginAttempts >= MAX_LOGIN_ATTEMPTS) {
            showError("Demasiados intentos fallidos. Reinicie la aplicación.");
            return;
        }
        
        // Ejecutar login en background
        performLogin(username, password);
    }
    
    /**
     * Valida la entrada del usuario.
     * 
     * @param username nombre de usuario
     * @param password contraseña
     * @return true si la validación es exitosa
     */
    private boolean validateInput(String username, String password) {
        if (username.isEmpty()) {
            showError("Por favor ingrese su nombre de usuario");
            usernameField.requestFocus();
            return false;
        }
        
        if (password.isEmpty()) {
            showError("Por favor ingrese su contraseña");
            passwordField.requestFocus();
            return false;
        }
        
        if (username.length() < 3) {
            showError("El nombre de usuario debe tener al menos 3 caracteres");
            usernameField.requestFocus();
            return false;
        }
        
        return true;
    }
    
    /**
     * Ejecuta el proceso de autenticación.
     * 
     * @param username nombre de usuario
     * @param password contraseña
     */
    private void performLogin(String username, String password) {
        // Deshabilitar controles durante el login
        setControlsDisabled(true);
        statusLabel.setText("Estado: Autenticando...");
        
        Task<Usuario> loginTask = new Task<Usuario>() {
            @Override
            protected Usuario call() throws Exception {
                // Simular tiempo de procesamiento
                Thread.sleep(500);
                
                // Usar el servicio real de usuario para autenticar
                return usuarioService.autenticar(username, password).orElse(null);
            }
            
            @Override
            protected void succeeded() {
                Usuario usuario = getValue();
                if (usuario != null) {
                    handleLoginSuccess(usuario);
                } else {
                    handleLoginFailure("Usuario o contraseña incorrectos");
                }
            }
            
            @Override
            protected void failed() {
                Throwable exception = getException();
                logger.error("Error durante la autenticación", exception);
                handleLoginFailure("Error de conexión: " + exception.getMessage());
            }
        };
        
        Thread loginThread = new Thread(loginTask);
        loginThread.setDaemon(true);
        loginThread.start();
    }
    
    
    /**
     * Maneja el éxito del login.
     * 
     * @param usuario usuario autenticado
     */
    private void handleLoginSuccess(Usuario usuario) {
        logger.info("Login exitoso para usuario: {}", usuario.getUsername());
        
        // Iniciar sesión
        SessionManager.getInstance().login(usuario);
        
        // Limpiar campos
        passwordField.clear();
        
        // Determinar pantalla de destino según el rol del usuario desde la base de datos
        String dashboardPath = "/fxml/dashboard-admin.fxml";
        String dashboardTitle = "Panel de Administración";
        
        // Verificar roles reales del usuario desde la base de datos
        if (usuario.esAsesor()) {
            dashboardPath = "/fxml/dashboard-asesor.fxml";
            dashboardTitle = "Panel de Asesor";
            logger.info("Redirigiendo a dashboard de ASESOR para usuario: {}", usuario.getUsername());
        } else if (usuario.esCliente()) {
            dashboardPath = "/fxml/dashboard-cliente.fxml";
            dashboardTitle = "Panel de Cliente";
            logger.info("Redirigiendo a dashboard de CLIENTE para usuario: {}", usuario.getUsername());
        } else if (usuario.esAdministrador()) {
            dashboardPath = "/fxml/dashboard-admin.fxml";
            dashboardTitle = "Panel de Administración";
            logger.info("Redirigiendo a dashboard de ADMINISTRADOR para usuario: {}", usuario.getUsername());
        } else {
            // Usuario sin rol específico, por defecto admin
            logger.warn("Usuario {} no tiene rol específico, usando dashboard admin por defecto", usuario.getUsername());
        }
        
        logger.info("Cambiando a escena: {} - {}", dashboardPath, dashboardTitle);
        
        // Cambiar a la pantalla correspondiente
        CrediActivaApp.changeScene(dashboardPath, dashboardTitle);
    }
    
    /**
     * Maneja el fallo del login.
     * 
     * @param message mensaje de error
     */
    private void handleLoginFailure(String message) {
        loginAttempts++;
        logger.warn("Login fallido para usuario: {} (intento {}/{})", 
                   usernameField.getText(), loginAttempts, MAX_LOGIN_ATTEMPTS);
        
        showError(message);
        passwordField.clear();
        passwordField.requestFocus();
        
        setControlsDisabled(false);
        statusLabel.setText("Estado: Error de autenticación");
        
        if (loginAttempts >= MAX_LOGIN_ATTEMPTS) {
            loginButton.setDisable(true);
            showError("Máximo de intentos alcanzado. Reinicie la aplicación.");
        }
    }
    
    /**
     * Maneja el evento de cancelar.
     */
    @FXML
    private void handleCancel() {
        logger.debug("Cancelando login");
        System.exit(0);
    }
    
    /**
     * Muestra un mensaje de error.
     * 
     * @param message mensaje a mostrar
     */
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
    
    /**
     * Limpia el mensaje de error.
     */
    private void clearErrorMessage() {
        if (errorLabel.isVisible()) {
            errorLabel.setVisible(false);
        }
    }
    
    /**
     * Habilita o deshabilita los controles.
     * 
     * @param disabled true para deshabilitar
     */
    private void setControlsDisabled(boolean disabled) {
        usernameField.setDisable(disabled);
        passwordField.setDisable(disabled);
        loginButton.setDisable(disabled);
        cancelButton.setDisable(disabled);
    }
}


