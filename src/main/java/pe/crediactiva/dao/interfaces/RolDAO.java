package pe.crediactiva.dao.interfaces;

import pe.crediactiva.model.Rol;

import java.util.List;
import java.util.Optional;

/**
 * Interfaz DAO para la entidad Rol.
 * Define las operaciones de acceso a datos para roles.
 * 
 * @author CrediActiva Development Team
 * @version 1.0
 */
public interface RolDAO {
    
    /**
     * Busca un rol por su ID.
     * 
     * @param id ID del rol
     * @return Optional con el rol si existe
     */
    Optional<Rol> findById(Integer id);
    
    /**
     * Busca un rol por su nombre.
     * 
     * @param nombre nombre del rol
     * @return Optional con el rol si existe
     */
    Optional<Rol> findByNombre(String nombre);
    
    /**
     * Obtiene todos los roles.
     * 
     * @return lista de roles
     */
    List<Rol> findAll();
    
    /**
     * Obtiene todos los roles activos.
     * 
     * @return lista de roles activos
     */
    List<Rol> findAllActive();
    
    /**
     * Guarda un nuevo rol.
     * 
     * @param rol rol a guardar
     * @return rol guardado con ID asignado
     */
    Rol save(Rol rol);
    
    /**
     * Actualiza un rol existente.
     * 
     * @param rol rol a actualizar
     * @return rol actualizado
     */
    Rol update(Rol rol);
    
    /**
     * Elimina un rol por su ID.
     * 
     * @param id ID del rol
     * @return true si se eliminó exitosamente
     */
    boolean deleteById(Integer id);
    
    /**
     * Desactiva un rol.
     * 
     * @param id ID del rol
     * @return true si se desactivó exitosamente
     */
    boolean deactivate(Integer id);
    
    /**
     * Activa un rol previamente desactivado.
     * 
     * @param id ID del rol
     * @return true si se activó exitosamente
     */
    boolean activate(Integer id);
    
    /**
     * Verifica si existe un rol con el nombre especificado.
     * 
     * @param nombre nombre del rol
     * @return true si existe
     */
    boolean existsByNombre(String nombre);
    
    /**
     * Cuenta el total de roles.
     * 
     * @return número total de roles
     */
    long count();
    
    /**
     * Cuenta el total de roles activos.
     * 
     * @return número de roles activos
     */
    long countActive();
    
    /**
     * Asigna un rol a un usuario.
     * 
     * @param usuarioId ID del usuario
     * @param rolId ID del rol
     * @return true si se asignó exitosamente
     */
    boolean asignarRolAUsuario(Integer usuarioId, Integer rolId);
    
    /**
     * Remueve un rol de un usuario.
     * 
     * @param usuarioId ID del usuario
     * @param rolId ID del rol
     * @return true si se removió exitosamente
     */
    boolean removerRolDeUsuario(Integer usuarioId, Integer rolId);
    
    /**
     * Obtiene todos los roles de un usuario.
     * 
     * @param usuarioId ID del usuario
     * @return lista de roles del usuario
     */
    List<Rol> findRolesByUsuarioId(Integer usuarioId);
}
