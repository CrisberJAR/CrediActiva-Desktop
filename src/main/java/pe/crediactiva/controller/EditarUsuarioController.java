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
import pe.crediactiva.model.Asesor;
import pe.crediactiva.model.Cliente;
import pe.crediactiva.model.enums.TipoRol;
import pe.crediactiva.service.RolService;
import pe.crediactiva.service.UsuarioService;
import pe.crediactiva.service.AsesorService;
import pe.crediactiva.service.ClienteService;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
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
    private AsesorService asesorService;
    private ClienteService clienteService;
    
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
        asesorService = new AsesorService();
        clienteService = new ClienteService();
            
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
        
        // LOG DETALLADO PARA DEBUGGING
        logger.info("🔍 Usuario recibido para edición:");
        logger.info("  - ID: {}", usuario.getId());
        logger.info("  - Username: {}", usuario.getUsername());
        logger.info("  - Nombre completo: {}", usuario.getNombreCompleto());
        logger.info("  - Roles actuales: {}", 
                   usuario.getRoles() != null ? 
                   usuario.getRoles().stream().map(Rol::getNombre).collect(java.util.stream.Collectors.toList()) : 
                   "Sin roles");
        
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
        if (rolSeleccionado == null) {
            logger.warn("No hay rol seleccionado para agregar");
            statusLabel.setText("Debe seleccionar un rol");
            return;
        }
        
        if (usuarioOriginal == null || usuarioOriginal.getId() == null) {
            logger.error("No hay usuario cargado o el usuario no tiene ID");
            statusLabel.setText("Error: No hay usuario seleccionado");
            return;
        }
        
        if (rolesUsuario.contains(rolSeleccionado)) {
            logger.warn("El usuario ya tiene el rol: {}", rolSeleccionado.getNombre());
            statusLabel.setText("El usuario ya tiene este rol");
            return;
        }
        
        try {
            Integer usuarioId = usuarioOriginal.getId();
            Integer rolId = rolSeleccionado.getId();
            
            logger.info("🔄 Asignando rol - Usuario ID: {}, Rol ID: {} ({})", 
                       usuarioId, rolId, rolSeleccionado.getNombre());
            
            // EJECUTAR INMEDIATAMENTE EL INSERT EN LA BASE DE DATOS
            rolService.asignarRolAUsuario(usuarioId, rolId);
            logger.info("✅ INSERT ejecutado: usuarios_roles (usuario_id={}, rol_id={})", usuarioId, rolId);
            
            // Crear registro específico si es ASESOR o CLIENTE
            crearRegistroEspecificoParaRol(usuarioId, rolSeleccionado);
            
            // Actualizar la interfaz
            rolesUsuario.add(rolSeleccionado);
            actualizarRolesDisponiblesParaAgregar();
            
            logger.info("✅ Rol {} asignado exitosamente al usuario {} ({})", 
                       rolSeleccionado.getNombre(), usuarioId, usuarioOriginal.getUsername());
            statusLabel.setText("✅ Rol asignado y guardado: " + rolSeleccionado.getNombreLegible());
            
        } catch (Exception e) {
            logger.error("❌ Error al asignar rol inmediatamente", e);
            statusLabel.setText("❌ Error al asignar rol: " + e.getMessage());
            
            // Mostrar alerta de error
            Platform.runLater(() -> {
                CrediActivaApp.showErrorAlert("Error", "Error al Asignar Rol", 
                                            "No se pudo asignar el rol: " + e.getMessage());
            });
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
     * Asigna/remueve roles y crea registros específicos en tablas correspondientes.
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
            
            // Agregar nuevos roles y crear registros específicos
            for (Rol rol : rolesParaAgregar) {
                // Asignar rol en usuarios_roles
                rolService.asignarRolAUsuario(usuarioId, rol.getId());
                logger.debug("Rol asignado: {} -> Usuario {}", rol.getNombre(), usuarioId);
                
                // Crear registro específico según el tipo de rol
                crearRegistroEspecificoParaRol(usuarioId, rol);
            }
            
            // Remover roles y eliminar registros específicos
            for (Rol rol : rolesParaRemover) {
                // Eliminar registro específico antes de remover el rol
                eliminarRegistroEspecificoParaRol(usuarioId, rol);
                
                // Remover rol de usuarios_roles
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
     * Crea el registro específico en la tabla correspondiente según el rol.
     */
    private void crearRegistroEspecificoParaRol(Integer usuarioId, Rol rol) {
        try {
            TipoRol tipoRol = TipoRol.valueOf(rol.getNombre().toUpperCase());
            
            switch (tipoRol) {
                case ASESOR:
                    crearRegistroAsesor(usuarioId);
                    break;
                case CLIENTE:
                    crearRegistroCliente(usuarioId);
                    break;
                default:
                    logger.debug("Rol {} no requiere registro específico", rol.getNombre());
                    break;
            }
        } catch (Exception e) {
            logger.error("Error al crear registro específico para rol {}: {}", rol.getNombre(), e.getMessage());
            throw e;
        }
    }
    
    /**
     * Elimina el registro específico de la tabla correspondiente según el rol.
     */
    private void eliminarRegistroEspecificoParaRol(Integer usuarioId, Rol rol) {
        try {
            TipoRol tipoRol = TipoRol.valueOf(rol.getNombre().toUpperCase());
            
            switch (tipoRol) {
                case ASESOR:
                    eliminarRegistroAsesor(usuarioId);
                    break;
                case CLIENTE:
                    eliminarRegistroCliente(usuarioId);
                    break;
                default:
                    logger.debug("Rol {} no tiene registro específico que eliminar", rol.getNombre());
                    break;
            }
        } catch (Exception e) {
            logger.error("Error al eliminar registro específico para rol {}: {}", rol.getNombre(), e.getMessage());
            // No lanzar excepción aquí para no interrumpir la eliminación del rol
        }
    }
    
    /**
     * Crea un registro en la tabla asesores con valores por defecto.
     */
    private void crearRegistroAsesor(Integer usuarioId) {
        try {
            // Verificar si ya existe un asesor para este usuario
            Optional<Asesor> asesorExistente = asesorService.buscarPorUsuarioId(usuarioId);
            if (asesorExistente.isPresent()) {
                logger.warn("Ya existe un asesor para el usuario ID: {}", usuarioId);
                return;
            }
            
            // Obtener el usuario para crear el asesor
            Usuario usuario = usuarioService.buscarPorId(usuarioId).orElse(null);
            if (usuario == null) {
                throw new RuntimeException("Usuario no encontrado con ID: " + usuarioId);
            }
            
            // Crear nuevo asesor con valores por defecto usando el servicio
            BigDecimal comisionPorDefecto = BigDecimal.valueOf(0.02); // 2%
            BigDecimal metaPorDefecto = BigDecimal.ZERO;
            
            Asesor asesorCreado = asesorService.crearAsesor(usuario, comisionPorDefecto, metaPorDefecto);
            if (asesorCreado != null) {
                logger.info("✅ Registro de asesor creado para usuario ID: {} con código: {}", 
                           usuarioId, asesorCreado.getCodigoAsesor());
            }
            
        } catch (Exception e) {
            logger.error("❌ Error al crear registro de asesor para usuario ID: {}", usuarioId, e);
            throw new RuntimeException("No se pudo crear el registro de asesor: " + e.getMessage());
        }
    }
    
    /**
     * Crea un registro en la tabla clientes con valores por defecto.
     */
    private void crearRegistroCliente(Integer usuarioId) {
        try {
            // Verificar si ya existe un cliente para este usuario
            Optional<Cliente> clienteExistente = clienteService.buscarPorUsuarioId(usuarioId);
            if (clienteExistente.isPresent()) {
                logger.warn("Ya existe un cliente para el usuario ID: {}", usuarioId);
                return;
            }
            
            // Obtener el usuario para crear el cliente
            Usuario usuario = usuarioService.buscarPorId(usuarioId).orElse(null);
            if (usuario == null) {
                throw new RuntimeException("Usuario no encontrado con ID: " + usuarioId);
            }
            
            // Crear nuevo cliente con valores por defecto usando el servicio
            Cliente.TipoCliente tipoDefecto = Cliente.TipoCliente.NUEVO;
            BigDecimal limiteDefecto = BigDecimal.valueOf(15000.00);
            int scoreDefecto = 650;
            
            Cliente clienteCreado = clienteService.crearCliente(usuario, tipoDefecto, limiteDefecto, scoreDefecto);
            if (clienteCreado != null) {
                logger.info("✅ Registro de cliente creado para usuario ID: {} con código: {}", 
                           usuarioId, clienteCreado.getCodigoCliente());
            }
            
        } catch (Exception e) {
            logger.error("❌ Error al crear registro de cliente para usuario ID: {}", usuarioId, e);
            throw new RuntimeException("No se pudo crear el registro de cliente: " + e.getMessage());
        }
    }
    
    /**
     * Elimina el registro de asesor para el usuario.
     */
    private void eliminarRegistroAsesor(Integer usuarioId) {
        try {
            Optional<Asesor> asesorOpt = asesorService.buscarPorUsuarioId(usuarioId);
            if (asesorOpt.isPresent()) {
                boolean desactivado = asesorService.desactivarAsesor(asesorOpt.get().getId());
                if (desactivado) {
                    logger.info("✅ Registro de asesor desactivado para usuario ID: {}", usuarioId);
                }
            }
        } catch (Exception e) {
            logger.error("❌ Error al desactivar registro de asesor para usuario ID: {}", usuarioId, e);
        }
    }
    
    /**
     * Elimina el registro de cliente para el usuario.
     */
    private void eliminarRegistroCliente(Integer usuarioId) {
        try {
            Optional<Cliente> clienteOpt = clienteService.buscarPorUsuarioId(usuarioId);
            if (clienteOpt.isPresent()) {
                boolean desactivado = clienteService.desactivarCliente(clienteOpt.get().getId());
                if (desactivado) {
                    logger.info("✅ Registro de cliente desactivado para usuario ID: {}", usuarioId);
                }
            }
        } catch (Exception e) {
            logger.error("❌ Error al desactivar registro de cliente para usuario ID: {}", usuarioId, e);
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
