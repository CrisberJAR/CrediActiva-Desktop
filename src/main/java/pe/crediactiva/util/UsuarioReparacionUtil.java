package pe.crediactiva.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pe.crediactiva.model.Rol;
import pe.crediactiva.model.Usuario;
import pe.crediactiva.model.enums.TipoRol;
import pe.crediactiva.service.AsesorService;
import pe.crediactiva.service.ClienteService;
import pe.crediactiva.service.UsuarioService;

import java.util.List;

/**
 * Utilidad para reparar inconsistencias en usuarios.
 * Especialmente útil para usuarios que tienen roles pero les faltan registros especiales.
 * 
 * @author CrediActiva Development Team
 * @version 1.0
 */
public class UsuarioReparacionUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(UsuarioReparacionUtil.class);
    
    private final UsuarioService usuarioService;
    private final AsesorService asesorService;
    private final ClienteService clienteService;
    
    public UsuarioReparacionUtil() {
        this.usuarioService = new UsuarioService();
        this.asesorService = new AsesorService();
        this.clienteService = new ClienteService();
    }
    
    /**
     * Repara un usuario específico creando los registros especiales faltantes.
     * 
     * @param username nombre de usuario a reparar
     * @return true si se reparó exitosamente
     */
    public boolean repararUsuario(String username) {
        logger.info("Iniciando reparación de usuario: {}", username);
        
        try {
            // Buscar el usuario
            var usuarioOpt = usuarioService.buscarPorUsername(username);
            if (usuarioOpt.isEmpty()) {
                logger.error("Usuario no encontrado: {}", username);
                return false;
            }
            
            Usuario usuario = usuarioOpt.get();
            logger.debug("Usuario encontrado: {} (ID: {})", usuario.getUsername(), usuario.getId());
            
            // Verificar y crear registros especiales según los roles
            boolean reparado = false;
            
            if (usuario.getRoles() != null) {
                for (Rol rol : usuario.getRoles()) {
                    if (rol.isActivo()) {
                        try {
                            TipoRol tipoRol = TipoRol.valueOf(rol.getNombre());
                            boolean registroCreado = crearRegistroEspecialSiEsNecesario(usuario, tipoRol);
                            if (registroCreado) {
                                reparado = true;
                            }
                        } catch (IllegalArgumentException e) {
                            logger.debug("Rol {} no requiere registros especiales", rol.getNombre());
                        }
                    }
                }
            }
            
            if (reparado) {
                logger.info("Usuario {} reparado exitosamente", username);
            } else {
                logger.info("Usuario {} no requería reparación", username);
            }
            
            return true;
            
        } catch (Exception e) {
            logger.error("Error al reparar usuario: {}", username, e);
            return false;
        }
    }
    
    /**
     * Repara todos los usuarios que tienen roles pero les faltan registros especiales.
     * 
     * @return número de usuarios reparados
     */
    public int repararTodosLosUsuarios() {
        logger.info("Iniciando reparación masiva de usuarios");
        
        int usuariosReparados = 0;
        
        try {
            // Obtener todos los usuarios activos
            List<Usuario> usuarios = usuarioService.obtenerUsuariosActivos();
            logger.debug("Verificando {} usuarios activos", usuarios.size());
            
            for (Usuario usuario : usuarios) {
                try {
                    boolean necesitaReparacion = false;
                    
                    if (usuario.getRoles() != null) {
                        for (Rol rol : usuario.getRoles()) {
                            if (rol.isActivo()) {
                                try {
                                    TipoRol tipoRol = TipoRol.valueOf(rol.getNombre());
                                    boolean registroCreado = crearRegistroEspecialSiEsNecesario(usuario, tipoRol);
                                    if (registroCreado) {
                                        necesitaReparacion = true;
                                    }
                                } catch (IllegalArgumentException e) {
                                    // Rol no requiere registros especiales
                                }
                            }
                        }
                    }
                    
                    if (necesitaReparacion) {
                        usuariosReparados++;
                        logger.info("Usuario reparado: {} (ID: {})", usuario.getUsername(), usuario.getId());
                    }
                    
                } catch (Exception e) {
                    logger.error("Error al reparar usuario: {} (ID: {})", usuario.getUsername(), usuario.getId(), e);
                }
            }
            
            logger.info("Reparación masiva completada. {} usuarios reparados", usuariosReparados);
            
        } catch (Exception e) {
            logger.error("Error durante la reparación masiva", e);
        }
        
        return usuariosReparados;
    }
    
    /**
     * Crea un registro especial si es necesario según el tipo de rol.
     * 
     * @param usuario usuario que necesita el registro
     * @param tipoRol tipo de rol
     * @return true si se creó un registro
     */
    private boolean crearRegistroEspecialSiEsNecesario(Usuario usuario, TipoRol tipoRol) {
        switch (tipoRol) {
            case ASESOR:
                return crearRegistroAsesorSiEsNecesario(usuario);
            
            case CLIENTE:
                return crearRegistroClienteSiEsNecesario(usuario);
            
            case ADMINISTRADOR:
            default:
                // No requieren registros especiales
                return false;
        }
    }
    
    /**
     * Crea un registro de asesor si no existe.
     * 
     * @param usuario usuario que debe ser asesor
     * @return true si se creó el registro
     */
    private boolean crearRegistroAsesorSiEsNecesario(Usuario usuario) {
        try {
            // Verificar si ya existe
            if (asesorService.esAsesor(usuario.getId())) {
                logger.debug("El usuario {} ya tiene registro de asesor", usuario.getUsername());
                return false;
            }
            
            // Crear el registro de asesor
            var asesor = asesorService.crearAsesor(usuario);
            
            if (asesor != null) {
                logger.info("Registro de asesor creado para usuario: {} - Código: {}", 
                           usuario.getUsername(), asesor.getCodigoAsesor());
                return true;
            }
            
            return false;
            
        } catch (Exception e) {
            logger.error("Error al crear registro de asesor para usuario: {}", usuario.getUsername(), e);
            return false;
        }
    }
    
    /**
     * Crea un registro de cliente si no existe.
     * 
     * @param usuario usuario que debe ser cliente
     * @return true si se creó el registro
     */
    private boolean crearRegistroClienteSiEsNecesario(Usuario usuario) {
        try {
            // Verificar si ya existe
            if (clienteService.esCliente(usuario.getId())) {
                logger.debug("El usuario {} ya tiene registro de cliente", usuario.getUsername());
                return false;
            }
            
            // Crear el registro de cliente
            var cliente = clienteService.crearCliente(usuario);
            
            if (cliente != null) {
                logger.info("Registro de cliente creado para usuario: {} - Código: {}", 
                           usuario.getUsername(), cliente.getCodigoCliente());
                return true;
            }
            
            return false;
            
        } catch (Exception e) {
            logger.error("Error al crear registro de cliente para usuario: {}", usuario.getUsername(), e);
            return false;
        }
    }
    
    /**
     * Método estático para reparar un usuario específico de forma rápida.
     * 
     * @param username nombre del usuario a reparar
     * @return true si se reparó exitosamente
     */
    public static boolean repararUsuarioRapido(String username) {
        UsuarioReparacionUtil util = new UsuarioReparacionUtil();
        return util.repararUsuario(username);
    }
}
