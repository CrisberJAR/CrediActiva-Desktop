package pe.crediactiva.model;

import pe.crediactiva.model.enums.TipoRol;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad que representa un rol en el sistema CrediActiva.
 * Define los permisos y capacidades de los usuarios.
 * 
 * @author CrediActiva Development Team
 * @version 1.0
 */
public class Rol {
    
    private Integer id;
    private String nombre;
    private String descripcion;
    private boolean activo;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    
    // Constructores
    public Rol() {
        this.activo = true;
        this.fechaCreacion = LocalDateTime.now();
        this.fechaActualizacion = LocalDateTime.now();
    }
    
    public Rol(String nombre, String descripcion) {
        this();
        this.nombre = nombre;
        this.descripcion = descripcion;
    }
    
    public Rol(TipoRol tipoRol) {
        this();
        this.nombre = tipoRol.name();
        this.descripcion = tipoRol.getDescripcion();
    }
    
    // Métodos de utilidad
    
    /**
     * Obtiene el TipoRol correspondiente a este rol.
     * 
     * @return el TipoRol o null si no coincide con ninguno
     */
    public TipoRol getTipoRol() {
        try {
            return TipoRol.valueOf(nombre.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    /**
     * Verifica si este rol es de tipo administrador.
     * 
     * @return true si es administrador
     */
    public boolean esAdministrador() {
        TipoRol tipo = getTipoRol();
        return tipo != null && tipo.esAdministrador();
    }
    
    /**
     * Verifica si este rol es de tipo asesor.
     * 
     * @return true si es asesor
     */
    public boolean esAsesor() {
        TipoRol tipo = getTipoRol();
        return tipo != null && tipo.esAsesor();
    }
    
    /**
     * Verifica si este rol es de tipo cliente.
     * 
     * @return true si es cliente
     */
    public boolean esCliente() {
        TipoRol tipo = getTipoRol();
        return tipo != null && tipo.esCliente();
    }
    
    /**
     * Obtiene el nombre legible del rol.
     * 
     * @return nombre formateado para mostrar
     */
    public String getNombreLegible() {
        TipoRol tipo = getTipoRol();
        return tipo != null ? tipo.getNombre() : nombre;
    }
    
    /**
     * Marca el rol como actualizado.
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
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
        marcarComoActualizado();
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
        marcarComoActualizado();
    }
    
    public boolean isActivo() {
        return activo;
    }
    
    public void setActivo(boolean activo) {
        this.activo = activo;
        marcarComoActualizado();
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
    
    // equals, hashCode y toString
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Rol rol = (Rol) o;
        return Objects.equals(id, rol.id) ||
               (Objects.equals(nombre, rol.nombre) && nombre != null);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, nombre);
    }
    
    @Override
    public String toString() {
        return "Rol{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", activo=" + activo +
                '}';
    }
}


