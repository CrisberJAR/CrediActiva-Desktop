package pe.crediactiva.dao.mysql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pe.crediactiva.config.DatabaseConfig;
import pe.crediactiva.dao.interfaces.UsuarioDAO;
import pe.crediactiva.model.Rol;
import pe.crediactiva.model.Usuario;
import pe.crediactiva.util.DateUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación MySQL del DAO para la entidad Usuario.
 * 
 * @author CrediActiva Development Team
 * @version 1.0
 */
public class UsuarioDAOImpl implements UsuarioDAO {
    
    private static final Logger logger = LoggerFactory.getLogger(UsuarioDAOImpl.class);
    
    // Consultas SQL
    private static final String SELECT_BASE = """
        SELECT u.id, u.username, u.email, u.password_hash, u.nombres, u.apellidos,
               u.documento_identidad, u.telefono, u.direccion, u.activo,
               u.ultimo_login, u.fecha_creacion, u.fecha_actualizacion
        FROM usuarios u
        """;
    
    private static final String SELECT_BY_ID = SELECT_BASE + "WHERE u.id = ?";
    private static final String SELECT_BY_USERNAME = SELECT_BASE + "WHERE u.username = ?";
    private static final String SELECT_BY_EMAIL = SELECT_BASE + "WHERE u.email = ?";
    private static final String SELECT_BY_DOCUMENTO = SELECT_BASE + "WHERE u.documento_identidad = ?";
    private static final String SELECT_ALL = SELECT_BASE + "ORDER BY u.nombres, u.apellidos";
    private static final String SELECT_ALL_ACTIVE = SELECT_BASE + "WHERE u.activo = TRUE ORDER BY u.nombres, u.apellidos";
    
    private static final String SELECT_BY_ROLE = SELECT_BASE + """
        INNER JOIN usuarios_roles ur ON u.id = ur.usuario_id
        INNER JOIN roles r ON ur.rol_id = r.id
        WHERE r.nombre = ? AND ur.activo = TRUE AND u.activo = TRUE
        ORDER BY u.nombres, u.apellidos
        """;
    
    private static final String SEARCH_BY_NAME = SELECT_BASE + """
        WHERE (u.nombres LIKE ? OR u.apellidos LIKE ?) AND u.activo = TRUE
        ORDER BY u.nombres, u.apellidos
        """;
    
    private static final String INSERT_USUARIO = """
        INSERT INTO usuarios (username, email, password_hash, nombres, apellidos,
                             documento_identidad, telefono, direccion, activo)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
    
    private static final String UPDATE_USUARIO = """
        UPDATE usuarios SET
            username = ?, email = ?, nombres = ?, apellidos = ?,
            documento_identidad = ?, telefono = ?, direccion = ?, activo = ?,
            fecha_actualizacion = CURRENT_TIMESTAMP
        WHERE id = ?
        """;
    
    private static final String UPDATE_PASSWORD = """
        UPDATE usuarios SET password_hash = ?, fecha_actualizacion = CURRENT_TIMESTAMP
        WHERE id = ?
        """;
    
    private static final String UPDATE_LAST_LOGIN = """
        UPDATE usuarios SET ultimo_login = CURRENT_TIMESTAMP, fecha_actualizacion = CURRENT_TIMESTAMP
        WHERE id = ?
        """;
    
    private static final String DELETE_BY_ID = "DELETE FROM usuarios WHERE id = ?";
    private static final String UPDATE_ACTIVE_STATUS = "UPDATE usuarios SET activo = ?, fecha_actualizacion = CURRENT_TIMESTAMP WHERE id = ?";
    
    private static final String EXISTS_BY_USERNAME = "SELECT COUNT(*) FROM usuarios WHERE username = ?";
    private static final String EXISTS_BY_EMAIL = "SELECT COUNT(*) FROM usuarios WHERE email = ?";
    private static final String EXISTS_BY_DOCUMENTO = "SELECT COUNT(*) FROM usuarios WHERE documento_identidad = ?";
    
    private static final String COUNT_ALL = "SELECT COUNT(*) FROM usuarios";
    private static final String COUNT_ACTIVE = "SELECT COUNT(*) FROM usuarios WHERE activo = TRUE";
    
    private static final String SELECT_ROLES = """
        SELECT r.id, r.nombre, r.descripcion, r.activo, r.fecha_creacion, r.fecha_actualizacion
        FROM roles r
        INNER JOIN usuarios_roles ur ON r.id = ur.rol_id
        WHERE ur.usuario_id = ? AND ur.activo = TRUE
        ORDER BY r.nombre
        """;
    
    @Override
    public Optional<Usuario> findById(Integer id) {
        if (id == null) return Optional.empty();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Usuario usuario = mapResultSetToUsuario(rs);
                    cargarRoles(usuario, conn);
                    return Optional.of(usuario);
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error al buscar usuario por ID: {}", id, e);
        }
        
        return Optional.empty();
    }
    
    @Override
    public Optional<Usuario> findByUsername(String username) {
        if (username == null || username.trim().isEmpty()) return Optional.empty();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_USERNAME)) {
            
            stmt.setString(1, username.trim());
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Usuario usuario = mapResultSetToUsuario(rs);
                    cargarRoles(usuario, conn);
                    return Optional.of(usuario);
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error al buscar usuario por username: {}", username, e);
        }
        
        return Optional.empty();
    }
    
    @Override
    public Optional<Usuario> findByEmail(String email) {
        if (email == null || email.trim().isEmpty()) return Optional.empty();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_EMAIL)) {
            
            stmt.setString(1, email.trim());
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Usuario usuario = mapResultSetToUsuario(rs);
                    cargarRoles(usuario, conn);
                    return Optional.of(usuario);
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error al buscar usuario por email: {}", email, e);
        }
        
        return Optional.empty();
    }
    
    @Override
    public Optional<Usuario> findByDocumentoIdentidad(String documentoIdentidad) {
        if (documentoIdentidad == null || documentoIdentidad.trim().isEmpty()) return Optional.empty();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_DOCUMENTO)) {
            
            stmt.setString(1, documentoIdentidad.trim());
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Usuario usuario = mapResultSetToUsuario(rs);
                    cargarRoles(usuario, conn);
                    return Optional.of(usuario);
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error al buscar usuario por documento: {}", documentoIdentidad, e);
        }
        
        return Optional.empty();
    }
    
    @Override
    public List<Usuario> findAll() {
        List<Usuario> usuarios = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_ALL);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Usuario usuario = mapResultSetToUsuario(rs);
                cargarRoles(usuario, conn);
                usuarios.add(usuario);
            }
            
        } catch (SQLException e) {
            logger.error("Error al obtener todos los usuarios", e);
        }
        
        return usuarios;
    }
    
    @Override
    public List<Usuario> findAllActive() {
        List<Usuario> usuarios = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_ALL_ACTIVE);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Usuario usuario = mapResultSetToUsuario(rs);
                cargarRoles(usuario, conn);
                usuarios.add(usuario);
            }
            
        } catch (SQLException e) {
            logger.error("Error al obtener usuarios activos", e);
        }
        
        return usuarios;
    }
    
    @Override
    public List<Usuario> findByRole(String rolNombre) {
        List<Usuario> usuarios = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ROLE)) {
            
            stmt.setString(1, rolNombre);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Usuario usuario = mapResultSetToUsuario(rs);
                    cargarRoles(usuario, conn);
                    usuarios.add(usuario);
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error al buscar usuarios por rol: {}", rolNombre, e);
        }
        
        return usuarios;
    }
    
    @Override
    public List<Usuario> searchByName(String termino) {
        List<Usuario> usuarios = new ArrayList<>();
        
        if (termino == null || termino.trim().isEmpty()) {
            return usuarios;
        }
        
        String searchTerm = "%" + termino.trim() + "%";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SEARCH_BY_NAME)) {
            
            stmt.setString(1, searchTerm);
            stmt.setString(2, searchTerm);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Usuario usuario = mapResultSetToUsuario(rs);
                    cargarRoles(usuario, conn);
                    usuarios.add(usuario);
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error al buscar usuarios por nombre: {}", termino, e);
        }
        
        return usuarios;
    }
    
    @Override
    public Usuario save(Usuario usuario) {
        if (usuario == null) return null;
        
        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            
            try (PreparedStatement stmt = conn.prepareStatement(INSERT_USUARIO, Statement.RETURN_GENERATED_KEYS)) {
                
                stmt.setString(1, usuario.getUsername());
                stmt.setString(2, usuario.getEmail());
                stmt.setString(3, usuario.getPasswordHash());
                stmt.setString(4, usuario.getNombres());
                stmt.setString(5, usuario.getApellidos());
                stmt.setString(6, usuario.getDocumentoIdentidad());
                stmt.setString(7, usuario.getTelefono());
                stmt.setString(8, usuario.getDireccion());
                stmt.setBoolean(9, usuario.isActivo());
                
                int affectedRows = stmt.executeUpdate();
                
                if (affectedRows == 0) {
                    throw new SQLException("Error al crear usuario, no se insertaron filas");
                }
                
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        usuario.setId(generatedKeys.getInt(1));
                    } else {
                        throw new SQLException("Error al crear usuario, no se obtuvo el ID");
                    }
                }
                
                conn.commit();
                logger.info("Usuario creado exitosamente: {}", usuario.getUsername());
                
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
            
        } catch (SQLException e) {
            logger.error("Error al guardar usuario: {}", usuario.getUsername(), e);
            return null;
        }
        
        return usuario;
    }
    
    @Override
    public Usuario update(Usuario usuario) {
        if (usuario == null || usuario.getId() == null) return null;
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_USUARIO)) {
            
            stmt.setString(1, usuario.getUsername());
            stmt.setString(2, usuario.getEmail());
            stmt.setString(3, usuario.getNombres());
            stmt.setString(4, usuario.getApellidos());
            stmt.setString(5, usuario.getDocumentoIdentidad());
            stmt.setString(6, usuario.getTelefono());
            stmt.setString(7, usuario.getDireccion());
            stmt.setBoolean(8, usuario.isActivo());
            stmt.setInt(9, usuario.getId());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                logger.info("Usuario actualizado exitosamente: {}", usuario.getUsername());
                return usuario;
            }
            
        } catch (SQLException e) {
            logger.error("Error al actualizar usuario: {}", usuario.getUsername(), e);
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
                logger.info("Usuario eliminado exitosamente: ID {}", id);
                return true;
            }
            
        } catch (SQLException e) {
            logger.error("Error al eliminar usuario: ID {}", id, e);
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
                logger.info("Usuario {} exitosamente: ID {}", active ? "activado" : "desactivado", id);
                return true;
            }
            
        } catch (SQLException e) {
            logger.error("Error al {} usuario: ID {}", active ? "activar" : "desactivar", id, e);
        }
        
        return false;
    }
    
    @Override
    public boolean existsByUsername(String username) {
        return existsByField(EXISTS_BY_USERNAME, username);
    }
    
    @Override
    public boolean existsByEmail(String email) {
        return existsByField(EXISTS_BY_EMAIL, email);
    }
    
    @Override
    public boolean existsByDocumentoIdentidad(String documentoIdentidad) {
        return existsByField(EXISTS_BY_DOCUMENTO, documentoIdentidad);
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
            logger.error("Error al contar usuarios", e);
        }
        
        return 0;
    }
    
    @Override
    public boolean updateLastLogin(Integer id) {
        if (id == null) return false;
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_LAST_LOGIN)) {
            
            stmt.setInt(1, id);
            int affectedRows = stmt.executeUpdate();
            
            return affectedRows > 0;
            
        } catch (SQLException e) {
            logger.error("Error al actualizar último login: ID {}", id, e);
        }
        
        return false;
    }
    
    /**
     * Mapea un ResultSet a un objeto Usuario.
     */
    private Usuario mapResultSetToUsuario(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        
        usuario.setId(rs.getInt("id"));
        usuario.setUsername(rs.getString("username"));
        usuario.setEmail(rs.getString("email"));
        usuario.setPasswordHash(rs.getString("password_hash"));
        usuario.setNombres(rs.getString("nombres"));
        usuario.setApellidos(rs.getString("apellidos"));
        usuario.setDocumentoIdentidad(rs.getString("documento_identidad"));
        usuario.setTelefono(rs.getString("telefono"));
        usuario.setDireccion(rs.getString("direccion"));
        usuario.setActivo(rs.getBoolean("activo"));
        usuario.setUltimoLogin(DateUtils.fromSqlTimestamp(rs.getTimestamp("ultimo_login")));
        usuario.setFechaCreacion(DateUtils.fromSqlTimestamp(rs.getTimestamp("fecha_creacion")));
        usuario.setFechaActualizacion(DateUtils.fromSqlTimestamp(rs.getTimestamp("fecha_actualizacion")));
        
        return usuario;
    }
    
    /**
     * Carga los roles de un usuario.
     */
    private void cargarRoles(Usuario usuario, Connection conn) throws SQLException {
        if (usuario.getId() == null) return;
        
        try (PreparedStatement stmt = conn.prepareStatement(SELECT_ROLES)) {
            stmt.setInt(1, usuario.getId());
            
            try (ResultSet rs = stmt.executeQuery()) {
                List<Rol> roles = new ArrayList<>();
                
                while (rs.next()) {
                    Rol rol = new Rol();
                    rol.setId(rs.getInt("id"));
                    rol.setNombre(rs.getString("nombre"));
                    rol.setDescripcion(rs.getString("descripcion"));
                    rol.setActivo(rs.getBoolean("activo"));
                    rol.setFechaCreacion(DateUtils.fromSqlTimestamp(rs.getTimestamp("fecha_creacion")));
                    rol.setFechaActualizacion(DateUtils.fromSqlTimestamp(rs.getTimestamp("fecha_actualizacion")));
                    
                    roles.add(rol);
                }
                
                usuario.setRoles(roles);
            }
        }
    }
}


