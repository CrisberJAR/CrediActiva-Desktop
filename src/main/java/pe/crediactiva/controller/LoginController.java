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
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.debug("Inicializando LoginController");
        
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
                // Simular autenticación (aquí iría la lógica real con DAO)
                Thread.sleep(1000); // Simular tiempo de procesamiento
                
                // Usuarios de prueba hardcodeados
                return authenticateUser(username, password);
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
     * Autentica un usuario (implementación temporal).
     * 
     * @param username nombre de usuario
     * @param password contraseña
     * @return usuario autenticado o null si falla
     */
    private Usuario authenticateUser(String username, String password) {
        // Usuarios de prueba hardcodeados
        // En producción esto se haría con DAO y BCrypt
        
        if ("admin".equals(username) && "admin123".equals(password)) {
            Usuario admin = new Usuario();
            admin.setId(1);
            admin.setUsername("admin");
            admin.setNombres("Administrador");
            admin.setApellidos("Sistema");
            admin.setEmail("admin@crediactiva.pe");
            return admin;
        }
        
        if ("asesor1".equals(username) && "asesor123".equals(password)) {
            Usuario asesor = new Usuario();
            asesor.setId(2);
            asesor.setUsername("asesor1");
            asesor.setNombres("Carlos");
            asesor.setApellidos("Mendoza");
            asesor.setEmail("asesor1@crediactiva.pe");
            return asesor;
        }
        
        if ("cliente1".equals(username) && "cliente123".equals(password)) {
            Usuario cliente = new Usuario();
            cliente.setId(3);
            cliente.setUsername("cliente1");
            cliente.setNombres("María");
            cliente.setApellidos("García");
            cliente.setEmail("cliente1@email.com");
            return cliente;
        }
        
        return null; // Usuario no encontrado
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
        
        // Determinar pantalla de destino según el rol del usuario
        String dashboardPath = "/fxml/dashboard-admin.fxml";
        String dashboardTitle = "Panel de Administración";
        
        // En una implementación real, verificaríamos los roles desde la base de datos
        if ("asesor1".equals(usuario.getUsername())) {
            dashboardPath = "/fxml/dashboard-asesor.fxml";
            dashboardTitle = "Panel de Asesor";
        } else if ("cliente1".equals(usuario.getUsername())) {
            dashboardPath = "/fxml/dashboard-cliente.fxml";
            dashboardTitle = "Panel de Cliente";
        }
        
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


