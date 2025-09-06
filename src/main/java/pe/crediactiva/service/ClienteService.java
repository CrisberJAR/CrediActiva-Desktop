package pe.crediactiva.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pe.crediactiva.dao.interfaces.ClienteDAO;
import pe.crediactiva.dao.mysql.ClienteDAOImpl;
import pe.crediactiva.model.Cliente;
import pe.crediactiva.model.Usuario;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para la gestión de clientes en CrediActiva.
 * Contiene la lógica de negocio relacionada con clientes.
 * 
 * @author CrediActiva Development Team
 * @version 1.0
 */
public class ClienteService {
    
    private static final Logger logger = LoggerFactory.getLogger(ClienteService.class);
    
    private final ClienteDAO clienteDAO;
    
    // Constructor
    public ClienteService() {
        this.clienteDAO = new ClienteDAOImpl();
    }
    
    // Constructor para inyección de dependencias (testing)
    public ClienteService(ClienteDAO clienteDAO) {
        this.clienteDAO = clienteDAO;
    }
    
    /**
     * Crea un nuevo cliente a partir de un usuario.
     * 
     * @param usuario usuario que será cliente
     * @return cliente creado o null si hay error
     */
    public Cliente crearCliente(Usuario usuario) {
        return crearCliente(usuario, Cliente.TipoCliente.NUEVO, BigDecimal.valueOf(10000), 600);
    }
    
    /**
     * Crea un nuevo cliente con parámetros específicos.
     * 
     * @param usuario usuario que será cliente
     * @param tipoCliente tipo de cliente
     * @param limiteCredito límite de crédito inicial
     * @param scoreCrediticio score crediticio inicial
     * @return cliente creado o null si hay error
     */
    public Cliente crearCliente(Usuario usuario, Cliente.TipoCliente tipoCliente, 
                               BigDecimal limiteCredito, Integer scoreCrediticio) {
        logger.debug("Creando cliente para usuario: {}", usuario.getUsername());
        
        try {
            // Validaciones
            if (usuario == null || usuario.getId() == null) {
                throw new IllegalArgumentException("Usuario es requerido y debe tener ID");
            }
            
            // Verificar que no existe ya un cliente para este usuario
            if (clienteDAO.existsByUsuarioId(usuario.getId())) {
                throw new IllegalArgumentException("Ya existe un cliente para este usuario");
            }
            
            // Generar código de cliente único
            String codigoCliente = clienteDAO.generarSiguienteCodigoCliente();
            
            // Crear cliente
            Cliente cliente = new Cliente(usuario, codigoCliente);
            
            // Establecer tipo de cliente
            if (tipoCliente != null) {
                cliente.setTipoCliente(tipoCliente);
            }
            
            // Establecer límite de crédito
            if (limiteCredito != null && limiteCredito.compareTo(BigDecimal.ZERO) > 0) {
                cliente.setLimiteCredito(limiteCredito);
            }
            
            // Establecer score crediticio
            if (scoreCrediticio != null && scoreCrediticio >= 0 && scoreCrediticio <= 1000) {
                cliente.setScoreCrediticio(scoreCrediticio);
            }
            
            // Guardar cliente
            Cliente clienteCreado = clienteDAO.save(cliente);
            
            if (clienteCreado != null) {
                logger.info("Cliente creado exitosamente: {} para usuario {}", 
                           clienteCreado.getCodigoCliente(), usuario.getUsername());
            }
            
            return clienteCreado;
            
        } catch (Exception e) {
            logger.error("Error al crear cliente para usuario: {}", usuario.getUsername(), e);
            throw new RuntimeException("Error al crear cliente: " + e.getMessage(), e);
        }
    }
    
    /**
     * Busca un cliente por su ID.
     * 
     * @param id ID del cliente
     * @return Optional con el cliente si existe
     */
    public Optional<Cliente> buscarPorId(Integer id) {
        try {
            return clienteDAO.findById(id);
        } catch (Exception e) {
            logger.error("Error al buscar cliente por ID: {}", id, e);
            return Optional.empty();
        }
    }
    
    /**
     * Busca un cliente por el ID del usuario.
     * 
     * @param usuarioId ID del usuario
     * @return Optional con el cliente si existe
     */
    public Optional<Cliente> buscarPorUsuarioId(Integer usuarioId) {
        try {
            return clienteDAO.findByUsuarioId(usuarioId);
        } catch (Exception e) {
            logger.error("Error al buscar cliente por usuario ID: {}", usuarioId, e);
            return Optional.empty();
        }
    }
    
    /**
     * Busca un cliente por su código.
     * 
     * @param codigoCliente código del cliente
     * @return Optional con el cliente si existe
     */
    public Optional<Cliente> buscarPorCodigo(String codigoCliente) {
        try {
            return clienteDAO.findByCodigoCliente(codigoCliente);
        } catch (Exception e) {
            logger.error("Error al buscar cliente por código: {}", codigoCliente, e);
            return Optional.empty();
        }
    }
    
    /**
     * Obtiene todos los clientes activos.
     * 
     * @return lista de clientes activos
     */
    public List<Cliente> obtenerClientesActivos() {
        try {
            return clienteDAO.findAllActive();
        } catch (Exception e) {
            logger.error("Error al obtener clientes activos", e);
            return List.of();
        }
    }
    
    /**
     * Obtiene todos los clientes.
     * 
     * @return lista de todos los clientes
     */
    public List<Cliente> obtenerTodosLosClientes() {
        try {
            return clienteDAO.findAll();
        } catch (Exception e) {
            logger.error("Error al obtener todos los clientes", e);
            return List.of();
        }
    }
    
    /**
     * Busca clientes por tipo.
     * 
     * @param tipoCliente tipo de cliente
     * @return lista de clientes del tipo especificado
     */
    public List<Cliente> buscarPorTipo(Cliente.TipoCliente tipoCliente) {
        try {
            return clienteDAO.findByTipo(tipoCliente.name());
        } catch (Exception e) {
            logger.error("Error al buscar clientes por tipo: {}", tipoCliente, e);
            return List.of();
        }
    }
    
    /**
     * Actualiza un cliente existente.
     * 
     * @param cliente cliente con datos actualizados
     * @return cliente actualizado o null si hay error
     */
    public Cliente actualizarCliente(Cliente cliente) {
        logger.debug("Actualizando cliente: {}", cliente.getCodigoCliente());
        
        try {
            // Validaciones
            if (cliente == null || cliente.getId() == null) {
                throw new IllegalArgumentException("Cliente y ID son requeridos");
            }
            
            // Verificar que el cliente existe
            Optional<Cliente> clienteExistente = clienteDAO.findById(cliente.getId());
            if (clienteExistente.isEmpty()) {
                throw new IllegalArgumentException("Cliente no encontrado");
            }
            
            // Actualizar cliente
            Cliente clienteActualizado = clienteDAO.update(cliente);
            
            if (clienteActualizado != null) {
                logger.info("Cliente actualizado exitosamente: {}", cliente.getCodigoCliente());
            }
            
            return clienteActualizado;
            
        } catch (Exception e) {
            logger.error("Error al actualizar cliente: {}", cliente.getCodigoCliente(), e);
            throw new RuntimeException("Error al actualizar cliente: " + e.getMessage(), e);
        }
    }
    
    /**
     * Actualiza el score crediticio de un cliente.
     * 
     * @param clienteId ID del cliente
     * @param nuevoScore nuevo score crediticio (0-1000)
     * @return true si se actualizó exitosamente
     */
    public boolean actualizarScore(Integer clienteId, Integer nuevoScore) {
        try {
            if (nuevoScore == null || nuevoScore < 0 || nuevoScore > 1000) {
                throw new IllegalArgumentException("Score crediticio debe estar entre 0 y 1000");
            }
            
            Optional<Cliente> clienteOpt = clienteDAO.findById(clienteId);
            if (clienteOpt.isEmpty()) {
                throw new IllegalArgumentException("Cliente no encontrado");
            }
            
            Cliente cliente = clienteOpt.get();
            cliente.setScoreCrediticio(nuevoScore);
            
            Cliente clienteActualizado = clienteDAO.update(cliente);
            
            if (clienteActualizado != null) {
                logger.info("Score crediticio actualizado para cliente {}: {}", 
                           cliente.getCodigoCliente(), nuevoScore);
                return true;
            }
            
            return false;
            
        } catch (Exception e) {
            logger.error("Error al actualizar score crediticio: Cliente ID {}", clienteId, e);
            return false;
        }
    }
    
    /**
     * Aumenta el límite de crédito de un cliente.
     * 
     * @param clienteId ID del cliente
     * @param incremento monto a incrementar
     * @return true si se actualizó exitosamente
     */
    public boolean aumentarLimiteCredito(Integer clienteId, BigDecimal incremento) {
        try {
            if (incremento == null || incremento.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Incremento debe ser mayor a cero");
            }
            
            Optional<Cliente> clienteOpt = clienteDAO.findById(clienteId);
            if (clienteOpt.isEmpty()) {
                throw new IllegalArgumentException("Cliente no encontrado");
            }
            
            Cliente cliente = clienteOpt.get();
            BigDecimal nuevoLimite = cliente.getLimiteCredito().add(incremento);
            cliente.setLimiteCredito(nuevoLimite);
            
            Cliente clienteActualizado = clienteDAO.update(cliente);
            
            if (clienteActualizado != null) {
                logger.info("Límite de crédito aumentado para cliente {}: S/ {}", 
                           cliente.getCodigoCliente(), nuevoLimite);
                return true;
            }
            
            return false;
            
        } catch (Exception e) {
            logger.error("Error al aumentar límite de crédito: Cliente ID {}", clienteId, e);
            return false;
        }
    }
    
    /**
     * Desactiva un cliente.
     * 
     * @param clienteId ID del cliente
     * @return true si se desactivó exitosamente
     */
    public boolean desactivarCliente(Integer clienteId) {
        try {
            boolean resultado = clienteDAO.deactivate(clienteId);
            if (resultado) {
                logger.info("Cliente desactivado exitosamente: ID {}", clienteId);
            }
            return resultado;
        } catch (Exception e) {
            logger.error("Error al desactivar cliente: ID {}", clienteId, e);
            return false;
        }
    }
    
    /**
     * Activa un cliente previamente desactivado.
     * 
     * @param clienteId ID del cliente
     * @return true si se activó exitosamente
     */
    public boolean activarCliente(Integer clienteId) {
        try {
            boolean resultado = clienteDAO.activate(clienteId);
            if (resultado) {
                logger.info("Cliente activado exitosamente: ID {}", clienteId);
            }
            return resultado;
        } catch (Exception e) {
            logger.error("Error al activar cliente: ID {}", clienteId, e);
            return false;
        }
    }
    
    /**
     * Verifica si un usuario ya tiene un registro de cliente.
     * 
     * @param usuarioId ID del usuario
     * @return true si ya es cliente
     */
    public boolean esCliente(Integer usuarioId) {
        try {
            return clienteDAO.existsByUsuarioId(usuarioId);
        } catch (Exception e) {
            logger.error("Error al verificar si usuario es cliente: {}", usuarioId, e);
            return false;
        }
    }
    
    /**
     * Cuenta el total de clientes activos.
     * 
     * @return número de clientes activos
     */
    public long contarClientesActivos() {
        try {
            return clienteDAO.countActive();
        } catch (Exception e) {
            logger.error("Error al contar clientes activos", e);
            return 0;
        }
    }
    
    /**
     * Genera el siguiente código de cliente disponible.
     * 
     * @return código único para cliente
     */
    public String generarCodigoCliente() {
        try {
            return clienteDAO.generarSiguienteCodigoCliente();
        } catch (Exception e) {
            logger.error("Error al generar código de cliente", e);
            return "CLI" + System.currentTimeMillis() % 10000;
        }
    }
}
