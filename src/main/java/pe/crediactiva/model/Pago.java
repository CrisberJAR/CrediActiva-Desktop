package pe.crediactiva.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad que representa un pago realizado en el sistema CrediActiva.
 * 
 * @author CrediActiva Development Team
 * @version 1.0
 */
public class Pago {
    
    /**
     * Enumeración para los métodos de pago disponibles.
     */
    public enum MetodoPago {
        EFECTIVO("Efectivo"),
        TRANSFERENCIA("Transferencia Bancaria"),
        CHEQUE("Cheque"),
        DEPOSITO("Depósito Bancario");
        
        private final String descripcion;
        
        MetodoPago(String descripcion) {
            this.descripcion = descripcion;
        }
        
        public String getDescripcion() {
            return descripcion;
        }
        
        @Override
        public String toString() {
            return descripcion;
        }
    }
    
    private Integer id;
    private String numeroRecibo;
    private Integer prestamoId;
    private Integer cronogramaId;
    private BigDecimal montoPago;
    private LocalDate fechaPago;
    private MetodoPago metodoPago;
    private String numeroOperacion;
    private String observaciones;
    private Integer registradoPor;
    private LocalDateTime fechaCreacion;
    
    // Relaciones
    private Prestamo prestamo;
    private CronogramaPago cronogramaPago;
    private Usuario registrador;
    
    // Constructores
    public Pago() {
        this.metodoPago = MetodoPago.EFECTIVO;
        this.fechaCreacion = LocalDateTime.now();
    }
    
    public Pago(String numeroRecibo, Prestamo prestamo, CronogramaPago cronogramaPago, 
               BigDecimal montoPago, LocalDate fechaPago, Usuario registrador) {
        this();
        this.numeroRecibo = numeroRecibo;
        this.prestamo = prestamo;
        this.prestamoId = prestamo != null ? prestamo.getId() : null;
        this.cronogramaPago = cronogramaPago;
        this.cronogramaId = cronogramaPago != null ? cronogramaPago.getId() : null;
        this.montoPago = montoPago;
        this.fechaPago = fechaPago;
        this.registrador = registrador;
        this.registradoPor = registrador != null ? registrador.getId() : null;
    }
    
    // Métodos de utilidad
    
    /**
     * Verifica si el pago es válido.
     * 
     * @return true si el pago tiene los datos mínimos requeridos
     */
    public boolean esValido() {
        return numeroRecibo != null && !numeroRecibo.trim().isEmpty() &&
               montoPago != null && montoPago.compareTo(BigDecimal.ZERO) > 0 &&
               fechaPago != null &&
               prestamoId != null &&
               cronogramaId != null;
    }
    
    /**
     * Verifica si el pago fue realizado en efectivo.
     * 
     * @return true si es pago en efectivo
     */
    public boolean esEfectivo() {
        return metodoPago == MetodoPago.EFECTIVO;
    }
    
    /**
     * Verifica si el pago requiere número de operación.
     * 
     * @return true si requiere número de operación
     */
    public boolean requiereNumeroOperacion() {
        return metodoPago == MetodoPago.TRANSFERENCIA || 
               metodoPago == MetodoPago.DEPOSITO ||
               metodoPago == MetodoPago.CHEQUE;
    }
    
    /**
     * Verifica si el pago tiene número de operación cuando es requerido.
     * 
     * @return true si no requiere operación o la tiene
     */
    public boolean tieneNumeroOperacionSiEsRequerido() {
        if (!requiereNumeroOperacion()) {
            return true;
        }
        return numeroOperacion != null && !numeroOperacion.trim().isEmpty();
    }
    
    /**
     * Obtiene el nombre del método de pago.
     * 
     * @return descripción del método de pago
     */
    public String getNombreMetodoPago() {
        return metodoPago != null ? metodoPago.getDescripcion() : "No especificado";
    }
    
    /**
     * Obtiene el número del préstamo asociado.
     * 
     * @return número del préstamo
     */
    public String getNumeroPrestamo() {
        return prestamo != null ? prestamo.getNumeroPrestamo() : "";
    }
    
    /**
     * Obtiene el número de cuota asociada.
     * 
     * @return número de cuota
     */
    public Integer getNumeroCuota() {
        return cronogramaPago != null ? cronogramaPago.getNumeroCuota() : null;
    }
    
    /**
     * Obtiene el nombre del cliente que realizó el pago.
     * 
     * @return nombre del cliente
     */
    public String getNombreCliente() {
        return prestamo != null && prestamo.getCliente() != null ? 
               prestamo.getCliente().getNombreCompleto() : "";
    }
    
    /**
     * Obtiene el nombre del usuario que registró el pago.
     * 
     * @return nombre del registrador
     */
    public String getNombreRegistrador() {
        return registrador != null ? registrador.getNombreCompleto() : "";
    }
    
    /**
     * Verifica si el pago fue registrado el mismo día que se realizó.
     * 
     * @return true si fue registrado el mismo día
     */
    public boolean fueRegistradoElMismoDia() {
        if (fechaPago == null || fechaCreacion == null) {
            return false;
        }
        return fechaPago.equals(fechaCreacion.toLocalDate());
    }
    
    /**
     * Genera una descripción completa del pago.
     * 
     * @return descripción del pago
     */
    public String getDescripcionCompleta() {
        StringBuilder sb = new StringBuilder();
        sb.append("Pago de S/ ").append(montoPago);
        sb.append(" por ").append(getNombreMetodoPago());
        
        if (numeroOperacion != null && !numeroOperacion.trim().isEmpty()) {
            sb.append(" (Op. ").append(numeroOperacion).append(")");
        }
        
        sb.append(" - Cuota ").append(getNumeroCuota());
        sb.append(" del préstamo ").append(getNumeroPrestamo());
        
        return sb.toString();
    }
    
    // Getters y Setters
    
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getNumeroRecibo() {
        return numeroRecibo;
    }
    
    public void setNumeroRecibo(String numeroRecibo) {
        this.numeroRecibo = numeroRecibo;
    }
    
    public Integer getPrestamoId() {
        return prestamoId;
    }
    
    public void setPrestamoId(Integer prestamoId) {
        this.prestamoId = prestamoId;
    }
    
    public Integer getCronogramaId() {
        return cronogramaId;
    }
    
    public void setCronogramaId(Integer cronogramaId) {
        this.cronogramaId = cronogramaId;
    }
    
    public BigDecimal getMontoPago() {
        return montoPago;
    }
    
    public void setMontoPago(BigDecimal montoPago) {
        this.montoPago = montoPago;
    }
    
    public LocalDate getFechaPago() {
        return fechaPago;
    }
    
    public void setFechaPago(LocalDate fechaPago) {
        this.fechaPago = fechaPago;
    }
    
    public MetodoPago getMetodoPago() {
        return metodoPago;
    }
    
    public void setMetodoPago(MetodoPago metodoPago) {
        this.metodoPago = metodoPago;
    }
    
    public String getNumeroOperacion() {
        return numeroOperacion;
    }
    
    public void setNumeroOperacion(String numeroOperacion) {
        this.numeroOperacion = numeroOperacion;
    }
    
    public String getObservaciones() {
        return observaciones;
    }
    
    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
    
    public Integer getRegistradoPor() {
        return registradoPor;
    }
    
    public void setRegistradoPor(Integer registradoPor) {
        this.registradoPor = registradoPor;
    }
    
    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }
    
    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
    
    public Prestamo getPrestamo() {
        return prestamo;
    }
    
    public void setPrestamo(Prestamo prestamo) {
        this.prestamo = prestamo;
        this.prestamoId = prestamo != null ? prestamo.getId() : null;
    }
    
    public CronogramaPago getCronogramaPago() {
        return cronogramaPago;
    }
    
    public void setCronogramaPago(CronogramaPago cronogramaPago) {
        this.cronogramaPago = cronogramaPago;
        this.cronogramaId = cronogramaPago != null ? cronogramaPago.getId() : null;
    }
    
    public Usuario getRegistrador() {
        return registrador;
    }
    
    public void setRegistrador(Usuario registrador) {
        this.registrador = registrador;
        this.registradoPor = registrador != null ? registrador.getId() : null;
    }
    
    // equals, hashCode y toString
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pago pago = (Pago) o;
        return Objects.equals(id, pago.id) ||
               (Objects.equals(numeroRecibo, pago.numeroRecibo) && numeroRecibo != null);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, numeroRecibo);
    }
    
    @Override
    public String toString() {
        return "Pago{" +
                "id=" + id +
                ", numeroRecibo='" + numeroRecibo + '\'' +
                ", montoPago=" + montoPago +
                ", fechaPago=" + fechaPago +
                ", metodoPago=" + metodoPago +
                ", prestamo=" + getNumeroPrestamo() +
                ", cuota=" + getNumeroCuota() +
                '}';
    }
}


