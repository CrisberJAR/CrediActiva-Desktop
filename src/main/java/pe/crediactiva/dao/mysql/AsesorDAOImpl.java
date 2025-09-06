package pe.crediactiva.dao.mysql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pe.crediactiva.config.DatabaseConfig;
import pe.crediactiva.dao.interfaces.AsesorDAO;
import pe.crediactiva.model.Asesor;
import pe.crediactiva.util.DateUtils;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación MySQL del DAO para la entidad Asesor.
 * 
 * @author CrediActiva Development Team
 * @version 1.0
 */
public class AsesorDAOImpl implements AsesorDAO {
    
    private static final Logger logger = LoggerFactory.getLogger(AsesorDAOImpl.class);
    
    // Consultas SQL
    private static final String SELECT_BASE = """
        SELECT a.id, a.usuario_id, a.codigo_asesor, a.comision_porcentaje, 
               a.meta_mensual, a.activo, a.fecha_creacion, a.fecha_actualizacion
        FROM asesores a
        """;
    
    private static final String SELECT_BY_ID = SELECT_BASE + "WHERE a.id = ?";
    private static final String SELECT_BY_USUARIO_ID = SELECT_BASE + "WHERE a.usuario_id = ?";
    private static final String SELECT_BY_CODIGO = SELECT_BASE + "WHERE a.codigo_asesor = ?";
    private static final String SELECT_ALL = SELECT_BASE + "ORDER BY a.codigo_asesor";
    private static final String SELECT_ALL_ACTIVE = SELECT_BASE + "WHERE a.activo = TRUE ORDER BY a.codigo_asesor";
    
    private static final String INSERT_ASESOR = """
        INSERT INTO asesores (usuario_id, codigo_asesor, comision_porcentaje, meta_mensual, activo)
        VALUES (?, ?, ?, ?, ?)
        """;
    
    private static final String UPDATE_ASESOR = """
        UPDATE asesores SET
            codigo_asesor = ?, comision_porcentaje = ?, meta_mensual = ?, activo = ?,
            fecha_actualizacion = CURRENT_TIMESTAMP
        WHERE id = ?
        """;
    
    private static final String DELETE_BY_ID = "DELETE FROM asesores WHERE id = ?";
    private static final String UPDATE_ACTIVE_STATUS = "UPDATE asesores SET activo = ?, fecha_actualizacion = CURRENT_TIMESTAMP WHERE id = ?";
    
    private static final String EXISTS_BY_CODIGO = "SELECT COUNT(*) FROM asesores WHERE codigo_asesor = ?";
    private static final String EXISTS_BY_USUARIO_ID = "SELECT COUNT(*) FROM asesores WHERE usuario_id = ?";
    
    private static final String COUNT_ALL = "SELECT COUNT(*) FROM asesores";
    private static final String COUNT_ACTIVE = "SELECT COUNT(*) FROM asesores WHERE activo = TRUE";
    
    private static final String SELECT_MAX_CODIGO = "SELECT MAX(CAST(SUBSTRING(codigo_asesor, 4) AS UNSIGNED)) FROM asesores WHERE codigo_asesor REGEXP '^ASE[0-9]+$'";
    
    @Override
    public Optional<Asesor> findById(Integer id) {
        if (id == null) return Optional.empty();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToAsesor(rs));
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error al buscar asesor por ID: {}", id, e);
        }
        
        return Optional.empty();
    }
    
    @Override
    public Optional<Asesor> findByUsuarioId(Integer usuarioId) {
        if (usuarioId == null) return Optional.empty();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_USUARIO_ID)) {
            
            stmt.setInt(1, usuarioId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToAsesor(rs));
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error al buscar asesor por usuario ID: {}", usuarioId, e);
        }
        
        return Optional.empty();
    }
    
    @Override
    public Optional<Asesor> findByCodigoAsesor(String codigoAsesor) {
        if (codigoAsesor == null || codigoAsesor.trim().isEmpty()) return Optional.empty();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_CODIGO)) {
            
            stmt.setString(1, codigoAsesor.trim());
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToAsesor(rs));
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error al buscar asesor por código: {}", codigoAsesor, e);
        }
        
        return Optional.empty();
    }
    
    @Override
    public List<Asesor> findAll() {
        List<Asesor> asesores = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_ALL);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                asesores.add(mapResultSetToAsesor(rs));
            }
            
        } catch (SQLException e) {
            logger.error("Error al obtener todos los asesores", e);
        }
        
        return asesores;
    }
    
    @Override
    public List<Asesor> findAllActive() {
        List<Asesor> asesores = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_ALL_ACTIVE);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                asesores.add(mapResultSetToAsesor(rs));
            }
            
        } catch (SQLException e) {
            logger.error("Error al obtener asesores activos", e);
        }
        
        return asesores;
    }
    
    @Override
    public Asesor save(Asesor asesor) {
        if (asesor == null) return null;
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_ASESOR, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, asesor.getUsuarioId());
            stmt.setString(2, asesor.getCodigoAsesor());
            stmt.setBigDecimal(3, asesor.getComisionPorcentaje());
            stmt.setBigDecimal(4, asesor.getMetaMensual());
            stmt.setBoolean(5, asesor.isActivo());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Error al crear asesor, no se insertaron filas");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    asesor.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Error al crear asesor, no se obtuvo el ID");
                }
            }
            
            logger.info("Asesor creado exitosamente: {}", asesor.getCodigoAsesor());
            
        } catch (SQLException e) {
            logger.error("Error al guardar asesor: {}", asesor.getCodigoAsesor(), e);
            return null;
        }
        
        return asesor;
    }
    
    @Override
    public Asesor update(Asesor asesor) {
        if (asesor == null || asesor.getId() == null) return null;
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_ASESOR)) {
            
            stmt.setString(1, asesor.getCodigoAsesor());
            stmt.setBigDecimal(2, asesor.getComisionPorcentaje());
            stmt.setBigDecimal(3, asesor.getMetaMensual());
            stmt.setBoolean(4, asesor.isActivo());
            stmt.setInt(5, asesor.getId());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                logger.info("Asesor actualizado exitosamente: {}", asesor.getCodigoAsesor());
                return asesor;
            }
            
        } catch (SQLException e) {
            logger.error("Error al actualizar asesor: {}", asesor.getCodigoAsesor(), e);
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
                logger.info("Asesor eliminado exitosamente: ID {}", id);
                return true;
            }
            
        } catch (SQLException e) {
            logger.error("Error al eliminar asesor: ID {}", id, e);
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
                logger.info("Asesor {} exitosamente: ID {}", active ? "activado" : "desactivado", id);
                return true;
            }
            
        } catch (SQLException e) {
            logger.error("Error al {} asesor: ID {}", active ? "activar" : "desactivar", id, e);
        }
        
        return false;
    }
    
    @Override
    public boolean existsByCodigoAsesor(String codigoAsesor) {
        return existsByField(EXISTS_BY_CODIGO, codigoAsesor);
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
            logger.error("Error al contar asesores", e);
        }
        
        return 0;
    }
    
    @Override
    public String generarSiguienteCodigoAsesor() {
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
            
            return String.format("ASE%03d", siguienteNumero);
            
        } catch (SQLException e) {
            logger.error("Error al generar siguiente código de asesor", e);
            // En caso de error, generar un código basado en timestamp
            return "ASE" + System.currentTimeMillis() % 10000;
        }
    }
    
    /**
     * Mapea un ResultSet a un objeto Asesor.
     */
    private Asesor mapResultSetToAsesor(ResultSet rs) throws SQLException {
        Asesor asesor = new Asesor();
        
        asesor.setId(rs.getInt("id"));
        asesor.setUsuarioId(rs.getInt("usuario_id"));
        asesor.setCodigoAsesor(rs.getString("codigo_asesor"));
        asesor.setComisionPorcentaje(rs.getBigDecimal("comision_porcentaje"));
        asesor.setMetaMensual(rs.getBigDecimal("meta_mensual"));
        asesor.setActivo(rs.getBoolean("activo"));
        asesor.setFechaCreacion(DateUtils.fromSqlTimestamp(rs.getTimestamp("fecha_creacion")));
        asesor.setFechaActualizacion(DateUtils.fromSqlTimestamp(rs.getTimestamp("fecha_actualizacion")));
        
        return asesor;
    }
}
