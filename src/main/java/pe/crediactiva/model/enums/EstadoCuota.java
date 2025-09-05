package pe.crediactiva.model.enums;

/**
 * Enumeración que define los estados dinámicos de una cuota de préstamo.
 * Estos estados se calculan en tiempo real basándose en la fecha actual y el estado de pago.
 * 
 * @author CrediActiva Development Team
 * @version 1.0
 */
public enum EstadoCuota {
    
    PAGADO("Pagado", "Cuota pagada completamente"),
    PUNTUAL("Puntual", "Cuota dentro del plazo de vencimiento"),
    ATRASADO("Atrasado", "Cuota con 1-7 días de atraso"),
    MUY_ATRASADO("Muy Atrasado", "Cuota con más de 7 días de atraso");
    
    private final String nombre;
    private final String descripcion;
    
    EstadoCuota(String nombre, String descripcion) {
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
     * Obtiene el EstadoCuota a partir de su nombre.
     * 
     * @param nombre el nombre del estado
     * @return el EstadoCuota correspondiente
     * @throws IllegalArgumentException si no se encuentra el estado
     */
    public static EstadoCuota fromNombre(String nombre) {
        for (EstadoCuota estado : values()) {
            if (estado.nombre.equalsIgnoreCase(nombre) || estado.name().equalsIgnoreCase(nombre)) {
                return estado;
            }
        }
        throw new IllegalArgumentException("Estado de cuota no encontrado: " + nombre);
    }
    
    /**
     * Calcula el estado de una cuota basándose en si está pagada y los días de atraso.
     * 
     * @param pagado true si la cuota está pagada
     * @param diasAtraso días transcurridos desde el vencimiento (0 o positivo)
     * @return el estado correspondiente de la cuota
     */
    public static EstadoCuota calcularEstado(boolean pagado, int diasAtraso) {
        if (pagado) {
            return PAGADO;
        }
        
        if (diasAtraso <= 0) {
            return PUNTUAL;
        } else if (diasAtraso <= 7) {
            return ATRASADO;
        } else {
            return MUY_ATRASADO;
        }
    }
    
    /**
     * Verifica si la cuota está al día (pagada o puntual).
     * 
     * @return true si la cuota está al día
     */
    public boolean estaAlDia() {
        return this == PAGADO || this == PUNTUAL;
    }
    
    /**
     * Verifica si la cuota está en mora (atrasada o muy atrasada).
     * 
     * @return true si la cuota está en mora
     */
    public boolean estaEnMora() {
        return this == ATRASADO || this == MUY_ATRASADO;
    }
    
    /**
     * Verifica si la cuota requiere atención urgente.
     * 
     * @return true si la cuota está muy atrasada
     */
    public boolean requiereAtencionUrgente() {
        return this == MUY_ATRASADO;
    }
    
    /**
     * Obtiene el nivel de prioridad de la cuota (1 = más urgente).
     * 
     * @return nivel de prioridad (1-4)
     */
    public int getNivelPrioridad() {
        return switch (this) {
            case MUY_ATRASADO -> 1;
            case ATRASADO -> 2;
            case PUNTUAL -> 3;
            case PAGADO -> 4;
        };
    }
    
    /**
     * Obtiene el color CSS asociado al estado para la interfaz.
     * 
     * @return clase CSS para el color
     */
    public String getColorCSS() {
        return switch (this) {
            case PAGADO -> "success";
            case PUNTUAL -> "info";
            case ATRASADO -> "warning";
            case MUY_ATRASADO -> "danger";
        };
    }
    
    @Override
    public String toString() {
        return nombre;
    }
}


