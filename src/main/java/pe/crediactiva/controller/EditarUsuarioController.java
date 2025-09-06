package pe.crediactiva.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import java.util.stream.Collectors;

/**
 * Controlador para el formulario de edición de usuarios existentes.
 * Maneja la interfaz y lógica de negocio para modificar usuarios.
 * 
 * @author CrediActiva Development Team
 * @version 1.0
 */
public class EditarUsuarioController implements Initializable {
    
    private static final Logger logger = LoggerFactory.getLogger(EditarUsuarioController.class);
    
    // Elementos de la interfaz - Información básica
    @FXML private Label subtituloLabel;
    @FXML private Label idLabel;
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    
    // Elementos de la interfaz - Información personal
    @FXML private TextField nombresField;
    @FXML private TextField apellidosField;
    @FXML private TextField documentoField;
    @FXML private TextField telefonoField;
    @FXML private TextArea direccionField;
    
    // Elementos de la interfaz - Roles
    @FXML private ListView<Rol> rolesActualesListView;
    @FXML private ComboBox<Rol> agregarRolComboBox;
    @FXML private Button agregarRolButton;
    @FXML private Button removerRolButton;
    
    // Elementos de la interfaz - Estado y auditoría
    @FXML private CheckBox activoCheckBox;
    @FXML private Label fechaCreacionLabel;
    @FXML private Label fechaActualizacionLabel;
    @FXML private Label ultimoLoginLabel;
    
    // Elementos de la interfaz - Cambio de contraseña
    @FXML private PasswordField nuevaPasswordField;
    @FXML private PasswordField confirmarPasswordField;
    
    // Elementos de la interfaz - Botones y labels
    @FXML private Button restaurarButton;
    @FXML private Button cancelarButton;
    @FXML private Button guardarButton;
    @FXML private Label statusLabel;
    @FXML private Label timeLabel;
    
    // Servicios
    private UsuarioService usuarioService;
    private RolService rolService;
    
    // Datos
    private Usuario usuarioOriginal;
    private Usuario usuarioEditado;
    private ObservableList<Rol> rolesDisponibles;
    private ObservableList<Rol> rolesUsuario;
    
    // Timer para el reloj
    private Timer clockTimer;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.debug("Inicializando EditarUsuarioController");
        
        try {
            // Inicializar servicios
            usuarioService = new UsuarioService();
            rolService = new RolService();
            
            // Configurar interfaz
            configurarFormulario();
            configurarRoles();
            configurarValidaciones();
            iniciarReloj();
            
            logger.debug("EditarUsuarioController inicializado correctamente");
            
        } catch (Exception e) {
            logger.error("Error al inicializar EditarUsuarioController", e);
            CrediActivaApp.showErrorAlert("Error de Inicialización", 
                                        "Error al cargar el formulario de edición", 
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
        nombresField.setTooltip(new Tooltip("Nombres completos del usuario"));
        apellidosField.setTooltip(new Tooltip("Apellidos completos del usuario"));
        documentoField.setTooltip(new Tooltip("DNI, CE u otro documento de identidad"));
        telefonoField.setTooltip(new Tooltip("Número de teléfono de contacto"));
        direccionField.setTooltip(new Tooltip("Dirección completa del usuario"));
        nuevaPasswordField.setTooltip(new Tooltip("Nueva contraseña (dejar vacío para no cambiar)"));
        confirmarPasswordField.setTooltip(new Tooltip("Confirmar nueva contraseña"));
        
        // Configurar límites de caracteres
        configurarLimitesCaracteres();
    }
    
    /**
     * Configura los límites de caracteres en los campos.
     */
    private void configurarLimitesCaracteres() {
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
    }
    
    /**
     * Configura la gestión de roles.
     */
    private void configurarRoles() {
        // Configurar ListView de roles actuales
        rolesUsuario = FXCollections.observableArrayList();
        rolesActualesListView.setItems(rolesUsuario);
        
        rolesActualesListView.setCellFactory(listView -> new ListCell<Rol>() {
            @Override
            protected void updateItem(Rol rol, boolean empty) {
                super.updateItem(rol, empty);
                if (empty || rol == null) {
                    setText(null);
                } else {
                    setText(rol.getNombreLegible() + " - " + rol.getDescripcion());
                }
            }
        });
        
        // Configurar ComboBox de roles disponibles
        agregarRolComboBox.setConverter(new StringConverter<Rol>() {
            @Override
            public String toString(Rol rol) {
                return rol != null ? rol.getNombreLegible() : "";
            }
            
            @Override
            public Rol fromString(String string) {
                return null;
            }
        });
        
        // Listener para habilitar/deshabilitar botón de remover
        rolesActualesListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            removerRolButton.setDisable(newVal == null);
        });
        
        // Listener para habilitar/deshabilitar botón de agregar
        agregarRolComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            agregarRolButton.setDisable(newVal == null || rolesUsuario.contains(newVal));
        });
        
        // Cargar roles disponibles
        cargarRolesDisponibles();
    }
    
    /**
     * Carga los roles disponibles en el sistema.
     */
    private void cargarRolesDisponibles() {
        try {
            List<Rol> roles = rolService.obtenerRolesActivos();
            rolesDisponibles = FXCollections.observableArrayList(roles);
            agregarRolComboBox.setItems(rolesDisponibles);
            
            logger.debug("Cargados {} roles disponibles", roles.size());
            
        } catch (Exception e) {
            logger.error("Error al cargar roles disponibles", e);
            CrediActivaApp.showErrorAlert("Error", "Error al cargar roles", 
                                        "No se pudieron cargar los roles disponibles: " + e.getMessage());
        }
    }
    
    /**
     * Configura las validaciones en tiempo real.
     */
    private void configurarValidaciones() {
        // Validaciones básicas
        usernameField.textProperty().addListener((obs, oldVal, newVal) -> validarFormulario());
        emailField.textProperty().addListener((obs, oldVal, newVal) -> validarFormulario());
        nombresField.textProperty().addListener((obs, oldVal, newVal) -> validarFormulario());
        apellidosField.textProperty().addListener((obs, oldVal, newVal) -> validarFormulario());
        
        // Validación de contraseñas
        nuevaPasswordField.textProperty().addListener((obs, oldVal, newVal) -> validarFormulario());
        confirmarPasswordField.textProperty().addListener((obs, oldVal, newVal) -> validarFormulario());
    }
    
    /**
     * Valida el formulario y habilita/deshabilita el botón guardar.
     */
    private void validarFormulario() {
        boolean esValido = true;
        StringBuilder errores = new StringBuilder();
        
        // Validar campos obligatorios
        String username = usernameField.getText();
        if (username == null || username.trim().length() < 3) {
            esValido = false;
            errores.append("Usuario debe tener al menos 3 caracteres. ");
        }
        
        String email = emailField.getText();
        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            esValido = false;
            errores.append("Email no válido. ");
        }
        
        String nombres = nombresField.getText();
        if (nombres == null || nombres.trim().isEmpty()) {
            esValido = false;
            errores.append("Nombres son requeridos. ");
        }
        
        String apellidos = apellidosField.getText();
        if (apellidos == null || apellidos.trim().isEmpty()) {
            esValido = false;
            errores.append("Apellidos son requeridos. ");
        }
        
        // Validar contraseñas si se están cambiando
        String nuevaPassword = nuevaPasswordField.getText();
        String confirmarPassword = confirmarPasswordField.getText();
        
        if (nuevaPassword != null && !nuevaPassword.isEmpty()) {
            if (nuevaPassword.length() < 6) {
                esValido = false;
                errores.append("Nueva contraseña debe tener al menos 6 caracteres. ");
            }
            
            if (!nuevaPassword.equals(confirmarPassword)) {
                esValido = false;
                errores.append("Las contraseñas no coinciden. ");
            }
        }
        
        // Actualizar estado del botón y mensaje
        guardarButton.setDisable(!esValido);
        
        if (esValido) {
            statusLabel.setText("Formulario válido - Listo para guardar cambios");
        } else {
            statusLabel.setText("Errores: " + errores.toString());
        }
    }
    
    /**
     * Establece el usuario a editar y carga sus datos en el formulario.
     */
    public void setUsuario(Usuario usuario) {
        if (usuario == null) {
            logger.error("Usuario no puede ser null");
            return;
        }
        
        this.usuarioOriginal = usuario;
        this.usuarioEditado = new Usuario(); // Copia para edición
        
        cargarDatosEnFormulario(usuario);
        
        logger.debug("Usuario cargado para edición: {}", usuario.getUsername());
    }
    
    /**
     * Carga los datos del usuario en el formulario.
     */
    private void cargarDatosEnFormulario(Usuario usuario) {
        // Información básica
        idLabel.setText(usuario.getId().toString());
        subtituloLabel.setText("Editando usuario: " + usuario.getNombreCompleto());
        
        // Campos editables
        usernameField.setText(usuario.getUsername());
        emailField.setText(usuario.getEmail());
        nombresField.setText(usuario.getNombres());
        apellidosField.setText(usuario.getApellidos());
        documentoField.setText(usuario.getDocumentoIdentidad() != null ? usuario.getDocumentoIdentidad() : "");
        telefonoField.setText(usuario.getTelefono() != null ? usuario.getTelefono() : "");
        direccionField.setText(usuario.getDireccion() != null ? usuario.getDireccion() : "");
        activoCheckBox.setSelected(usuario.isActivo());
        
        // Información de auditoría
        fechaCreacionLabel.setText(usuario.getFechaCreacion() != null ? 
            usuario.getFechaCreacion().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) : 
            "No disponible");
        
        fechaActualizacionLabel.setText(usuario.getFechaActualizacion() != null ? 
            usuario.getFechaActualizacion().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) : 
            "No disponible");
        
        ultimoLoginLabel.setText(usuario.getUltimoLogin() != null ? 
            usuario.getUltimoLogin().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) : 
            "Nunca ha iniciado sesión");
        
        // Cargar roles del usuario
        cargarRolesUsuario(usuario);
    }
    
    /**
     * Carga los roles del usuario en la lista.
     */
    private void cargarRolesUsuario(Usuario usuario) {
        rolesUsuario.clear();
        
        if (usuario.getRoles() != null) {
            List<Rol> rolesActivos = usuario.getRoles().stream()
                .filter(Rol::isActivo)
                .collect(Collectors.toList());
            rolesUsuario.addAll(rolesActivos);
        }
        
        // Actualizar ComboBox para excluir roles ya asignados
        actualizarRolesDisponiblesParaAgregar();
    }
    
    /**
     * Actualiza la lista de roles disponibles para agregar.
     */
    private void actualizarRolesDisponiblesParaAgregar() {
        if (rolesDisponibles == null) return;
        
        List<Rol> disponibles = rolesDisponibles.stream()
            .filter(rol -> !rolesUsuario.contains(rol))
            .collect(Collectors.toList());
        
        agregarRolComboBox.setItems(FXCollections.observableArrayList(disponibles));
        agregarRolComboBox.setValue(null);
        agregarRolButton.setDisable(true);
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
    private void handleAgregarRol() {
        Rol rolSeleccionado = agregarRolComboBox.getValue();
        if (rolSeleccionado == null) return;
        
        if (!rolesUsuario.contains(rolSeleccionado)) {
            rolesUsuario.add(rolSeleccionado);
            actualizarRolesDisponiblesParaAgregar();
            
            logger.debug("Rol agregado: {}", rolSeleccionado.getNombre());
            statusLabel.setText("Rol agregado: " + rolSeleccionado.getNombreLegible());
        }
    }
    
    @FXML
    private void handleRemoverRol() {
        Rol rolSeleccionado = rolesActualesListView.getSelectionModel().getSelectedItem();
        if (rolSeleccionado == null) {
            CrediActivaApp.showWarningAlert("Advertencia", "Sin Selección", 
                                          "Debe seleccionar un rol para remover.");
            return;
        }
        
        boolean confirmar = CrediActivaApp.showConfirmAlert("Remover Rol", 
                                                          "¿Está seguro que desea remover este rol?", 
                                                          "Rol: " + rolSeleccionado.getNombreLegible());
        
        if (confirmar) {
            rolesUsuario.remove(rolSeleccionado);
            actualizarRolesDisponiblesParaAgregar();
            
            logger.debug("Rol removido: {}", rolSeleccionado.getNombre());
            statusLabel.setText("Rol removido: " + rolSeleccionado.getNombreLegible());
        }
    }
    
    @FXML
    private void handleGuardar() {
        logger.debug("Guardando cambios del usuario");
        statusLabel.setText("Guardando cambios...");
        guardarButton.setDisable(true);
        
        try {
            // Crear usuario con los datos actualizados
            usuarioEditado.setId(usuarioOriginal.getId());
            usuarioEditado.setUsername(usernameField.getText().trim());
            usuarioEditado.setEmail(emailField.getText().trim());
            usuarioEditado.setNombres(nombresField.getText().trim());
            usuarioEditado.setApellidos(apellidosField.getText().trim());
            usuarioEditado.setDocumentoIdentidad(documentoField.getText().trim().isEmpty() ? null : documentoField.getText().trim());
            usuarioEditado.setTelefono(telefonoField.getText().trim().isEmpty() ? null : telefonoField.getText().trim());
            usuarioEditado.setDireccion(direccionField.getText().trim().isEmpty() ? null : direccionField.getText().trim());
            usuarioEditado.setActivo(activoCheckBox.isSelected());
            
            // Mantener datos originales que no se editan
            usuarioEditado.setPasswordHash(usuarioOriginal.getPasswordHash());
            usuarioEditado.setFechaCreacion(usuarioOriginal.getFechaCreacion());
            usuarioEditado.setUltimoLogin(usuarioOriginal.getUltimoLogin());
            
            // Actualizar usuario
            Usuario usuarioActualizado = usuarioService.actualizarUsuario(usuarioEditado);
            
            if (usuarioActualizado != null) {
                // Gestionar roles
                gestionarRoles(usuarioActualizado.getId());
                
                // Cambiar contraseña si es necesario
                cambiarPasswordSiEsNecesario(usuarioActualizado.getId());
                
                // Mostrar mensaje de éxito
                CrediActivaApp.showInfoAlert("Éxito", "Usuario Actualizado", 
                                           "El usuario ha sido actualizado exitosamente.\n\n" +
                                           "Usuario: " + usuarioActualizado.getNombreCompleto());
                
                // Cerrar ventana
                cerrarVentana();
                
                logger.info("Usuario actualizado exitosamente: {}", usuarioActualizado.getUsername());
                
            } else {
                throw new RuntimeException("No se pudo actualizar el usuario");
            }
            
        } catch (Exception e) {
            logger.error("Error al actualizar usuario", e);
            CrediActivaApp.showErrorAlert("Error", "Error al Actualizar Usuario", 
                                        "No se pudo actualizar el usuario: " + e.getMessage());
            statusLabel.setText("Error al actualizar usuario");
        } finally {
            guardarButton.setDisable(false);
        }
    }
    
    /**
     * Gestiona los cambios en los roles del usuario.
     */
    private void gestionarRoles(Integer usuarioId) {
        try {
            // Obtener roles actuales del usuario desde la base de datos
            List<Rol> rolesActualesDB = rolService.obtenerRolesDeUsuario(usuarioId);
            
            // Roles que se deben agregar
            List<Rol> rolesParaAgregar = rolesUsuario.stream()
                .filter(rol -> !rolesActualesDB.contains(rol))
                .collect(Collectors.toList());
            
            // Roles que se deben remover
            List<Rol> rolesParaRemover = rolesActualesDB.stream()
                .filter(rol -> !rolesUsuario.contains(rol))
                .collect(Collectors.toList());
            
            // Agregar nuevos roles
            for (Rol rol : rolesParaAgregar) {
                rolService.asignarRolAUsuario(usuarioId, rol.getId());
                logger.debug("Rol asignado: {} -> Usuario {}", rol.getNombre(), usuarioId);
            }
            
            // Remover roles
            for (Rol rol : rolesParaRemover) {
                rolService.removerRolDeUsuario(usuarioId, rol.getId());
                logger.debug("Rol removido: {} -> Usuario {}", rol.getNombre(), usuarioId);
            }
            
        } catch (Exception e) {
            logger.error("Error al gestionar roles del usuario", e);
            CrediActivaApp.showWarningAlert("Advertencia", "Error en Roles", 
                                          "El usuario fue actualizado pero hubo problemas con los roles: " + e.getMessage());
        }
    }
    
    /**
     * Cambia la contraseña del usuario si es necesario.
     */
    private void cambiarPasswordSiEsNecesario(Integer usuarioId) {
        String nuevaPassword = nuevaPasswordField.getText();
        
        if (nuevaPassword != null && !nuevaPassword.isEmpty()) {
            try {
                // Para cambiar contraseña necesitamos la contraseña actual, pero como admin podemos usar un método especial
                // Por ahora, implementaremos un método en el servicio que permita cambio directo
                // TODO: Implementar método de cambio de contraseña por administrador
                logger.info("Cambio de contraseña solicitado para usuario ID: {}", usuarioId);
                
                CrediActivaApp.showInfoAlert("Información", "Cambio de Contraseña", 
                                           "El cambio de contraseña se implementará en una versión futura.\n" +
                                           "Por ahora, el usuario mantiene su contraseña actual.");
                
            } catch (Exception e) {
                logger.error("Error al cambiar contraseña", e);
                CrediActivaApp.showWarningAlert("Advertencia", "Error en Contraseña", 
                                              "El usuario fue actualizado pero no se pudo cambiar la contraseña: " + e.getMessage());
            }
        }
    }
    
    @FXML
    private void handleRestaurar() {
        logger.debug("Restaurando valores originales");
        
        boolean confirmar = CrediActivaApp.showConfirmAlert("Restaurar Valores", 
                                                          "¿Está seguro que desea restaurar los valores originales?", 
                                                          "Se perderán todos los cambios realizados.");
        
        if (confirmar && usuarioOriginal != null) {
            cargarDatosEnFormulario(usuarioOriginal);
            nuevaPasswordField.clear();
            confirmarPasswordField.clear();
            statusLabel.setText("Valores restaurados");
        }
    }
    
    @FXML
    private void handleCancelar() {
        logger.debug("Cancelando edición de usuario");
        
        boolean confirmar = CrediActivaApp.showConfirmAlert("Cancelar", 
                                                          "¿Está seguro que desea cancelar?", 
                                                          "Se perderán todos los cambios realizados.");
        
        if (confirmar) {
            cerrarVentana();
        }
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
        
        logger.debug("Ventana de edición de usuario cerrada");
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
