package pe.crediactiva.model;

import pe.crediactiva.model.enums.TipoRol;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Entidad que representa un usuario del sistema CrediActiva.
 * Incluye información personal y de autenticación.
 * 
 * @author CrediActiva Development Team
 * @version 1.0
 */
public class Usuario {
    
    private Integer id;
    private String username;
    private String email;
    private String passwordHash;
    private String nombres;
    private String apellidos;
    private String documentoIdentidad;
    private String telefono;
    private String direccion;
    private boolean activo;
    private LocalDateTime ultimoLogin;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    
    // Relaciones
    private List<Rol> roles;
    
    // Constructores
    public Usuario() {
        this.activo = true;
        this.roles = new ArrayList<>();
        this.fechaCreacion = LocalDateTime.now();
        this.fechaActualizacion = LocalDateTime.now();
    }
    
    public Usuario(String username, String email, String passwordHash, String nombres, String apellidos) {
        this();
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.nombres = nombres;
        this.apellidos = apellidos;
    }
    
    // Métodos de utilidad
    
    /**
     * Obtiene el nombre completo del usuario.
     * 
     * @return nombres y apellidos concatenados
     */
    public String getNombreCompleto() {
        return (nombres != null ? nombres : "") + " " + (apellidos != null ? apellidos : "");
    }
    
    /**
     * Verifica si el usuario tiene un rol específico.
     * 
     * @param tipoRol el tipo de rol a verificar
     * @return true si el usuario tiene el rol
     */
    public boolean tieneRol(TipoRol tipoRol) {
        return roles != null && roles.stream()
                .anyMatch(rol -> rol.getNombre().equalsIgnoreCase(tipoRol.name()) && rol.isActivo());
    }
    
    /**
     * Verifica si el usuario es administrador.
     * 
     * @return true si tiene rol de administrador
     */
    public boolean esAdministrador() {
        return tieneRol(TipoRol.ADMINISTRADOR);
    }
    
    /**
     * Verifica si el usuario es asesor.
     * 
     * @return true si tiene rol de asesor
     */
    public boolean esAsesor() {
        return tieneRol(TipoRol.ASESOR);
    }
    
    /**
     * Verifica si el usuario es cliente.
     * 
     * @return true si tiene rol de cliente
     */
    public boolean esCliente() {
        return tieneRol(TipoRol.CLIENTE);
    }
    
    /**
     * Obtiene el primer rol activo del usuario.
     * 
     * @return el primer rol activo o null si no tiene roles
     */
    public Rol getRolPrincipal() {
        return roles != null ? roles.stream()
                .filter(Rol::isActivo)
                .findFirst()
                .orElse(null) : null;
    }
    
    /**
     * Agrega un rol al usuario.
     * 
     * @param rol el rol a agregar
     */
    public void agregarRol(Rol rol) {
        if (roles == null) {
            roles = new ArrayList<>();
        }
        if (!roles.contains(rol)) {
            roles.add(rol);
        }
    }
    
    /**
     * Actualiza la fecha de último login.
     */
    public void actualizarUltimoLogin() {
        this.ultimoLogin = LocalDateTime.now();
        this.fechaActualizacion = LocalDateTime.now();
    }
    
    /**
     * Marca el usuario como actualizado.
     */
    public void marcarComoActualizado() {
        this.fechaActualizacion = LocalDateTime.now();
    }
    
    // Getters y Setters
    
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
        marcarComoActualizado();
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
        marcarComoActualizado();
    }
    
    public String getPasswordHash() {
        return passwordHash;
    }
    
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
        marcarComoActualizado();
    }
    
    public String getNombres() {
        return nombres;
    }
    
    public void setNombres(String nombres) {
        this.nombres = nombres;
        marcarComoActualizado();
    }
    
    public String getApellidos() {
        return apellidos;
    }
    
    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
        marcarComoActualizado();
    }
    
    public String getDocumentoIdentidad() {
        return documentoIdentidad;
    }
    
    public void setDocumentoIdentidad(String documentoIdentidad) {
        this.documentoIdentidad = documentoIdentidad;
        marcarComoActualizado();
    }
    
    public String getTelefono() {
        return telefono;
    }
    
    public void setTelefono(String telefono) {
        this.telefono = telefono;
        marcarComoActualizado();
    }
    
    public String getDireccion() {
        return direccion;
    }
    
    public void setDireccion(String direccion) {
        this.direccion = direccion;
        marcarComoActualizado();
    }
    
    public boolean isActivo() {
        return activo;
    }
    
    public void setActivo(boolean activo) {
        this.activo = activo;
        marcarComoActualizado();
    }
    
    public LocalDateTime getUltimoLogin() {
        return ultimoLogin;
    }
    
    public void setUltimoLogin(LocalDateTime ultimoLogin) {
        this.ultimoLogin = ultimoLogin;
    }
    
    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }
    
    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
    
    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }
    
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }
    
    public List<Rol> getRoles() {
        return roles;
    }
    
    public void setRoles(List<Rol> roles) {
        this.roles = roles;
    }
    
    // equals, hashCode y toString
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Usuario usuario = (Usuario) o;
        return Objects.equals(id, usuario.id) ||
               (Objects.equals(username, usuario.username) && username != null) ||
               (Objects.equals(email, usuario.email) && email != null);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, username, email);
    }
    
    @Override
    public String toString() {
        return "Usuario{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", nombreCompleto='" + getNombreCompleto() + '\'' +
                ", activo=" + activo +
                '}';
    }
}


