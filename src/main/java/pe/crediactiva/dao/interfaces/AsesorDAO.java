package pe.crediactiva.dao.interfaces;

import pe.crediactiva.model.Asesor;

import java.util.List;
import java.util.Optional;

/**
 * Interfaz DAO para la entidad Asesor.
 * Define las operaciones de acceso a datos para asesores.
 * 
 * @author CrediActiva Development Team
 * @version 1.0
 */
public interface AsesorDAO {
    
    /**
     * Busca un asesor por su ID.
     * 
     * @param id ID del asesor
     * @return Optional con el asesor si existe
     */
    Optional<Asesor> findById(Integer id);
    
    /**
     * Busca un asesor por su ID de usuario.
     * 
     * @param usuarioId ID del usuario
     * @return Optional con el asesor si existe
     */
    Optional<Asesor> findByUsuarioId(Integer usuarioId);
    
    /**
     * Busca un asesor por su código.
     * 
     * @param codigoAsesor código del asesor
     * @return Optional con el asesor si existe
     */
    Optional<Asesor> findByCodigoAsesor(String codigoAsesor);
    
    /**
     * Obtiene todos los asesores.
     * 
     * @return lista de asesores
     */
    List<Asesor> findAll();
    
    /**
     * Obtiene todos los asesores activos.
     * 
     * @return lista de asesores activos
     */
    List<Asesor> findAllActive();
    
    /**
     * Guarda un nuevo asesor.
     * 
     * @param asesor asesor a guardar
     * @return asesor guardado con ID asignado
     */
    Asesor save(Asesor asesor);
    
    /**
     * Actualiza un asesor existente.
     * 
     * @param asesor asesor a actualizar
     * @return asesor actualizado
     */
    Asesor update(Asesor asesor);
    
    /**
     * Elimina un asesor por su ID.
     * 
     * @param id ID del asesor
     * @return true si se eliminó exitosamente
     */
    boolean deleteById(Integer id);
    
    /**
     * Desactiva un asesor.
     * 
     * @param id ID del asesor
     * @return true si se desactivó exitosamente
     */
    boolean deactivate(Integer id);
    
    /**
     * Activa un asesor previamente desactivado.
     * 
     * @param id ID del asesor
     * @return true si se activó exitosamente
     */
    boolean activate(Integer id);
    
    /**
     * Verifica si existe un asesor con el código especificado.
     * 
     * @param codigoAsesor código del asesor
     * @return true si existe
     */
    boolean existsByCodigoAsesor(String codigoAsesor);
    
    /**
     * Verifica si existe un asesor para el usuario especificado.
     * 
     * @param usuarioId ID del usuario
     * @return true si existe
     */
    boolean existsByUsuarioId(Integer usuarioId);
    
    /**
     * Cuenta el total de asesores.
     * 
     * @return número total de asesores
     */
    long count();
    
    /**
     * Cuenta el total de asesores activos.
     * 
     * @return número de asesores activos
     */
    long countActive();
    
    /**
     * Genera el siguiente código de asesor disponible.
     * 
     * @return código de asesor único
     */
    String generarSiguienteCodigoAsesor();
}
