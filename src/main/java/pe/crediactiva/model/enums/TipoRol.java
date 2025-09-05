package pe.crediactiva.model.enums;

/**
 * Enumeración que define los tipos de roles disponibles en el sistema CrediActiva.
 * 
 * @author CrediActiva Development Team
 * @version 1.0
 */
public enum TipoRol {
    
    ADMINISTRADOR("Administrador", "Acceso completo al sistema, gestión de usuarios y aprobación de préstamos"),
    ASESOR("Asesor", "Creación de solicitudes, gestión de clientes y seguimiento de préstamos"),
    CLIENTE("Cliente", "Acceso a información personal de préstamos y solicitudes");
    
    private final String nombre;
    private final String descripcion;
    
    TipoRol(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    /**
     * Obtiene el TipoRol a partir de su nombre.
     * 
     * @param nombre el nombre del rol
     * @return el TipoRol correspondiente
     * @throws IllegalArgumentException si no se encuentra el rol
     */
    public static TipoRol fromNombre(String nombre) {
        for (TipoRol tipo : values()) {
            if (tipo.nombre.equalsIgnoreCase(nombre)) {
                return tipo;
            }
        }
        throw new IllegalArgumentException("Tipo de rol no encontrado: " + nombre);
    }
    
    /**
     * Verifica si el rol tiene permisos de administración.
     * 
     * @return true si es administrador
     */
    public boolean esAdministrador() {
        return this == ADMINISTRADOR;
    }
    
    /**
     * Verifica si el rol tiene permisos de asesor.
     * 
     * @return true si es asesor
     */
    public boolean esAsesor() {
        return this == ASESOR;
    }
    
    /**
     * Verifica si el rol es de cliente.
     * 
     * @return true si es cliente
     */
    public boolean esCliente() {
        return this == CLIENTE;
    }
    
    @Override
    public String toString() {
        return nombre;
    }
}


