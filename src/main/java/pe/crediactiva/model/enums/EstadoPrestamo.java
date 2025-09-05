package pe.crediactiva.model.enums;

/**
 * Enumeración que define los estados posibles de un préstamo.
 * 
 * @author CrediActiva Development Team
 * @version 1.0
 */
public enum EstadoPrestamo {
    
    ACTIVO("Activo", "Préstamo vigente con cuotas pendientes"),
    PAGADO("Pagado", "Préstamo completamente pagado"),
    VENCIDO("Vencido", "Préstamo con cuotas vencidas no pagadas"),
    CANCELADO("Cancelado", "Préstamo cancelado por acuerdo mutuo");
    
    private final String nombre;
    private final String descripcion;
    
    EstadoPrestamo(String nombre, String descripcion) {
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
     * Obtiene el EstadoPrestamo a partir de su nombre.
     * 
     * @param nombre el nombre del estado
     * @return el EstadoPrestamo correspondiente
     * @throws IllegalArgumentException si no se encuentra el estado
     */
    public static EstadoPrestamo fromNombre(String nombre) {
        for (EstadoPrestamo estado : values()) {
            if (estado.nombre.equalsIgnoreCase(nombre) || estado.name().equalsIgnoreCase(nombre)) {
                return estado;
            }
        }
        throw new IllegalArgumentException("Estado de préstamo no encontrado: " + nombre);
    }
    
    /**
     * Verifica si el préstamo permite registrar pagos.
     * 
     * @return true si se pueden registrar pagos
     */
    public boolean permiteRegistrarPagos() {
        return this == ACTIVO || this == VENCIDO;
    }
    
    /**
     * Verifica si el préstamo está finalizado.
     * 
     * @return true si el préstamo no genera más movimientos
     */
    public boolean esFinalizado() {
        return this == PAGADO || this == CANCELADO;
    }
    
    /**
     * Verifica si el préstamo está en mora.
     * 
     * @return true si tiene cuotas vencidas
     */
    public boolean estaEnMora() {
        return this == VENCIDO;
    }
    
    /**
     * Verifica si el préstamo está activo y al día.
     * 
     * @return true si está activo sin mora
     */
    public boolean estaAlDia() {
        return this == ACTIVO;
    }
    
    @Override
    public String toString() {
        return nombre;
    }
}


