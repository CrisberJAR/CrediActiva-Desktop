package pe.crediactiva.dao.mysql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pe.crediactiva.config.DatabaseConfig;
import pe.crediactiva.dao.interfaces.ClienteDAO;
import pe.crediactiva.model.Cliente;
import pe.crediactiva.util.DateUtils;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación MySQL del DAO para la entidad Cliente.
 * 
 * @author CrediActiva Development Team
 * @version 1.0
 */
public class ClienteDAOImpl implements ClienteDAO {
    
    private static final Logger logger = LoggerFactory.getLogger(ClienteDAOImpl.class);
    
    // Consultas SQL
    private static final String SELECT_BASE = """
        SELECT c.id, c.usuario_id, c.codigo_cliente, c.tipo_cliente, 
               c.limite_credito, c.score_crediticio, c.ingresos_declarados,
               c.ocupacion, c.empresa, c.referencias_personales, c.activo,
               c.fecha_creacion, c.fecha_actualizacion
        FROM clientes c
        """;
    
    private static final String SELECT_BY_ID = SELECT_BASE + "WHERE c.id = ?";
    private static final String SELECT_BY_USUARIO_ID = SELECT_BASE + "WHERE c.usuario_id = ?";
    private static final String SELECT_BY_CODIGO = SELECT_BASE + "WHERE c.codigo_cliente = ?";
    private static final String SELECT_ALL = SELECT_BASE + "ORDER BY c.codigo_cliente";
    private static final String SELECT_ALL_ACTIVE = SELECT_BASE + "WHERE c.activo = TRUE ORDER BY c.codigo_cliente";
    private static final String SELECT_BY_TIPO = SELECT_BASE + "WHERE c.tipo_cliente = ? AND c.activo = TRUE ORDER BY c.codigo_cliente";
    
    private static final String INSERT_CLIENTE = """
        INSERT INTO clientes (usuario_id, codigo_cliente, tipo_cliente, limite_credito, 
                             score_crediticio, ingresos_declarados, ocupacion, empresa, 
                             referencias_personales, activo)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
    
    private static final String UPDATE_CLIENTE = """
        UPDATE clientes SET
            codigo_cliente = ?, tipo_cliente = ?, limite_credito = ?, score_crediticio = ?,
            ingresos_declarados = ?, ocupacion = ?, empresa = ?, referencias_personales = ?,
            activo = ?, fecha_actualizacion = CURRENT_TIMESTAMP
        WHERE id = ?
        """;
    
    private static final String DELETE_BY_ID = "DELETE FROM clientes WHERE id = ?";
    private static final String UPDATE_ACTIVE_STATUS = "UPDATE clientes SET activo = ?, fecha_actualizacion = CURRENT_TIMESTAMP WHERE id = ?";
    
    private static final String EXISTS_BY_CODIGO = "SELECT COUNT(*) FROM clientes WHERE codigo_cliente = ?";
    private static final String EXISTS_BY_USUARIO_ID = "SELECT COUNT(*) FROM clientes WHERE usuario_id = ?";
    
    private static final String COUNT_ALL = "SELECT COUNT(*) FROM clientes";
    private static final String COUNT_ACTIVE = "SELECT COUNT(*) FROM clientes WHERE activo = TRUE";
    
    private static final String SELECT_MAX_CODIGO = "SELECT MAX(CAST(SUBSTRING(codigo_cliente, 4) AS UNSIGNED)) FROM clientes WHERE codigo_cliente REGEXP '^CLI[0-9]+$'";
    
    @Override
    public Optional<Cliente> findById(Integer id) {
        if (id == null) return Optional.empty();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCliente(rs));
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error al buscar cliente por ID: {}", id, e);
        }
        
        return Optional.empty();
    }
    
    @Override
    public Optional<Cliente> findByUsuarioId(Integer usuarioId) {
        if (usuarioId == null) return Optional.empty();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_USUARIO_ID)) {
            
            stmt.setInt(1, usuarioId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCliente(rs));
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error al buscar cliente por usuario ID: {}", usuarioId, e);
        }
        
        return Optional.empty();
    }
    
    @Override
    public Optional<Cliente> findByCodigoCliente(String codigoCliente) {
        if (codigoCliente == null || codigoCliente.trim().isEmpty()) return Optional.empty();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_CODIGO)) {
            
            stmt.setString(1, codigoCliente.trim());
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCliente(rs));
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error al buscar cliente por código: {}", codigoCliente, e);
        }
        
        return Optional.empty();
    }
    
    @Override
    public List<Cliente> findAll() {
        List<Cliente> clientes = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_ALL);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                clientes.add(mapResultSetToCliente(rs));
            }
            
        } catch (SQLException e) {
            logger.error("Error al obtener todos los clientes", e);
        }
        
        return clientes;
    }
    
    @Override
    public List<Cliente> findAllActive() {
        List<Cliente> clientes = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_ALL_ACTIVE);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                clientes.add(mapResultSetToCliente(rs));
            }
            
        } catch (SQLException e) {
            logger.error("Error al obtener clientes activos", e);
        }
        
        return clientes;
    }
    
    @Override
    public List<Cliente> findByTipo(String tipoCliente) {
        List<Cliente> clientes = new ArrayList<>();
        
        if (tipoCliente == null || tipoCliente.trim().isEmpty()) {
            return clientes;
        }
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_TIPO)) {
            
            stmt.setString(1, tipoCliente.trim());
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    clientes.add(mapResultSetToCliente(rs));
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error al buscar clientes por tipo: {}", tipoCliente, e);
        }
        
        return clientes;
    }
    
    @Override
    public Cliente save(Cliente cliente) {
        if (cliente == null) return null;
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_CLIENTE, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, cliente.getUsuarioId());
            stmt.setString(2, cliente.getCodigoCliente());
            stmt.setString(3, cliente.getTipoCliente().name());
            stmt.setBigDecimal(4, cliente.getLimiteCredito());
            stmt.setInt(5, cliente.getScoreCrediticio());
            stmt.setBigDecimal(6, cliente.getIngresosDeclarados());
            stmt.setString(7, cliente.getOcupacion());
            stmt.setString(8, cliente.getEmpresa());
            stmt.setString(9, cliente.getReferenciasPersonales());
            stmt.setBoolean(10, cliente.isActivo());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Error al crear cliente, no se insertaron filas");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    cliente.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Error al crear cliente, no se obtuvo el ID");
                }
            }
            
            logger.info("Cliente creado exitosamente: {}", cliente.getCodigoCliente());
            
        } catch (SQLException e) {
            logger.error("Error al guardar cliente: {}", cliente.getCodigoCliente(), e);
            return null;
        }
        
        return cliente;
    }
    
    @Override
    public Cliente update(Cliente cliente) {
        if (cliente == null || cliente.getId() == null) return null;
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_CLIENTE)) {
            
            stmt.setString(1, cliente.getCodigoCliente());
            stmt.setString(2, cliente.getTipoCliente().name());
            stmt.setBigDecimal(3, cliente.getLimiteCredito());
            stmt.setInt(4, cliente.getScoreCrediticio());
            stmt.setBigDecimal(5, cliente.getIngresosDeclarados());
            stmt.setString(6, cliente.getOcupacion());
            stmt.setString(7, cliente.getEmpresa());
            stmt.setString(8, cliente.getReferenciasPersonales());
            stmt.setBoolean(9, cliente.isActivo());
            stmt.setInt(10, cliente.getId());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                logger.info("Cliente actualizado exitosamente: {}", cliente.getCodigoCliente());
                return cliente;
            }
            
        } catch (SQLException e) {
            logger.error("Error al actualizar cliente: {}", cliente.getCodigoCliente(), e);
        }
        
        return null;
    }
    
    @Override
    public boolean deleteById(Integer id) {
        if (id == null) return false;
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_BY_ID)) {
            
            stmt.setInt(1, id);
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                logger.info("Cliente eliminado exitosamente: ID {}", id);
                return true;
            }
            
        } catch (SQLException e) {
            logger.error("Error al eliminar cliente: ID {}", id, e);
        }
        
        return false;
    }
    
    @Override
    public boolean deactivate(Integer id) {
        return updateActiveStatus(id, false);
    }
    
    @Override
    public boolean activate(Integer id) {
        return updateActiveStatus(id, true);
    }
    
    private boolean updateActiveStatus(Integer id, boolean active) {
        if (id == null) return false;
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_ACTIVE_STATUS)) {
            
            stmt.setBoolean(1, active);
            stmt.setInt(2, id);
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                logger.info("Cliente {} exitosamente: ID {}", active ? "activado" : "desactivado", id);
                return true;
            }
            
        } catch (SQLException e) {
            logger.error("Error al {} cliente: ID {}", active ? "activar" : "desactivar", id, e);
        }
        
        return false;
    }
    
    @Override
    public boolean existsByCodigoCliente(String codigoCliente) {
        return existsByField(EXISTS_BY_CODIGO, codigoCliente);
    }
    
    @Override
    public boolean existsByUsuarioId(Integer usuarioId) {
        if (usuarioId == null) return false;
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(EXISTS_BY_USUARIO_ID)) {
            
            stmt.setInt(1, usuarioId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error al verificar existencia por usuario ID: {}", usuarioId, e);
        }
        
        return false;
    }
    
    private boolean existsByField(String sql, String value) {
        if (value == null || value.trim().isEmpty()) return false;
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, value.trim());
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error al verificar existencia por campo: {}", value, e);
        }
        
        return false;
    }
    
    @Override
    public long count() {
        return countByQuery(COUNT_ALL);
    }
    
    @Override
    public long countActive() {
        return countByQuery(COUNT_ACTIVE);
    }
    
    private long countByQuery(String sql) {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getLong(1);
            }
            
        } catch (SQLException e) {
            logger.error("Error al contar clientes", e);
        }
        
        return 0;
    }
    
    @Override
    public String generarSiguienteCodigoCliente() {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_MAX_CODIGO);
             ResultSet rs = stmt.executeQuery()) {
            
            int siguienteNumero = 1;
            
            if (rs.next()) {
                Integer maxNumero = rs.getObject(1, Integer.class);
                if (maxNumero != null) {
                    siguienteNumero = maxNumero + 1;
                }
            }
            
            return String.format("CLI%03d", siguienteNumero);
            
        } catch (SQLException e) {
            logger.error("Error al generar siguiente código de cliente", e);
            // En caso de error, generar un código basado en timestamp
            return "CLI" + System.currentTimeMillis() % 10000;
        }
    }
    
    /**
     * Mapea un ResultSet a un objeto Cliente.
     */
    private Cliente mapResultSetToCliente(ResultSet rs) throws SQLException {
        Cliente cliente = new Cliente();
        
        cliente.setId(rs.getInt("id"));
        cliente.setUsuarioId(rs.getInt("usuario_id"));
        cliente.setCodigoCliente(rs.getString("codigo_cliente"));
        
        // Mapear tipo de cliente
        String tipoStr = rs.getString("tipo_cliente");
        if (tipoStr != null) {
            try {
                cliente.setTipoCliente(Cliente.TipoCliente.valueOf(tipoStr));
            } catch (IllegalArgumentException e) {
                cliente.setTipoCliente(Cliente.TipoCliente.NUEVO);
            }
        }
        
        cliente.setLimiteCredito(rs.getBigDecimal("limite_credito"));
        cliente.setScoreCrediticio(rs.getInt("score_crediticio"));
        cliente.setIngresosDeclarados(rs.getBigDecimal("ingresos_declarados"));
        cliente.setOcupacion(rs.getString("ocupacion"));
        cliente.setEmpresa(rs.getString("empresa"));
        cliente.setReferenciasPersonales(rs.getString("referencias_personales"));
        cliente.setActivo(rs.getBoolean("activo"));
        cliente.setFechaCreacion(DateUtils.fromSqlTimestamp(rs.getTimestamp("fecha_creacion")));
        cliente.setFechaActualizacion(DateUtils.fromSqlTimestamp(rs.getTimestamp("fecha_actualizacion")));
        
        return cliente;
    }
}
