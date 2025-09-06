package pe.crediactiva.controller;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pe.crediactiva.app.CrediActivaApp;
import pe.crediactiva.model.Rol;
import pe.crediactiva.model.Usuario;
import pe.crediactiva.service.RolService;
import pe.crediactiva.service.UsuarioService;
import pe.crediactiva.util.UsuarioReparacionUtil;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

/**
 * Controlador para la gestión completa de usuarios.
 * Maneja la interfaz de administración de usuarios del sistema.
 * 
 * @author CrediActiva Development Team
 * @version 1.0
 */
public class GestionUsuariosController implements Initializable {
    
    private static final Logger logger = LoggerFactory.getLogger(GestionUsuariosController.class);
    
    // Elementos de la interfaz - Labels de estadísticas
    @FXML private Label totalUsuariosLabel;
    @FXML private Label usuariosActivosLabel;
    @FXML private Label usuariosInactivosLabel;
    
    // Elementos de la interfaz - Botones
    @FXML private Button volverButton;
    @FXML private Button nuevoUsuarioButton;
    @FXML private Button editarUsuarioButton;
    @FXML private Button activarUsuarioButton;
    @FXML private Button desactivarUsuarioButton;
    @FXML private Button actualizarButton;
    @FXML private Button repararUsuariosButton;
    @FXML private Button exportarButton;
    @FXML private Button buscarButton;
    @FXML private Button limpiarFiltrosButton;
    @FXML private Button anteriorButton;
    @FXML private Button siguienteButton;
    
    // Elementos de la interfaz - Campos de búsqueda y filtros
    @FXML private TextField buscarField;
    @FXML private ComboBox<Rol> filtroRolComboBox;
    @FXML private ComboBox<String> filtroEstadoComboBox;
    
    // Elementos de la interfaz - Tabla de usuarios
    @FXML private TableView<Usuario> usuariosTable;
    @FXML private TableColumn<Usuario, Integer> idColumn;
    @FXML private TableColumn<Usuario, String> usernameColumn;
    @FXML private TableColumn<Usuario, String> nombresColumn;
    @FXML private TableColumn<Usuario, String> apellidosColumn;
    @FXML private TableColumn<Usuario, String> emailColumn;
    @FXML private TableColumn<Usuario, String> documentoColumn;
    @FXML private TableColumn<Usuario, String> telefonoColumn;
    @FXML private TableColumn<Usuario, String> rolesColumn;
    @FXML private TableColumn<Usuario, String> estadoColumn;
    @FXML private TableColumn<Usuario, String> ultimoLoginColumn;
    @FXML private TableColumn<Usuario, String> fechaCreacionColumn;
    
    // Elementos de la interfaz - Panel de detalles
    @FXML private VBox detallesPanel;
    @FXML private Label detalleIdLabel;
    @FXML private Label detalleUsernameLabel;
    @FXML private Label detalleNombreCompletoLabel;
    @FXML private Label detalleEmailLabel;
    @FXML private Label detalleDocumentoLabel;
    @FXML private Label detalleTelefonoLabel;
    @FXML private Label detalleDireccionLabel;
    @FXML private Label detalleRolesLabel;
    @FXML private Label detalleEstadoLabel;
    @FXML private Label detalleUltimoLoginLabel;
    @FXML private Label detalleFechaCreacionLabel;
    @FXML private Label detalleFechaActualizacionLabel;
    
    // Elementos de la interfaz - Paginación y estado
    @FXML private Label paginacionLabel;
    @FXML private Label paginaLabel;
    @FXML private Label statusLabel;
    @FXML private Label timeLabel;
    
    // Servicios
    private UsuarioService usuarioService;
    private RolService rolService;
    
    // Datos
    private ObservableList<Usuario> todosLosUsuarios;
    private FilteredList<Usuario> usuariosFiltrados;
    
    // Paginación
    private int paginaActual = 1;
    private int usuariosPorPagina = 50;
    private int totalPaginas = 1;
    
    // Timer para el reloj
    private Timer clockTimer;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.debug("Inicializando GestionUsuariosController");
        
        try {
            // Inicializar servicios
            usuarioService = new UsuarioService();
            rolService = new RolService();
            
            // Configurar interfaz
            configurarTabla();
            configurarFiltros();
            configurarEventos();
            
            // Cargar datos
            cargarDatos();
            
            // Iniciar reloj
            iniciarReloj();
            
            logger.debug("GestionUsuariosController inicializado correctamente");
            
        } catch (Exception e) {
            logger.error("Error al inicializar GestionUsuariosController", e);
            CrediActivaApp.showErrorAlert("Error de Inicialización", 
                                        "Error al cargar la gestión de usuarios", 
                                        e.getMessage());
        }
    }
    
    /**
     * Configura la tabla de usuarios.
     */
    private void configurarTabla() {
        // Configurar columnas
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        nombresColumn.setCellValueFactory(new PropertyValueFactory<>("nombres"));
        apellidosColumn.setCellValueFactory(new PropertyValueFactory<>("apellidos"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        documentoColumn.setCellValueFactory(new PropertyValueFactory<>("documentoIdentidad"));
        telefonoColumn.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        
        // Columna de roles (personalizada)
        rolesColumn.setCellValueFactory(cellData -> {
            Usuario usuario = cellData.getValue();
            String roles = usuario.getRoles() != null ? 
                usuario.getRoles().stream()
                    .filter(Rol::isActivo)
                    .map(Rol::getNombreLegible)
                    .collect(Collectors.joining(", ")) : 
                "Sin roles";
            return new ReadOnlyStringWrapper(roles);
        });
        
        // Columna de estado (personalizada)
        estadoColumn.setCellValueFactory(cellData -> {
            Usuario usuario = cellData.getValue();
            return new ReadOnlyStringWrapper(usuario.isActivo() ? "Activo" : "Inactivo");
        });
        
        // Columna de último login (personalizada)
        ultimoLoginColumn.setCellValueFactory(cellData -> {
            Usuario usuario = cellData.getValue();
            String ultimoLogin = usuario.getUltimoLogin() != null ? 
                usuario.getUltimoLogin().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : 
                "Nunca";
            return new ReadOnlyStringWrapper(ultimoLogin);
        });
        
        // Columna de fecha de creación (personalizada)
        fechaCreacionColumn.setCellValueFactory(cellData -> {
            Usuario usuario = cellData.getValue();
            String fechaCreacion = usuario.getFechaCreacion() != null ? 
                usuario.getFechaCreacion().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : 
                "-";
            return new ReadOnlyStringWrapper(fechaCreacion);
        });
        
        // Configurar selección
        usuariosTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        usuariosTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            actualizarBotonesSegunSeleccion(newSelection);
            mostrarDetallesUsuario(newSelection);
        });
        
        // Configurar doble clic para editar
        usuariosTable.setRowFactory(tv -> {
            TableRow<Usuario> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    handleEditarUsuario();
                }
            });
            return row;
        });
    }
    
    /**
     * Configura los filtros y ComboBox.
     */
    private void configurarFiltros() {
        // Configurar ComboBox de roles
        filtroRolComboBox.setConverter(new StringConverter<Rol>() {
            @Override
            public String toString(Rol rol) {
                return rol != null ? rol.getNombreLegible() : "Todos los roles";
            }
            
            @Override
            public Rol fromString(String string) {
                return null;
            }
        });
        
        // Configurar ComboBox de estado
        filtroEstadoComboBox.setItems(FXCollections.observableArrayList("Todos", "Activos", "Inactivos"));
        filtroEstadoComboBox.setValue("Todos");
        
        // Cargar roles en el filtro
        try {
            List<Rol> roles = rolService.obtenerRolesActivos();
            ObservableList<Rol> rolesConTodos = FXCollections.observableArrayList();
            rolesConTodos.add(null); // Para "Todos los roles"
            rolesConTodos.addAll(roles);
            filtroRolComboBox.setItems(rolesConTodos);
            filtroRolComboBox.setValue(null);
        } catch (Exception e) {
            logger.error("Error al cargar roles para filtro", e);
        }
    }
    
    /**
     * Configura los eventos de los filtros.
     */
    private void configurarEventos() {
        // Listener para búsqueda en tiempo real
        buscarField.textProperty().addListener((obs, oldVal, newVal) -> {
            aplicarFiltros();
        });
        
        // Listeners para filtros
        filtroRolComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            aplicarFiltros();
        });
        
        filtroEstadoComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            aplicarFiltros();
        });
    }
    
    /**
     * Carga los datos iniciales.
     */
    private void cargarDatos() {
        statusLabel.setText("Estado: Cargando usuarios...");
        
        try {
            // Cargar todos los usuarios
            List<Usuario> usuarios = usuarioService.obtenerUsuariosActivos();
            usuarios.addAll(usuarioService.buscarPorNombre("")); // Obtener también inactivos
            
            // Eliminar duplicados manteniendo solo una instancia de cada usuario
            usuarios = usuarios.stream()
                .collect(Collectors.toMap(Usuario::getId, u -> u, (existing, replacement) -> existing))
                .values()
                .stream()
                .collect(Collectors.toList());
            
            todosLosUsuarios = FXCollections.observableArrayList(usuarios);
            usuariosFiltrados = new FilteredList<>(todosLosUsuarios);
            
            usuariosTable.setItems(usuariosFiltrados);
            
            // Actualizar estadísticas
            actualizarEstadisticas();
            
            // Actualizar paginación
            actualizarPaginacion();
            
            statusLabel.setText("Estado: " + usuarios.size() + " usuarios cargados");
            
            logger.debug("Cargados {} usuarios", usuarios.size());
            
        } catch (Exception e) {
            logger.error("Error al cargar usuarios", e);
            CrediActivaApp.showErrorAlert("Error", "Error al Cargar Usuarios", 
                                        "No se pudieron cargar los usuarios: " + e.getMessage());
            statusLabel.setText("Estado: Error al cargar usuarios");
        }
    }
    
    /**
     * Aplica los filtros a la lista de usuarios.
     */
    private void aplicarFiltros() {
        if (usuariosFiltrados == null) return;
        
        usuariosFiltrados.setPredicate(usuario -> {
            // Filtro de búsqueda
            String textoBusqueda = buscarField.getText();
            if (textoBusqueda != null && !textoBusqueda.trim().isEmpty()) {
                String busqueda = textoBusqueda.toLowerCase();
                boolean coincide = usuario.getUsername().toLowerCase().contains(busqueda) ||
                                 usuario.getNombres().toLowerCase().contains(busqueda) ||
                                 usuario.getApellidos().toLowerCase().contains(busqueda) ||
                                 usuario.getEmail().toLowerCase().contains(busqueda) ||
                                 (usuario.getDocumentoIdentidad() != null && usuario.getDocumentoIdentidad().toLowerCase().contains(busqueda));
                
                if (!coincide) return false;
            }
            
            // Filtro de rol
            Rol rolSeleccionado = filtroRolComboBox.getValue();
            if (rolSeleccionado != null) {
                boolean tieneRol = usuario.getRoles() != null && 
                    usuario.getRoles().stream().anyMatch(rol -> 
                        rol.getId().equals(rolSeleccionado.getId()) && rol.isActivo());
                if (!tieneRol) return false;
            }
            
            // Filtro de estado
            String estadoSeleccionado = filtroEstadoComboBox.getValue();
            if (estadoSeleccionado != null && !estadoSeleccionado.equals("Todos")) {
                if (estadoSeleccionado.equals("Activos") && !usuario.isActivo()) return false;
                if (estadoSeleccionado.equals("Inactivos") && usuario.isActivo()) return false;
            }
            
            return true;
        });
        
        // Actualizar información de paginación
        actualizarPaginacion();
    }
    
    /**
     * Actualiza las estadísticas mostradas.
     */
    private void actualizarEstadisticas() {
        if (todosLosUsuarios == null) return;
        
        int total = todosLosUsuarios.size();
        int activos = (int) todosLosUsuarios.stream().filter(Usuario::isActivo).count();
        int inactivos = total - activos;
        
        totalUsuariosLabel.setText(String.valueOf(total));
        usuariosActivosLabel.setText(String.valueOf(activos));
        usuariosInactivosLabel.setText(String.valueOf(inactivos));
    }
    
    /**
     * Actualiza la información de paginación.
     */
    private void actualizarPaginacion() {
        if (usuariosFiltrados == null) return;
        
        int totalFiltrados = usuariosFiltrados.size();
        paginacionLabel.setText("Mostrando " + totalFiltrados + " usuarios");
        
        // Por simplicidad, no implementamos paginación real por ahora
        paginaLabel.setText("Página 1 de 1");
        anteriorButton.setDisable(true);
        siguienteButton.setDisable(true);
    }
    
    /**
     * Actualiza los botones según la selección actual.
     */
    private void actualizarBotonesSegunSeleccion(Usuario usuarioSeleccionado) {
        boolean haySeleccion = usuarioSeleccionado != null;
        
        editarUsuarioButton.setDisable(!haySeleccion);
        
        if (haySeleccion) {
            activarUsuarioButton.setDisable(usuarioSeleccionado.isActivo());
            desactivarUsuarioButton.setDisable(!usuarioSeleccionado.isActivo());
        } else {
            activarUsuarioButton.setDisable(true);
            desactivarUsuarioButton.setDisable(true);
        }
    }
    
    /**
     * Muestra los detalles del usuario seleccionado.
     */
    private void mostrarDetallesUsuario(Usuario usuario) {
        if (usuario == null) {
            detallesPanel.setVisible(false);
            return;
        }
        
        // Llenar los campos de detalles
        detalleIdLabel.setText(usuario.getId().toString());
        detalleUsernameLabel.setText(usuario.getUsername());
        detalleNombreCompletoLabel.setText(usuario.getNombreCompleto());
        detalleEmailLabel.setText(usuario.getEmail());
        detalleDocumentoLabel.setText(usuario.getDocumentoIdentidad() != null ? usuario.getDocumentoIdentidad() : "No especificado");
        detalleTelefonoLabel.setText(usuario.getTelefono() != null ? usuario.getTelefono() : "No especificado");
        detalleDireccionLabel.setText(usuario.getDireccion() != null ? usuario.getDireccion() : "No especificada");
        
        // Roles
        String roles = usuario.getRoles() != null ? 
            usuario.getRoles().stream()
                .filter(Rol::isActivo)
                .map(rol -> rol.getNombreLegible() + " (" + rol.getDescripcion() + ")")
                .collect(Collectors.joining(", ")) : 
            "Sin roles asignados";
        detalleRolesLabel.setText(roles);
        
        // Estado
        detalleEstadoLabel.setText(usuario.isActivo() ? "✅ Activo" : "❌ Inactivo");
        detalleEstadoLabel.setStyle(usuario.isActivo() ? "-fx-text-fill: #28a745;" : "-fx-text-fill: #dc3545;");
        
        // Fechas
        detalleUltimoLoginLabel.setText(usuario.getUltimoLogin() != null ? 
            usuario.getUltimoLogin().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) : 
            "Nunca ha iniciado sesión");
        
        detalleFechaCreacionLabel.setText(usuario.getFechaCreacion() != null ? 
            usuario.getFechaCreacion().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) : 
            "No disponible");
        
        detalleFechaActualizacionLabel.setText(usuario.getFechaActualizacion() != null ? 
            usuario.getFechaActualizacion().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) : 
            "No disponible");
        
        detallesPanel.setVisible(true);
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
    private void handleVolver() {
        logger.debug("Volviendo al dashboard");
        
        // Detener el timer
        if (clockTimer != null) {
            clockTimer.cancel();
        }
        
        // Cambiar a dashboard según el rol del usuario actual
        CrediActivaApp.changeScene("/fxml/dashboard-admin.fxml", "Panel de Administración");
    }
    
    @FXML
    private void handleNuevoUsuario() {
        logger.debug("Abriendo formulario de nuevo usuario");
        statusLabel.setText("Estado: Abriendo formulario de nuevo usuario...");
        
        try {
            CrediActivaApp.openNewWindow("/fxml/nuevo-usuario.fxml", 
                                       "Crear Nuevo Usuario", 
                                       800, 700, 
                                       true);
            
            statusLabel.setText("Estado: Formulario de nuevo usuario abierto");
            
        } catch (Exception e) {
            logger.error("Error al abrir formulario de nuevo usuario", e);
            CrediActivaApp.showErrorAlert("Error", "Error al Abrir Formulario", 
                                        "No se pudo abrir el formulario de nuevo usuario: " + e.getMessage());
            statusLabel.setText("Estado: Error al abrir formulario");
        }
    }
    
    @FXML
    private void handleEditarUsuario() {
        Usuario usuarioSeleccionado = usuariosTable.getSelectionModel().getSelectedItem();
        if (usuarioSeleccionado == null) {
            CrediActivaApp.showWarningAlert("Advertencia", "Sin Selección", 
                                          "Debe seleccionar un usuario para editar.");
            return;
        }
        
        logger.debug("Editando usuario: {}", usuarioSeleccionado.getUsername());
        statusLabel.setText("Estado: Abriendo editor de usuario...");
        
        try {
            // Cargar el FXML y obtener el controlador
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/editar-usuario.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            
            // Obtener el controlador y pasarle el usuario
            EditarUsuarioController controller = fxmlLoader.getController();
            controller.setUsuario(usuarioSeleccionado);
            
            // Crear y mostrar la ventana
            Stage editarStage = new Stage();
            editarStage.setTitle("Editar Usuario - " + usuarioSeleccionado.getNombreCompleto());
            editarStage.setScene(scene);
            editarStage.setWidth(900);
            editarStage.setHeight(800);
            editarStage.setResizable(true);
            editarStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            editarStage.initOwner(usuariosTable.getScene().getWindow());
            
            // Configurar evento de cierre para recargar datos
            editarStage.setOnHidden(event -> {
                handleActualizar(); // Recargar datos después de editar
            });
            
            editarStage.showAndWait();
            
            statusLabel.setText("Estado: Editor de usuario cerrado");
            
        } catch (Exception e) {
            logger.error("Error al abrir editor de usuario", e);
            CrediActivaApp.showErrorAlert("Error", "Error al Abrir Editor", 
                                        "No se pudo abrir el editor de usuario: " + e.getMessage());
            statusLabel.setText("Estado: Error al abrir editor");
        }
    }
    
    @FXML
    private void handleActivarUsuario() {
        Usuario usuarioSeleccionado = usuariosTable.getSelectionModel().getSelectedItem();
        if (usuarioSeleccionado == null) return;
        
        if (usuarioSeleccionado.isActivo()) {
            CrediActivaApp.showWarningAlert("Advertencia", "Usuario Ya Activo", 
                                          "El usuario seleccionado ya está activo.");
            return;
        }
        
        boolean confirmar = CrediActivaApp.showConfirmAlert("Activar Usuario", 
                                                          "¿Está seguro que desea activar este usuario?", 
                                                          "Usuario: " + usuarioSeleccionado.getNombreCompleto());
        
        if (confirmar) {
            try {
                boolean resultado = usuarioService.activarUsuario(usuarioSeleccionado.getId());
                
                if (resultado) {
                    CrediActivaApp.showInfoAlert("Éxito", "Usuario Activado", 
                                               "El usuario ha sido activado exitosamente.");
                    handleActualizar(); // Recargar datos
                } else {
                    CrediActivaApp.showErrorAlert("Error", "Error al Activar Usuario", 
                                                "No se pudo activar el usuario.");
                }
                
            } catch (Exception e) {
                logger.error("Error al activar usuario", e);
                CrediActivaApp.showErrorAlert("Error", "Error al Activar Usuario", 
                                            "Error: " + e.getMessage());
            }
        }
    }
    
    @FXML
    private void handleDesactivarUsuario() {
        Usuario usuarioSeleccionado = usuariosTable.getSelectionModel().getSelectedItem();
        if (usuarioSeleccionado == null) return;
        
        if (!usuarioSeleccionado.isActivo()) {
            CrediActivaApp.showWarningAlert("Advertencia", "Usuario Ya Inactivo", 
                                          "El usuario seleccionado ya está inactivo.");
            return;
        }
        
        boolean confirmar = CrediActivaApp.showConfirmAlert("Desactivar Usuario", 
                                                          "¿Está seguro que desea desactivar este usuario?", 
                                                          "Usuario: " + usuarioSeleccionado.getNombreCompleto() + 
                                                          "\n\nEl usuario no podrá iniciar sesión hasta que sea reactivado.");
        
        if (confirmar) {
            try {
                boolean resultado = usuarioService.desactivarUsuario(usuarioSeleccionado.getId());
                
                if (resultado) {
                    CrediActivaApp.showInfoAlert("Éxito", "Usuario Desactivado", 
                                               "El usuario ha sido desactivado exitosamente.");
                    handleActualizar(); // Recargar datos
                } else {
                    CrediActivaApp.showErrorAlert("Error", "Error al Desactivar Usuario", 
                                                "No se pudo desactivar el usuario.");
                }
                
            } catch (Exception e) {
                logger.error("Error al desactivar usuario", e);
                CrediActivaApp.showErrorAlert("Error", "Error al Desactivar Usuario", 
                                            "Error: " + e.getMessage());
            }
        }
    }
    
    @FXML
    private void handleActualizar() {
        logger.debug("Actualizando lista de usuarios");
        cargarDatos();
    }
    
    @FXML
    private void handleRepararUsuarios() {
        logger.debug("Iniciando reparación de usuarios");
        
        boolean confirmar = CrediActivaApp.showConfirmAlert("Reparar Usuarios", 
                                                          "¿Desea reparar usuarios con registros faltantes?", 
                                                          "Esta operación creará registros especiales (como asesores) para usuarios " +
                                                          "que tengan roles asignados pero les falten los registros correspondientes.\n\n" +
                                                          "Esta operación es segura y no afectará datos existentes.");
        
        if (confirmar) {
            statusLabel.setText("Estado: Reparando usuarios...");
            repararUsuariosButton.setDisable(true);
            
            // Ejecutar reparación en hilo separado para no bloquear la UI
            new Thread(() -> {
                try {
                    UsuarioReparacionUtil util = new UsuarioReparacionUtil();
                    int usuariosReparados = util.repararTodosLosUsuarios();
                    
                    Platform.runLater(() -> {
                        if (usuariosReparados > 0) {
                            CrediActivaApp.showInfoAlert("Reparación Completada", 
                                                       "Usuarios Reparados", 
                                                       String.format("Se repararon %d usuarios exitosamente.\n\n" +
                                                                   "Los usuarios con rol ASESOR ahora tienen sus registros " +
                                                                   "correspondientes en la tabla de asesores.", usuariosReparados));
                            
                            // Recargar datos para mostrar cambios
                            handleActualizar();
                        } else {
                            CrediActivaApp.showInfoAlert("Reparación Completada", 
                                                       "Sin Reparaciones Necesarias", 
                                                       "Todos los usuarios ya tienen sus registros correctos.");
                        }
                        
                        statusLabel.setText("Estado: Reparación completada - " + usuariosReparados + " usuarios reparados");
                        repararUsuariosButton.setDisable(false);
                    });
                    
                } catch (Exception e) {
                    logger.error("Error durante la reparación de usuarios", e);
                    Platform.runLater(() -> {
                        CrediActivaApp.showErrorAlert("Error", "Error en Reparación", 
                                                    "Error durante la reparación de usuarios: " + e.getMessage());
                        statusLabel.setText("Estado: Error en reparación");
                        repararUsuariosButton.setDisable(false);
                    });
                }
            }).start();
        }
    }
    
    @FXML
    private void handleExportar() {
        logger.debug("Exportando usuarios");
        // TODO: Implementar exportación a Excel/CSV
        CrediActivaApp.showInfoAlert("Próximamente", "Exportar Usuarios", 
                                   "La funcionalidad de exportación se implementará próximamente.");
    }
    
    @FXML
    private void handleBuscar() {
        // La búsqueda se hace en tiempo real, este método puede ser usado para búsquedas más complejas
        aplicarFiltros();
        statusLabel.setText("Estado: Búsqueda aplicada");
    }
    
    @FXML
    private void handleLimpiarFiltros() {
        logger.debug("Limpiando filtros");
        
        buscarField.clear();
        filtroRolComboBox.setValue(null);
        filtroEstadoComboBox.setValue("Todos");
        
        aplicarFiltros();
        statusLabel.setText("Estado: Filtros limpiados");
    }
    
    @FXML
    private void handleAnterior() {
        // TODO: Implementar paginación real
    }
    
    @FXML
    private void handleSiguiente() {
        // TODO: Implementar paginación real
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
