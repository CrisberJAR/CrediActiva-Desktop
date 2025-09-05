package pe.crediactiva.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pe.crediactiva.dao.interfaces.UsuarioDAO;
import pe.crediactiva.dao.mysql.UsuarioDAOImpl;
import pe.crediactiva.model.Usuario;
import pe.crediactiva.security.PasswordEncoder;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para la gestión de usuarios en CrediActiva.
 * Contiene la lógica de negocio relacionada con usuarios.
 * 
 * @author CrediActiva Development Team
 * @version 1.0
 */
public class UsuarioService {
    
    private static final Logger logger = LoggerFactory.getLogger(UsuarioService.class);
    
    private final UsuarioDAO usuarioDAO;
    private final PasswordEncoder passwordEncoder;
    
    // Constructor
    public UsuarioService() {
        this.usuarioDAO = new UsuarioDAOImpl();
        this.passwordEncoder = new PasswordEncoder();
    }
    
    // Constructor para inyección de dependencias (testing)
    public UsuarioService(UsuarioDAO usuarioDAO, PasswordEncoder passwordEncoder) {
        this.usuarioDAO = usuarioDAO;
        this.passwordEncoder = passwordEncoder;
    }
    
    /**
     * Autentica un usuario con username y contraseña.
     * 
     * @param username nombre de usuario
     * @param password contraseña en texto plano
     * @return Optional con el usuario si la autenticación es exitosa
     */
    public Optional<Usuario> autenticar(String username, String password) {
        logger.debug("Intentando autenticar usuario: {}", username);
        
        if (username == null || username.trim().isEmpty() || password == null || password.isEmpty()) {
            logger.warn("Intento de autenticación con credenciales vacías");
            return Optional.empty();
        }
        
        try {
            Optional<Usuario> usuarioOpt = usuarioDAO.findByUsername(username.trim());
            
            if (usuarioOpt.isEmpty()) {
                logger.warn("Usuario no encontrado: {}", username);
                return Optional.empty();
            }
            
            Usuario usuario = usuarioOpt.get();
            
            if (!usuario.isActivo()) {
                logger.warn("Intento de login con usuario inactivo: {}", username);
                return Optional.empty();
            }
            
            if (!passwordEncoder.matches(password, usuario.getPasswordHash())) {
                logger.warn("Contraseña incorrecta para usuario: {}", username);
                return Optional.empty();
            }
            
            // Actualizar último login
            usuarioDAO.updateLastLogin(usuario.getId());
            
            logger.info("Autenticación exitosa para usuario: {}", username);
            return Optional.of(usuario);
            
        } catch (Exception e) {
            logger.error("Error durante la autenticación del usuario: {}", username, e);
            return Optional.empty();
        }
    }
    
    /**
     * Crea un nuevo usuario.
     * 
     * @param usuario datos del usuario
     * @param passwordPlain contraseña en texto plano
     * @return usuario creado o null si hay error
     */
    public Usuario crearUsuario(Usuario usuario, String passwordPlain) {
        logger.debug("Creando nuevo usuario: {}", usuario.getUsername());
        
        try {
            // Validaciones
            validarDatosUsuario(usuario, passwordPlain, true);
            
            // Verificar unicidad
            if (usuarioDAO.existsByUsername(usuario.getUsername())) {
                throw new IllegalArgumentException("Ya existe un usuario con ese nombre de usuario");
            }
            
            if (usuarioDAO.existsByEmail(usuario.getEmail())) {
                throw new IllegalArgumentException("Ya existe un usuario con ese email");
            }
            
            if (usuario.getDocumentoIdentidad() != null && 
                usuarioDAO.existsByDocumentoIdentidad(usuario.getDocumentoIdentidad())) {
                throw new IllegalArgumentException("Ya existe un usuario con ese documento de identidad");
            }
            
            // Encriptar contraseña
            String passwordHash = passwordEncoder.encode(passwordPlain);
            usuario.setPasswordHash(passwordHash);
            
            // Guardar usuario
            Usuario usuarioCreado = usuarioDAO.save(usuario);
            
            if (usuarioCreado != null) {
                logger.info("Usuario creado exitosamente: {}", usuario.getUsername());
            }
            
            return usuarioCreado;
            
        } catch (Exception e) {
            logger.error("Error al crear usuario: {}", usuario.getUsername(), e);
            throw new RuntimeException("Error al crear usuario: " + e.getMessage(), e);
        }
    }
    
    /**
     * Actualiza un usuario existente.
     * 
     * @param usuario datos actualizados del usuario
     * @return usuario actualizado o null si hay error
     */
    public Usuario actualizarUsuario(Usuario usuario) {
        logger.debug("Actualizando usuario: {}", usuario.getUsername());
        
        try {
            // Validaciones básicas
            validarDatosUsuario(usuario, null, false);
            
            if (usuario.getId() == null) {
                throw new IllegalArgumentException("El ID del usuario es requerido para actualización");
            }
            
            // Verificar que el usuario existe
            Optional<Usuario> usuarioExistente = usuarioDAO.findById(usuario.getId());
            if (usuarioExistente.isEmpty()) {
                throw new IllegalArgumentException("Usuario no encontrado");
            }
            
            // Verificar unicidad (excluyendo el usuario actual)
            Optional<Usuario> usuarioConMismoUsername = usuarioDAO.findByUsername(usuario.getUsername());
            if (usuarioConMismoUsername.isPresent() && 
                !usuarioConMismoUsername.get().getId().equals(usuario.getId())) {
                throw new IllegalArgumentException("Ya existe otro usuario con ese nombre de usuario");
            }
            
            Optional<Usuario> usuarioConMismoEmail = usuarioDAO.findByEmail(usuario.getEmail());
            if (usuarioConMismoEmail.isPresent() && 
                !usuarioConMismoEmail.get().getId().equals(usuario.getId())) {
                throw new IllegalArgumentException("Ya existe otro usuario con ese email");
            }
            
            if (usuario.getDocumentoIdentidad() != null) {
                Optional<Usuario> usuarioConMismoDocumento = usuarioDAO.findByDocumentoIdentidad(usuario.getDocumentoIdentidad());
                if (usuarioConMismoDocumento.isPresent() && 
                    !usuarioConMismoDocumento.get().getId().equals(usuario.getId())) {
                    throw new IllegalArgumentException("Ya existe otro usuario con ese documento de identidad");
                }
            }
            
            // Actualizar usuario
            Usuario usuarioActualizado = usuarioDAO.update(usuario);
            
            if (usuarioActualizado != null) {
                logger.info("Usuario actualizado exitosamente: {}", usuario.getUsername());
            }
            
            return usuarioActualizado;
            
        } catch (Exception e) {
            logger.error("Error al actualizar usuario: {}", usuario.getUsername(), e);
            throw new RuntimeException("Error al actualizar usuario: " + e.getMessage(), e);
        }
    }
    
    /**
     * Cambia la contraseña de un usuario.
     * 
     * @param usuarioId ID del usuario
     * @param passwordActual contraseña actual
     * @param passwordNueva nueva contraseña
     * @return true si se cambió exitosamente
     */
    public boolean cambiarPassword(Integer usuarioId, String passwordActual, String passwordNueva) {
        logger.debug("Cambiando contraseña para usuario ID: {}", usuarioId);
        
        try {
            // Validaciones
            if (usuarioId == null) {
                throw new IllegalArgumentException("ID de usuario requerido");
            }
            
            if (passwordActual == null || passwordActual.isEmpty()) {
                throw new IllegalArgumentException("Contraseña actual requerida");
            }
            
            validarPassword(passwordNueva);
            
            // Obtener usuario
            Optional<Usuario> usuarioOpt = usuarioDAO.findById(usuarioId);
            if (usuarioOpt.isEmpty()) {
                throw new IllegalArgumentException("Usuario no encontrado");
            }
            
            Usuario usuario = usuarioOpt.get();
            
            // Verificar contraseña actual
            if (!passwordEncoder.matches(passwordActual, usuario.getPasswordHash())) {
                throw new IllegalArgumentException("Contraseña actual incorrecta");
            }
            
            // Encriptar nueva contraseña
            String nuevoPasswordHash = passwordEncoder.encode(passwordNueva);
            usuario.setPasswordHash(nuevoPasswordHash);
            
            // Actualizar en base de datos
            Usuario usuarioActualizado = usuarioDAO.update(usuario);
            
            boolean exitoso = usuarioActualizado != null;
            
            if (exitoso) {
                logger.info("Contraseña cambiada exitosamente para usuario: {}", usuario.getUsername());
            }
            
            return exitoso;
            
        } catch (Exception e) {
            logger.error("Error al cambiar contraseña para usuario ID: {}", usuarioId, e);
            throw new RuntimeException("Error al cambiar contraseña: " + e.getMessage(), e);
        }
    }
    
    /**
     * Busca un usuario por su ID.
     * 
     * @param id ID del usuario
     * @return Optional con el usuario si existe
     */
    public Optional<Usuario> buscarPorId(Integer id) {
        try {
            return usuarioDAO.findById(id);
        } catch (Exception e) {
            logger.error("Error al buscar usuario por ID: {}", id, e);
            return Optional.empty();
        }
    }
    
    /**
     * Busca un usuario por su username.
     * 
     * @param username nombre de usuario
     * @return Optional con el usuario si existe
     */
    public Optional<Usuario> buscarPorUsername(String username) {
        try {
            return usuarioDAO.findByUsername(username);
        } catch (Exception e) {
            logger.error("Error al buscar usuario por username: {}", username, e);
            return Optional.empty();
        }
    }
    
    /**
     * Obtiene todos los usuarios activos.
     * 
     * @return lista de usuarios activos
     */
    public List<Usuario> obtenerUsuariosActivos() {
        try {
            return usuarioDAO.findAllActive();
        } catch (Exception e) {
            logger.error("Error al obtener usuarios activos", e);
            return List.of();
        }
    }
    
    /**
     * Busca usuarios por rol.
     * 
     * @param rolNombre nombre del rol
     * @return lista de usuarios con el rol
     */
    public List<Usuario> buscarPorRol(String rolNombre) {
        try {
            return usuarioDAO.findByRole(rolNombre);
        } catch (Exception e) {
            logger.error("Error al buscar usuarios por rol: {}", rolNombre, e);
            return List.of();
        }
    }
    
    /**
     * Busca usuarios por nombre (búsqueda parcial).
     * 
     * @param termino término de búsqueda
     * @return lista de usuarios que coinciden
     */
    public List<Usuario> buscarPorNombre(String termino) {
        try {
            return usuarioDAO.searchByName(termino);
        } catch (Exception e) {
            logger.error("Error al buscar usuarios por nombre: {}", termino, e);
            return List.of();
        }
    }
    
    /**
     * Desactiva un usuario.
     * 
     * @param usuarioId ID del usuario
     * @return true si se desactivó exitosamente
     */
    public boolean desactivarUsuario(Integer usuarioId) {
        try {
            boolean resultado = usuarioDAO.deactivate(usuarioId);
            if (resultado) {
                logger.info("Usuario desactivado exitosamente: ID {}", usuarioId);
            }
            return resultado;
        } catch (Exception e) {
            logger.error("Error al desactivar usuario: ID {}", usuarioId, e);
            return false;
        }
    }
    
    /**
     * Activa un usuario previamente desactivado.
     * 
     * @param usuarioId ID del usuario
     * @return true si se activó exitosamente
     */
    public boolean activarUsuario(Integer usuarioId) {
        try {
            boolean resultado = usuarioDAO.activate(usuarioId);
            if (resultado) {
                logger.info("Usuario activado exitosamente: ID {}", usuarioId);
            }
            return resultado;
        } catch (Exception e) {
            logger.error("Error al activar usuario: ID {}", usuarioId, e);
            return false;
        }
    }
    
    /**
     * Cuenta el total de usuarios activos.
     * 
     * @return número de usuarios activos
     */
    public long contarUsuariosActivos() {
        try {
            return usuarioDAO.countActive();
        } catch (Exception e) {
            logger.error("Error al contar usuarios activos", e);
            return 0;
        }
    }
    
    // Métodos privados de validación
    
    /**
     * Valida los datos de un usuario.
     */
    private void validarDatosUsuario(Usuario usuario, String password, boolean esNuevo) {
        if (usuario == null) {
            throw new IllegalArgumentException("Usuario no puede ser null");
        }
        
        if (usuario.getUsername() == null || usuario.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Nombre de usuario es requerido");
        }
        
        if (usuario.getUsername().trim().length() < 3) {
            throw new IllegalArgumentException("Nombre de usuario debe tener al menos 3 caracteres");
        }
        
        if (usuario.getEmail() == null || usuario.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email es requerido");
        }
        
        if (!esValidoEmail(usuario.getEmail())) {
            throw new IllegalArgumentException("Email no tiene formato válido");
        }
        
        if (usuario.getNombres() == null || usuario.getNombres().trim().isEmpty()) {
            throw new IllegalArgumentException("Nombres son requeridos");
        }
        
        if (usuario.getApellidos() == null || usuario.getApellidos().trim().isEmpty()) {
            throw new IllegalArgumentException("Apellidos son requeridos");
        }
        
        if (esNuevo && (password == null || password.isEmpty())) {
            throw new IllegalArgumentException("Contraseña es requerida para usuarios nuevos");
        }
        
        if (password != null && !password.isEmpty()) {
            validarPassword(password);
        }
    }
    
    /**
     * Valida una contraseña.
     */
    private void validarPassword(String password) {
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Contraseña debe tener al menos 6 caracteres");
        }
        
        // Aquí se pueden agregar más validaciones de complejidad
    }
    
    /**
     * Valida formato de email.
     */
    private boolean esValidoEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }
}


