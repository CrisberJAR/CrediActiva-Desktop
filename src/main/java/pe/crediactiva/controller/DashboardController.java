package pe.crediactiva.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pe.crediactiva.app.CrediActivaApp;
import pe.crediactiva.model.Usuario;
import pe.crediactiva.security.SessionManager;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Controlador para el dashboard principal de CrediActiva.
 * Maneja la interfaz del panel de administración.
 * 
 * @author CrediActiva Development Team
 * @version 1.0
 */
public class DashboardController implements Initializable {
    
    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);
    
    // Elementos de la interfaz
    @FXML private Label userLabel;
    @FXML private Label welcomeLabel;
    @FXML private Label statusLabel;
    @FXML private Label timeLabel;
    
    // Botones del menú
    @FXML private Button logoutButton;
    @FXML private Button usuariosButton;
    @FXML private Button solicitudesButton;
    @FXML private Button prestamosButton;
    @FXML private Button reportesButton;
    @FXML private Button configButton;
    
    // Botones específicos del Asesor
    @FXML private Button nuevaSolicitudButton;
    @FXML private Button cronogramasButton;
    @FXML private Button perfilButton;
    
    // Botones específicos del Cliente
    @FXML private Button historialButton;
    @FXML private Button solicitarButton;
    @FXML private Button contactoButton;
    
    // Labels de estadísticas (Admin)
    @FXML private Label usuariosActivosLabel;
    @FXML private Label solicitudesPendientesLabel;
    @FXML private Label prestamosActivosLabel;
    @FXML private Label montoTotalLabel;
    @FXML private Label cuotasVencidasLabel;
    @FXML private Label asesoresActivosLabel;
    @FXML private Label cobrosHoyLabel;
    @FXML private Label estadoSistemaLabel;
    
    // Labels específicos del Asesor
    @FXML private Label misSolicitudesLabel;
    @FXML private Label montoGestionadoLabel;
    @FXML private Label comisionesLabel;
    
    // Labels específicos del Cliente
    @FXML private Label misPrestamosTotalLabel;
    @FXML private Label deudaTotalLabel;
    @FXML private Label proximaCuotaLabel;
    
    // Tabla de actividad
    @FXML private TableView<?> actividadTable;
    
    private Timer clockTimer;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.debug("Inicializando DashboardController");
        
        try {
            // Configurar información del usuario
            setupUserInfo();
            
            // Cargar datos del dashboard
            loadDashboardData();
            
            // Iniciar reloj
            startClock();
            
            // Configurar tooltips
            setupTooltips();
            
            logger.debug("DashboardController inicializado correctamente");
            
        } catch (Exception e) {
            logger.error("Error al inicializar DashboardController", e);
            CrediActivaApp.showErrorAlert("Error de Inicialización", 
                                        "Error al cargar el dashboard", 
                                        e.getMessage());
        }
    }
    
    /**
     * Configura la información del usuario en la interfaz.
     */
    private void setupUserInfo() {
        Usuario currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            userLabel.setText("Usuario: " + currentUser.getNombreCompleto() + " (" + currentUser.getUsername() + ")");
            welcomeLabel.setText("Bienvenido, " + currentUser.getNombres());
        } else {
            logger.warn("No hay usuario en sesión");
            handleLogout();
        }
    }
    
    /**
     * Carga los datos del dashboard.
     */
    private void loadDashboardData() {
        // En una implementación real, estos datos vendrían de servicios/DAOs
        // Por ahora usamos datos simulados
        
        // Labels del Admin (solo si existen)
        if (usuariosActivosLabel != null) usuariosActivosLabel.setText("5");
        if (solicitudesPendientesLabel != null) solicitudesPendientesLabel.setText("3");
        if (prestamosActivosLabel != null) prestamosActivosLabel.setText("12");
        if (montoTotalLabel != null) montoTotalLabel.setText("S/ 125,000.00");
        if (cuotasVencidasLabel != null) cuotasVencidasLabel.setText("2");
        if (asesoresActivosLabel != null) asesoresActivosLabel.setText("3");
        if (cobrosHoyLabel != null) cobrosHoyLabel.setText("S/ 5,250.00");
        if (estadoSistemaLabel != null) estadoSistemaLabel.setText("🟢 Operativo");
        
        // Labels del Asesor (solo si existen)
        if (misSolicitudesLabel != null) misSolicitudesLabel.setText("3");
        if (montoGestionadoLabel != null) montoGestionadoLabel.setText("S/ 45,000.00");
        if (comisionesLabel != null) comisionesLabel.setText("S/ 1,250.00");
        
        // Labels del Cliente (solo si existen)
        if (misPrestamosTotalLabel != null) misPrestamosTotalLabel.setText("2");
        if (deudaTotalLabel != null) deudaTotalLabel.setText("S/ 15,000.00");
        if (proximaCuotaLabel != null) proximaCuotaLabel.setText("S/ 850.00");
        
        if (statusLabel != null) statusLabel.setText("Estado: Datos cargados correctamente");
        
        logger.debug("Datos del dashboard cargados");
    }
    
    /**
     * Inicia el reloj en tiempo real.
     */
    private void startClock() {
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
        }, 0, 1000); // Actualizar cada segundo
    }
    
    /**
     * Configura los tooltips de los botones.
     */
    private void setupTooltips() {
        // Tooltips para botones del Admin
        if (usuariosButton != null) usuariosButton.setTooltip(new Tooltip("Gestionar usuarios del sistema"));
        if (solicitudesButton != null) solicitudesButton.setTooltip(new Tooltip("Revisar y aprobar solicitudes de préstamo"));
        if (prestamosButton != null) prestamosButton.setTooltip(new Tooltip("Gestionar préstamos activos"));
        if (reportesButton != null) reportesButton.setTooltip(new Tooltip("Generar reportes y estadísticas"));
        if (configButton != null) configButton.setTooltip(new Tooltip("Configuración del sistema"));
        if (logoutButton != null) logoutButton.setTooltip(new Tooltip("Cerrar sesión"));
        
        // Tooltips para botones del Asesor
        if (nuevaSolicitudButton != null) nuevaSolicitudButton.setTooltip(new Tooltip("Crear nueva solicitud de préstamo"));
        if (cronogramasButton != null) cronogramasButton.setTooltip(new Tooltip("Ver cronogramas de pago"));
        if (perfilButton != null) perfilButton.setTooltip(new Tooltip("Ver y editar mi perfil"));
        
        // Tooltips para botones del Cliente
        if (historialButton != null) historialButton.setTooltip(new Tooltip("Ver historial de pagos"));
        if (solicitarButton != null) solicitarButton.setTooltip(new Tooltip("Solicitar nuevo préstamo"));
        if (contactoButton != null) contactoButton.setTooltip(new Tooltip("Contactar con mi asesor"));
    }
    
    // Manejadores de eventos del menú
    
    @FXML
    private void handleUsuarios() {
        logger.debug("Navegando a gestión de usuarios");
        statusLabel.setText("Estado: Cargando gestión de usuarios...");
        
        try {
            // Cambiar a la pantalla de gestión de usuarios
            CrediActivaApp.changeScene("/fxml/gestion-usuarios.fxml", "Gestión de Usuarios");
            
        } catch (Exception e) {
            logger.error("Error al navegar a gestión de usuarios", e);
            CrediActivaApp.showErrorAlert("Error", "Error de Navegación", 
                                        "No se pudo cargar la gestión de usuarios: " + e.getMessage());
            statusLabel.setText("Estado: Error al cargar gestión de usuarios");
        }
    }
    
    @FXML
    private void handleSolicitudes() {
        logger.debug("Navegando a solicitudes");
        statusLabel.setText("Estado: Cargando solicitudes...");
        // TODO: Implementar navegación a solicitudes
        CrediActivaApp.showInfoAlert("Próximamente", "Solicitudes de Préstamo", 
                                   "Esta funcionalidad se implementará próximamente.");
    }
    
    @FXML
    private void handlePrestamos() {
        logger.debug("Navegando a préstamos");
        statusLabel.setText("Estado: Cargando préstamos...");
        // TODO: Implementar navegación a préstamos
        CrediActivaApp.showInfoAlert("Próximamente", "Gestión de Préstamos", 
                                   "Esta funcionalidad se implementará próximamente.");
    }
    
    @FXML
    private void handleReportes() {
        logger.debug("Navegando a reportes");
        statusLabel.setText("Estado: Cargando reportes...");
        // TODO: Implementar navegación a reportes
        CrediActivaApp.showInfoAlert("Próximamente", "Reportes y Estadísticas", 
                                   "Esta funcionalidad se implementará próximamente.");
    }
    
    @FXML
    private void handleConfiguracion() {
        logger.debug("Navegando a configuración");
        statusLabel.setText("Estado: Cargando configuración...");
        // TODO: Implementar navegación a configuración
        CrediActivaApp.showInfoAlert("Próximamente", "Configuración del Sistema", 
                                   "Esta funcionalidad se implementará próximamente.");
    }
    
    // Manejadores de acciones rápidas
    
    @FXML
    private void handleNuevoUsuario() {
        logger.debug("Abriendo formulario de nuevo usuario");
        statusLabel.setText("Estado: Abriendo formulario de nuevo usuario...");
        
        try {
            // Abrir ventana de nuevo usuario
            CrediActivaApp.openNewWindow("/fxml/nuevo-usuario.fxml", 
                                       "Crear Nuevo Usuario", 
                                       800, 700, 
                                       false);
            
            statusLabel.setText("Estado: Formulario de nuevo usuario abierto");
            
        } catch (Exception e) {
            logger.error("Error al abrir formulario de nuevo usuario", e);
            CrediActivaApp.showErrorAlert("Error", "Error al Abrir Formulario", 
                                        "No se pudo abrir el formulario de nuevo usuario: " + e.getMessage());
            statusLabel.setText("Estado: Error al abrir formulario");
        }
    }
    
    
    @FXML
    private void handleInsertarRolDirecto() {
        logger.debug("Abriendo formulario de INSERT DIRECTO");
        statusLabel.setText("Estado: Abriendo INSERT DIRECTO...");
        
        try {
            // Abrir ventana de INSERT directo
            CrediActivaApp.openNewWindow("/fxml/insertar-rol-directo.fxml", 
                                       "🎯 INSERT DIRECTO - Asignar Rol", 
                                       700, 600, 
                                       true);
            
            statusLabel.setText("Estado: Formulario INSERT DIRECTO abierto");
            
        } catch (Exception e) {
            logger.error("Error al abrir formulario de INSERT directo", e);
            CrediActivaApp.showErrorAlert("Error", "Error al Abrir Formulario", 
                                        "No se pudo abrir el formulario de INSERT directo: " + e.getMessage());
            statusLabel.setText("Estado: Error al abrir formulario");
        }
    }
    
    
    @FXML
    private void handleRevisarSolicitudes() {
        logger.debug("Revisando solicitudes");
        handleSolicitudes();
    }
    
    @FXML
    private void handleGenerarReporte() {
        logger.debug("Generando reporte");
        handleReportes();
    }
    
    @FXML
    private void handleActualizarDatos() {
        logger.debug("Actualizando datos del dashboard");
        statusLabel.setText("Estado: Actualizando datos...");
        
        // Simular actualización
        new Thread(() -> {
            try {
                Thread.sleep(1000);
                Platform.runLater(() -> {
                    loadDashboardData();
                    statusLabel.setText("Estado: Datos actualizados");
                });
            } catch (InterruptedException e) {
                logger.error("Error al actualizar datos", e);
            }
        }).start();
    }
    
    @FXML
    private void handleLogout() {
        logger.debug("Cerrando sesión");
        
        boolean confirm = CrediActivaApp.showConfirmAlert("Cerrar Sesión", 
                                                        "¿Está seguro que desea cerrar sesión?", 
                                                        "Se perderán los datos no guardados.");
        
        if (confirm) {
            // Detener el timer
            if (clockTimer != null) {
                clockTimer.cancel();
            }
            
            // Cerrar sesión
            SessionManager.getInstance().logout();
            
            // Volver al login
            CrediActivaApp.changeScene("/fxml/login.fxml", "Iniciar Sesión");
            
            logger.info("Sesión cerrada correctamente");
        }
    }
    
    // ==============================================
    // MÉTODOS PARA DASHBOARD DE ASESOR
    // ==============================================
    
    @FXML
    private void handleNuevaSolicitud() {
        logger.debug("Creando nueva solicitud de préstamo");
        statusLabel.setText("Estado: Abriendo formulario de nueva solicitud...");
        CrediActivaApp.showInfoAlert("Próximamente", "Nueva Solicitud", 
                                   "El formulario de nueva solicitud se implementará próximamente.");
    }
    
    @FXML
    private void handleCronogramas() {
        logger.debug("Navegando a cronogramas de pago");
        statusLabel.setText("Estado: Cargando cronogramas...");
        CrediActivaApp.showInfoAlert("Próximamente", "Cronogramas de Pago", 
                                   "La gestión de cronogramas se implementará próximamente.");
    }
    
    @FXML
    private void handlePerfil() {
        logger.debug("Navegando a perfil de usuario");
        statusLabel.setText("Estado: Cargando perfil...");
        CrediActivaApp.showInfoAlert("Próximamente", "Mi Perfil", 
                                   "La gestión de perfil se implementará próximamente.");
    }
    
    @FXML
    private void handleVerPrestamos() {
        logger.debug("Navegando a vista de préstamos");
        handlePrestamos();
    }
    
    // ==============================================
    // MÉTODOS PARA DASHBOARD DE CLIENTE
    // ==============================================
    
    @FXML
    private void handleHistorial() {
        logger.debug("Navegando a historial de pagos");
        statusLabel.setText("Estado: Cargando historial...");
        CrediActivaApp.showInfoAlert("Próximamente", "Historial de Pagos", 
                                   "El historial de pagos se implementará próximamente.");
    }
    
    @FXML
    private void handleSolicitar() {
        logger.debug("Iniciando solicitud de préstamo");
        statusLabel.setText("Estado: Abriendo simulador de préstamo...");
        CrediActivaApp.showInfoAlert("Próximamente", "Solicitar Préstamo", 
                                   "El simulador de préstamos se implementará próximamente.");
    }
    
    @FXML
    private void handleContacto() {
        logger.debug("Navegando a contacto");
        statusLabel.setText("Estado: Cargando información de contacto...");
        CrediActivaApp.showInfoAlert("Información de Contacto", "CrediActiva", 
                                   "📞 Teléfono: (01) 123-4567\n📧 Email: info@crediactiva.pe\n🏢 Dirección: Av. Principal 123, Lima");
    }
    
    @FXML
    private void handleVerCronogramas() {
        logger.debug("Navegando a cronogramas de cliente");
        handleCronogramas();
    }
    
    @FXML
    private void handleSolicitarPrestamo() {
        logger.debug("Iniciando solicitud de préstamo desde acción rápida");
        handleSolicitar();
    }
    
    @FXML
    private void handleContactarAsesor() {
        logger.debug("Contactando al asesor");
        Usuario currentUser = SessionManager.getInstance().getCurrentUser();
        String mensaje = "Su asesor asignado lo contactará pronto.\n\n";
        mensaje += "Cliente: " + (currentUser != null ? currentUser.getNombreCompleto() : "Usuario") + "\n";
        mensaje += "📞 También puede llamar al (01) 123-4567";
        
        CrediActivaApp.showInfoAlert("Contacto con Asesor", "Solicitud Enviada", mensaje);
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
