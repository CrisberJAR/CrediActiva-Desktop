package pe.crediactiva.dao.interfaces;

import pe.crediactiva.model.Cliente;

import java.util.List;
import java.util.Optional;

/**
 * Interfaz DAO para la entidad Cliente.
 * Define las operaciones de acceso a datos para clientes.
 * 
 * @author CrediActiva Development Team
 * @version 1.0
 */
public interface ClienteDAO {
    
    /**
     * Busca un cliente por su ID.
     * 
     * @param id ID del cliente
     * @return Optional con el cliente si existe
     */
    Optional<Cliente> findById(Integer id);
    
    /**
     * Busca un cliente por su ID de usuario.
     * 
     * @param usuarioId ID del usuario
     * @return Optional con el cliente si existe
     */
    Optional<Cliente> findByUsuarioId(Integer usuarioId);
    
    /**
     * Busca un cliente por su código.
     * 
     * @param codigoCliente código del cliente
     * @return Optional con el cliente si existe
     */
    Optional<Cliente> findByCodigoCliente(String codigoCliente);
    
    /**
     * Obtiene todos los clientes.
     * 
     * @return lista de clientes
     */
    List<Cliente> findAll();
    
    /**
     * Obtiene todos los clientes activos.
     * 
     * @return lista de clientes activos
     */
    List<Cliente> findAllActive();
    
    /**
     * Busca clientes por tipo.
     * 
     * @param tipoCliente tipo de cliente
     * @return lista de clientes del tipo especificado
     */
    List<Cliente> findByTipo(String tipoCliente);
    
    /**
     * Guarda un nuevo cliente.
     * 
     * @param cliente cliente a guardar
     * @return cliente guardado con ID asignado
     */
    Cliente save(Cliente cliente);
    
    /**
     * Actualiza un cliente existente.
     * 
     * @param cliente cliente a actualizar
     * @return cliente actualizado
     */
    Cliente update(Cliente cliente);
    
    /**
     * Elimina un cliente por su ID.
     * 
     * @param id ID del cliente
     * @return true si se eliminó exitosamente
     */
    boolean deleteById(Integer id);
    
    /**
     * Desactiva un cliente.
     * 
     * @param id ID del cliente
     * @return true si se desactivó exitosamente
     */
    boolean deactivate(Integer id);
    
    /**
     * Activa un cliente previamente desactivado.
     * 
     * @param id ID del cliente
     * @return true si se activó exitosamente
     */
    boolean activate(Integer id);
    
    /**
     * Verifica si existe un cliente con el código especificado.
     * 
     * @param codigoCliente código del cliente
     * @return true si existe
     */
    boolean existsByCodigoCliente(String codigoCliente);
    
    /**
     * Verifica si existe un cliente para el usuario especificado.
     * 
     * @param usuarioId ID del usuario
     * @return true si existe
     */
    boolean existsByUsuarioId(Integer usuarioId);
    
    /**
     * Cuenta el total de clientes.
     * 
     * @return número total de clientes
     */
    long count();
    
    /**
     * Cuenta el total de clientes activos.
     * 
     * @return número de clientes activos
     */
    long countActive();
    
    /**
     * Genera el siguiente código de cliente disponible.
     * 
     * @return código de cliente único
     */
    String generarSiguienteCodigoCliente();
}
