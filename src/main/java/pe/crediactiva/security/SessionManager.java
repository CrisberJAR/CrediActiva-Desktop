package pe.crediactiva.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pe.crediactiva.model.Usuario;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Gestor de sesiones de usuario para CrediActiva.
 * Maneja la sesión activa del usuario autenticado.
 * 
 * @author CrediActiva Development Team
 * @version 1.0
 */
public class SessionManager {
    
    private static final Logger logger = LoggerFactory.getLogger(SessionManager.class);
    private static final SessionManager instance = new SessionManager();
    
    private final AtomicReference<Usuario> currentUser = new AtomicReference<>();
    private LocalDateTime loginTime;
    private LocalDateTime lastActivity;
    
    private SessionManager() {
        // Constructor privado para singleton
    }
    
    /**
     * Obtiene la instancia singleton del SessionManager.
     * 
     * @return instancia del SessionManager
     */
    public static SessionManager getInstance() {
        return instance;
    }
    
    /**
     * Inicializa el gestor de sesiones.
     */
    public void initialize() {
        logger.info("SessionManager inicializado");
    }
    
    /**
     * Inicia sesión para un usuario.
     * 
     * @param usuario el usuario que inicia sesión
     */
    public void login(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("El usuario no puede ser null");
        }
        
        currentUser.set(usuario);
        loginTime = LocalDateTime.now();
        lastActivity = LocalDateTime.now();
        
        // Actualizar último login del usuario
        usuario.actualizarUltimoLogin();
        
        logger.info("Usuario '{}' ha iniciado sesión", usuario.getUsername());
    }
    
    /**
     * Cierra la sesión actual.
     */
    public void logout() {
        Usuario usuario = currentUser.get();
        if (usuario != null) {
            logger.info("Usuario '{}' ha cerrado sesión", usuario.getUsername());
        }
        
        currentUser.set(null);
        loginTime = null;
        lastActivity = null;
    }
    
    /**
     * Obtiene el usuario actualmente autenticado.
     * 
     * @return el usuario actual o null si no hay sesión
     */
    public Usuario getCurrentUser() {
        return currentUser.get();
    }
    
    /**
     * Verifica si hay un usuario autenticado.
     * 
     * @return true si hay una sesión activa
     */
    public boolean isLoggedIn() {
        return currentUser.get() != null;
    }
    
    /**
     * Verifica si el usuario actual tiene un rol específico.
     * 
     * @param roleName nombre del rol a verificar
     * @return true si el usuario tiene el rol
     */
    public boolean hasRole(String roleName) {
        Usuario usuario = currentUser.get();
        return usuario != null && usuario.getRoles().stream()
                .anyMatch(rol -> rol.getNombre().equalsIgnoreCase(roleName) && rol.isActivo());
    }
    
    /**
     * Verifica si el usuario actual es administrador.
     * 
     * @return true si es administrador
     */
    public boolean isAdmin() {
        Usuario usuario = currentUser.get();
        return usuario != null && usuario.esAdministrador();
    }
    
    /**
     * Verifica si el usuario actual es asesor.
     * 
     * @return true si es asesor
     */
    public boolean isAsesor() {
        Usuario usuario = currentUser.get();
        return usuario != null && usuario.esAsesor();
    }
    
    /**
     * Verifica si el usuario actual es cliente.
     * 
     * @return true si es cliente
     */
    public boolean isCliente() {
        Usuario usuario = currentUser.get();
        return usuario != null && usuario.esCliente();
    }
    
    /**
     * Actualiza la última actividad del usuario.
     */
    public void updateLastActivity() {
        if (isLoggedIn()) {
            lastActivity = LocalDateTime.now();
        }
    }
    
    /**
     * Obtiene el tiempo de la última actividad.
     * 
     * @return tiempo de la última actividad
     */
    public LocalDateTime getLastActivity() {
        return lastActivity;
    }
    
    /**
     * Obtiene el tiempo de inicio de sesión.
     * 
     * @return tiempo de inicio de sesión
     */
    public LocalDateTime getLoginTime() {
        return loginTime;
    }
    
    /**
     * Verifica si la sesión ha expirado.
     * 
     * @param timeoutMinutes tiempo de expiración en minutos
     * @return true si la sesión ha expirado
     */
    public boolean isSessionExpired(int timeoutMinutes) {
        if (!isLoggedIn() || lastActivity == null) {
            return true;
        }
        
        return lastActivity.plusMinutes(timeoutMinutes).isBefore(LocalDateTime.now());
    }
    
    /**
     * Requiere que haya una sesión activa, lanza excepción si no la hay.
     * 
     * @throws SecurityException si no hay sesión activa
     */
    public void requireLogin() throws SecurityException {
        if (!isLoggedIn()) {
            throw new SecurityException("Se requiere iniciar sesión para acceder a esta funcionalidad");
        }
        updateLastActivity();
    }
    
    /**
     * Requiere que el usuario tenga un rol específico.
     * 
     * @param roleName nombre del rol requerido
     * @throws SecurityException si el usuario no tiene el rol
     */
    public void requireRole(String roleName) throws SecurityException {
        requireLogin();
        if (!hasRole(roleName)) {
            throw new SecurityException("No tiene permisos para acceder a esta funcionalidad");
        }
    }
    
    /**
     * Requiere que el usuario sea administrador.
     * 
     * @throws SecurityException si el usuario no es administrador
     */
    public void requireAdmin() throws SecurityException {
        requireLogin();
        if (!isAdmin()) {
            throw new SecurityException("Se requieren permisos de administrador");
        }
    }
    
    /**
     * Obtiene información de la sesión actual.
     * 
     * @return información de la sesión
     */
    public String getSessionInfo() {
        Usuario usuario = currentUser.get();
        if (usuario == null) {
            return "No hay sesión activa";
        }
        
        return String.format("Usuario: %s (%s) - Login: %s - Última actividad: %s",
                usuario.getUsername(),
                usuario.getNombreCompleto(),
                loginTime,
                lastActivity);
    }
}


