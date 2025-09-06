package pe.crediactiva.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pe.crediactiva.model.Asesor;
import pe.crediactiva.model.Rol;
import pe.crediactiva.model.Usuario;
import pe.crediactiva.model.enums.TipoRol;
import pe.crediactiva.service.AsesorService;
import pe.crediactiva.service.RolService;
import pe.crediactiva.service.UsuarioService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Utilidad para diagnosticar y reparar problemas con usuarios ASESOR.
 * 
 * @author CrediActiva Development Team
 * @version 1.0
 */
public class DiagnosticoAsesorUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(DiagnosticoAsesorUtil.class);
    
    private final UsuarioService usuarioService;
    private final RolService rolService;
    private final AsesorService asesorService;
    
    public DiagnosticoAsesorUtil() {
        this.usuarioService = new UsuarioService();
        this.rolService = new RolService();
        this.asesorService = new AsesorService();
    }
    
    /**
     * Ejecuta un diagnóstico completo del sistema de asesores.
     */
    public void ejecutarDiagnosticoCompleto() {
        logger.info("🔍 INICIANDO DIAGNÓSTICO COMPLETO DE ASESORES");
        logger.info("=" .repeat(60));
        
        try {
            // 1. Verificar conexión a base de datos
            verificarConexionBaseDatos();
            
            // 2. Listar todos los usuarios
            listarTodosLosUsuarios();
            
            // 3. Verificar usuarios con rol ASESOR
            verificarUsuariosAsesor();
            
            // 4. Verificar registros en tabla asesores
            verificarTablaAsesores();
            
            // 5. Identificar usuarios ASESOR sin registro
            identificarUsuariosSinRegistro();
            
            // 6. Reparar usuarios problemáticos
            repararUsuariosProblematicos();
            
            logger.info("✅ DIAGNÓSTICO COMPLETO FINALIZADO");
            
        } catch (Exception e) {
            logger.error("💥 ERROR EN DIAGNÓSTICO", e);
        }
    }
    
    /**
     * Verifica la conexión a la base de datos.
     */
    private void verificarConexionBaseDatos() {
        logger.info("🔌 Verificando conexión a base de datos...");
        
        try {
            List<Usuario> usuarios = usuarioService.obtenerTodosLosUsuarios();
            logger.info("✅ Conexión OK - {} usuarios encontrados", usuarios.size());
        } catch (Exception e) {
            logger.error("❌ Error de conexión a base de datos", e);
            throw e;
        }
    }
    
    /**
     * Lista todos los usuarios del sistema.
     */
    private void listarTodosLosUsuarios() {
        logger.info("👥 Listando todos los usuarios...");
        
        try {
            List<Usuario> usuarios = usuarioService.obtenerTodosLosUsuarios();
            
            for (Usuario usuario : usuarios) {
                List<Rol> roles = rolService.obtenerRolesDeUsuario(usuario.getId());
                String rolesStr = roles.isEmpty() ? "SIN ROLES" : 
                    roles.stream().map(Rol::getNombre).reduce((a, b) -> a + ", " + b).orElse("");
                
                logger.info("👤 Usuario: {} | ID: {} | Nombre: {} {} | Roles: {}", 
                           usuario.getUsername(), usuario.getId(), 
                           usuario.getNombres(), usuario.getApellidos(), rolesStr);
            }
            
        } catch (Exception e) {
            logger.error("❌ Error al listar usuarios", e);
        }
    }
    
    /**
     * Verifica usuarios con rol ASESOR.
     */
    private void verificarUsuariosAsesor() {
        logger.info("📋 Verificando usuarios con rol ASESOR...");
        
        try {
            List<Usuario> usuariosAsesor = usuarioService.buscarPorRol("ASESOR");
            logger.info("📊 Usuarios con rol ASESOR: {}", usuariosAsesor.size());
            
            for (Usuario usuario : usuariosAsesor) {
                logger.info("📋 ASESOR: {} | ID: {} | Nombre: {} {}", 
                           usuario.getUsername(), usuario.getId(),
                           usuario.getNombres(), usuario.getApellidos());
            }
            
        } catch (Exception e) {
            logger.error("❌ Error al verificar usuarios ASESOR", e);
        }
    }
    
    /**
     * Verifica registros en la tabla asesores.
     */
    private void verificarTablaAsesores() {
        logger.info("🏢 Verificando tabla asesores...");
        
        try {
            List<Asesor> asesores = asesorService.obtenerTodosLosAsesores();
            logger.info("📊 Registros en tabla asesores: {}", asesores.size());
            
            for (Asesor asesor : asesores) {
                logger.info("🏢 ASESOR: {} | Usuario ID: {} | Comisión: {}% | Meta: S/ {}", 
                           asesor.getCodigoAsesor(), asesor.getUsuarioId(),
                           asesor.getComisionPorcentaje().multiply(BigDecimal.valueOf(100)),
                           asesor.getMetaMensual());
            }
            
        } catch (Exception e) {
            logger.error("❌ Error al verificar tabla asesores", e);
        }
    }
    
    /**
     * Identifica usuarios ASESOR sin registro en tabla asesores.
     */
    private void identificarUsuariosSinRegistro() {
        logger.info("🔍 Identificando usuarios ASESOR sin registro...");
        
        try {
            List<Usuario> usuariosAsesor = usuarioService.buscarPorRol("ASESOR");
            
            for (Usuario usuario : usuariosAsesor) {
                boolean tieneRegistro = asesorService.esAsesor(usuario.getId());
                
                if (!tieneRegistro) {
                    logger.warn("⚠️ PROBLEMA: Usuario {} (ID: {}) tiene rol ASESOR pero NO tiene registro en tabla asesores", 
                               usuario.getUsername(), usuario.getId());
                } else {
                    logger.info("✅ OK: Usuario {} tiene registro completo", usuario.getUsername());
                }
            }
            
        } catch (Exception e) {
            logger.error("❌ Error al identificar usuarios sin registro", e);
        }
    }
    
    /**
     * Repara usuarios problemáticos.
     */
    private void repararUsuariosProblematicos() {
        logger.info("🔧 Reparando usuarios problemáticos...");
        
        try {
            List<Usuario> usuariosAsesor = usuarioService.buscarPorRol("ASESOR");
            int reparados = 0;
            
            for (Usuario usuario : usuariosAsesor) {
                boolean tieneRegistro = asesorService.esAsesor(usuario.getId());
                
                if (!tieneRegistro) {
                    logger.info("🔧 REPARANDO: Usuario {} (ID: {})", usuario.getUsername(), usuario.getId());
                    
                    if (crearRegistroAsesorDirecto(usuario)) {
                        reparados++;
                        logger.info("✅ REPARADO: Usuario {} ahora tiene registro de asesor", usuario.getUsername());
                    } else {
                        logger.error("❌ FALLÓ: No se pudo reparar usuario {}", usuario.getUsername());
                    }
                }
            }
            
            logger.info("🎉 REPARACIÓN COMPLETA: {} usuarios reparados", reparados);
            
        } catch (Exception e) {
            logger.error("❌ Error en reparación", e);
        }
    }
    
    /**
     * Crea un registro de asesor directamente.
     */
    private boolean crearRegistroAsesorDirecto(Usuario usuario) {
        try {
            logger.info("🏗️ Creando registro directo para usuario: {}", usuario.getUsername());
            
            // Usar valores por defecto
            BigDecimal comisionDefault = new BigDecimal("0.02"); // 2%
            BigDecimal metaDefault = BigDecimal.ZERO;
            
            Asesor asesor = asesorService.crearAsesor(usuario, comisionDefault, metaDefault);
            
            if (asesor != null) {
                logger.info("✅ Registro creado: {} para usuario {}", 
                           asesor.getCodigoAsesor(), usuario.getUsername());
                return true;
            }
            
            return false;
            
        } catch (Exception e) {
            logger.error("💥 Error al crear registro directo para usuario: {}", usuario.getUsername(), e);
            return false;
        }
    }
    
    /**
     * Repara un usuario específico por username.
     */
    public boolean repararUsuarioEspecifico(String username) {
        logger.info("🎯 Reparando usuario específico: {}", username);
        
        try {
            Optional<Usuario> usuarioOpt = usuarioService.buscarPorUsername(username);
            
            if (usuarioOpt.isEmpty()) {
                logger.error("❌ Usuario no encontrado: {}", username);
                return false;
            }
            
            Usuario usuario = usuarioOpt.get();
            
            // Verificar si tiene rol ASESOR
            List<Rol> roles = rolService.obtenerRolesDeUsuario(usuario.getId());
            boolean tieneRolAsesor = roles.stream().anyMatch(rol -> "ASESOR".equals(rol.getNombre()));
            
            if (!tieneRolAsesor) {
                logger.warn("⚠️ Usuario {} no tiene rol ASESOR", username);
                return false;
            }
            
            // Verificar si ya tiene registro
            if (asesorService.esAsesor(usuario.getId())) {
                logger.info("✅ Usuario {} ya tiene registro de asesor", username);
                return true;
            }
            
            // Crear registro
            return crearRegistroAsesorDirecto(usuario);
            
        } catch (Exception e) {
            logger.error("💥 Error al reparar usuario específico: {}", username, e);
            return false;
        }
    }
    
    /**
     * Método estático para uso rápido.
     */
    public static void ejecutarDiagnostico() {
        DiagnosticoAsesorUtil util = new DiagnosticoAsesorUtil();
        util.ejecutarDiagnosticoCompleto();
    }
    
    /**
     * Método estático para reparar usuario específico.
     */
    public static boolean repararUsuario(String username) {
        DiagnosticoAsesorUtil util = new DiagnosticoAsesorUtil();
        return util.repararUsuarioEspecifico(username);
    }
}
