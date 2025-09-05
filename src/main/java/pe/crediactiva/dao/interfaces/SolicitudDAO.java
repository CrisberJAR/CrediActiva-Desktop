package pe.crediactiva.dao.interfaces;

import pe.crediactiva.model.Solicitud;
import pe.crediactiva.model.enums.EstadoSolicitud;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz DAO para la entidad Solicitud.
 * Define las operaciones de acceso a datos para solicitudes de préstamo.
 * 
 * @author CrediActiva Development Team
 * @version 1.0
 */
public interface SolicitudDAO {
    
    /**
     * Busca una solicitud por su ID.
     * 
     * @param id ID de la solicitud
     * @return Optional con la solicitud si existe
     */
    Optional<Solicitud> findById(Integer id);
    
    /**
     * Busca una solicitud por su número.
     * 
     * @param numeroSolicitud número de la solicitud
     * @return Optional con la solicitud si existe
     */
    Optional<Solicitud> findByNumero(String numeroSolicitud);
    
    /**
     * Obtiene todas las solicitudes.
     * 
     * @return lista de solicitudes
     */
    List<Solicitud> findAll();
    
    /**
     * Busca solicitudes por estado.
     * 
     * @param estado estado de la solicitud
     * @return lista de solicitudes con el estado especificado
     */
    List<Solicitud> findByEstado(EstadoSolicitud estado);
    
    /**
     * Busca solicitudes por asesor.
     * 
     * @param asesorId ID del asesor
     * @return lista de solicitudes del asesor
     */
    List<Solicitud> findByAsesor(Integer asesorId);
    
    /**
     * Busca solicitudes por cliente.
     * 
     * @param clienteId ID del cliente
     * @return lista de solicitudes del cliente
     */
    List<Solicitud> findByCliente(Integer clienteId);
    
    /**
     * Busca solicitudes por documento del cliente.
     * 
     * @param documentoCliente documento del cliente
     * @return lista de solicitudes del documento
     */
    List<Solicitud> findByDocumentoCliente(String documentoCliente);
    
    /**
     * Busca solicitudes en un rango de fechas.
     * 
     * @param fechaInicio fecha de inicio
     * @param fechaFin fecha de fin
     * @return lista de solicitudes en el rango
     */
    List<Solicitud> findByFechaRange(LocalDate fechaInicio, LocalDate fechaFin);
    
    /**
     * Busca solicitudes por rango de monto.
     * 
     * @param montoMinimo monto mínimo
     * @param montoMaximo monto máximo
     * @return lista de solicitudes en el rango de monto
     */
    List<Solicitud> findByMontoRange(BigDecimal montoMinimo, BigDecimal montoMaximo);
    
    /**
     * Busca solicitudes pendientes de revisión.
     * 
     * @return lista de solicitudes pendientes
     */
    List<Solicitud> findPendientes();
    
    /**
     * Busca solicitudes en revisión.
     * 
     * @return lista de solicitudes en revisión
     */
    List<Solicitud> findEnRevision();
    
    /**
     * Busca solicitudes aprobadas sin préstamo generado.
     * 
     * @return lista de solicitudes aprobadas pendientes
     */
    List<Solicitud> findAprobadasSinPrestamo();
    
    /**
     * Busca solicitudes por término de búsqueda en nombres del cliente.
     * 
     * @param termino término de búsqueda
     * @return lista de solicitudes que coinciden
     */
    List<Solicitud> searchByClienteName(String termino);
    
    /**
     * Guarda una nueva solicitud.
     * 
     * @param solicitud solicitud a guardar
     * @return solicitud guardada con ID asignado
     */
    Solicitud save(Solicitud solicitud);
    
    /**
     * Actualiza una solicitud existente.
     * 
     * @param solicitud solicitud a actualizar
     * @return solicitud actualizada
     */
    Solicitud update(Solicitud solicitud);
    
    /**
     * Elimina una solicitud por su ID.
     * 
     * @param id ID de la solicitud a eliminar
     * @return true si se eliminó correctamente
     */
    boolean deleteById(Integer id);
    
    /**
     * Actualiza el estado de una solicitud.
     * 
     * @param id ID de la solicitud
     * @param estado nuevo estado
     * @param revisadoPor ID del usuario que revisa
     * @param observaciones observaciones del cambio
     * @return true si se actualizó correctamente
     */
    boolean updateEstado(Integer id, EstadoSolicitud estado, Integer revisadoPor, String observaciones);
    
    /**
     * Verifica si existe una solicitud con el número especificado.
     * 
     * @param numeroSolicitud número de solicitud
     * @return true si existe
     */
    boolean existsByNumero(String numeroSolicitud);
    
    /**
     * Cuenta el total de solicitudes.
     * 
     * @return número total de solicitudes
     */
    long count();
    
    /**
     * Cuenta solicitudes por estado.
     * 
     * @param estado estado a contar
     * @return número de solicitudes con el estado
     */
    long countByEstado(EstadoSolicitud estado);
    
    /**
     * Cuenta solicitudes por asesor.
     * 
     * @param asesorId ID del asesor
     * @return número de solicitudes del asesor
     */
    long countByAsesor(Integer asesorId);
    
    /**
     * Obtiene el siguiente número de solicitud disponible.
     * 
     * @return próximo número de solicitud
     */
    String getNextNumeroSolicitud();
}


