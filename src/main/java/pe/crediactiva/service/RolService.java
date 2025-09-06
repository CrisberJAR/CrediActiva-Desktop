package pe.crediactiva.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pe.crediactiva.dao.interfaces.RolDAO;
import pe.crediactiva.dao.mysql.RolDAOImpl;
import pe.crediactiva.model.Rol;
import pe.crediactiva.model.enums.TipoRol;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para la gestión de roles en CrediActiva.
 * Contiene la lógica de negocio relacionada con roles.
 * 
 * @author CrediActiva Development Team
 * @version 1.0
 */
public class RolService {
    
    private static final Logger logger = LoggerFactory.getLogger(RolService.class);
    
    private final RolDAO rolDAO;
    
    // Constructor
    public RolService() {
        this.rolDAO = new RolDAOImpl();
    }
    
    // Constructor para inyección de dependencias (testing)
    public RolService(RolDAO rolDAO) {
        this.rolDAO = rolDAO;
    }
    
    /**
     * Obtiene todos los roles activos disponibles para asignación.
     * 
     * @return lista de roles activos
     */
    public List<Rol> obtenerRolesActivos() {
        try {
            return rolDAO.findAllActive();
        } catch (Exception e) {
            logger.error("Error al obtener roles activos", e);
            return List.of();
        }
    }
    
    /**
     * Busca un rol por su ID.
     * 
     * @param id ID del rol
     * @return Optional con el rol si existe
     */
    public Optional<Rol> buscarPorId(Integer id) {
        try {
            return rolDAO.findById(id);
        } catch (Exception e) {
            logger.error("Error al buscar rol por ID: {}", id, e);
            return Optional.empty();
        }
    }
    
    /**
     * Busca un rol por su nombre.
     * 
     * @param nombre nombre del rol
     * @return Optional con el rol si existe
     */
    public Optional<Rol> buscarPorNombre(String nombre) {
        try {
            return rolDAO.findByNombre(nombre);
        } catch (Exception e) {
            logger.error("Error al buscar rol por nombre: {}", nombre, e);
            return Optional.empty();
        }
    }
    
    /**
     * Obtiene todos los roles del sistema.
     * 
     * @return lista de todos los roles
     */
    public List<Rol> obtenerTodosLosRoles() {
        try {
            return rolDAO.findAll();
        } catch (Exception e) {
            logger.error("Error al obtener todos los roles", e);
            return List.of();
        }
    }
    
    /**
     * Asigna un rol a un usuario.
     * 
     * @param usuarioId ID del usuario
     * @param rolId ID del rol
     * @return true si se asignó exitosamente
     */
    public boolean asignarRolAUsuario(Integer usuarioId, Integer rolId) {
        logger.debug("Asignando rol {} a usuario {}", rolId, usuarioId);
        
        try {
            // Validaciones
            if (usuarioId == null || rolId == null) {
                throw new IllegalArgumentException("Usuario ID y Rol ID son requeridos");
            }
            
            // Verificar que el rol existe y está activo
            Optional<Rol> rolOpt = rolDAO.findById(rolId);
            if (rolOpt.isEmpty()) {
                throw new IllegalArgumentException("Rol no encontrado");
            }
            
            if (!rolOpt.get().isActivo()) {
                throw new IllegalArgumentException("No se puede asignar un rol inactivo");
            }
            
            // Asignar rol
            boolean resultado = rolDAO.asignarRolAUsuario(usuarioId, rolId);
            
            if (resultado) {
                logger.info("Rol asignado exitosamente: Usuario {} -> Rol {}", usuarioId, rolOpt.get().getNombre());
                
                // Crear registros especiales según el tipo de rol
                try {
                    String nombreRol = rolOpt.get().getNombre();
                    logger.info("Intentando crear registros especiales para usuario {} con rol {}", usuarioId, nombreRol);
                    
                    TipoRol tipoRol = TipoRol.valueOf(nombreRol);
                    logger.debug("TipoRol convertido exitosamente: {}", tipoRol);
                    
                    pe.crediactiva.service.UsuarioService usuarioService = new pe.crediactiva.service.UsuarioService();
                    boolean registrosCreados = usuarioService.crearRegistrosEspecialesPorRol(usuarioId, tipoRol);
                    
                    if (registrosCreados) {
                        logger.info("✅ Registros especiales creados exitosamente para usuario {} con rol {}", 
                                   usuarioId, tipoRol);
                    } else {
                        logger.error("❌ Rol asignado pero NO se pudieron crear registros especiales para usuario {} con rol {}", 
                                   usuarioId, tipoRol);
                    }
                } catch (IllegalArgumentException e) {
                    logger.debug("Rol {} no requiere registros especiales (no está en enum TipoRol)", rolOpt.get().getNombre());
                } catch (Exception e) {
                    logger.error("💥 ERROR CRÍTICO al crear registros especiales para usuario {} con rol {}", 
                               usuarioId, rolOpt.get().getNombre(), e);
                }
            }
            
            return resultado;
            
        } catch (Exception e) {
            logger.error("Error al asignar rol: Usuario {} -> Rol {}", usuarioId, rolId, e);
            throw new RuntimeException("Error al asignar rol: " + e.getMessage(), e);
        }
    }
    
    /**
     * Remueve un rol de un usuario.
     * 
     * @param usuarioId ID del usuario
     * @param rolId ID del rol
     * @return true si se removió exitosamente
     */
    public boolean removerRolDeUsuario(Integer usuarioId, Integer rolId) {
        logger.debug("Removiendo rol {} de usuario {}", rolId, usuarioId);
        
        try {
            // Validaciones
            if (usuarioId == null || rolId == null) {
                throw new IllegalArgumentException("Usuario ID y Rol ID son requeridos");
            }
            
            // Remover rol
            boolean resultado = rolDAO.removerRolDeUsuario(usuarioId, rolId);
            
            if (resultado) {
                logger.info("Rol removido exitosamente: Usuario {} -> Rol {}", usuarioId, rolId);
            }
            
            return resultado;
            
        } catch (Exception e) {
            logger.error("Error al remover rol: Usuario {} -> Rol {}", usuarioId, rolId, e);
            throw new RuntimeException("Error al remover rol: " + e.getMessage(), e);
        }
    }
    
    /**
     * Obtiene todos los roles de un usuario.
     * 
     * @param usuarioId ID del usuario
     * @return lista de roles del usuario
     */
    public List<Rol> obtenerRolesDeUsuario(Integer usuarioId) {
        try {
            return rolDAO.findRolesByUsuarioId(usuarioId);
        } catch (Exception e) {
            logger.error("Error al obtener roles del usuario: {}", usuarioId, e);
            return List.of();
        }
    }
    
    /**
     * Crea un nuevo rol en el sistema.
     * 
     * @param rol datos del rol
     * @return rol creado o null si hay error
     */
    public Rol crearRol(Rol rol) {
        logger.debug("Creando nuevo rol: {}", rol.getNombre());
        
        try {
            // Validaciones
            validarDatosRol(rol, true);
            
            // Verificar unicidad
            if (rolDAO.existsByNombre(rol.getNombre())) {
                throw new IllegalArgumentException("Ya existe un rol con ese nombre");
            }
            
            // Guardar rol
            Rol rolCreado = rolDAO.save(rol);
            
            if (rolCreado != null) {
                logger.info("Rol creado exitosamente: {}", rol.getNombre());
            }
            
            return rolCreado;
            
        } catch (Exception e) {
            logger.error("Error al crear rol: {}", rol.getNombre(), e);
            throw new RuntimeException("Error al crear rol: " + e.getMessage(), e);
        }
    }
    
    /**
     * Actualiza un rol existente.
     * 
     * @param rol datos actualizados del rol
     * @return rol actualizado o null si hay error
     */
    public Rol actualizarRol(Rol rol) {
        logger.debug("Actualizando rol: {}", rol.getNombre());
        
        try {
            // Validaciones
            validarDatosRol(rol, false);
            
            if (rol.getId() == null) {
                throw new IllegalArgumentException("El ID del rol es requerido para actualización");
            }
            
            // Verificar que el rol existe
            Optional<Rol> rolExistente = rolDAO.findById(rol.getId());
            if (rolExistente.isEmpty()) {
                throw new IllegalArgumentException("Rol no encontrado");
            }
            
            // Verificar unicidad (excluyendo el rol actual)
            Optional<Rol> rolConMismoNombre = rolDAO.findByNombre(rol.getNombre());
            if (rolConMismoNombre.isPresent() && 
                !rolConMismoNombre.get().getId().equals(rol.getId())) {
                throw new IllegalArgumentException("Ya existe otro rol con ese nombre");
            }
            
            // Actualizar rol
            Rol rolActualizado = rolDAO.update(rol);
            
            if (rolActualizado != null) {
                logger.info("Rol actualizado exitosamente: {}", rol.getNombre());
            }
            
            return rolActualizado;
            
        } catch (Exception e) {
            logger.error("Error al actualizar rol: {}", rol.getNombre(), e);
            throw new RuntimeException("Error al actualizar rol: " + e.getMessage(), e);
        }
    }
    
    /**
     * Desactiva un rol.
     * 
     * @param rolId ID del rol
     * @return true si se desactivó exitosamente
     */
    public boolean desactivarRol(Integer rolId) {
        try {
            boolean resultado = rolDAO.deactivate(rolId);
            if (resultado) {
                logger.info("Rol desactivado exitosamente: ID {}", rolId);
            }
            return resultado;
        } catch (Exception e) {
            logger.error("Error al desactivar rol: ID {}", rolId, e);
            return false;
        }
    }
    
    /**
     * Activa un rol previamente desactivado.
     * 
     * @param rolId ID del rol
     * @return true si se activó exitosamente
     */
    public boolean activarRol(Integer rolId) {
        try {
            boolean resultado = rolDAO.activate(rolId);
            if (resultado) {
                logger.info("Rol activado exitosamente: ID {}", rolId);
            }
            return resultado;
        } catch (Exception e) {
            logger.error("Error al activar rol: ID {}", rolId, e);
            return false;
        }
    }
    
    /**
     * Cuenta el total de roles activos.
     * 
     * @return número de roles activos
     */
    public long contarRolesActivos() {
        try {
            return rolDAO.countActive();
        } catch (Exception e) {
            logger.error("Error al contar roles activos", e);
            return 0;
        }
    }
    
    /**
     * Obtiene el rol por tipo.
     * 
     * @param tipoRol tipo de rol
     * @return Optional con el rol si existe
     */
    public Optional<Rol> obtenerRolPorTipo(TipoRol tipoRol) {
        return buscarPorNombre(tipoRol.name());
    }
    
    // Métodos privados de validación
    
    /**
     * Valida los datos de un rol.
     */
    private void validarDatosRol(Rol rol, boolean esNuevo) {
        if (rol == null) {
            throw new IllegalArgumentException("Rol no puede ser null");
        }
        
        if (rol.getNombre() == null || rol.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("Nombre del rol es requerido");
        }
        
        if (rol.getNombre().trim().length() < 2) {
            throw new IllegalArgumentException("Nombre del rol debe tener al menos 2 caracteres");
        }
        
        if (rol.getDescripcion() == null || rol.getDescripcion().trim().isEmpty()) {
            throw new IllegalArgumentException("Descripción del rol es requerida");
        }
    }
}
