package pe.crediactiva.dao.mysql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pe.crediactiva.config.DatabaseConfig;
import pe.crediactiva.dao.interfaces.RolDAO;
import pe.crediactiva.model.Rol;
import pe.crediactiva.util.DateUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación MySQL del DAO para la entidad Rol.
 * 
 * @author CrediActiva Development Team
 * @version 1.0
 */
public class RolDAOImpl implements RolDAO {
    
    private static final Logger logger = LoggerFactory.getLogger(RolDAOImpl.class);
    
    // Consultas SQL
    private static final String SELECT_BASE = """
        SELECT r.id, r.nombre, r.descripcion, r.activo,
               r.fecha_creacion, r.fecha_actualizacion
        FROM roles r
        """;
    
    private static final String SELECT_BY_ID = SELECT_BASE + "WHERE r.id = ?";
    private static final String SELECT_BY_NOMBRE = SELECT_BASE + "WHERE r.nombre = ?";
    private static final String SELECT_ALL = SELECT_BASE + "ORDER BY r.nombre";
    private static final String SELECT_ALL_ACTIVE = SELECT_BASE + "WHERE r.activo = TRUE ORDER BY r.nombre";
    
    private static final String INSERT_ROL = """
        INSERT INTO roles (nombre, descripcion, activo)
        VALUES (?, ?, ?)
        """;
    
    private static final String UPDATE_ROL = """
        UPDATE roles SET
            nombre = ?, descripcion = ?, activo = ?,
            fecha_actualizacion = CURRENT_TIMESTAMP
        WHERE id = ?
        """;
    
    private static final String DELETE_BY_ID = "DELETE FROM roles WHERE id = ?";
    private static final String UPDATE_ACTIVE_STATUS = "UPDATE roles SET activo = ?, fecha_actualizacion = CURRENT_TIMESTAMP WHERE id = ?";
    
    private static final String EXISTS_BY_NOMBRE = "SELECT COUNT(*) FROM roles WHERE nombre = ?";
    
    private static final String COUNT_ALL = "SELECT COUNT(*) FROM roles";
    private static final String COUNT_ACTIVE = "SELECT COUNT(*) FROM roles WHERE activo = TRUE";
    
    private static final String INSERT_USUARIO_ROL = """
        INSERT INTO usuarios_roles (usuario_id, rol_id, activo)
        VALUES (?, ?, TRUE)
        ON DUPLICATE KEY UPDATE activo = TRUE, fecha_asignacion = CURRENT_TIMESTAMP
        """;
    
    private static final String DELETE_USUARIO_ROL = """
        UPDATE usuarios_roles SET activo = FALSE
        WHERE usuario_id = ? AND rol_id = ?
        """;
    
    private static final String SELECT_ROLES_BY_USUARIO = SELECT_BASE + """
        INNER JOIN usuarios_roles ur ON r.id = ur.rol_id
        WHERE ur.usuario_id = ? AND ur.activo = TRUE AND r.activo = TRUE
        ORDER BY r.nombre
        """;
    
    @Override
    public Optional<Rol> findById(Integer id) {
        if (id == null) return Optional.empty();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToRol(rs));
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error al buscar rol por ID: {}", id, e);
        }
        
        return Optional.empty();
    }
    
    @Override
    public Optional<Rol> findByNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) return Optional.empty();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_NOMBRE)) {
            
            stmt.setString(1, nombre.trim());
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToRol(rs));
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error al buscar rol por nombre: {}", nombre, e);
        }
        
        return Optional.empty();
    }
    
    @Override
    public List<Rol> findAll() {
        List<Rol> roles = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_ALL);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                roles.add(mapResultSetToRol(rs));
            }
            
        } catch (SQLException e) {
            logger.error("Error al obtener todos los roles", e);
        }
        
        return roles;
    }
    
    @Override
    public List<Rol> findAllActive() {
        List<Rol> roles = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_ALL_ACTIVE);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                roles.add(mapResultSetToRol(rs));
            }
            
        } catch (SQLException e) {
            logger.error("Error al obtener roles activos", e);
        }
        
        return roles;
    }
    
    @Override
    public Rol save(Rol rol) {
        if (rol == null) return null;
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_ROL, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, rol.getNombre());
            stmt.setString(2, rol.getDescripcion());
            stmt.setBoolean(3, rol.isActivo());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Error al crear rol, no se insertaron filas");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    rol.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Error al crear rol, no se obtuvo el ID");
                }
            }
            
            logger.info("Rol creado exitosamente: {}", rol.getNombre());
            
        } catch (SQLException e) {
            logger.error("Error al guardar rol: {}", rol.getNombre(), e);
            return null;
        }
        
        return rol;
    }
    
    @Override
    public Rol update(Rol rol) {
        if (rol == null || rol.getId() == null) return null;
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_ROL)) {
            
            stmt.setString(1, rol.getNombre());
            stmt.setString(2, rol.getDescripcion());
            stmt.setBoolean(3, rol.isActivo());
            stmt.setInt(4, rol.getId());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                logger.info("Rol actualizado exitosamente: {}", rol.getNombre());
                return rol;
            }
            
        } catch (SQLException e) {
            logger.error("Error al actualizar rol: {}", rol.getNombre(), e);
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
                logger.info("Rol eliminado exitosamente: ID {}", id);
                return true;
            }
            
        } catch (SQLException e) {
            logger.error("Error al eliminar rol: ID {}", id, e);
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
                logger.info("Rol {} exitosamente: ID {}", active ? "activado" : "desactivado", id);
                return true;
            }
            
        } catch (SQLException e) {
            logger.error("Error al {} rol: ID {}", active ? "activar" : "desactivar", id, e);
        }
        
        return false;
    }
    
    @Override
    public boolean existsByNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) return false;
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(EXISTS_BY_NOMBRE)) {
            
            stmt.setString(1, nombre.trim());
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error al verificar existencia de rol por nombre: {}", nombre, e);
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
            logger.error("Error al contar roles", e);
        }
        
        return 0;
    }
    
    @Override
    public boolean asignarRolAUsuario(Integer usuarioId, Integer rolId) {
        if (usuarioId == null || rolId == null) return false;
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_USUARIO_ROL)) {
            
            stmt.setInt(1, usuarioId);
            stmt.setInt(2, rolId);
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                logger.info("Rol asignado exitosamente: Usuario ID {} -> Rol ID {}", usuarioId, rolId);
                return true;
            }
            
        } catch (SQLException e) {
            logger.error("Error al asignar rol: Usuario ID {} -> Rol ID {}", usuarioId, rolId, e);
        }
        
        return false;
    }
    
    @Override
    public boolean removerRolDeUsuario(Integer usuarioId, Integer rolId) {
        if (usuarioId == null || rolId == null) return false;
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_USUARIO_ROL)) {
            
            stmt.setInt(1, usuarioId);
            stmt.setInt(2, rolId);
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                logger.info("Rol removido exitosamente: Usuario ID {} -> Rol ID {}", usuarioId, rolId);
                return true;
            }
            
        } catch (SQLException e) {
            logger.error("Error al remover rol: Usuario ID {} -> Rol ID {}", usuarioId, rolId, e);
        }
        
        return false;
    }
    
    @Override
    public List<Rol> findRolesByUsuarioId(Integer usuarioId) {
        List<Rol> roles = new ArrayList<>();
        
        if (usuarioId == null) return roles;
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_ROLES_BY_USUARIO)) {
            
            stmt.setInt(1, usuarioId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    roles.add(mapResultSetToRol(rs));
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error al buscar roles por usuario ID: {}", usuarioId, e);
        }
        
        return roles;
    }
    
    /**
     * Mapea un ResultSet a un objeto Rol.
     */
    private Rol mapResultSetToRol(ResultSet rs) throws SQLException {
        Rol rol = new Rol();
        
        rol.setId(rs.getInt("id"));
        rol.setNombre(rs.getString("nombre"));
        rol.setDescripcion(rs.getString("descripcion"));
        rol.setActivo(rs.getBoolean("activo"));
        rol.setFechaCreacion(DateUtils.fromSqlTimestamp(rs.getTimestamp("fecha_creacion")));
        rol.setFechaActualizacion(DateUtils.fromSqlTimestamp(rs.getTimestamp("fecha_actualizacion")));
        
        return rol;
    }
}
