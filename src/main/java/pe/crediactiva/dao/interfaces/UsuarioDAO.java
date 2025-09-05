package pe.crediactiva.dao.interfaces;

import pe.crediactiva.model.Usuario;

import java.util.List;
import java.util.Optional;

/**
 * Interfaz DAO para la entidad Usuario.
 * Define las operaciones de acceso a datos para usuarios.
 * 
 * @author CrediActiva Development Team
 * @version 1.0
 */
public interface UsuarioDAO {
    
    /**
     * Busca un usuario por su ID.
     * 
     * @param id ID del usuario
     * @return Optional con el usuario si existe
     */
    Optional<Usuario> findById(Integer id);
    
    /**
     * Busca un usuario por su nombre de usuario.
     * 
     * @param username nombre de usuario
     * @return Optional con el usuario si existe
     */
    Optional<Usuario> findByUsername(String username);
    
    /**
     * Busca un usuario por su email.
     * 
     * @param email email del usuario
     * @return Optional con el usuario si existe
     */
    Optional<Usuario> findByEmail(String email);
    
    /**
     * Busca un usuario por su documento de identidad.
     * 
     * @param documentoIdentidad documento de identidad
     * @return Optional con el usuario si existe
     */
    Optional<Usuario> findByDocumentoIdentidad(String documentoIdentidad);
    
    /**
     * Obtiene todos los usuarios.
     * 
     * @return lista de usuarios
     */
    List<Usuario> findAll();
    
    /**
     * Obtiene todos los usuarios activos.
     * 
     * @return lista de usuarios activos
     */
    List<Usuario> findAllActive();
    
    /**
     * Busca usuarios por rol.
     * 
     * @param rolNombre nombre del rol
     * @return lista de usuarios con el rol especificado
     */
    List<Usuario> findByRole(String rolNombre);
    
    /**
     * Busca usuarios por nombre o apellido (búsqueda parcial).
     * 
     * @param termino término de búsqueda
     * @return lista de usuarios que coinciden
     */
    List<Usuario> searchByName(String termino);
    
    /**
     * Guarda un nuevo usuario.
     * 
     * @param usuario usuario a guardar
     * @return usuario guardado con ID asignado
     */
    Usuario save(Usuario usuario);
    
    /**
     * Actualiza un usuario existente.
     * 
     * @param usuario usuario a actualizar
     * @return usuario actualizado
     */
    Usuario update(Usuario usuario);
    
    /**
     * Elimina un usuario por su ID.
     * 
     * @param id ID del usuario a eliminar
     * @return true si se eliminó correctamente
     */
    boolean deleteById(Integer id);
    
    /**
     * Desactiva un usuario (soft delete).
     * 
     * @param id ID del usuario a desactivar
     * @return true si se desactivó correctamente
     */
    boolean deactivate(Integer id);
    
    /**
     * Activa un usuario previamente desactivado.
     * 
     * @param id ID del usuario a activar
     * @return true si se activó correctamente
     */
    boolean activate(Integer id);
    
    /**
     * Verifica si existe un usuario con el username especificado.
     * 
     * @param username nombre de usuario
     * @return true si existe
     */
    boolean existsByUsername(String username);
    
    /**
     * Verifica si existe un usuario con el email especificado.
     * 
     * @param email email
     * @return true si existe
     */
    boolean existsByEmail(String email);
    
    /**
     * Verifica si existe un usuario con el documento especificado.
     * 
     * @param documentoIdentidad documento de identidad
     * @return true si existe
     */
    boolean existsByDocumentoIdentidad(String documentoIdentidad);
    
    /**
     * Cuenta el total de usuarios.
     * 
     * @return número total de usuarios
     */
    long count();
    
    /**
     * Cuenta el total de usuarios activos.
     * 
     * @return número de usuarios activos
     */
    long countActive();
    
    /**
     * Actualiza la fecha de último login de un usuario.
     * 
     * @param id ID del usuario
     * @return true si se actualizó correctamente
     */
    boolean updateLastLogin(Integer id);
}


