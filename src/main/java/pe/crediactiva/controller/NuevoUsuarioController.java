package pe.crediactiva.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pe.crediactiva.app.CrediActivaApp;
import pe.crediactiva.model.Rol;
import pe.crediactiva.model.Usuario;
import pe.crediactiva.service.RolService;
import pe.crediactiva.service.UsuarioService;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Controlador para el formulario de creación de nuevos usuarios.
 * Maneja la interfaz y lógica de negocio para crear usuarios.
 * 
 * @author CrediActiva Development Team
 * @version 1.0
 */
public class NuevoUsuarioController implements Initializable {
    
    private static final Logger logger = LoggerFactory.getLogger(NuevoUsuarioController.class);
    
    // Elementos de la interfaz - Información de Acceso
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    
    // Elementos de la interfaz - Información Personal
    @FXML private TextField nombresField;
    @FXML private TextField apellidosField;
    @FXML private TextField documentoField;
    @FXML private TextField telefonoField;
    @FXML private TextArea direccionField;
    
    // Elementos de la interfaz - Configuración de Rol
    @FXML private ComboBox<Rol> rolComboBox;
    @FXML private Label rolDescripcionLabel;
    
    // Elementos de la interfaz - Estado
    @FXML private CheckBox activoCheckBox;
    
    // Elementos de la interfaz - Botones y Labels
    @FXML private Button limpiarButton;
    @FXML private Button cancelarButton;
    @FXML private Button crearButton;
    @FXML private Label statusLabel;
    @FXML private Label timeLabel;
    
    // Servicios
    private UsuarioService usuarioService;
    private RolService rolService;
    
    // Timer para el reloj
    private Timer clockTimer;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.debug("Inicializando NuevoUsuarioController");
        
        try {
            // Inicializar servicios
            usuarioService = new UsuarioService();
            rolService = new RolService();
            
            // Configurar interfaz
            configurarFormulario();
            cargarRoles();
            configurarValidaciones();
            iniciarReloj();
            
            logger.debug("NuevoUsuarioController inicializado correctamente");
            
        } catch (Exception e) {
            logger.error("Error al inicializar NuevoUsuarioController", e);
            CrediActivaApp.showErrorAlert("Error de Inicialización", 
                                        "Error al cargar el formulario", 
                                        e.getMessage());
        }
    }
    
    /**
     * Configura los elementos del formulario.
     */
    private void configurarFormulario() {
        // Configurar tooltips
        usernameField.setTooltip(new Tooltip("Nombre de usuario único (mínimo 3 caracteres)"));
        emailField.setTooltip(new Tooltip("Dirección de email válida"));
        passwordField.setTooltip(new Tooltip("Contraseña segura (mínimo 6 caracteres)"));
        confirmPasswordField.setTooltip(new Tooltip("Debe coincidir con la contraseña"));
        nombresField.setTooltip(new Tooltip("Nombres completos del usuario"));
        apellidosField.setTooltip(new Tooltip("Apellidos completos del usuario"));
        documentoField.setTooltip(new Tooltip("DNI, CE u otro documento de identidad"));
        telefonoField.setTooltip(new Tooltip("Número de teléfono de contacto"));
        direccionField.setTooltip(new Tooltip("Dirección completa del usuario"));
        rolComboBox.setTooltip(new Tooltip("Seleccione el rol que tendrá el usuario"));
        
        // Configurar límites de caracteres
        usernameField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() > 50) {
                usernameField.setText(oldVal);
            }
        });
        
        emailField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() > 100) {
                emailField.setText(oldVal);
            }
        });
        
        nombresField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() > 100) {
                nombresField.setText(oldVal);
            }
        });
        
        apellidosField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() > 100) {
                apellidosField.setText(oldVal);
            }
        });
        
        documentoField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() > 20) {
                documentoField.setText(oldVal);
            }
        });
        
        telefonoField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() > 20) {
                telefonoField.setText(oldVal);
            }
        });
        
        direccionField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() > 500) {
                direccionField.setText(oldVal);
            }
        });
        
        // Configurar ComboBox de roles
        rolComboBox.setConverter(new StringConverter<Rol>() {
            @Override
            public String toString(Rol rol) {
                return rol != null ? rol.getNombreLegible() : "";
            }
            
            @Override
            public Rol fromString(String string) {
                return null; // No necesario para ComboBox
            }
        });
        
        // Listener para mostrar descripción del rol
        rolComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                rolDescripcionLabel.setText(newVal.getDescripcion());
            } else {
                rolDescripcionLabel.setText("Seleccione un rol para ver su descripción");
            }
        });
    }
    
    /**
     * Carga los roles disponibles en el ComboBox.
     */
    private void cargarRoles() {
        try {
            List<Rol> rolesActivos = rolService.obtenerRolesActivos();
            rolComboBox.setItems(FXCollections.observableArrayList(rolesActivos));
            
            if (!rolesActivos.isEmpty()) {
                // Seleccionar "CLIENTE" por defecto si existe
                rolesActivos.stream()
                    .filter(rol -> "CLIENTE".equals(rol.getNombre()))
                    .findFirst()
                    .ifPresent(rol -> rolComboBox.getSelectionModel().select(rol));
            }
            
            logger.debug("Cargados {} roles activos", rolesActivos.size());
            
        } catch (Exception e) {
            logger.error("Error al cargar roles", e);
            CrediActivaApp.showErrorAlert("Error", "Error al cargar roles", 
                                        "No se pudieron cargar los roles disponibles: " + e.getMessage());
        }
    }
    
    /**
     * Configura las validaciones en tiempo real.
     */
    private void configurarValidaciones() {
        // Validación de username
        usernameField.textProperty().addListener((obs, oldVal, newVal) -> {
            validarFormulario();
        });
        
        // Validación de email
        emailField.textProperty().addListener((obs, oldVal, newVal) -> {
            validarFormulario();
        });
        
        // Validación de contraseñas
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> {
            validarFormulario();
        });
        
        confirmPasswordField.textProperty().addListener((obs, oldVal, newVal) -> {
            validarFormulario();
        });
        
        // Validación de nombres y apellidos
        nombresField.textProperty().addListener((obs, oldVal, newVal) -> {
            validarFormulario();
        });
        
        apellidosField.textProperty().addListener((obs, oldVal, newVal) -> {
            validarFormulario();
        });
        
        // Validación de rol
        rolComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            validarFormulario();
        });
    }
    
    /**
     * Valida el formulario y habilita/deshabilita el botón crear.
     */
    private void validarFormulario() {
        boolean esValido = true;
        StringBuilder errores = new StringBuilder();
        
        // Validar username
        String username = usernameField.getText();
        if (username == null || username.trim().length() < 3) {
            esValido = false;
            errores.append("Usuario debe tener al menos 3 caracteres. ");
        }
        
        // Validar email
        String email = emailField.getText();
        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            esValido = false;
            errores.append("Email no válido. ");
        }
        
        // Validar contraseña
        String password = passwordField.getText();
        if (password == null || password.length() < 6) {
            esValido = false;
            errores.append("Contraseña debe tener al menos 6 caracteres. ");
        }
        
        // Validar confirmación de contraseña
        String confirmPassword = confirmPasswordField.getText();
        if (!password.equals(confirmPassword)) {
            esValido = false;
            errores.append("Las contraseñas no coinciden. ");
        }
        
        // Validar nombres
        String nombres = nombresField.getText();
        if (nombres == null || nombres.trim().isEmpty()) {
            esValido = false;
            errores.append("Nombres son requeridos. ");
        }
        
        // Validar apellidos
        String apellidos = apellidosField.getText();
        if (apellidos == null || apellidos.trim().isEmpty()) {
            esValido = false;
            errores.append("Apellidos son requeridos. ");
        }
        
        // Validar rol
        if (rolComboBox.getSelectionModel().getSelectedItem() == null) {
            esValido = false;
            errores.append("Debe seleccionar un rol. ");
        }
        
        // Actualizar estado del botón y mensaje
        crearButton.setDisable(!esValido);
        
        if (esValido) {
            statusLabel.setText("Formulario válido - Listo para crear usuario");
        } else {
            statusLabel.setText("Errores: " + errores.toString());
        }
    }
    
    /**
     * Inicia el reloj en tiempo real.
     */
    private void iniciarReloj() {
        clockTimer = new Timer(true);
        clockTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    LocalDateTime now = LocalDateTime.now();
                    String timeText = now.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
                    timeLabel.setText(timeText);
                });
            }
        }, 0, 1000);
    }
    
    // Manejadores de eventos
    
    @FXML
    private void handleCrear() {
        logger.debug("Iniciando creación de usuario");
        statusLabel.setText("Creando usuario...");
        crearButton.setDisable(true);
        
        try {
            // Crear objeto Usuario
            Usuario nuevoUsuario = new Usuario();
            nuevoUsuario.setUsername(usernameField.getText().trim());
            nuevoUsuario.setEmail(emailField.getText().trim());
            nuevoUsuario.setNombres(nombresField.getText().trim());
            nuevoUsuario.setApellidos(apellidosField.getText().trim());
            nuevoUsuario.setDocumentoIdentidad(documentoField.getText().trim().isEmpty() ? null : documentoField.getText().trim());
            nuevoUsuario.setTelefono(telefonoField.getText().trim().isEmpty() ? null : telefonoField.getText().trim());
            nuevoUsuario.setDireccion(direccionField.getText().trim().isEmpty() ? null : direccionField.getText().trim());
            nuevoUsuario.setActivo(activoCheckBox.isSelected());
            
            String password = passwordField.getText();
            
            // Crear usuario
            Usuario usuarioCreado = usuarioService.crearUsuario(nuevoUsuario, password);
            
            if (usuarioCreado != null) {
                // Asignar rol
                Rol rolSeleccionado = rolComboBox.getSelectionModel().getSelectedItem();
                if (rolSeleccionado != null) {
                    boolean rolAsignado = rolService.asignarRolAUsuario(usuarioCreado.getId(), rolSeleccionado.getId());
                    
                    if (!rolAsignado) {
                        logger.warn("Usuario creado pero no se pudo asignar el rol");
                        CrediActivaApp.showWarningAlert("Advertencia", "Usuario Creado", 
                                                      "El usuario fue creado exitosamente pero no se pudo asignar el rol. " +
                                                      "Puede asignarlo manualmente desde la gestión de usuarios.");
                    }
                }
                
                // Mostrar mensaje de éxito
                String mensaje = String.format("Usuario '%s' creado exitosamente.\n\nNombres: %s %s\nEmail: %s\nRol: %s", 
                                              usuarioCreado.getUsername(),
                                              usuarioCreado.getNombres(),
                                              usuarioCreado.getApellidos(),
                                              usuarioCreado.getEmail(),
                                              rolSeleccionado != null ? rolSeleccionado.getNombreLegible() : "Sin rol");
                
                CrediActivaApp.showInfoAlert("Éxito", "Usuario Creado", mensaje);
                
                // Limpiar formulario
                limpiarFormulario();
                statusLabel.setText("Usuario creado exitosamente");
                
                logger.info("Usuario creado exitosamente: {}", usuarioCreado.getUsername());
                
            } else {
                throw new RuntimeException("No se pudo crear el usuario");
            }
            
        } catch (Exception e) {
            logger.error("Error al crear usuario", e);
            CrediActivaApp.showErrorAlert("Error", "Error al Crear Usuario", 
                                        "No se pudo crear el usuario: " + e.getMessage());
            statusLabel.setText("Error al crear usuario");
        } finally {
            crearButton.setDisable(false);
        }
    }
    
    @FXML
    private void handleLimpiar() {
        logger.debug("Limpiando formulario");
        
        boolean confirmar = CrediActivaApp.showConfirmAlert("Limpiar Formulario", 
                                                          "¿Está seguro que desea limpiar el formulario?", 
                                                          "Se perderán todos los datos ingresados.");
        
        if (confirmar) {
            limpiarFormulario();
            statusLabel.setText("Formulario limpiado");
        }
    }
    
    @FXML
    private void handleCancelar() {
        logger.debug("Cancelando creación de usuario");
        
        boolean confirmar = CrediActivaApp.showConfirmAlert("Cancelar", 
                                                          "¿Está seguro que desea cancelar?", 
                                                          "Se perderán todos los datos ingresados.");
        
        if (confirmar) {
            cerrarVentana();
        }
    }
    
    /**
     * Limpia todos los campos del formulario.
     */
    private void limpiarFormulario() {
        usernameField.clear();
        emailField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
        nombresField.clear();
        apellidosField.clear();
        documentoField.clear();
        telefonoField.clear();
        direccionField.clear();
        rolComboBox.getSelectionModel().clearSelection();
        activoCheckBox.setSelected(true);
        rolDescripcionLabel.setText("Seleccione un rol para ver su descripción");
        
        // Enfocar el primer campo
        usernameField.requestFocus();
    }
    
    /**
     * Cierra la ventana actual.
     */
    private void cerrarVentana() {
        if (clockTimer != null) {
            clockTimer.cancel();
        }
        
        Stage stage = (Stage) cancelarButton.getScene().getWindow();
        stage.close();
        
        logger.debug("Ventana de nuevo usuario cerrada");
    }
    
    /**
     * Limpieza al cerrar el controlador.
     */
    public void cleanup() {
        if (clockTimer != null) {
            clockTimer.cancel();
        }
    }
}
