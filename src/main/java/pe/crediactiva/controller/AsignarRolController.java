package pe.crediactiva.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pe.crediactiva.app.CrediActivaApp;
import pe.crediactiva.model.Cliente;
import pe.crediactiva.model.Rol;
import pe.crediactiva.model.Usuario;
import pe.crediactiva.model.enums.TipoRol;
import pe.crediactiva.service.AsesorService;
import pe.crediactiva.service.ClienteService;
import pe.crediactiva.service.RolService;
import pe.crediactiva.service.UsuarioService;

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
 * Controlador para la asignación de roles a usuarios.
 * Permite seleccionar usuarios, asignar roles y completar registros especiales.
 * 
 * @author CrediActiva Development Team
 * @version 1.0
 */
public class AsignarRolController implements Initializable {
    
    private static final Logger logger = LoggerFactory.getLogger(AsignarRolController.class);
    
    // Elementos de la interfaz - Tiempo
    @FXML private Label timeLabel;
    @FXML private Label footerTimeLabel;
    
    // Elementos de la interfaz - Selección de usuario
    @FXML private ComboBox<Usuario> usuarioComboBox;
    @FXML private Button buscarUsuarioButton;
    @FXML private Button refrescarUsuariosButton;
    @FXML private Label usuarioInfoLabel;
    
    // Elementos de la interfaz - Información del usuario
    @FXML private VBox usuarioDetalleVBox;
    @FXML private Label usernameLabel;
    @FXML private Label nombreCompletoLabel;
    @FXML private Label emailLabel;
    @FXML private Label rolesActualesLabel;
    
    // Elementos de la interfaz - Selección de rol
    @FXML private ComboBox<Rol> rolComboBox;
    @FXML private Label rolDescripcionLabel;
    
    // Elementos de la interfaz - Configuración específica
    @FXML private VBox configuracionRolVBox;
    
    // Configuración ASESOR
    @FXML private VBox configuracionAsesorVBox;
    @FXML private TextField codigoAsesorField;
    @FXML private TextField comisionField;
    @FXML private TextField metaMensualField;
    
    // Configuración CLIENTE
    @FXML private VBox configuracionClienteVBox;
    @FXML private TextField codigoClienteField;
    @FXML private ComboBox<Cliente.TipoCliente> tipoClienteComboBox;
    @FXML private TextField limiteCreditoField;
    @FXML private TextField scoreCrediticioField;
    @FXML private TextField ingresosDeclaradosField;
    @FXML private TextField ocupacionField;
    @FXML private TextField empresaField;
    
    // Elementos de la interfaz - Acciones
    @FXML private Button asignarButton;
    @FXML private Button limpiarButton;
    @FXML private Button cancelarButton;
    @FXML private Button repararButton;
    
    // Elementos de la interfaz - Estado
    @FXML private Label statusLabel;
    @FXML private ProgressBar progressBar;
    
    // Servicios
    private UsuarioService usuarioService;
    private RolService rolService;
    private AsesorService asesorService;
    private ClienteService clienteService;
    
    // Timer para el reloj
    private Timer clockTimer;
    
    // Usuario y rol seleccionados
    private Usuario usuarioSeleccionado;
    private Rol rolSeleccionado;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.debug("Inicializando AsignarRolController");
        
        try {
            // Inicializar servicios
            usuarioService = new UsuarioService();
            rolService = new RolService();
            asesorService = new AsesorService();
            clienteService = new ClienteService();
            
            // Configurar interfaz
            configurarComboBoxes();
            configurarValidaciones();
            cargarDatos();
            iniciarReloj();
            
            logger.debug("AsignarRolController inicializado correctamente");
            
        } catch (Exception e) {
            logger.error("Error al inicializar AsignarRolController", e);
            CrediActivaApp.showErrorAlert("Error de Inicialización", 
                                        "Error al cargar el formulario", 
                                        e.getMessage());
        }
    }
    
    /**
     * Configura los ComboBoxes.
     */
    private void configurarComboBoxes() {
        // Configurar ComboBox de usuarios
        usuarioComboBox.setConverter(new StringConverter<Usuario>() {
            @Override
            public String toString(Usuario usuario) {
                if (usuario == null) return "";
                return String.format("%s - %s %s (%s)", 
                                   usuario.getUsername(), 
                                   usuario.getNombres(), 
                                   usuario.getApellidos(),
                                   usuario.getEmail());
            }
            
            @Override
            public Usuario fromString(String string) {
                return null; // No necesario para ComboBox
            }
        });
        
        // Listener para selección de usuario
        usuarioComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            usuarioSeleccionado = newVal;
            actualizarInformacionUsuario();
            validarFormulario();
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
        
        // Listener para selección de rol
        rolComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            rolSeleccionado = newVal;
            actualizarDescripcionRol();
            mostrarConfiguracionRol();
            validarFormulario();
        });
        
        // Configurar ComboBox de tipo de cliente
        tipoClienteComboBox.setItems(FXCollections.observableArrayList(Cliente.TipoCliente.values()));
        tipoClienteComboBox.getSelectionModel().select(Cliente.TipoCliente.NUEVO);
        
        tipoClienteComboBox.setConverter(new StringConverter<Cliente.TipoCliente>() {
            @Override
            public String toString(Cliente.TipoCliente tipo) {
                return tipo != null ? tipo.getNombre() : "";
            }
            
            @Override
            public Cliente.TipoCliente fromString(String string) {
                return null;
            }
        });
    }
    
    /**
     * Configura las validaciones en tiempo real.
     */
    private void configurarValidaciones() {
        // Validación de comisión
        comisionField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*")) {
                comisionField.setText(oldVal);
            }
            validarFormulario();
        });
        
        // Validación de meta mensual
        metaMensualField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*")) {
                metaMensualField.setText(oldVal);
            }
            validarFormulario();
        });
        
        // Validación de límite de crédito
        limiteCreditoField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*")) {
                limiteCreditoField.setText(oldVal);
            }
            validarFormulario();
        });
        
        // Validación de score crediticio
        scoreCrediticioField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                scoreCrediticioField.setText(oldVal);
            } else if (!newVal.isEmpty()) {
                try {
                    int score = Integer.parseInt(newVal);
                    if (score > 1000) {
                        scoreCrediticioField.setText("1000");
                    }
                } catch (NumberFormatException e) {
                    scoreCrediticioField.setText(oldVal);
                }
            }
            validarFormulario();
        });
        
        // Validación de ingresos declarados
        ingresosDeclaradosField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*")) {
                ingresosDeclaradosField.setText(oldVal);
            }
            validarFormulario();
        });
    }
    
    /**
     * Carga los datos iniciales.
     */
    private void cargarDatos() {
        cargarUsuarios();
        cargarRoles();
        generarCodigosAutomaticos();
    }
    
    /**
     * Carga los usuarios disponibles.
     */
    private void cargarUsuarios() {
        try {
            List<Usuario> usuarios = usuarioService.obtenerTodosLosUsuarios();
            usuarioComboBox.setItems(FXCollections.observableArrayList(usuarios));
            
            usuarioInfoLabel.setText(String.format("Usuarios disponibles: %d", usuarios.size()));
            logger.debug("Cargados {} usuarios", usuarios.size());
            
        } catch (Exception e) {
            logger.error("Error al cargar usuarios", e);
            CrediActivaApp.showErrorAlert("Error", "Error al cargar usuarios", 
                                        "No se pudieron cargar los usuarios: " + e.getMessage());
        }
    }
    
    /**
     * Carga los roles disponibles.
     */
    private void cargarRoles() {
        try {
            List<Rol> roles = rolService.obtenerRolesActivos();
            rolComboBox.setItems(FXCollections.observableArrayList(roles));
            
            logger.debug("Cargados {} roles activos", roles.size());
            
        } catch (Exception e) {
            logger.error("Error al cargar roles", e);
            CrediActivaApp.showErrorAlert("Error", "Error al cargar roles", 
                                        "No se pudieron cargar los roles: " + e.getMessage());
        }
    }
    
    /**
     * Genera códigos automáticos para asesor y cliente.
     */
    private void generarCodigosAutomaticos() {
        try {
            String codigoAsesor = asesorService.generarCodigoAsesor();
            codigoAsesorField.setText(codigoAsesor);
            
            String codigoCliente = clienteService.generarCodigoCliente();
            codigoClienteField.setText(codigoCliente);
            
        } catch (Exception e) {
            logger.warn("Error al generar códigos automáticos", e);
        }
    }
    
    /**
     * Actualiza la información del usuario seleccionado.
     */
    private void actualizarInformacionUsuario() {
        if (usuarioSeleccionado == null) {
            usuarioDetalleVBox.setVisible(false);
            usuarioInfoLabel.setText("Seleccione un usuario para ver su información");
            return;
        }
        
        try {
            // Mostrar información básica
            usernameLabel.setText(usuarioSeleccionado.getUsername());
            nombreCompletoLabel.setText(usuarioSeleccionado.getNombreCompleto());
            emailLabel.setText(usuarioSeleccionado.getEmail());
            
            // Obtener roles actuales
            List<Rol> rolesActuales = rolService.obtenerRolesDeUsuario(usuarioSeleccionado.getId());
            String rolesTexto = rolesActuales.stream()
                .map(Rol::getNombreLegible)
                .collect(Collectors.joining(", "));
            
            if (rolesTexto.isEmpty()) {
                rolesTexto = "Sin roles asignados";
            }
            
            rolesActualesLabel.setText(rolesTexto);
            usuarioDetalleVBox.setVisible(true);
            
            usuarioInfoLabel.setText(String.format("Usuario seleccionado: %s", usuarioSeleccionado.getUsername()));
            
        } catch (Exception e) {
            logger.error("Error al actualizar información del usuario", e);
            usuarioInfoLabel.setText("Error al cargar información del usuario");
        }
    }
    
    /**
     * Actualiza la descripción del rol seleccionado.
     */
    private void actualizarDescripcionRol() {
        if (rolSeleccionado == null) {
            rolDescripcionLabel.setText("Seleccione un rol para ver su descripción");
        } else {
            rolDescripcionLabel.setText(rolSeleccionado.getDescripcion());
        }
    }
    
    /**
     * Muestra la configuración específica según el rol seleccionado.
     */
    private void mostrarConfiguracionRol() {
        // Ocultar todas las configuraciones
        configuracionRolVBox.setVisible(false);
        configuracionAsesorVBox.setVisible(false);
        configuracionClienteVBox.setVisible(false);
        
        if (rolSeleccionado == null) {
            return;
        }
        
        try {
            TipoRol tipoRol = TipoRol.valueOf(rolSeleccionado.getNombre());
            
            switch (tipoRol) {
                case ASESOR:
                    configuracionRolVBox.setVisible(true);
                    configuracionAsesorVBox.setVisible(true);
                    break;
                    
                case CLIENTE:
                    configuracionRolVBox.setVisible(true);
                    configuracionClienteVBox.setVisible(true);
                    break;
                    
                default:
                    // Para ADMINISTRADOR u otros roles no hay configuración especial
                    break;
            }
            
        } catch (IllegalArgumentException e) {
            logger.debug("Rol {} no requiere configuración especial", rolSeleccionado.getNombre());
        }
    }
    
    /**
     * Valida el formulario y habilita/deshabilita el botón de asignar.
     */
    private void validarFormulario() {
        boolean esValido = true;
        StringBuilder errores = new StringBuilder();
        
        // Validar selección de usuario
        if (usuarioSeleccionado == null) {
            esValido = false;
            errores.append("Debe seleccionar un usuario. ");
        }
        
        // Validar selección de rol
        if (rolSeleccionado == null) {
            esValido = false;
            errores.append("Debe seleccionar un rol. ");
        }
        
        // Validaciones específicas por rol
        if (rolSeleccionado != null) {
            // El nombre del rol en BD es ASESOR, CLIENTE, ADMINISTRADOR
            // que coincide con los nombres del enum
            String nombreRol = rolSeleccionado.getNombre();
            logger.debug("🔍 Validando rol: '{}'", nombreRol);
            
            try {
                TipoRol tipoRol = TipoRol.valueOf(nombreRol);
                
                switch (tipoRol) {
                    case ASESOR:
                        if (comisionField.getText().trim().isEmpty()) {
                            esValido = false;
                            errores.append("Comisión es requerida. ");
                        }
                        if (metaMensualField.getText().trim().isEmpty()) {
                            esValido = false;
                            errores.append("Meta mensual es requerida. ");
                        }
                        break;
                        
                    case CLIENTE:
                        if (limiteCreditoField.getText().trim().isEmpty()) {
                            esValido = false;
                            errores.append("Límite de crédito es requerido. ");
                        }
                        if (scoreCrediticioField.getText().trim().isEmpty()) {
                            esValido = false;
                            errores.append("Score crediticio es requerido. ");
                        }
                        break;
                }
                
                } catch (IllegalArgumentException e) {
                    // Rol que no requiere validaciones especiales o error en valueOf
                    logger.warn("⚠️ Error al convertir rol '{}' a TipoRol: {}", nombreRol, e.getMessage());
                }
        }
        
        // Actualizar estado del botón y mensaje
        asignarButton.setDisable(!esValido);
        
        if (esValido) {
            statusLabel.setText("Listo para asignar rol");
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
                    footerTimeLabel.setText(timeText);
                });
            }
        }, 0, 1000);
    }
    
    // Manejadores de eventos
    
    @FXML
    private void handleBuscarUsuario() {
        // Implementar búsqueda de usuarios (opcional)
        CrediActivaApp.showInfoAlert("Información", "Búsqueda de Usuarios", 
                                   "La función de búsqueda estará disponible en una próxima versión.");
    }
    
    @FXML
    private void handleRefrescarUsuarios() {
        logger.debug("Refrescando lista de usuarios");
        cargarUsuarios();
        generarCodigosAutomaticos();
        statusLabel.setText("Lista de usuarios actualizada");
    }
    
    @FXML
    private void handleAsignar() {
        logger.debug("Iniciando asignación de rol");
        
        if (usuarioSeleccionado == null || rolSeleccionado == null) {
            CrediActivaApp.showWarningAlert("Advertencia", "Selección Incompleta", 
                                          "Debe seleccionar tanto un usuario como un rol.");
            return;
        }
        
        // Confirmar acción
        boolean confirmar = CrediActivaApp.showConfirmAlert("Confirmar Asignación", 
                                                          "¿Está seguro que desea asignar el rol?", 
                                                          String.format("Se asignará el rol '%s' al usuario '%s' y se crearán los registros necesarios.",
                                                                       rolSeleccionado.getNombreLegible(),
                                                                       usuarioSeleccionado.getUsername()));
        
        if (!confirmar) {
            return;
        }
        
        // Ejecutar asignación en hilo separado
        Task<Boolean> asignacionTask = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                return ejecutarAsignacionRol();
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    progressBar.setVisible(false);
                    asignarButton.setDisable(false);
                    
                    if (getValue()) {
                        statusLabel.setText("✅ Rol asignado exitosamente");
                        CrediActivaApp.showInfoAlert("Éxito", "Rol Asignado", 
                                                   "El rol ha sido asignado exitosamente y se han creado todos los registros necesarios.");
                        limpiarFormulario();
                    } else {
                        statusLabel.setText("❌ Error al asignar rol");
                        CrediActivaApp.showErrorAlert("Error", "Error al Asignar Rol", 
                                                    "No se pudo completar la asignación del rol. Revise los logs para más detalles.");
                    }
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    progressBar.setVisible(false);
                    asignarButton.setDisable(false);
                    statusLabel.setText("❌ Error al asignar rol");
                    
                    Throwable exception = getException();
                    logger.error("Error en tarea de asignación", exception);
                    CrediActivaApp.showErrorAlert("Error", "Error al Asignar Rol", 
                                                "Error: " + exception.getMessage());
                });
            }
        };
        
        // Mostrar progreso
        progressBar.setVisible(true);
        asignarButton.setDisable(true);
        statusLabel.setText("Asignando rol...");
        
        // Ejecutar tarea
        Thread thread = new Thread(asignacionTask);
        thread.setDaemon(true);
        thread.start();
    }
    
    /**
     * Ejecuta la asignación del rol y creación de registros especiales.
     */
    private boolean ejecutarAsignacionRol() {
        try {
            logger.info("🎭 INICIANDO asignación de rol {} a usuario {}", 
                       rolSeleccionado.getNombre(), usuarioSeleccionado.getUsername());
            
            // 1. Asignar rol al usuario
            logger.info("🎯 Asignando rol {} (ID: {}) a usuario {} (ID: {})", 
                       rolSeleccionado.getNombre(), rolSeleccionado.getId(),
                       usuarioSeleccionado.getUsername(), usuarioSeleccionado.getId());
            
            boolean rolAsignado = rolService.asignarRolAUsuario(usuarioSeleccionado.getId(), rolSeleccionado.getId());
            
            if (!rolAsignado) {
                logger.error("❌ No se pudo asignar el rol");
                return false;
            }
            
            logger.info("✅ Rol asignado exitosamente");
            
            // 2. Crear registros especiales según el tipo de rol
            TipoRol tipoRol = TipoRol.valueOf(rolSeleccionado.getNombre());
            
            switch (tipoRol) {
                case ASESOR:
                    return crearRegistroAsesorPersonalizado();
                    
                case CLIENTE:
                    return crearRegistroClientePersonalizado();
                    
                default:
                    // Para otros roles no hay registros especiales
                    logger.info("ℹ️ Rol {} no requiere registros especiales", tipoRol);
                    return true;
            }
            
        } catch (Exception e) {
            logger.error("💥 Error al ejecutar asignación de rol", e);
            return false;
        }
    }
    
    /**
     * Crea registro de asesor con configuración personalizada.
     */
    private boolean crearRegistroAsesorPersonalizado() {
        try {
            logger.info("📋 Creando registro de asesor personalizado");
            
            // Verificar si ya existe
            if (asesorService.esAsesor(usuarioSeleccionado.getId())) {
                logger.info("✅ El usuario ya tiene registro de asesor");
                return true;
            }
            
            // Obtener valores del formulario
            String comisionTexto = comisionField.getText().trim();
            String metaTexto = metaMensualField.getText().trim();
            
            logger.info("📊 Valores del formulario - Comisión: '{}', Meta: '{}'", comisionTexto, metaTexto);
            
            BigDecimal comision = new BigDecimal(comisionTexto).divide(BigDecimal.valueOf(100));
            BigDecimal metaMensual = new BigDecimal(metaTexto);
            
            logger.info("📊 Valores procesados - Comisión: {}, Meta: {}", comision, metaMensual);
            
            // Crear asesor con configuración personalizada
            var asesor = asesorService.crearAsesor(usuarioSeleccionado, comision, metaMensual);
            
            if (asesor != null) {
                logger.info("✅ Registro de asesor creado: {}", asesor.getCodigoAsesor());
                return true;
            }
            
            return false;
            
        } catch (Exception e) {
            logger.error("💥 Error al crear registro de asesor personalizado", e);
            return false;
        }
    }
    
    /**
     * Crea registro de cliente con configuración personalizada.
     */
    private boolean crearRegistroClientePersonalizado() {
        try {
            logger.info("👤 Creando registro de cliente personalizado");
            
            // Verificar si ya existe
            if (clienteService.esCliente(usuarioSeleccionado.getId())) {
                logger.info("✅ El usuario ya tiene registro de cliente");
                return true;
            }
            
            // Obtener valores del formulario
            Cliente.TipoCliente tipoCliente = tipoClienteComboBox.getValue();
            BigDecimal limiteCredito = new BigDecimal(limiteCreditoField.getText().trim());
            Integer scoreCrediticio = Integer.parseInt(scoreCrediticioField.getText().trim());
            
            // Crear cliente básico
            var cliente = clienteService.crearCliente(usuarioSeleccionado, tipoCliente, limiteCredito, scoreCrediticio);
            
            if (cliente != null) {
                // Actualizar campos adicionales si están llenos
                if (!ingresosDeclaradosField.getText().trim().isEmpty()) {
                    cliente.setIngresosDeclarados(new BigDecimal(ingresosDeclaradosField.getText().trim()));
                }
                
                if (!ocupacionField.getText().trim().isEmpty()) {
                    cliente.setOcupacion(ocupacionField.getText().trim());
                }
                
                if (!empresaField.getText().trim().isEmpty()) {
                    cliente.setEmpresa(empresaField.getText().trim());
                }
                
                // Actualizar cliente con campos adicionales
                clienteService.actualizarCliente(cliente);
                
                logger.info("✅ Registro de cliente creado: {}", cliente.getCodigoCliente());
                return true;
            }
            
            return false;
            
        } catch (Exception e) {
            logger.error("💥 Error al crear registro de cliente personalizado", e);
            return false;
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
        }
    }
    
    @FXML
    private void handleCancelar() {
        logger.debug("Cancelando asignación de rol");
        
        boolean confirmar = CrediActivaApp.showConfirmAlert("Cancelar", 
                                                          "¿Está seguro que desea cancelar?", 
                                                          "Se perderán todos los datos ingresados.");
        
        if (confirmar) {
            cerrarVentana();
        }
    }
    
    @FXML
    private void handleReparar() {
        logger.debug("Iniciando reparación de usuarios ASESOR");
        
        boolean confirmar = CrediActivaApp.showConfirmAlert("Reparar Usuarios ASESOR", 
                                                          "¿Está seguro que desea reparar usuarios ASESOR?", 
                                                          "Se crearán registros en la tabla 'asesores' para todos los usuarios que tengan rol ASESOR pero no tengan registro.");
        
        if (!confirmar) {
            return;
        }
        
        // Ejecutar reparación en hilo separado
        Task<Integer> reparacionTask = new Task<Integer>() {
            @Override
            protected Integer call() throws Exception {
                return ejecutarReparacionAsesores();
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    progressBar.setVisible(false);
                    repararButton.setDisable(false);
                    
                    Integer usuariosReparados = getValue();
                    if (usuariosReparados != null && usuariosReparados > 0) {
                        statusLabel.setText(String.format("✅ Reparación completada: %d usuarios reparados", usuariosReparados));
                        CrediActivaApp.showInfoAlert("Reparación Completada", "Usuarios Reparados", 
                                                   String.format("Se repararon %d usuarios ASESOR exitosamente.\n\nAhora todos los usuarios con rol ASESOR tienen su registro en la tabla 'asesores'.", usuariosReparados));
                    } else {
                        statusLabel.setText("ℹ️ No se encontraron usuarios que requieran reparación");
                        CrediActivaApp.showInfoAlert("Reparación Completada", "Sin Problemas", 
                                                   "Todos los usuarios ASESOR ya tienen sus registros correctos en la tabla 'asesores'.");
                    }
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    progressBar.setVisible(false);
                    repararButton.setDisable(false);
                    statusLabel.setText("❌ Error en reparación");
                    
                    Throwable exception = getException();
                    logger.error("Error en tarea de reparación", exception);
                    CrediActivaApp.showErrorAlert("Error", "Error en Reparación", 
                                                "Error: " + exception.getMessage());
                });
            }
        };
        
        // Mostrar progreso
        progressBar.setVisible(true);
        repararButton.setDisable(true);
        statusLabel.setText("Reparando usuarios ASESOR...");
        
        // Ejecutar tarea
        Thread thread = new Thread(reparacionTask);
        thread.setDaemon(true);
        thread.start();
    }
    
    /**
     * Ejecuta la reparación de usuarios ASESOR.
     * 
     * @return número de usuarios reparados
     */
    private Integer ejecutarReparacionAsesores() {
        try {
            logger.info("🔧 INICIANDO reparación de usuarios ASESOR");
            
            // Obtener todos los usuarios con rol ASESOR
            List<Usuario> usuariosAsesor = usuarioService.buscarPorRol("ASESOR");
            logger.info("📊 Usuarios con rol ASESOR encontrados: {}", usuariosAsesor.size());
            
            int usuariosReparados = 0;
            
            for (Usuario usuario : usuariosAsesor) {
                logger.info("🔍 Verificando usuario: {} (ID: {})", usuario.getUsername(), usuario.getId());
                
                // Verificar si ya tiene registro de asesor
                boolean tieneRegistro = asesorService.esAsesor(usuario.getId());
                
                if (!tieneRegistro) {
                    logger.info("🔧 REPARANDO: Usuario {} no tiene registro de asesor", usuario.getUsername());
                    
                    // Crear registro con valores por defecto
                    BigDecimal comisionDefault = new BigDecimal("0.02"); // 2%
                    BigDecimal metaDefault = BigDecimal.ZERO;
                    
                    var asesor = asesorService.crearAsesor(usuario, comisionDefault, metaDefault);
                    
                    if (asesor != null) {
                        usuariosReparados++;
                        logger.info("✅ REPARADO: Usuario {} - Código: {}", 
                                   usuario.getUsername(), asesor.getCodigoAsesor());
                    } else {
                        logger.error("❌ FALLÓ: No se pudo reparar usuario {}", usuario.getUsername());
                    }
                } else {
                    logger.info("✅ OK: Usuario {} ya tiene registro de asesor", usuario.getUsername());
                }
            }
            
            logger.info("🎉 REPARACIÓN COMPLETA: {} usuarios reparados de {} usuarios ASESOR", 
                       usuariosReparados, usuariosAsesor.size());
            
            return usuariosReparados;
            
        } catch (Exception e) {
            logger.error("💥 ERROR en reparación de usuarios ASESOR", e);
            throw new RuntimeException("Error en reparación: " + e.getMessage(), e);
        }
    }
    
    /**
     * Limpia todos los campos del formulario.
     */
    private void limpiarFormulario() {
        usuarioComboBox.getSelectionModel().clearSelection();
        rolComboBox.getSelectionModel().clearSelection();
        
        usuarioSeleccionado = null;
        rolSeleccionado = null;
        
        usuarioDetalleVBox.setVisible(false);
        configuracionRolVBox.setVisible(false);
        
        // Limpiar campos de asesor
        comisionField.setText("2.0");
        metaMensualField.setText("0.00");
        
        // Limpiar campos de cliente
        tipoClienteComboBox.getSelectionModel().select(Cliente.TipoCliente.NUEVO);
        limiteCreditoField.setText("10000.00");
        scoreCrediticioField.setText("600");
        ingresosDeclaradosField.clear();
        ocupacionField.clear();
        empresaField.clear();
        
        // Regenerar códigos
        generarCodigosAutomaticos();
        
        statusLabel.setText("Formulario limpiado - Listo para nueva asignación");
        
        // Enfocar el primer campo
        usuarioComboBox.requestFocus();
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
        
        logger.debug("Ventana de asignación de rol cerrada");
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
