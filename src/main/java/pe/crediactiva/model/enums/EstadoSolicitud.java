package pe.crediactiva.model.enums;

/**
 * Enumeración que define los estados posibles de una solicitud de préstamo.
 * 
 * @author CrediActiva Development Team
 * @version 1.0
 */
public enum EstadoSolicitud {
    
    PENDIENTE("Pendiente", "Solicitud registrada, esperando revisión"),
    EN_REVISION("En Revisión", "Solicitud siendo evaluada por el equipo"),
    APROBADA("Aprobada", "Solicitud aprobada, préstamo generado"),
    RECHAZADA("Rechazada", "Solicitud rechazada por no cumplir criterios"),
    CANCELADA("Cancelada", "Solicitud cancelada por el cliente o sistema");
    
    private final String nombre;
    private final String descripcion;
    
    EstadoSolicitud(String nombre, String descripcion) {
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
     * Obtiene el EstadoSolicitud a partir de su nombre.
     * 
     * @param nombre el nombre del estado
     * @return el EstadoSolicitud correspondiente
     * @throws IllegalArgumentException si no se encuentra el estado
     */
    public static EstadoSolicitud fromNombre(String nombre) {
        for (EstadoSolicitud estado : values()) {
            if (estado.nombre.equalsIgnoreCase(nombre) || estado.name().equalsIgnoreCase(nombre)) {
                return estado;
            }
        }
        throw new IllegalArgumentException("Estado de solicitud no encontrado: " + nombre);
    }
    
    /**
     * Verifica si la solicitud puede ser editada.
     * 
     * @return true si la solicitud puede ser modificada
     */
    public boolean puedeEditar() {
        return this == PENDIENTE || this == EN_REVISION;
    }
    
    /**
     * Verifica si la solicitud puede ser aprobada.
     * 
     * @return true si la solicitud puede ser aprobada
     */
    public boolean puedeAprobar() {
        return this == PENDIENTE || this == EN_REVISION;
    }
    
    /**
     * Verifica si la solicitud puede ser rechazada.
     * 
     * @return true si la solicitud puede ser rechazada
     */
    public boolean puedeRechazar() {
        return this == PENDIENTE || this == EN_REVISION;
    }
    
    /**
     * Verifica si la solicitud está finalizada.
     * 
     * @return true si la solicitud no puede cambiar de estado
     */
    public boolean esFinalizada() {
        return this == APROBADA || this == RECHAZADA || this == CANCELADA;
    }
    
    @Override
    public String toString() {
        return nombre;
    }
}


